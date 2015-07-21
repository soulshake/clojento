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

;(defn load-config
;  "Given filename(s), load then merge & return a config map"
;  [& filenames]
;  (reduce deep-merge (map (comp edn/read-string slurp)
;                          filenames)))

(defn load-config [filenames]
  (l/info "loading config")
  {:host "localhost" :port 32783})

(defrecord Configurator [filenames config]
  component/Lifecycle

  (start [this]
         (if config ; already started
           this
           (assoc this :config (load-config filenames))))

  (stop [this]
        (if (not config) ; already stopped
          this
          (do
            (l/info "unloading config")
            (assoc this :config nil)))))

(defn configurator [filenames]
  (map->Configurator {:filenames filenames}))

; PUBLIC API

(defn config
  ([configurator key]
   (get (:config configurator) key))
  ([configurator key not-found]
   (get (:config configurator) key not-found)))

(defn config-in
 ([configurator ks]
  (get-in (:config configurator) ks))
 ([configurator ks not-found]
  (get-in (:config configurator) ks not-found)))
