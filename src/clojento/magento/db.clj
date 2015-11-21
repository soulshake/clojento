(ns clojento.magento.db
  (:require [com.stuartsierra.component :as component]
            [clojento.config :as config]
            [hikari-cp.core :as hikari]
            [jdbc.core :as jdbc]
            [taoensso.timbre :as log]
            [yesqueries.core :as yq]))

(log/info "loading clojento.magento.db namespace")

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
    (log/info  "fetching " stmt)
    (raw-jdbc-fetch db stmt :debug debug)))

; ------------------------------------------------------------------------------

(defn get-product-data [db product-id & {:keys [debug] :or {debug false}}]
  (let [q-product-by-id (run-query db :product-by-id [product-id] :debug debug)
        product-by-id (first q-product-by-id)]
    (if (nil? product-by-id)
      {:found false :is-product false :product-id nil}
      {:found true  :is-product true  :product-id product-id})))
