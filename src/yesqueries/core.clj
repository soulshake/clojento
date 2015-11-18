(ns yesqueries.core
  (:require [taoensso.timbre :as l]
            [yesql.named-parameters :refer [split-at-parameters reassemble-query]]
            [yesql.parser :refer [parse-tagged-queries]]
            [yesql.util :refer [slurp-from-classpath]]))

(l/info "loading yesqueries.core namespace")

(defn process-query [q]
  (let [stmt (:statement q)
        split (split-at-parameters stmt)]
    [(keyword (:name q)) (-> q (dissoc :name) (assoc :split split))]))

(defn load-queries [filename]
  (l/info  "loading queries from " filename)
  (let [queries (->> filename
                  slurp-from-classpath
                  parse-tagged-queries
                  (map process-query)
                  (into {}))]
    queries))



(defn sqlvec-raw
  [split-sql params]
  (l/info  "sqlvec-raw " split-sql params)
  (reassemble-query split-sql params))

;(reassemble-query (split-at-parameters "SELECT age FROM users WHERE country = :country AND asdf = :country") ["gb"])

; TODO distinguish fetch vs insert vs exec

; PUBLIC API
