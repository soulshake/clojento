(ns clojento.utils.db
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [ragtime.jdbc]
            [ragtime.repl]))

(def h2-db-defaults
  {:name-prefix "temp-db-"
   :delete-on-exit true
   :mode "MySQL"
   :trace-level-file 1
   :trace-level-system-out 1})

(defn create-temp-db [ & {:as options} ]
  (let [config   (merge h2-db-defaults options)
        file     (java.io.File/createTempFile (:name-prefix config) ".mv.db")
        absolute (.getAbsolutePath file)
        path     (subs absolute 0 (- (count absolute) 6))
        trace    (io/file (str path ".trace.db"))]
    (when (:delete-on-exit config)
      (.deleteOnExit file)
      (.deleteOnExit trace))
    {:file file
     :path path
     :trace trace
     :rw (str "jdbc:h2:file:" path ";MODE=" (:mode config) ";TRACE_LEVEL_FILE=" (:trace-level-file config) ";TRACE_LEVEL_SYSTEM_OUT=" (:trace-level-system-out config))
     :ro (str "jdbc:h2:file:" path ";MODE=" (:mode config) ";TRACE_LEVEL_FILE=" (:trace-level-file config) ";TRACE_LEVEL_SYSTEM_OUT=" (:trace-level-system-out config) ";ACCESS_MODE_DATA=r")}))

(defn destroy-temp-db [config]
  (log/info "deleting database: " (:path config))
  (io/delete-file (:trace config) true)
  (io/delete-file (:file config)))

(defn hikari-cp-temp-db-config [config]
  {:adapter  "h2"
   :url      (:ro config)
   :connection-timeout 1000
   :validation-timeout 1000
   :maximum-pool-size  3})

; ------------------------------------------------------------------------------

(defn migration-reporter [op id]
  (case op
    :up   (log/debug "Applying" id)
    :down (log/debug "Rolling back" id)))

(defn migrate-db [config]
  (log/info "migrating database: " (:path config))
  (ragtime.repl/migrate {:datastore  (ragtime.jdbc/sql-database (:rw config))
                         :migrations (ragtime.jdbc/load-resources "migrations/magento-tests")
                         :reporter   migration-reporter}))
