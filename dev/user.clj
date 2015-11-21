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
            [clojento.magento :as mage]
            [ragtime.jdbc]
            [ragtime.repl]
            [midje.repl :refer [autotest]]))

(def system nil)

(defn init []
  (alter-var-root #'system
    (constantly (app/local-live-system))))

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

(def test-db "jdbc:mysql://192.168.99.100:32776/magento-migrations?user=root&password=123")

(defn migrations-config [connection-url]
  {:datastore  (ragtime.jdbc/sql-database connection-url)
   :migrations (ragtime.jdbc/load-resources "migrations/magento")})

(defn db-migrate []
  ; always returns nil
  (ragtime.repl/migrate (migrations-config test-db))
  :done)

(defn db-rollback []
  ; always returns nil
  (ragtime.repl/rollback (migrations-config test-db))
  :done)

(defn db-rollback-all []
  ; TODO count migration files instead of just doing this 10 times
  ; or dig into ragtime to find out how to get a more usefull return value
  (doall (take 10 (repeatedly db-rollback)))
  :done)

(defn db-show-tables []
  (clojento.magento.db/raw-jdbc-fetch (:db system) "show tables;"))
