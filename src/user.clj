(ns user
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.namespace.repl :refer (refresh)]
            [clojento.core :as app]))

; maybe we don't need to reload the user namespace?
;(clojure.tools.namespace.repl/disable-reload!)

(def system nil)

(defn init []
  (alter-var-root #'system
    (constantly (app/example-system {:host "dbhost.com" :port 123}))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system
    (fn [s] (when s (component/stop s)))))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'user/go))
