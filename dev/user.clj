(ns user
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
  ;(:require
  ; [clojure.java.io :as io]
  ; [clojure.java.javadoc :refer [javadoc]]
  ; [clojure.pprint :refer [pprint]]
  ; [clojure.reflect :refer [reflect]]
  ; [clojure.repl :refer [apropos dir doc find-doc pst source]]
  ; [clojure.set :as set]
  ; [clojure.string :as str]
  ; [clojure.test :as test]
  ;)

  (:require [com.stuartsierra.component :as component]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]
            [clojento.logback :as logback]
            [clojento.core :as app]
            [clojento.magento :as mage]))

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
  (logback/set-level "ROOT" :info)
  (init)
  (start)
  :ready)

(defn reset []
  (stop)
  (refresh :after 'user/go))

(defn asdf []
  (mage/load-product (:magento system) 806))
