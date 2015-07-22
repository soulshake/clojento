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
  (l/info "starting connection pool")
  (hikari/make-datasource (datasource-options configurator)))

; TODO make idempotent
(defrecord Database [configurator datasource]
  component/Lifecycle

  (start [this]
         (if datasource  ; already started
           this
           (assoc this :datasource (make-datasource configurator))))

  (stop [this]
        (if (not datasource) ; already stopped
          this
          (do
            (l/info "stopping connection pool")
            (hikari/close-datasource datasource)
            (assoc this :datasource nil)))))

(defn new-database []
  (map->Database {}))
