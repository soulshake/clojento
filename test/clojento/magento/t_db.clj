(ns clojento.magento.t_db
  (:require [midje.sweet :refer :all]
            [clojento.magento.db :refer :all]
            [com.stuartsierra.component :as component]))

(defn in-memory-db-config []
  {:adapter  "h2"
   :url      (str "jdbc:h2:mem:" (gensym))})

(defn fresh-system []
  (let [system (assoc (clojento.core/base-system)
                      :configurator (clojento.config/static-configurator {:db (in-memory-db-config)}))]
      (component/start system)))

(def system (atom nil))

(with-state-changes [(before :facts (reset! system (fresh-system)))
                     (after  :facts (component/stop @system))]
  (fact "db exists"
        (first (run-query (:db @system) :check [])) => {:check "passed"}))
