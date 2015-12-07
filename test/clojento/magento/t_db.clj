(ns clojento.magento.t_db
  (:require [midje.sweet :refer :all]
            [clojento.magento.db :refer :all]
            [clj-time.core :as t]
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

(defn migration-reporter [op id]
  (case op
    :up   (log/debug "Applying" id)
    :down (log/debug "Rolling back" id)))

(defn prepare-test-db []
  (log/info "preparing read-only test DB")
  (io/delete-file "./data/test-db.mv.db" true)
  (io/delete-file "./data/test-db.trace.db" true)
  (ragtime.repl/migrate {:datastore  (ragtime.jdbc/sql-database test-db-rw)
                         :migrations (ragtime.jdbc/load-resources "migrations/magento-tests")
                         :reporter   migration-reporter}))

(defn fresh-system [before-start db-config]
  (before-start)
  (let [system (assoc (clojento.core/base-system)
                      :configurator (clojento.config/static-configurator {:db db-config}))]
      (component/start system)))

; TODO review (namespace-state-changes)

; ------------------------------------------------------------------------------

(facts "with in-memory DB"
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
    (fact "converts org.joda.time.DateTime to java.sql.Timestamp"
          (let [create-table (str "CREATE TABLE `table_with_datetime` ( "
                                  "`id` int(11) NOT NULL AUTO_INCREMENT, "
                                  "`value` datetime DEFAULT NOT NULL, "
                                  "PRIMARY KEY (`id`) "
                                  ") ENGINE=InnoDB DEFAULT CHARSET=utf8;")
                insert-query (str "INSERT INTO `table_with_datetime` (`value`) "
                                  "VALUES (?);")]
            (do
              (raw-jdbc-execute (:db @system) create-table)
              (raw-jdbc-execute (:db @system) [insert-query (t/now)])) =not=> (throws java.lang.Exception)))
    (log/info "completed tests with in-memory DB")))


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
      (fact "db contains 3 websites (admin + 2) and 2 stores"
            (count (raw-jdbc-fetch (:db @system) "SELECT * FROM core_website;")) => 3
            (count (raw-jdbc-fetch (:db @system) "SELECT * FROM core_store;")) => 2
            (count (run-query (:db @system) :websites [])) => 3
            (count (run-query (:db @system) :websites [] :debug true)) => 3)
      (fact "meta contains :hits"
            (meta (raw-jdbc-fetch (:db @system) "SELECT * FROM core_website;"  :debug true)) => (contains {:hits 3})))

    (facts "query :product-entities"
      (fact "get missing product(s)"
            (run-query (:db @system) :product-entities [-1]) => []
            (run-query (:db @system) :product-entities [[-1 -2]]) => [])
      (fact "get product with id 1 (as simple param or list)"
            (first (run-query (:db @system) :product-entities [ 1 ])) => (just {:id 1 :sku "sku-1" :type "simple" :attribute-set 4 :date-created anything :date-updated anything})
            (first (run-query (:db @system) :product-entities [[1]])) => (just {:id 1 :sku "sku-1" :type "simple" :attribute-set 4 :date-created anything :date-updated anything}))
      (fact "get multiple products"
            (run-query (:db @system) :product-entities [[1 2]])   => (just [(contains {:id 1})
                                                                            (contains {:id 2})] :in-any-order)
            (run-query (:db @system) :product-entities [[3 2 1]]) => (just [(contains {:id 1})
                                                                            (contains {:id 2})
                                                                            (contains {:id 3})] :in-any-order))
      (fact "type of date-created"
            (type (:date-created (first (run-query (:db @system) :product-entities [1])))) => org.joda.time.DateTime))

    (facts "query :product-websites"
      (fact "get missing product(s)"
            (run-query (:db @system) :product-websites [-1]) => []
            (run-query (:db @system) :product-websites [[-1 -2]]) => [])
      (fact "get product with id 1"
            (run-query (:db @system) :product-websites [ 1 ]) => (just [{:id 1 :website-id 1}
                                                                        {:id 1 :website-id 2}] :in-any-order))
      (fact "get multiple products"
            (run-query (:db @system) :product-websites [[1 2]])   => (just [{:id 1 :website-id 1}
                                                                            {:id 1 :website-id 2}
                                                                            {:id 2 :website-id 1}] :in-any-order)
            (count (run-query (:db @system) :product-websites [[3 2 1]])) => 4))

    (facts "query :product-stock"
      (fact "get missing product"
            (run-query (:db @system) :product-stock [-1]) => [])
      (fact "get product with id 1"
            (run-query (:db @system) :product-stock [ 1 ]) => (just [{:id 1 :website-id 1 :stock-id 1 :qty 2.0000M :stock-status 1}
                                                                     {:id 1 :website-id 2 :stock-id 1 :qty 0.0000M :stock-status 0}] :in-any-order)))

    (facts "query :product-attributes-varchar"
      (fact "get missing product"
            (run-query (:db @system) :product-attributes-varchar [-1]) => [])
      (fact "get product with id 1"
            (run-query (:db @system) :product-attributes-varchar [1]) => (just [{:id 1 :attribute-id 60 :store-id 0 :value "Simple Product 1"}
                                                                                {:id 1 :attribute-id 71 :store-id 0 :value nil}
                                                                                {:id 1 :attribute-id 73 :store-id 0 :value "This is the simple product with id 1"}] :in-any-order)))

    (facts "query :product-attributes-text"
      (fact "get missing product"
            (run-query (:db @system) :product-attributes-text [-1]) => [])
      (fact "get product with id 1"
            (run-query (:db @system) :product-attributes-text [1]) => (just [(contains {:id 1 :attribute-id 61 :store-id 0 :value anything})                   ; TODO h2 returns a org.h2.jdbc.JdbcClob, .toString == "This is the long description for product 1"
                                                                             (contains {:id 1 :attribute-id 62 :store-id 0 :value anything})] :in-any-order))) ; TODO h2 returns a org.h2.jdbc.JdbcClob, .toString == "This is the short description for product 1"

    (facts "query :product-attributes-datetime"
      (fact "get missing product"
            (run-query (:db @system) :product-attributes-datetime [-1]) => [])
      (fact "get product with id 1"
            (run-query (:db @system) :product-attributes-datetime [1]) => (just [(contains {:id 1 :attribute-id 66 :store-id 1 :value anything})                   ; TODO checking dates
                                                                                 (contains {:id 1 :attribute-id 67 :store-id 1 :value anything})] :in-any-order))) ; TODO checking dates

    (facts "get-variants-info"
      (fact "not found"
            (get-variants-info (:db @system) -1) => {:found-variants false :is-variant false})
      (fact "simple product"
            (get-variants-info (:db @system) 1)  => {:found-variants false :is-variant false})
      (fact "configurable product"
            (get-variants-info (:db @system) 2)  => {:found-variants true  :is-variant false :variant-ids [3 4 5]})
      (fact "variant (child product)"
            (get-variants-info (:db @system) 3)  => {:found-variants false :is-variant true  :product-id 2})
      (fact "configurable product without children"
            (get-variants-info (:db @system) 6)  => {:found-variants false :is-variant false})
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
            (get-product-data (:db @system) 3)  => (contains {:found true})
            ; configurable product (no children)
            (get-product-data (:db @system) 6)  => (contains {:found true}))
      (fact "is-product"
            (get-product-data (:db @system) -1) => (contains {:is-product false})
            (get-product-data (:db @system) 1)  => (contains {:is-product true})
            (get-product-data (:db @system) 2)  => (contains {:is-product true})
            (get-product-data (:db @system) 3)  => (contains {:is-product false})
            (get-product-data (:db @system) 6)  => (contains {:is-product true}))
      (fact "is-variant"
            (get-product-data (:db @system) -1) => (contains {:is-variant false})
            (get-product-data (:db @system) 1)  => (contains {:is-variant false})
            (get-product-data (:db @system) 2)  => (contains {:is-variant false})
            (get-product-data (:db @system) 3)  => (contains {:is-variant true})
            (get-product-data (:db @system) 6)  => (contains {:is-variant false}))
      (fact "product-id (id of the parent for variants)"
            (get-product-data (:db @system) -1) => (contains {:product-id nil})
            (get-product-data (:db @system) 1)  => (contains {:product-id 1})
            (get-product-data (:db @system) 2)  => (contains {:product-id 2})
            (get-product-data (:db @system) 3)  => (contains {:product-id 2}) ; product-id == parent-id
            (get-product-data (:db @system) 6)  => (contains {:product-id 6}))
      (fact "only configurable products have variants"
            (get-product-data (:db @system) -1) =not=> (contains {:variants anything})
            (get-product-data (:db @system) 1)  =not=> (contains {:variants anything})
            (get-product-data (:db @system) 2)  =>     (contains {:variants anything})
            (get-product-data (:db @system) 3)  =not=> (contains {:variants anything})
            (get-product-data (:db @system) 6)  =>     (contains {:variants []}))
      (fact "entity-ids"
            (meta (get-product-data (:db @system) -1 :debug true)) => (contains {:entity-ids [-1]})
            (meta (get-product-data (:db @system)  1 :debug true)) => (contains {:entity-ids [1]})
            (meta (get-product-data (:db @system)  2 :debug true)) => (contains {:entity-ids [2 3 4 5]})
            (meta (get-product-data (:db @system)  3 :debug true)) => (contains {:entity-ids [3]}))
      (fact "type"
            (get-product-data (:db @system) -1) =not=> (contains {:type anything})
            (get-product-data (:db @system) 1)  => (contains {:type "simple"})
            (get-product-data (:db @system) 2)  => (contains {:type "configurable"})
            (get-product-data (:db @system) 3)  => (contains {:type "simple"})
            (get-product-data (:db @system) 6)  => (contains {:type "configurable"}))
      (fact "sku"
            (get-product-data (:db @system) -1) =not=> (contains {:sku anything})
            (get-product-data (:db @system) 1)  => (contains {:sku "sku-1"})
            (get-product-data (:db @system) 2)  => (contains {:sku "sku-2"})
            (get-product-data (:db @system) 3)  => (contains {:sku "sku-2.1"})
            (get-product-data (:db @system) 6)  => (contains {:sku "sku-6"}))
      (fact "variant entities"
            (get-product-data (:db @system) 2)  => (contains {:variants (contains (contains {:type "simple" :sku "sku-2.1"}
                                                                                            {:type "simple" :sku "sku-2.2"}))}))
      (fact "simple product"
            (get-product-data (:db @system) 1) => (just [{:found         true
                                                          :is-product    true
                                                          :is-variant    false
                                                          :product-id    1
                                                          :id            1
                                                          :type          "simple"
                                                          :sku           "sku-1"
                                                          :attribute-set 4
                                                          :date-created  (t/date-time 2011 11 21 14 02 58)
                                                          :date-updated  (t/date-time 2013 11 14 10 17 49)
                                                          ; :websites      (just ["website_1" "website_2"] :in-any-order)
                                                          }]))
      (fact "has meta"
            (meta (get-product-data (:db @system) -1 :debug true)) =not=> nil?)
      (fact "meta contains time and total time"
            (meta (get-product-data (:db @system) -1 :debug true)) => (contains {:time pos? :total-time pos?}))
      (fact "meta contains all queries"
            (count (:queries (meta (get-product-data (:db @system) -1 :debug true)))) => 3))

    (log/info "completed tests with read-only DB")))

(facts "combine-queries-meta"
  (let [queries [(with-meta {} {:a 1 :time 2})
                 (with-meta {} {:b 3 :time 4})]]
    (fact "assign queries meta to :queries'"
          (combine-queries-meta queries) => (contains {:queries [{:a 1 :time 2} {:b 3 :time 4}]}))
    (fact "sum(query-time) => time"
          (combine-queries-meta queries) => (contains {:time 6})
          (combine-queries-meta [(with-meta {} {:time 1}) (with-meta {} {:time 2})]) => (contains {:time 3}))))
