(ns clojento.core
  (:require [taoensso.timbre :as log]
            [com.stuartsierra.component :as component]
            [clojento.config :as config]
            [clojento.magento :as magento]
            [clojento.magento.db :as magento-db]))

(log/info "loading clojento.core namespace")

(defn example-system [config-options]
  (let [{:keys [host port]} config-options]
    (component/system-map
     :configurator (config/new-configurator ["config/example-base.edn" "config/example-override.edn"])
     :db (component/using (magento-db/new-database) [:configurator])
     :magento (component/using (magento/map->Magento {}) [:db]))))

(defn local-live-system []
  (component/system-map
   :configurator (config/new-configurator ["config/vgl.edn"])
   :db (component/using (magento-db/new-database) [:configurator])
   :magento (component/using (magento/map->Magento {}) [:db])))


(defn base-system []
  (component/system-map
   :configurator (config/static-configurator {})
   :db (component/using (magento-db/new-database) [:configurator])))
