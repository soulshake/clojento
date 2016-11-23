(ns clojento.utils.temp-db
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojento.magento.db :as m.db]
            [clojento.utils.db :as db-utils]
            [mount.lite :as mount]
            [ragtime.jdbc]
            [ragtime.repl]))

; ------------------------------------------------------------------------------

(defn migration-reporter [op id]
  (case op
    :up   (log/debug "Applying" id)
    :down (log/debug "Rolling back" id)))

(defn migrate-temp-db [url]
  (ragtime.repl/migrate {:datastore  (ragtime.jdbc/sql-database url)
                         :migrations (ragtime.jdbc/load-resources "migrations/magento-tests")
                         :reporter   migration-reporter}))

; ------------------------------------------------------------------------------

(def temp-db-defaults
  {:name-prefix "temp-db-"
   :mode "MySQL"
   :trace-level-file 1
   :trace-level-system-out 1
   :migrate-on-create false
   :delete-on-exit true})

(defn create-temp-db [ & {:as options} ]
  (let [config   (merge temp-db-defaults options)
        file     (java.io.File/createTempFile (:name-prefix config) ".mv.db")
        absolute (.getAbsolutePath file)
        path     (subs absolute 0 (- (count absolute) 6))
        trace    (io/file (str path ".trace.db"))
        url      (str "jdbc:h2:file:" path ";MODE=" (:mode config) ";TRACE_LEVEL_FILE=" (:trace-level-file config) ";TRACE_LEVEL_SYSTEM_OUT=" (:trace-level-system-out config))]
    (when (:delete-on-exit config)
      (.deleteOnExit file)
      (.deleteOnExit trace))
    (when (:migrate-on-create config)
      (log/info "migrating database: " path)
      (migrate-temp-db url))
    {:file file
     :path path
     :trace trace
     :rw url
     :ro (str url ";ACCESS_MODE_DATA=r")}))

(defn destroy-temp-db [db]
  (log/info "deleting database: " (:path db))
  (io/delete-file (:trace db) true)
  (io/delete-file (:file db)))

; ------------------------------------------------------------------------------

(defn custom-config [db]
  {:adapter  "h2"
   :url      (:ro db)})

; ------------------------------------------------------------------------------

(defn create-and-connect []
  (let [db   (create-temp-db :migrate-on-create true :trace-level-system-out 0)
        pool (db-utils/connect (db-utils/datasource-config (custom-config db)))]
    (merge pool {:db db})))

(defn disconnect-and-destroy [db]
  (log/info "stopping temp-db")
  (db-utils/disconnect db)
  (destroy-temp-db (:db db)))

(def db (mount/state :start (create-and-connect)
                     :stop  (disconnect-and-destroy m.db/db)))
