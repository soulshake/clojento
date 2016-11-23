(ns clojento.magento.db
  (:require [clojento.config :refer [config]]
            [clojento.magento.db.products :as db-products]
            [clojento.utils.db :as db-utils]
            [clojure.tools.logging :as log]
            [jdbc.core :as jdbc]
            [mount.lite :refer [defstate]]))

(log/debug "loading clojento.magento.db namespace")

(defstate db :start (db-utils/connect (db-utils/datasource-config (:db config)))
             :stop  (db-utils/disconnect db))

; ------------------------------------------------------------------------------

(defn conn []
  (jdbc/connection (:pool db)))

; ------------------------------------------------------------------------------

(defn raw-jdbc-execute [stmt-or-sqlvec & {:keys [debug] :or {debug false}}]
  (with-open [conn (conn)]
    (let [starttime (System/nanoTime)
          result (jdbc/execute conn stmt-or-sqlvec)]
      (if debug
        (with-meta result {:stmt stmt-or-sqlvec
                           :hits (count result)
                           :time (/ (- (System/nanoTime) starttime) 1e6)})
        result))))

(defn raw-jdbc-fetch [stmt-or-sqlvec & {:keys [debug] :or {debug false}}]
  (with-open [conn (conn)]
    (let [starttime (System/nanoTime)
          result (jdbc/fetch conn stmt-or-sqlvec)]
      (if debug
        (with-meta result {:stmt stmt-or-sqlvec
                           :hits (count result)
                           :time (/ (- (System/nanoTime) starttime) 1e6)})
        result))))

; ------------------------------------------------------------------------------

; combine meta at this level?
(defn get-products [product-ids & {:keys [debug] :or {debug false}}]
  (with-open [conn (conn)]
    (db-products/get-products conn product-ids :debug debug)))


; ------------------------------------------------------------------------------

(defn run-query [query-name params & {:keys [debug] :or {debug false}}]
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

#_(defn get-product-data [query-id & {:keys [debug] :or {debug false}}]
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
