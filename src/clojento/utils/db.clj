(ns clojento.utils.db
  (:require [clj-time.coerce :as tc]
            [clojure.tools.logging :as log]
            [hikari-cp.core :as hikari]
            [hugsql.core :as hugsql]
            [hugsql.adapter.clojure-jdbc :as cj-adapter]
            [jdbc.proto :as proto]))

(log/debug "loading clojento.utils.db namespace")

; ------------------------------------------------------------------------------
; set global default adapter

(hugsql/set-adapter! (cj-adapter/hugsql-adapter-clojure-jdbc))

; ------------------------------------------------------------------------------
; convert java.sql.Timestamp <=> org.joda.time.DateTime
; see https://github.com/clj-time/clj-time/blob/master/src/clj_time/jdbc.clj
; and https://github.com/funcool/clojure.jdbc/blob/master/src/jdbc/proto.clj
; and https://github.com/funcool/clojure.jdbc/blob/master/src/jdbc/impl.clj

; TODO pull request for doc at http://funcool.github.io/clojure.jdbc/latest/#extend-sql-types

(extend-protocol proto/ISQLResultSetReadColumn
  java.sql.Timestamp
  (from-sql-type [v _2 _3 _4]
    (tc/from-sql-time v))
  java.sql.Date
  (from-sql-type [v _2 _3 _4]
    (tc/from-sql-date v))
  java.sql.Time
  (result-set-read-column [v _2 _3 _4]
    (org.joda.time.DateTime. v)))

(extend-protocol proto/ISQLType
  org.joda.time.DateTime
  (set-stmt-parameter! [v conn stmt index]
    (.setObject stmt index (proto/as-sql-type v conn)))
  (as-sql-type [v _] (tc/to-sql-time v)))

; ------------------------------------------------------------------------------

; see https://github.com/tomekw/hikari-cp
; all time values are specified in milliseconds
(def default-datasource-config
  {:auto-commit        true
   :read-only          false
   :connection-timeout 1000
   :validation-timeout 1000
   :idle-timeout       600000
   :max-lifetime       1800000
   :minimum-idle       2
   :maximum-pool-size  10
   :pool-name          "db-pool"})

(defn datasource-config [custom-config]
  (merge
   default-datasource-config
   custom-config))

; ------------------------------------------------------------------------------

(defn connect [datasource-config]
  (let [id (gensym)]
    (log/info "starting connection pool" id)
    {:id     id
     :config datasource-config
     :pool   (hikari/make-datasource datasource-config)}))

(defn disconnect [db]
  (log/info "stopping connection pool" (:id db))
  (hikari/close-datasource (:pool db)))
