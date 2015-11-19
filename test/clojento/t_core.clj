(ns clojento.t_core
  (:require [midje.sweet :refer :all]
            [clojento.core :refer :all]))

(facts "the system"
  (fact "contains a database component"
        (keys (example-system [])) => (contains :db)))
