(ns clojento.magento.db.products
  (:require [hugsql.core :as hugsql]
            [jdbc.core :as jdbc]))

(hugsql/def-db-fns     "clojento/magento/db/products.sql")
(hugsql/def-sqlvec-fns "clojento/magento/db/products.sql")

; ------------------------------------------------------------------------------

(defn get-products [conn product-ids & {:keys [debug] :or {debug false}}]
  (jdbc/atomic conn
               (let [query-ids   (into (sorted-set) product-ids)
                     variants    (variants conn {:product-ids query-ids}) #_"TODO add debug"
                     entity-ids  (into query-ids (map :variant_id variants))]
                 {:query-ids  query-ids
                  :entity-ids entity-ids
                  :variants variants
                  #_"TODO add debug / meta"
                  :entities (product-entities conn {:product-ids query-ids})
                  :websites (product-websites conn {:product-ids query-ids})
                  :stock    (product-stock    conn {:product-ids query-ids})
                  :attributes-varchar  (product-attributes-varchar  conn {:product-ids query-ids})
                  :attributes-text     (product-attributes-text     conn {:product-ids query-ids})
                  :attributes-datetime (product-attributes-datetime conn {:product-ids query-ids})
                  :attributes-int      (product-attributes-int      conn {:product-ids query-ids})
                  :attributes-decimal  (product-attributes-decimal  conn {:product-ids query-ids})
                  :url-keys (product-url-keys conn {:product-ids query-ids}) })))

; ------------------------------------------------------------------------------

(defn get-variants-info [conn query-id & {:keys [debug] :or {debug false}}]
  (let [q-result             (variants conn {:product-ids [query-id]}) #_"TODO add debug"
        q-meta               (meta q-result)
        grouped-by-parent    (group-by :product_id q-result)
        found-variants       (contains? grouped-by-parent query-id)
        product-with-variant (first q-result)]
    (if found-variants
      (with-meta {:is-variant false :found-variants true :variant-ids (map :variant_id (get grouped-by-parent query-id))} q-meta)
      (if (nil? product-with-variant)
        (with-meta {:is-variant false :found-variants false} q-meta)
        (with-meta {:is-variant true  :found-variants false :product-id (:product_id product-with-variant)} q-meta)))))
