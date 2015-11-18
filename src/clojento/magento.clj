(ns clojento.magento
  (:require [com.stuartsierra.component :as component]
            [clojento.config :as config]
            [clojento.magento.db :as db]
            [taoensso.timbre :as log]))

(log/info "loading clojento.magento namespace")

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
  (let [db (:db magento)
        p (clojento.magento.db/run-query db :product-by-id [product-id])]
    (with-meta {:id product-id} {:queries [{:sql "foo" :hits 3 :time 2} {:sql "bar" :hits 5 :time 31}] :total_time 33 })))

;(defn load-product-or-variant [magento product-or-variant-id] "TODO")
