(ns clojento.core
  (:require [taoensso.timbre :as l]
            [com.stuartsierra.component :as component]
            [clojento.config :as c]
            [clojento.magento.db :as db]))

(l/info "loading clojento.core namespace")

;(defn example-system [_]
;  {:host "dbhost.com" :port 123})

(defn example-system [config-options]
  (let [{:keys [host port]} config-options]
    (component/system-map
     :config (c/config))))
