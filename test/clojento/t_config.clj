(ns clojento.t_config
  (:require [midje.sweet :refer :all]
            [clojento.config :refer :all]))

(facts "reading config files"
  (fact "existing file"
        (load-config-from-file "config/test/configurator_1.edn") => (contains {:a (contains {:aa "aa"})}))
  (fact "missing file throws an exception"
        (load-config-from-file "config/test/missing.edn") => (throws java.io.FileNotFoundException)))

(facts "reading and merging files"
  (fact "contains the key from file 1 and 2"
        (load-config ["config/test/configurator_1.edn" "config/test/configurator_2.edn"]) => {:a {:aa "new" :ab "ab"} :b "b" :c "c"}))

(fact "static configurator"
      (config (static-configurator {:a "a"}) :a) => "a")
