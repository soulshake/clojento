(ns clojento.magento.db
  (:require [clj-time.coerce :as tc]
            [clojento.config :as config]
            [clojento.magento.db.products :as db-products]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [hikari-cp.core :as hikari]
            [hugsql.core :as hugsql]
            [hugsql.adapter.clojure-jdbc :as cj-adapter]
            [jdbc.core :as jdbc]
            [jdbc.proto :as proto]))

(log/debug "loading clojento.magento.db namespace")

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
(defn default-datasource-options []
  {:auto-commit        true
   :read-only          false
   :connection-timeout 30000
   :validation-timeout 5000
   :idle-timeout       600000
   :max-lifetime       1800000
   :minimum-idle       2
   :maximum-pool-size  10
   :pool-name          "db-pool"
   :adapter            "mysql"})

(defn datasource-options [configurator]
  (merge
   (default-datasource-options)
   (config/config configurator :db)))

(defn make-datasource [configurator]
  (log/info "starting connection pool")
  (hikari/make-datasource (datasource-options configurator)))

(defrecord Database [configurator datasource queries]
  component/Lifecycle

  (start [this]
         (if datasource  ; already started
           this
           (assoc this
                  :datasource (make-datasource configurator))))

  (stop [this]
        (if (not datasource) ; already stopped
          this
          (do
            (log/info "stopping connection pool")
            (hikari/close-datasource datasource)
            (assoc this
                   :datasource nil)))))

; PUBLIC API

(defn new-database []
  "Database component requires a Configurator component"
  (map->Database {}))

(defn connection [db]
  (jdbc/connection (:datasource db)))

(defn raw-jdbc-execute [db stmt-or-sqlvec & {:keys [debug] :or {debug false}}]
  (with-open [conn (connection db)]
    (let [starttime (System/nanoTime)
          result (jdbc/execute conn stmt-or-sqlvec)]
      (if debug
        (with-meta result {:stmt stmt-or-sqlvec
                           :hits (count result)
                           :time (/ (- (System/nanoTime) starttime) 1e6)})
        result))))

(defn raw-jdbc-fetch [db stmt-or-sqlvec & {:keys [debug] :or {debug false}}]
  (with-open [conn (connection db)]
    (let [starttime (System/nanoTime)
          result (jdbc/fetch conn stmt-or-sqlvec)]
      (if debug
        (with-meta result {:stmt stmt-or-sqlvec
                           :hits (count result)
                           :time (/ (- (System/nanoTime) starttime) 1e6)})
        result))))

; ------------------------------------------------------------------------------

; combine meta at this level?
(defn get-products [db product-ids & {:keys [debug] :or {debug false}}]
  (with-open [conn (connection db)]
    (db-products/get-products conn product-ids :debug debug)))


; ------------------------------------------------------------------------------

(defn run-query [db query-name params & {:keys [debug] :or {debug false}}]
  #_(let [q (get (:queries db) query-name)
        stmt (yq/sqlvec-raw (:split q) params)]
    (log/debug "fetching " stmt)
    (raw-jdbc-fetch db stmt :debug debug))
  nil)

; ------------------------------------------------------------------------------

(defn combine-queries-meta [queries]
  (let [queries-meta (map meta queries)]
    {:queries queries-meta :time (reduce + (map :time queries-meta))}))

; ------------------------------------------------------------------------------

#_(defn get-product-data [db query-id & {:keys [debug] :or {debug false}}]
  (let [starttime          (System/nanoTime)
        q-variants-info    (get-variants-info db query-id :debug debug)
        is-variant         (:is-variant q-variants-info)
        product-id         (if is-variant (:product-id q-variants-info) query-id)
        entity-ids         (cons query-id (:variant-ids q-variants-info))
        q-product-entities (run-query db :product-entities [entity-ids] :debug debug)
        q-product-websites (run-query db :product-websites [entity-ids] :debug debug)
        product-entities   (group-by :id q-product-entities) ; map (first) on values
        query-entity       (first (get product-entities query-id))
        variants           (dissoc product-entities query-id)
        found              (not (nil? query-entity))
        is-product         (and found (not is-variant))
        queries            [q-variants-info q-product-entities q-product-websites]
        basic-result       (if found
                             (merge {:found true :is-variant is-variant :is-product is-product :product-id product-id}
                                    query-entity)
                             {:found false :is-variant is-variant :is-product is-product :product-id nil})
        with-variants      (if (.equals "configurable" (:type basic-result))
                             (assoc basic-result :variants (map first (vals variants)))
                             basic-result)
        result             with-variants]
    (if debug
      (with-meta result (assoc (combine-queries-meta queries) :entity-ids entity-ids :total-time (/ (- (System/nanoTime) starttime) 1e6)) )
      result)))
