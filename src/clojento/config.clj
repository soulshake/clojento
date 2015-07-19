(ns clojento.config
  (:require [clojure.edn :as edn]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as l]))

(l/info "loading clojento.config namespace")

; see https://github.com/clojure-cookbook/clojure-cookbook/blob/master/04_local-io/4-15_edn-config.asciidoc

(defn deep-merge
  "Deep merge two maps"
  [& values]
  (if (every? map? values)
    (apply merge-with deep-merge values)
    (last values)))

;(defn load-config
;  "Given a filename, load & return a config map"
;  [filename]
;  (edn/read-string (slurp filename)))

(defn load-config
  "Given filename(s), load then merge & return a config map"
  [& filenames]
  (reduce deep-merge (map (comp edn/read-string slurp)
                          filenames)))

(defrecord Config []
  component/Lifecycle

  (start [this]
         (l/info "loading config")
         (assoc this :config {:host "localhost" :port 32783}))

  (stop [this]
        (l/info "unloading config")
        (dissoc this :config)))

(defn config []
  (map->Config {}))
