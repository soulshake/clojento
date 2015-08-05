(ns clojento.core
  (:require [taoensso.timbre :as log]
            [com.stuartsierra.component :as component]
            [clojento.config :as config]
            [clojento.magento.db :as magento-db]))

(log/info "loading clojento.core namespace")

;(defn example-system [_]
;  {:host "dbhost.com" :port 123})

(defn example-system [config-options]
  (let [{:keys [host port]} config-options]
    (component/system-map
     :configurator (config/new-configurator ["config/example-base.edn" "config/example-override.edn"])
     :db (component/using (magento-db/new-database) [:configurator]))))
