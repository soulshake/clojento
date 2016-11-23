(ns clojento.t_config
  (:require [clojure.test :refer :all]
            [clojento.config :as nut]
            [clojure.tools.logging :as log]))

(log/debug "loading clojento.t_config namespace")

(deftest reading_config_files
  (testing "existing file"
    (is (= {:a {:aa "aa", :ab "ab"}, :b "b"} (nut/load-config-from-file "config/test/configurator_1.edn"))))
  (testing "missing file throws an exception"
    (is (thrown? java.io.FileNotFoundException (nut/load-config-from-file "config/test/missing.edn")))))

(deftest reading_and_merging_files
  (testing "contains the key from file 1 and 2"
    (is (= {:a {:aa "new" :ab "ab"} :b "b" :c "c"} (nut/load-config ["config/test/configurator_1.edn" "config/test/configurator_2.edn"])))))
