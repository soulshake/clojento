(ns clojento.core
  (:require [taoensso.timbre :as l]
            [clojento.config :as c]
            [clojento.magento.db :as db]))

(l/info "loading clojento.core namespace")

(defn example-system [_]
  {:host "dbhost.com" :port 123})
