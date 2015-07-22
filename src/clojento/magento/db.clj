(ns clojento.magento.db
  (:require [com.stuartsierra.component :as component]
            [clojento.config :as c]
            [hikari-cp.core :as hikari]
            [taoensso.timbre :as l]))

(l/info "loading clojento.magento.db namespace")

; see https://github.com/tomekw/hikari-cp
; all time values are specified in milliseconds
(defn default-datasource-options []
  {:auto-commit        true
   :read-only          false
   :connection-timeout 30000
   :validation-timeout 5000
   :idle-timeout       600000
   :max-lifetime       1800000
   :minimum-idle       10
   :maximum-pool-size  10
   :pool-name          "db-pool"
   :adapter            "mysql"
   :username           "username"
   :password           "password"
   :database-name      "database"
   :server-name        "localhost"
   :port-number        3306})

(defn datasource-options [configurator]
  (merge
   (default-datasource-options)
   (c/config configurator :db)))

(defn make-datasource [configurator]
  (hikari/make-datasource (datasource-options configurator)))

; TODO make idempotent
(defrecord Database [configurator datasource]
  component/Lifecycle

  (start [this]
         (l/info "starting connection pool")

         (let [ds (make-datasource configurator)]
           ;; Return an updated version of the component with
           ;; the run-time state assoc'd in.
           (assoc this :datasource ds)))

  (stop [this]
        (l/info "stopping connection pool")

        (hikari/close-datasource datasource)
        ;(.close connection)
        ;; Return the component, optionally modified. Remember that if you
        ;; dissoc one of a record's base fields, you get a plain map.
        (assoc this :datasource nil)))

(defn new-database []
  (map->Database {}))
