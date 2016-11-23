(ns user
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
  ;(:require
  ; [clojure.java.io :as io]
  ; [clojure.java.javadoc :refer [javadoc]]
  ; [clojure.reflect :refer [reflect]]
  ; [clojure.repl :refer [apropos dir doc find-doc pst source]]
  ; [clojure.set :as set]
  ; [clojure.string :as str]
  ; [clojure.test :as test]
  ;)

  (:require [clojure.test :refer [run-tests]]
            [clojure.tools.logging :as log]
            [clojure.tools.namespace.repl :as tn]
            [clojento.logback :as logback]
            [clojento.core :as app]
            [clojento.config :as config]
            [clojento.magento :as mage]
            [clojento.magento.db :as mage-db]
            [clojento.magento.db.products :as mage-db-products]
            [clojento.magento.t_db :as mage-db-test]
            [clojento.mount.logging :refer [with-logging-status]]
            [ragtime.jdbc]
            [ragtime.repl]
            [mount.core :as mount :refer [defstate]]))

(defn start []
  (log/info "*** START ***")
  (with-logging-status)
  (mount/start))

; (mount/start #'app.conf/config
;              #'app.db/conn
;              #'app.www/nyse-app
;              #'app.example/nrepl)

(defn stop []
  (log/info "*** STOP ***")
  (mount/stop))

(defn refresh []
  (log/info "*** REFRESH ***")
  (stop)
  (tn/refresh))

(defn refresh-all []
  (log/info "*** REFRESH-ALL ***")
  (stop)
  (tn/refresh-all))

(defn go []
  (logback/set-level "ROOT" :info)
  (start)
  :ready)

(defn reset []
  (log/info "*** RESET ***")
  (stop)
  (tn/refresh :after 'user/go))

; (defn asdf []
;   (mage/load-product (:magento system) 806))

(defstate migrations-config :start
  {:datastore  (ragtime.jdbc/sql-database (get-in config/config [:db :url]))
   :migrations (ragtime.jdbc/load-resources "migrations/magento")})

(defn db-migrate []
  ; always returns nil
  (ragtime.repl/migrate migrations-config)
  :done)

(defn db-rollback []
  ; always returns nil
  (ragtime.repl/rollback migrations-config)
  :done)

(defn db-rollback-all []
  ; TODO count migration files instead of just doing this 10 times
  ; or dig into ragtime to find out how to get a more usefull return value
  (doall (take 10 (repeatedly db-rollback)))
  :done)

(defn db-show-tables []
  (clojento.magento.db/raw-jdbc-fetch "show tables;"))

(defn db-products [ & product-ids ]
  (mage-db/get-products product-ids))
