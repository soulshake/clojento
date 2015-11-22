(ns clojento.logback
  (:require [clojure.tools.logging :as log])
  (:import [org.slf4j LoggerFactory]
           [ch.qos.logback.classic Level]))

(log/debug "loading clojento.logback namespace")

;(defrecord Logger [name level effective])

;(defn make-logger [object]
;  (map->Logger {:name (.getName object) :level (.getLevel object)}))

(defn level-2-keyword [level]
  (if level
    (keyword (clojure.string/lower-case (.toString level)))
    nil))

(defn keyword-2-level [k]
  (if k
    (Level/valueOf (clojure.string/upper-case (name k)))
    nil))

(defn logger-2-map [logger]
  {:name (.getName logger) :level (level-2-keyword (.getLevel logger)) :effective (level-2-keyword (.getEffectiveLevel logger))})

(defn all-loggers []
  (let [logger-context (LoggerFactory/getILoggerFactory)
        loggers (.getLoggerList logger-context)]
    (map logger-2-map loggers)))

(defn set-level [name level]
  (let [logger (LoggerFactory/getLogger name)]
    (.setLevel logger (keyword-2-level level))))
