(ns clojento.magento.db.t_core
  (:require [clojure.test :refer :all]
            [clojento.magento.db :as db]
            [clojento.magento.t_db :as t_db]
            [clojento.magento.db.core :as nut]
            [clj-time.core :as t]
            [clojure.tools.logging :as log]))

(use-fixtures :once t_db/ro-db-fixture)

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

; (facts "db and migrations"
;   (fact "make sure db is read-only"
;         (db/raw-jdbc-execute write-query) => (throws org.h2.jdbc.JdbcBatchUpdateException #"read only"))
;   (fact "db contains 3 websites (admin + 2) and 2 stores"
;         (count (db/raw-jdbc-fetch "SELECT * FROM core_website;")) => 3
;         (count (db/raw-jdbc-fetch "SELECT * FROM core_store;")) => 2
;         (count (with-open [conn (db/conn)]
;                  (db-core/websites conn))) => 3
;         (count (with-open [conn (db/conn)]
;                  (db-core/websites conn {} {:debug true}))) => 3)
;   (fact "meta contains :hits"
;         (meta (db/raw-jdbc-fetch "SELECT * FROM core_website;" :debug true)) => (contains {:hits 3})))
