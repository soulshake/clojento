(ns clojento.magento.db.t_products
  (:require [midje.sweet :refer :all]
            [clojento.magento.db :as db]
            [clojento.magento.t_db :as t_db]
            [clojento.magento.db.products :as db-products]
            [clj-time.core :as t]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

(def test-system t_db/test-system-with-ro-db)

(namespace-state-changes [(before :facts (t_db/setup-test-system-with-ro-db))])

; ------------------------------------------------------------------------------

(facts "query product-entities"
  (fact "get missing product(s)"
        (db-products/product-entities (:db @test-system) {:product-ids [-1 -2]}) => [])
  (fact "get product with id 1"
        (first (db-products/product-entities (:db @test-system) {:product-ids [1]})) => (just {:id 1 :sku "sku-1" :type "simple" :attribute-set 4 :date-created anything :date-updated anything}))
  (fact "get multiple products"
        (db-products/product-entities (:db @test-system) {:product-ids [1 2]}) => (just [(contains {:id 1})
                                                                                         (contains {:id 2})] :in-any-order)
        (db-products/product-entities (:db @test-system) {:product-ids [1 2 3]}) => (just [(contains {:id 1})
                                                                                           (contains {:id 2})
                                                                                           (contains {:id 3})] :in-any-order))
  (fact "type of date-created"
        (type (:date-created (first (db-products/product-entities (:db @test-system) {:product-ids [1]})))) => org.joda.time.DateTime))

(facts "query product-websites"
  (fact "get missing product(s)"
        (db-products/product-websites (:db @test-system) {:product-ids [-1 -2]}) => [])
  (fact "get product with id 1"
        (db-products/product-websites (:db @test-system) {:product-ids [1]}) => (just [{:id 1 :website-id 1}
                                                                                       {:id 1 :website-id 2}] :in-any-order))
  (fact "get multiple products"
        (db-products/product-websites (:db @test-system) {:product-ids [1 2]}) => (just [{:id 1 :website-id 1}
                                                                                         {:id 1 :website-id 2}
                                                                                         {:id 2 :website-id 1}] :in-any-order)
        (count (db-products/product-websites (:db @test-system) {:product-ids [3 2 1]})) => 4))

(facts "query product-stock"
  (fact "get missing product"
        (db-products/product-stock (:db @test-system) {:product-ids [-1]}) => [])
  (fact "get product with id 1"
        (db-products/product-stock (:db @test-system) {:product-ids [1]}) => (just [{:id 1 :website-id 1 :stock-id 1 :qty 2.0000M :stock-status 1}
                                                                                    {:id 1 :website-id 2 :stock-id 1 :qty 0.0000M :stock-status 0}] :in-any-order)))

(facts "query product-attributes-varchar"
  (fact "get missing product"
        (db-products/product-attributes-varchar (:db @test-system) {:product-ids [-1]}) => [])
  (fact "get product with id 1"
        (db-products/product-attributes-varchar (:db @test-system) {:product-ids [1]}) => (just [{:id 1 :attribute-id 60 :store-id 0 :value "Simple Product 1"}
                                                                                                 {:id 1 :attribute-id 71 :store-id 0 :value nil}
                                                                                                 {:id 1 :attribute-id 73 :store-id 0 :value "This is the simple product with id 1"}] :in-any-order)))

(facts "query product-attributes-text"
  (fact "get missing product"
        (db-products/product-attributes-text (:db @test-system) {:product-ids [-1]}) => [])
  (fact "get product with id 1"
        (db-products/product-attributes-text (:db @test-system) {:product-ids [1]}) => (just [(contains {:id 1 :attribute-id 61 :store-id 0 :value anything})                   ; TODO h2 returns a org.h2.jdbc.JdbcClob, .toString == "This is the long description for product 1"
                                                                                              (contains {:id 1 :attribute-id 62 :store-id 0 :value anything})] :in-any-order))) ; TODO h2 returns a org.h2.jdbc.JdbcClob, .toString == "This is the short description for product 1"

(facts "query product-attributes-datetime"
  (fact "get missing product"
        (db-products/product-attributes-datetime (:db @test-system) {:product-ids [-1]}) => [])
  (fact "get product with id 1"
        (db-products/product-attributes-datetime (:db @test-system) {:product-ids [1]}) => (just [(contains {:id 1 :attribute-id 66 :store-id 1 :value anything})                   ; TODO checking dates
                                                                                                  (contains {:id 1 :attribute-id 67 :store-id 1 :value anything})] :in-any-order))) ; TODO checking dates

; ------------------------------------------------------------------------------

(future-facts "get-variants-info"
              (fact "not found"
                    (get-variants-info (:db @test-system) -1) => {:found-variants false :is-variant false})
              (fact "simple product"
                    (get-variants-info (:db @test-system) 1)  => {:found-variants false :is-variant false})
              (fact "configurable product"
                    (get-variants-info (:db @test-system) 2)  => {:found-variants true  :is-variant false :variant-ids [3 4 5]})
              (fact "variant (child product)"
                    (get-variants-info (:db @test-system) 3)  => {:found-variants false :is-variant true  :product-id 2})
              (fact "configurable product without children"
                    (get-variants-info (:db @test-system) 6)  => {:found-variants false :is-variant false})
              (fact "has meta"
                    (meta (get-variants-info (:db @test-system) -1 :debug true)) => (contains {:hits 0})))

(future-facts "get-product-data"
              (fact "found"
                    ; not found
                    (get-product-data (:db @test-system) -1) => (contains {:found false})
                    ; simple product
                    (get-product-data (:db @test-system) 1)  => (contains {:found true})
                    ; configurable product
                    (get-product-data (:db @test-system) 2)  => (contains {:found true})
                    ; variant (child product)
                    (get-product-data (:db @test-system) 3)  => (contains {:found true})
                    ; configurable product (no children)
                    (get-product-data (:db @test-system) 6)  => (contains {:found true}))
              (fact "is-product"
                    (get-product-data (:db @test-system) -1) => (contains {:is-product false})
                    (get-product-data (:db @test-system) 1)  => (contains {:is-product true})
                    (get-product-data (:db @test-system) 2)  => (contains {:is-product true})
                    (get-product-data (:db @test-system) 3)  => (contains {:is-product false})
                    (get-product-data (:db @test-system) 6)  => (contains {:is-product true}))
              (fact "is-variant"
                    (get-product-data (:db @test-system) -1) => (contains {:is-variant false})
                    (get-product-data (:db @test-system) 1)  => (contains {:is-variant false})
                    (get-product-data (:db @test-system) 2)  => (contains {:is-variant false})
                    (get-product-data (:db @test-system) 3)  => (contains {:is-variant true})
                    (get-product-data (:db @test-system) 6)  => (contains {:is-variant false}))
              (fact "product-id (id of the parent for variants)"
                    (get-product-data (:db @test-system) -1) => (contains {:product-id nil})
                    (get-product-data (:db @test-system) 1)  => (contains {:product-id 1})
                    (get-product-data (:db @test-system) 2)  => (contains {:product-id 2})
                    (get-product-data (:db @test-system) 3)  => (contains {:product-id 2}) ; product-id == parent-id
                    (get-product-data (:db @test-system) 6)  => (contains {:product-id 6}))
              (fact "only configurable products have variants"
                    (get-product-data (:db @test-system) -1) =not=> (contains {:variants anything})
                    (get-product-data (:db @test-system) 1)  =not=> (contains {:variants anything})
                    (get-product-data (:db @test-system) 2)  =>     (contains {:variants anything})
                    (get-product-data (:db @test-system) 3)  =not=> (contains {:variants anything})
                    (get-product-data (:db @test-system) 6)  =>     (contains {:variants []}))
              (fact "entity-ids"
                    (meta (get-product-data (:db @test-system) -1 :debug true)) => (contains {:entity-ids [-1]})
                    (meta (get-product-data (:db @test-system)  1 :debug true)) => (contains {:entity-ids [1]})
                    (meta (get-product-data (:db @test-system)  2 :debug true)) => (contains {:entity-ids [2 3 4 5]})
                    (meta (get-product-data (:db @test-system)  3 :debug true)) => (contains {:entity-ids [3]}))
              (fact "type"
                    (get-product-data (:db @test-system) -1) =not=> (contains {:type anything})
                    (get-product-data (:db @test-system) 1)  => (contains {:type "simple"})
                    (get-product-data (:db @test-system) 2)  => (contains {:type "configurable"})
                    (get-product-data (:db @test-system) 3)  => (contains {:type "simple"})
                    (get-product-data (:db @test-system) 6)  => (contains {:type "configurable"}))
              (fact "sku"
                    (get-product-data (:db @test-system) -1) =not=> (contains {:sku anything})
                    (get-product-data (:db @test-system) 1)  => (contains {:sku "sku-1"})
                    (get-product-data (:db @test-system) 2)  => (contains {:sku "sku-2"})
                    (get-product-data (:db @test-system) 3)  => (contains {:sku "sku-2.1"})
                    (get-product-data (:db @test-system) 6)  => (contains {:sku "sku-6"}))
              (fact "variant entities"
                    (get-product-data (:db @test-system) 2)  => (contains {:variants (contains (contains {:type "simple" :sku "sku-2.1"}
                                                                                                         {:type "simple" :sku "sku-2.2"}))}))
              (fact "simple product"
                    (get-product-data (:db @test-system) 1) => (just [{:found         true
                                                                       :is-product    true
                                                                       :is-variant    false
                                                                       :product-id    1
                                                                       :id            1
                                                                       :type          "simple"
                                                                       :sku           "sku-1"
                                                                       :attribute-set 4
                                                                       :date-created  (t/date-time 2011 11 21 14 02 58)
                                                                       :date-updated  (t/date-time 2013 11 14 10 17 49)
                                                                       ; :websites      (just ["website_1" "website_2"] :in-any-order)
                                                                       }]))
              (fact "has meta"
                    (meta (get-product-data (:db @test-system) -1 :debug true)) =not=> nil?)
              (fact "meta contains time and total time"
                    (meta (get-product-data (:db @test-system) -1 :debug true)) => (contains {:time pos? :total-time pos?}))
              (fact "meta contains all queries"
                    (count (:queries (meta (get-product-data (:db @test-system) -1 :debug true)))) => 3))

(log/debug "end of 'with read-only database' facts")
