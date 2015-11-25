(ns clojento.magento.t_db
  (:require [midje.sweet :refer :all]
            [clojento.magento.db :refer :all]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

(log/debug "loading clojento.magento.t_db namespace")

(def test-db-rw "jdbc:h2:file:./data/test-db;MODE=MySQL")
(def test-db-ro "jdbc:h2:file:./data/test-db;MODE=MySQL;ACCESS_MODE_DATA=r")

(def db-config-ro
  {:adapter  "h2"
   :url      test-db-ro})

(def db-config-in-memory
  {:adapter  "h2"
   :url      (str "jdbc:h2:mem:" (gensym))})

(def system (atom nil))

; ------------------------------------------------------------------------------

(defn prepare-test-db []
  (log/info "preparing read-only test DB")
  (io/delete-file "./data/test-db.mv.db" true)
  (io/delete-file "./data/test-db.trace.db" true)
  (ragtime.repl/migrate {:datastore  (ragtime.jdbc/sql-database test-db-rw)
                         :migrations (ragtime.jdbc/load-resources "migrations/magento-tests")}))

(defn fresh-system [before-start db-config]
  (before-start)
  (let [system (assoc (clojento.core/base-system)
                      :configurator (clojento.config/static-configurator {:db db-config}))]
      (component/start system)))

; TODO review (namespace-state-changes)

; ------------------------------------------------------------------------------

; TODO wrap in (facts ...), maybe?
(with-state-changes [(before :facts (reset! system (fresh-system (fn []) db-config-in-memory)))
                     (after  :facts (component/stop @system))]
  (log/info "starting tests with in-memory DB")
  (fact "db exists"
        (first (run-query (:db @system) :check [])) => {:check "passed"})
  (fact "meta on query result only when requested"
        (meta (run-query (:db @system) :check [] :debug true)) =not=> nil
        (meta (run-query (:db @system) :check [])) => nil
        (meta (raw-jdbc-fetch (:db @system) "SELECT 'passed' as check")) => nil
        (meta (raw-jdbc-fetch (:db @system) "SELECT 'passed' as check" :debug true)) =not=> nil)
  (fact "meta contains :stmt"
        (meta (raw-jdbc-fetch (:db @system) "SELECT 'passed' as check" :debug true)) => (contains {:stmt "SELECT 'passed' as check"}))
  (fact "meta contains :hits"
        (meta (raw-jdbc-fetch (:db @system) "SELECT 'passed' as check" :debug true)) => (contains {:hits 1}))
  (fact "meta contains :time > 0"
        (meta (raw-jdbc-fetch (:db @system) "SELECT 'passed' as check" :debug true)) => (contains {:time pos?})
        (meta (raw-jdbc-execute (:db @system) "CREATE TABLE new_tbl;" :debug true))  => (contains {:time pos?}))
  (log/info "completed tests with in-memory DB"))


(def write-query (str
                  "INSERT INTO `core_website` (`website_id`, `code`, `name`, `sort_order`, `default_group_id`, `is_default`, `is_staging`, `master_login`, `master_password`, `visibility`) "
                  "VALUES (0, 'admin', 'Admin', 0, 0, 0, 0, '', '', '');"))

(facts "with read-only database"
  (with-state-changes [(before :contents (reset! system (fresh-system prepare-test-db db-config-ro)))
                       (after  :contents (component/stop @system))]
    (log/info "starting tests with read-only DB")

    (facts "db and migrations"
      (fact "migration table exists"
            (raw-jdbc-fetch (:db @system) "SHOW TABLES;") => (contains {:table_name "ragtime_migrations", :table_schema "public"}))
      (fact "store table exists"
            (raw-jdbc-fetch (:db @system) "SHOW TABLES;") => (contains {:table_name "core_store", :table_schema "public"}))
      (fact "make sure db is read-only"
            (raw-jdbc-execute (:db @system) write-query) => (throws org.h2.jdbc.JdbcBatchUpdateException #"read only"))
      (fact "db contains 2 websites (admin + 1) and 2 stores"
            (count (raw-jdbc-fetch (:db @system) "SELECT * FROM core_website;")) => 2
            (count (raw-jdbc-fetch (:db @system) "SELECT * FROM core_store;")) => 2
            (count (run-query (:db @system) :websites [])) => 2
            (count (run-query (:db @system) :websites [] :debug true)) => 2)
      (fact "meta contains :hits"
            (meta (raw-jdbc-fetch (:db @system) "SELECT * FROM core_website;"  :debug true)) => (contains {:hits 2})))

    (facts "get-variants-info"
      (fact "not found"
            (get-variants-info (:db @system) -1) => {:has-variants false :is-variant false})
      (fact "simple product"
            (get-variants-info (:db @system) 1)  => {:has-variants false :is-variant false})
      (fact "configurable product"
            (get-variants-info (:db @system) 2)  => {:has-variants true  :is-variant false :variant-ids [3 4 5]})
      (fact "variant (child product)"
            (get-variants-info (:db @system) 3)  => {:has-variants false :is-variant true  :product-id 2})
      (fact "has meta"
            (meta (get-variants-info (:db @system) -1 :debug true)) => (contains {:hits 0})))

    (facts "get-product-data"
      (fact "found"
            ; not found
            (get-product-data (:db @system) -1) => (contains {:found false})
            ; simple product
            (get-product-data (:db @system) 1)  => (contains {:found true})
            ; configurable product
            (get-product-data (:db @system) 2)  => (contains {:found true})
            ; variant (child product)
            (get-product-data (:db @system) 3)  => (contains {:found true}))
      (fact "is-product"
                   (get-product-data (:db @system) -1) => (contains {:is-product false})
                   (get-product-data (:db @system) 1)  => (contains {:is-product true})
                   (get-product-data (:db @system) 2)  => (contains {:is-product true})
                   (get-product-data (:db @system) 3)  => (contains {:is-product false}))
      (fact "is-variant"
                   (get-product-data (:db @system) -1) => (contains {:is-variant false})
                   (get-product-data (:db @system) 1)  => (contains {:is-variant false})
                   (get-product-data (:db @system) 2)  => (contains {:is-variant false})
                   (get-product-data (:db @system) 3)  => (contains {:is-variant true}))
      (future-fact "product-id (id of the parent for variants)"
                   (get-product-data (:db @system) -1) => (contains {:product-id nil})
                   (get-product-data (:db @system) 1)  => (contains {:product-id 1})
                   (get-product-data (:db @system) 2)  => (contains {:product-id 2})
                   (get-product-data (:db @system) 3)  => (contains {:product-id 2}))
      (fact "has meta"
            (meta (get-product-data (:db @system) -1 :debug true)) =not=> nil?)
      (fact "meta contains total time"
            (meta (get-product-data (:db @system) -1 :debug true)) => (contains {:total-time pos?}))
      (fact "meta contains all queries"
            (count (:queries (meta (get-product-data (:db @system) -1 :debug true)))) => 2))

    (log/info "completed tests with read-only DB")))

(facts "combine-queries-meta"
  (let [queries [(with-meta {} {:a 1 :time 2})
                 (with-meta {} {:b 3 :time 4})]]
    (fact "assign queries meta to :queries'"
          (combine-queries-meta queries) => (contains {:queries [{:a 1 :time 2} {:b 3 :time 4}]}))
    (fact "sum(query-time) => time"
          (combine-queries-meta queries) => (contains {:time 6})
          (combine-queries-meta [(with-meta {} {:time 1}) (with-meta {} {:time 2})]) => (contains {:time 3}))))
