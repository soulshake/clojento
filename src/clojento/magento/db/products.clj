(ns clojento.magento.db.products
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns     "clojento/magento/db/products.sql")
(hugsql/def-sqlvec-fns "clojento/magento/db/products.sql")

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
