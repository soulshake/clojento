(ns clojento.magento.db.core-test
  (:require [clojure.test :refer :all]
            [clojento.magento.db :as db]
            [clojento.magento.db-test :as db-test]
            [clojento.magento.db.core :as nut]
            [clj-time.core :as t]
            [clojure.tools.logging :as log]))

(use-fixtures :once db-test/ro-db-fixture)

; ------------------------------------------------------------------------------

(def write-query (str
                  "INSERT INTO `core_website` (`website_id`, `code`, `name`, `sort_order`, `default_group_id`, `is_default`, `is_staging`, `master_login`, `master_password`, `visibility`) "
                  "VALUES (0, 'admin', 'Admin', 0, 0, 0, 0, '', '', '');"))

; ------------------------------------------------------------------------------

(deftest tables
  (let [tables (db/raw-jdbc-fetch "SHOW TABLES;")]
    (testing "migration table exists"
      (is (some #(= {:table_name "ragtime_migrations", :table_schema "public"} %) tables)))
    (testing "store table exists"
      (is (some #(= {:table_name "core_store", :table_schema "public"} %) tables)))))

(deftest read-only
  (testing "make sure db is read-only"
    (is (thrown? org.h2.jdbc.JdbcBatchUpdateException (db/raw-jdbc-execute write-query)))))

(deftest data
  (testing "db contains 3 websites (admin + 2)"
    (is (= 3 (count (db/raw-jdbc-fetch "SELECT * FROM core_website;"))))
    (is (= 3 (count (with-open [conn (db/conn)]
                      (nut/websites conn)))))
    (is (= 3 (count (with-open [conn (db/conn)]
                      (nut/websites conn {} {:debug true}))))))
  (testing "db contains 2 stores"
    (is (= 2 (count (db/raw-jdbc-fetch "SELECT * FROM core_store;")))))
  (testing "meta contains :hits"
    (is (= 3 (:hits (meta (db/raw-jdbc-fetch "SELECT * FROM core_website;" :debug true)))))
    ; no meta on nut/websites (yet?)
    #_(is (= 3 (:hits (meta (with-open [conn (db/conn)]
                            (nut/websites conn {} {:debug true}))))))))
