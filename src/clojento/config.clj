(ns clojento.config
  (:require [clojure.edn :as edn]
            [taoensso.timbre :as l]))

(l/info "loading clojento.config namespace")

(defn deep-merge
  "Deep merge two maps"
  [& values]
  (if (every? map? values)
    (apply merge-with deep-merge values)
    (last values)))

    (require '[clojure.edn :as edn])


(defn load-config
  "Given a filename, load & return a config file"
  [filename]
  (edn/read-string (slurp filename)))
