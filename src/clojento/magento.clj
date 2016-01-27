(ns clojento.magento
  (:require [com.stuartsierra.component :as component]
            [clojento.config :as config]
            [clojento.magento.db :as db]
            [clojure.tools.logging :as log]))

(log/debug "loading clojento.magento namespace")

(defn load-state []
  (log/info "loading state")
  {})

(defrecord Magento [db state]
  component/Lifecycle

  (start [this]
         (if state  ; already started
           this
           (assoc this :state (load-state))))

  (stop [this]
        (if (not state) ; already stopped
          this
          (do
            (log/info "discarding state")
            (assoc this :state nil)))))


; PUBLIC API

; TODO make meta optional
(defn load-product [magento product-id]
  "TODO")

;(defn load-product-or-variant [magento product-or-variant-id] "TODO")
