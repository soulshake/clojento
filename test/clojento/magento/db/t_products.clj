(ns clojento.magento.db.t_products
  (:require [clojure.test :refer :all]
            [clojento.magento.db :as db]
            [clojento.magento.t_db :as t_db]
            [clojento.magento.db.products :as nut]
            [clj-time.core :as t]
            [clojure.tools.logging :as log]))

(use-fixtures :once t_db/ro-db-fixture)

; ------------------------------------------------------------------------------

(deftest product-entities
  (with-open [conn (db/conn)]
    (testing "get missing product(s)"
      (is (= [] (nut/product-entities conn {:product-ids [-1 -2]}))))
    (testing "get product with id 1"
      (is (= {:id 1 :sku "sku-1" :type "simple" :attribute-set 4 :date-created (t/date-time 2011 11 21 14 02 58) :date-updated (t/date-time 2013 11 14 10 17 49)}
             (first (nut/product-entities conn {:product-ids [1]})))))
    (testing "get multiple products"
      ; TODO think about order
      (is (= [1 2]   (map :id (nut/product-entities conn {:product-ids [1 2]}))))
      (is (= [1 2]   (map :id (nut/product-entities conn {:product-ids [2 1]}))))
      (is (= [1 2 3] (map :id (nut/product-entities conn {:product-ids [1 2 3]})))))
    (testing "type of date-created"
      (is (= org.joda.time.DateTime (type (:date-created (first (nut/product-entities conn {:product-ids [1]})))))))))

(deftest product-websites
  (with-open [conn (db/conn)]
    (testing "get missing product(s)"
      (is (= [] (nut/product-websites conn {:product-ids [-1 -2]}))))
    (testing "get product with id 1"
      (is (= [{:id 1 :website-id 1} {:id 1 :website-id 2}]
             (nut/product-websites conn {:product-ids [1]}))))
    (testing "get multiple products"
      (is (= [{:id 1 :website-id 1} {:id 1 :website-id 2} {:id 2 :website-id 1}]
             (nut/product-websites conn {:product-ids [1 2]})))
      (is (= 4 (count (nut/product-websites conn {:product-ids [3 2 1]})))))))

(deftest product-stock
  (with-open [conn (db/conn)]
    (testing "get missing product"
      (is (= [] (nut/product-stock conn {:product-ids [-1]}))))
    (testing "get product with id 1"
      (is (= [{:id 1 :website-id 1 :stock-id 1 :qty 2.0000M :stock-status 1}
              {:id 1 :website-id 2 :stock-id 1 :qty 0.0000M :stock-status 0}]
             (nut/product-stock conn {:product-ids [1]}))))))


(deftest product-attributes-varchar
  (with-open [conn (db/conn)]
    (testing "get missing product"
      (is (= [] (nut/product-attributes-varchar conn {:product-ids [-1]}))))
    (testing "get product with id 1"
      (is (= [{:id 1 :attribute-id 60 :store-id 0 :value "Simple Product 1"}
              {:id 1 :attribute-id 71 :store-id 0 :value nil}
              {:id 1 :attribute-id 73 :store-id 0 :value "This is the simple product with id 1"}]
             (nut/product-attributes-varchar conn {:product-ids [1]}))))))

(deftest product-attributes-text
  (with-open [conn (db/conn)]
    (testing "get missing product"
      (is (= [] (nut/product-attributes-text conn {:product-ids [-1]}))))
    (testing "get product with id 1"
      (is (= [{:id 1 :attribute-id 61 :store-id 0 :value ""}
              {:id 1 :attribute-id 62 :store-id 0 :value ""}]
             (map (fn [orig] (assoc orig :value "")) (nut/product-attributes-text conn {:product-ids [1]})))))))

(deftest product-attributes-datetime
  (with-open [conn (db/conn)]
    (testing "get missing product"
      (is (= [] (nut/product-attributes-datetime conn {:product-ids [-1]}))))
    (testing "get product with id 1"
      (is (= [{:id 1 :attribute-id 66 :store-id 1 :value ""}
              {:id 1 :attribute-id 67 :store-id 1 :value ""}]
             ; TODO checking dates
             (map (fn [orig] (assoc orig :value "")) (nut/product-attributes-datetime conn {:product-ids [1]})))))))

(deftest variants
  (with-open [conn (db/conn)]
    (testing "get missing product"
      (is (= [] (nut/variants conn {:product-ids [-1]}))))
    (testing "get simple product"
      (is (= [] (nut/variants conn {:product-ids [1]}))))
    (testing "get configurable product"
      (is (= [{:product_id 2 :variant_id 3}
              {:product_id 2 :variant_id 4}
              {:product_id 2 :variant_id 5}]
             (nut/variants conn {:product-ids [2]}))))
    (testing "get variant of configurable product"
      (is (= [{:product_id 2 :variant_id 3}]
             (nut/variants conn {:product-ids [3]}))))
    (testing "get configurable product without variants"
      (is (= [] (nut/variants conn {:product-ids [6]}))))
    (testing "get grouped product (TODO)")
    (testing "get bundle product (TODO)")
    (testing "get multiple products"
      (is (= [{:product_id 2 :variant_id 3}
              {:product_id 2 :variant_id 4}
              {:product_id 2 :variant_id 5}]
             (nut/variants conn {:product-ids [1 2 3 6]}))))))

(deftest get-variants-info
  (with-open [conn (db/conn)]
    (testing "not found"
      (is (= {:found-variants false :is-variant false}
             (nut/get-variants-info conn -1))))
    (testing "simple product"
      (is (= {:found-variants false :is-variant false}
             (nut/get-variants-info conn 1))))
    (testing "configurable product"
      (is (= {:found-variants true  :is-variant false :variant-ids [3 4 5]}
             (nut/get-variants-info conn 2))))
    (testing "variant (child product)"
      (is (= {:found-variants false :is-variant true  :product-id 2}
             (nut/get-variants-info conn 3))))
    (testing "configurable product without children"
      (is (= {:found-variants false :is-variant false}
             (nut/get-variants-info conn 6))))
    (testing "has meta (TODO)")))

; ------------------------------------------------------------------------------

; (future-facts "get-product-data"
;               (fact "found"
;                     ; not found
;                     (get-product-data (:db @test-system) -1) => (contains {:found false})
;                     ; simple product
;                     (get-product-data (:db @test-system) 1)  => (contains {:found true})
;                     ; configurable product
;                     (get-product-data (:db @test-system) 2)  => (contains {:found true})
;                     ; variant (child product)
;                     (get-product-data (:db @test-system) 3)  => (contains {:found true})
;                     ; configurable product (no children)
;                     (get-product-data (:db @test-system) 6)  => (contains {:found true}))
;               (fact "is-product"
;                     (get-product-data (:db @test-system) -1) => (contains {:is-product false})
;                     (get-product-data (:db @test-system) 1)  => (contains {:is-product true})
;                     (get-product-data (:db @test-system) 2)  => (contains {:is-product true})
;                     (get-product-data (:db @test-system) 3)  => (contains {:is-product false})
;                     (get-product-data (:db @test-system) 6)  => (contains {:is-product true}))
;               (fact "is-variant"
;                     (get-product-data (:db @test-system) -1) => (contains {:is-variant false})
;                     (get-product-data (:db @test-system) 1)  => (contains {:is-variant false})
;                     (get-product-data (:db @test-system) 2)  => (contains {:is-variant false})
;                     (get-product-data (:db @test-system) 3)  => (contains {:is-variant true})
;                     (get-product-data (:db @test-system) 6)  => (contains {:is-variant false}))
;               (fact "product-id (id of the parent for variants)"
;                     (get-product-data (:db @test-system) -1) => (contains {:product-id nil})
;                     (get-product-data (:db @test-system) 1)  => (contains {:product-id 1})
;                     (get-product-data (:db @test-system) 2)  => (contains {:product-id 2})
;                     (get-product-data (:db @test-system) 3)  => (contains {:product-id 2}) ; product-id == parent-id
;                     (get-product-data (:db @test-system) 6)  => (contains {:product-id 6}))
;               (fact "only configurable products have variants"
;                     (get-product-data (:db @test-system) -1) =not=> (contains {:variants anything})
;                     (get-product-data (:db @test-system) 1)  =not=> (contains {:variants anything})
;                     (get-product-data (:db @test-system) 2)  =>     (contains {:variants anything})
;                     (get-product-data (:db @test-system) 3)  =not=> (contains {:variants anything})
;                     (get-product-data (:db @test-system) 6)  =>     (contains {:variants []}))
;               (fact "entity-ids"
;                     (meta (get-product-data (:db @test-system) -1 :debug true)) => (contains {:entity-ids [-1]})
;                     (meta (get-product-data (:db @test-system)  1 :debug true)) => (contains {:entity-ids [1]})
;                     (meta (get-product-data (:db @test-system)  2 :debug true)) => (contains {:entity-ids [2 3 4 5]})
;                     (meta (get-product-data (:db @test-system)  3 :debug true)) => (contains {:entity-ids [3]}))
;               (fact "type"
;                     (get-product-data (:db @test-system) -1) =not=> (contains {:type anything})
;                     (get-product-data (:db @test-system) 1)  => (contains {:type "simple"})
;                     (get-product-data (:db @test-system) 2)  => (contains {:type "configurable"})
;                     (get-product-data (:db @test-system) 3)  => (contains {:type "simple"})
;                     (get-product-data (:db @test-system) 6)  => (contains {:type "configurable"}))
;               (fact "sku"
;                     (get-product-data (:db @test-system) -1) =not=> (contains {:sku anything})
;                     (get-product-data (:db @test-system) 1)  => (contains {:sku "sku-1"})
;                     (get-product-data (:db @test-system) 2)  => (contains {:sku "sku-2"})
;                     (get-product-data (:db @test-system) 3)  => (contains {:sku "sku-2.1"})
;                     (get-product-data (:db @test-system) 6)  => (contains {:sku "sku-6"}))
;               (fact "variant entities"
;                     (get-product-data (:db @test-system) 2)  => (contains {:variants (contains (contains {:type "simple" :sku "sku-2.1"}
;                                                                                                          {:type "simple" :sku "sku-2.2"}))}))
;               (fact "simple product"
;                     (get-product-data (:db @test-system) 1) => (just [{:found         true
;                                                                        :is-product    true
;                                                                        :is-variant    false
;                                                                        :product-id    1
;                                                                        :id            1
;                                                                        :type          "simple"
;                                                                        :sku           "sku-1"
;                                                                        :attribute-set 4
;                                                                        :date-created  (t/date-time 2011 11 21 14 02 58)
;                                                                        :date-updated  (t/date-time 2013 11 14 10 17 49)
;                                                                        ; :websites      (just ["website_1" "website_2"] :in-any-order)
;                                                                        }]))
;               (fact "has meta"
;                     (meta (get-product-data (:db @test-system) -1 :debug true)) =not=> nil?)
;               (fact "meta contains time and total time"
;                     (meta (get-product-data (:db @test-system) -1 :debug true)) => (contains {:time pos? :total-time pos?}))
;               (fact "meta contains all queries"
;                     (count (:queries (meta (get-product-data (:db @test-system) -1 :debug true)))) => 3))

(log/debug "end of 'with read-only database' facts")
