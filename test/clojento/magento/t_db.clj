(ns clojento.magento.t_db
  (:require [clojure.test :refer :all]
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

(defn in-memory-db-fixture [f]
  (setup-in-memory-db)
  (f)
  (teardown-in-memory-db))

; ------------------------------------------------------------------------------

; (def test-system-with-ro-db (atom nil))

; (defn setup-test-system-with-ro-db []
;   ; (when (nil? @test-system-with-ro-db))
;   (log/info "creating test system with read-only DB")
;   (mount/start-with-states {#'clojento.magento.db/db #'clojento.utils.temp-db/db}))
;   ; (reset! test-system-with-ro-db :started)
;
; (defn teardown-test-system-with-ro-db []
;   (mount/stop)
;   (log/info "teardown complete: test system with read-only DB"))
;
; (defn ro-db-fixture [f]
;   (setup-in-memory-db)
;   (f)
;   (teardown-in-memory-db))

; ------------------------------------------------------------------------------

(use-fixtures :each in-memory-db-fixture)

(deftest meta-check
  (let [q-check       (nut/raw-jdbc-fetch "SELECT 'passed' as check")
        q-check-debug (nut/raw-jdbc-fetch "SELECT 'passed' as check" :debug true)
        m-check       (meta q-check-debug)]

    (testing "meta on query result only when requested"
      (is (nil? (meta q-check)))
      (is (not (nil? m-check))))

    (testing "meta contains :stmt"
      (is (= "SELECT 'passed' as check" (:stmt m-check))))

    (testing "meta contains :hits"
      (is (= 1 (:hits m-check))))

    (testing "meta contains :time > 0"
      (is (< 0 (:time m-check)))
      (is (< 0 (:time (meta (nut/raw-jdbc-execute "CREATE TABLE new_tbl;" :debug true))))))))

; this would throw an Exception if not configured correctly
(deftest joda-time
  (testing "converts org.joda.time.DateTime to java.sql.Timestamp"
    (let [create-table (str "CREATE TABLE `table_with_datetime` ( "
                            "`id` int(11) NOT NULL AUTO_INCREMENT, "
                            "`value` datetime DEFAULT NOT NULL, "
                            "PRIMARY KEY (`id`) "
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;")
          insert-query (str "INSERT INTO `table_with_datetime` (`value`) "
                            "VALUES (?);")]
      (is (= 1 (do
                 (nut/raw-jdbc-execute create-table)
                 (nut/raw-jdbc-execute [insert-query (t/now)])))))))
