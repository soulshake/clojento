(ns clojento.config
  (:require [clojure.edn :as edn]
            [clojure.tools.logging :as log]
            [mount.core :refer [defstate]]))

(log/debug "loading clojento.config namespace")

; see https://github.com/clojure-cookbook/clojure-cookbook/blob/master/04_local-io/4-15_edn-config.asciidoc

(defn deep-merge
  "Deep merge two maps"
  [& values]
  (if (every? map? values)
    (apply merge-with deep-merge values)
    (last values)))

(defn load-config-from-file
  "Given a filename, load & return a config map"
  [filename]
  (edn/read-string (slurp filename)))

(defn load-config [filenames]
  (log/info "loading config")
  (reduce deep-merge (map load-config-from-file filenames)))

(defstate config :start (load-config ["config/default.edn"]))
