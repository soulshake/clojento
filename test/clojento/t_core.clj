(ns clojento.t_core
  (:require [midje.sweet :refer :all]
            [clojento.core :refer :all]
            [clojure.tools.logging :as log]))

(log/debug "loading clojento.t_core namespace")

(facts "the system"
  (fact "contains a database component"
        (keys (example-system [])) => (contains :db)))
