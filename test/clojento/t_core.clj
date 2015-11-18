(ns clojento.t_core
  (:require [midje.sweet :refer :all]
            [clojento.core :refer :all]))

(facts "hello facts"
  (fact "math works"
        (+ 10 10) => 20)
  (fact "hello me"
        "hello world" => "hello world"
        (str "hello " "you") => "hello you"))
