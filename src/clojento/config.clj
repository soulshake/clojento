(ns clojento.config
  (:require [clojure.edn :as edn]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

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
            (log/info "unloading config")
            (assoc this :config nil)))))

(defn new-configurator [filenames]
  (map->Configurator {:filenames filenames}))

(defn static-configurator [config]
  {:config config})

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
