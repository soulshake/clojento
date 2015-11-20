(ns clojento.magento.t_db
  (:require [midje.sweet :refer :all]
            [clojento.magento.db :refer :all]
            [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]))

(log/info "loading clojento.magento.t_db namespace")

(def test-db-rw "jdbc:h2:file:./data/test-db;MODE=MySQL")
(def test-db-ro "jdbc:h2:file:./data/test-db;MODE=MySQL;ACCESS_MODE_DATA=r")

(def db-config-ro
  {:adapter  "h2"
   :url      test-db-ro})

(def db-config-in-memory
  {:adapter  "h2"
   :url      (str "jdbc:h2:mem:" (gensym))})


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

(def system (atom nil))

; TODO review (namespace-state-changes)

; TODO wrap in (facts ...), maybe?
(with-state-changes [(before :facts (reset! system (fresh-system (fn []) db-config-in-memory)))
                     (after  :facts (component/stop @system))]
  (log/info "starting tests with in-memory DB")
  (fact "db exists"
        (first (run-query (:db @system) :check [])) => {:check "passed"})
  (fact "meta on query result only when requested"
        (meta (run-query (:db @system) :check [])) => nil
        (meta (run-query (:db @system) :check [] :debug true)) =not=> nil)
  (log/info "completed tests with in-memory DB"))


; TODO wrap in (facts ...), possibly?
(with-state-changes [(before :contents (reset! system (fresh-system prepare-test-db db-config-ro)))
                     (after  :contents (component/stop @system))]
  (log/info "starting tests with read-only DB")
  (fact "migration table exists"
        (raw-jdbc-fetch (:db @system) "show tables;") => (contains {:table_name "ragtime_migrations", :table_schema "public"}))
  (fact "migration table exists"
        (raw-jdbc-fetch (:db @system) "show tables;") => (contains {:table_name "core_store", :table_schema "public"}))
  (log/info "completed tests with read-only DB"))
