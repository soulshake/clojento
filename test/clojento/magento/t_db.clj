(ns clojento.magento.t_db
  (:require [midje.sweet :refer :all]
            [clojento.magento.db :as nut]
            [clojento.magento.db.core :as db-core]
            [clojento.utils.temp-db :as temp-db]
            [clj-time.core :as t]
            [clojure.tools.logging :as log]
            [mount.core :as mount]))

(log/debug "loading clojento.magento.t_db namespace")

; ------------------------------------------------------------------------------

(def db-config-in-memory
  {:db {:adapter  "h2"
   :url      (str "jdbc:h2:mem:" (gensym))
   :connection-timeout 1000
   :validation-timeout 1000
   :maximum-pool-size  3}})

(defn setup-in-memory-db []
  (log/info "starting setup: in-memory DB")
  (-> (mount/only #{#'clojento.config/config
                    #'clojento.magento.db/db})
      (mount/swap {#'clojento.config/config db-config-in-memory})
      (mount/start)))

(defn teardown-in-memory-db []
  (mount/stop)
  (log/info "teardown complete: in-memory DB"))

; ------------------------------------------------------------------------------

(def test-system-with-ro-db (atom nil))

(defn setup-test-system-with-ro-db []
  (when (nil? @test-system-with-ro-db)
    (log/info "creating test system with read-only DB")
    (mount/start-with-states {#'clojento.magento.db/db #'clojento.utils.temp-db/db})
    (reset! test-system-with-ro-db :started))
  :ready)

; ------------------------------------------------------------------------------

; TODO use (with-open (nut/conn))?
(with-state-changes [(before :facts (setup-in-memory-db))
                     (after  :facts (teardown-in-memory-db))]
  (fact "db exists"
        (db-core/check (:pool nut/db)) => {:check "passed"})
  (future-fact "add meta to generated functions"
        (meta (db-core/check (:pool nut/db) {} {:debug true})) =not=> nil
        (meta (db-core/check (:pool nut/db))) => nil)
  (fact "meta on query result only when requested"
        (meta (nut/raw-jdbc-fetch "SELECT 'passed' as check")) => nil
        (meta (nut/raw-jdbc-fetch "SELECT 'passed' as check" :debug true)) =not=> nil)
  (fact "meta contains :stmt"
        (meta (nut/raw-jdbc-fetch "SELECT 'passed' as check" :debug true)) => (contains {:stmt "SELECT 'passed' as check"}))
  (fact "meta contains :hits"
        (meta (nut/raw-jdbc-fetch "SELECT 'passed' as check" :debug true)) => (contains {:hits 1}))
  (fact "meta contains :time > 0"
        (meta (nut/raw-jdbc-fetch "SELECT 'passed' as check" :debug true)) => (contains {:time pos?})
        (meta (nut/raw-jdbc-execute "CREATE TABLE new_tbl;" :debug true))  => (contains {:time pos?}))
  (fact "converts org.joda.time.DateTime to java.sql.Timestamp"
        (let [create-table (str "CREATE TABLE `table_with_datetime` ( "
                                "`id` int(11) NOT NULL AUTO_INCREMENT, "
                                "`value` datetime DEFAULT NOT NULL, "
                                "PRIMARY KEY (`id`) "
                                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;")
              insert-query (str "INSERT INTO `table_with_datetime` (`value`) "
                                "VALUES (?);")]
          (do
            (nut/raw-jdbc-execute create-table)
            (nut/raw-jdbc-execute [insert-query (t/now)])) =not=> (throws java.lang.Exception))))

; ------------------------------------------------------------------------------

(facts "combine-queries-meta"
  (let [queries [(with-meta {} {:a 1 :time 2})
                 (with-meta {} {:b 3 :time 4})]]
    (fact "assign queries meta to :queries'"
          (nut/combine-queries-meta queries) => (contains {:queries [{:a 1 :time 2} {:b 3 :time 4}]}))
    (fact "sum(query-time) => time"
          (nut/combine-queries-meta queries) => (contains {:time 6})
          (nut/combine-queries-meta [(with-meta {} {:time 1}) (with-meta {} {:time 2})]) => (contains {:time 3}))))
