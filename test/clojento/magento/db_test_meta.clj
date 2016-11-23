(ns clojento.magento.db-test-meta
  (:require [clojure.test :refer :all]
            [clojento.magento.db :as nut]
            [clojure.tools.logging :as log]))

(log/debug "loading clojento.magento.db-test-meta namespace")

(deftest combine-queries-meta
  (let [queries [(with-meta {} {:a 1 :time 2})
                 (with-meta {} {:b 3 :time 4})]
        combined (nut/combine-queries-meta queries)]
    (testing "assign queries meta to :queries'"
      (is (= [{:a 1 :time 2} {:b 3 :time 4}] (:queries combined))))
    (testing "sum(query-time) => time"
      (is (= 6 (:time combined)))
      (is (= 3 (:time (nut/combine-queries-meta [(with-meta {} {:time 1}) (with-meta {} {:time 2})])))))))
