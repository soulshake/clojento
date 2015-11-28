(ns clojento.magento.db
  (:require [com.stuartsierra.component :as component]
            [clojento.config :as config]
            [clojure.tools.logging :as log]
            [hikari-cp.core :as hikari]
            [jdbc.core :as jdbc]
            [yesqueries.core :as yq]))

(log/debug "loading clojento.magento.db namespace")

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
                  :datasource (make-datasource configurator)
                  :queries (yq/load-queries "clojento/magento/queries.sql"))))

  (stop [this]
        (if (not datasource) ; already stopped
          this
          (do
            (log/info "stopping connection pool")
            (hikari/close-datasource datasource)
            (assoc this
                   :datasource nil
                   :queries nil)))))

; PUBLIC API

(defn new-database []
  "Database component requires a Configurator component"
  (map->Database {}))

(defn get-connection
  [params]
  ())

(defn raw-jdbc-execute [db stmt-or-sqlvec & {:keys [debug] :or {debug false}}]
  (with-open [conn (jdbc/connection (:datasource db))]
    (let [starttime (System/nanoTime)
          result (jdbc/execute conn stmt-or-sqlvec)]
      (if debug
        (with-meta result {:stmt stmt-or-sqlvec
                           :hits (count result)
                           :time (/ (- (System/nanoTime) starttime) 1e6)})
        result))))

(defn raw-jdbc-fetch [db stmt-or-sqlvec & {:keys [debug] :or {debug false}}]
  (with-open [conn (jdbc/connection (:datasource db))]
    (let [starttime (System/nanoTime)
          result (jdbc/fetch conn stmt-or-sqlvec)]
      (if debug
        (with-meta result {:stmt stmt-or-sqlvec
                           :hits (count result)
                           :time (/ (- (System/nanoTime) starttime) 1e6)})
        result))))

; ------------------------------------------------------------------------------

(defn run-query [db query-name params & {:keys [debug] :or {debug false}}]
  (let [q (get (:queries db) query-name)
        stmt (yq/sqlvec-raw (:split q) params)]
    (log/debug "fetching " stmt)
    (raw-jdbc-fetch db stmt :debug debug)))

; ------------------------------------------------------------------------------

(defn get-variants-info [db query-id & {:keys [debug] :or {debug false}}]
  (let [q-result             (run-query db :variants [query-id query-id] :debug debug)
        q-meta               (meta q-result)
        grouped-by-parent    (group-by :product_id q-result)
        found-variants       (contains? grouped-by-parent query-id)
        product-with-variant (first q-result)]
    (if found-variants
      (with-meta {:is-variant false :found-variants true :variant-ids (map :variant_id (get grouped-by-parent query-id))} q-meta)
      (if (nil? product-with-variant)
        (with-meta {:is-variant false :found-variants false} q-meta)
        (with-meta {:is-variant true  :found-variants false :product-id (:product_id product-with-variant)} q-meta)))))

; ------------------------------------------------------------------------------

(defn combine-queries-meta [queries]
  (let [queries-meta (map meta queries)]
    {:queries queries-meta :time (reduce + (map :time queries-meta))}))

; ------------------------------------------------------------------------------

(defn get-product-data [db query-id & {:keys [debug] :or {debug false}}]
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
