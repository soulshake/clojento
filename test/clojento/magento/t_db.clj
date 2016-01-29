(ns clojento.magento.t_db
  (:require [midje.sweet :refer :all]
            [clojento.magento.db :refer :all]
            [clojento.magento.db.core :as db-core]
            [clj-time.core :as t]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [ragtime.jdbc]
            [ragtime.repl]))

(log/debug "loading clojento.magento.t_db namespace")

; ------------------------------------------------------------------------------

(def test-system (atom nil))

(defn fresh-system [before-start db-config]
  (before-start)
  (let [system (assoc (clojento.core/base-system)
                      :configurator (clojento.config/static-configurator {:db db-config}))]
      (component/start system)))

(def db-config-in-memory
  {:adapter  "h2"
   :url      (str "jdbc:h2:mem:" (gensym))
   :connection-timeout 1000
   :validation-timeout 1000
   :maximum-pool-size  3})

(defn setup-in-memory-db []
  (log/info "starting setup: in-memory DB")
  (reset! test-system (fresh-system (fn []) db-config-in-memory)))

(defn teardown-in-memory-db []
  (component/stop @test-system)
  (log/info "teardown complete: in-memory DB"))

; ------------------------------------------------------------------------------

(defn gen-db-fixture-config []
  (let [file     (java.io.File/createTempFile "test-db-" ".mv.db")
        absolute (.getAbsolutePath file)
        path     (subs absolute 0 (- (count absolute) 6))
        trace    (clojure.java.io/file (str path ".trace.db"))]
    (.deleteOnExit file)
    (.deleteOnExit trace)
    {:file file
     :path path
     :trace trace
     :rw (str "jdbc:h2:file:" path ";MODE=MySQL;TRACE_LEVEL_FILE=1;TRACE_LEVEL_SYSTEM_OUT=1")
     :ro (str "jdbc:h2:file:" path ";MODE=MySQL;TRACE_LEVEL_FILE=1;TRACE_LEVEL_SYSTEM_OUT=1;ACCESS_MODE_DATA=r")}))

(defn test-db-config-ro [test-db-urls]
  {:adapter  "h2"
   :url      (:ro test-db-urls)
   :connection-timeout 1000
   :validation-timeout 1000
   :maximum-pool-size  3})

(defn migration-reporter [op id]
  (case op
    :up   (log/debug "Applying" id)
    :down (log/debug "Rolling back" id)))

(defn cleanup-db-fixture [config]
  (log/info "deleting temporaray test database: " (:path config))
  (io/delete-file (:trace config) true)
  (io/delete-file (:file config)))

(defrecord DatabaseFixture [config created]
  component/Lifecycle

  (start [this]
    (log/debug "start called for DB fixture with path:" (:path config))
    (if created ; already started
      this
      (do
        (log/info "creating database fixture:" (:path config))
        (ragtime.repl/migrate {:datastore  (ragtime.jdbc/sql-database (:rw config))
                               :migrations (ragtime.jdbc/load-resources "migrations/magento-tests")
                               :reporter   migration-reporter})
        (log/info "migrated database fixture:" (:path config))
        (assoc this :created true))))

  (stop [this]
    (log/debug "stop called for DB fixture with path:" (:path config))
    (if (not created) ; already stopped
      this
      (do
        (cleanup-db-fixture config)
        (assoc this :created false)))))

(defn new-db-fixture []
  (map->DatabaseFixture {:config (gen-db-fixture-config)}))

(defn db-fixture-config [db-fixture]
  (:config db-fixture))

; ------------------------------------------------------------------------------

(def test-system-with-ro-db (atom nil))

(defn fresh-system-with-ro-db []
  (let [db-fixture (new-db-fixture)
        db-config  (test-db-config-ro (db-fixture-config db-fixture))]
      (assoc (clojento.core/base-system)
             :db-fixture db-fixture
             :configurator (component/using
                            (clojento.config/static-configurator {:db db-config})
                            ; fake dependency to enforce startup order
                            {:db-fixture :db-fixture}))))

(defn setup-test-system-with-ro-db []
  (when (nil? @test-system-with-ro-db)
    (log/info "creating test system with read-only DB")
    (reset! test-system-with-ro-db (fresh-system-with-ro-db)))
  (log/debug "starting test system read-only DB")
  (reset! test-system-with-ro-db (component/start-system @test-system-with-ro-db))
  nil)

(defn teardown-test-system-with-ro-db []
  (when-let [system @test-system-with-ro-db]
    (log/info "stopping system test system read-only DB")
    (reset! test-system-with-ro-db (component/stop-system @test-system-with-ro-db)))
    nil)

; ------------------------------------------------------------------------------

(with-state-changes [(before :facts (setup-in-memory-db))
                     (after  :facts (teardown-in-memory-db))]
  (fact "db exists"
        (db-core/check (:db @test-system)) => {:check "passed"})
  (future-fact "add meta to generated functions"
        (meta (db-core/check (:db @test-system) {} {:debug true})) =not=> nil
        (meta (db-core/check (:db @test-system))) => nil)
  (fact "meta on query result only when requested"
        (meta (raw-jdbc-fetch (:db @test-system) "SELECT 'passed' as check")) => nil
        (meta (raw-jdbc-fetch (:db @test-system) "SELECT 'passed' as check" :debug true)) =not=> nil)
  (fact "meta contains :stmt"
        (meta (raw-jdbc-fetch (:db @test-system) "SELECT 'passed' as check" :debug true)) => (contains {:stmt "SELECT 'passed' as check"}))
  (fact "meta contains :hits"
        (meta (raw-jdbc-fetch (:db @test-system) "SELECT 'passed' as check" :debug true)) => (contains {:hits 1}))
  (fact "meta contains :time > 0"
        (meta (raw-jdbc-fetch (:db @test-system) "SELECT 'passed' as check" :debug true)) => (contains {:time pos?})
        (meta (raw-jdbc-execute (:db @test-system) "CREATE TABLE new_tbl;" :debug true))  => (contains {:time pos?}))
  (fact "converts org.joda.time.DateTime to java.sql.Timestamp"
        (let [create-table (str "CREATE TABLE `table_with_datetime` ( "
                                "`id` int(11) NOT NULL AUTO_INCREMENT, "
                                "`value` datetime DEFAULT NOT NULL, "
                                "PRIMARY KEY (`id`) "
                                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;")
              insert-query (str "INSERT INTO `table_with_datetime` (`value`) "
                                "VALUES (?);")]
          (do
            (raw-jdbc-execute (:db @test-system) create-table)
            (raw-jdbc-execute (:db @test-system) [insert-query (t/now)])) =not=> (throws java.lang.Exception))))

; ------------------------------------------------------------------------------

(facts "combine-queries-meta"
  (let [queries [(with-meta {} {:a 1 :time 2})
                 (with-meta {} {:b 3 :time 4})]]
    (fact "assign queries meta to :queries'"
          (combine-queries-meta queries) => (contains {:queries [{:a 1 :time 2} {:b 3 :time 4}]}))
    (fact "sum(query-time) => time"
          (combine-queries-meta queries) => (contains {:time 6})
          (combine-queries-meta [(with-meta {} {:time 1}) (with-meta {} {:time 2})]) => (contains {:time 3}))))
