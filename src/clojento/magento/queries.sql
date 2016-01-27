
-- name: all-product-ids
-- no comment (TODO what about bundles? TODO maybe use catalog_product_super_link table)
SELECT entity_id
FROM catalog_product_entity      p
JOIN catalog_product_entity_int  pv ON pv.entity_id = p.entity_id AND pv.attribute_id = :visibility_attribute_id AND pv.store_id = 0 AND pv.value > 1


-- name: order-by-id
-- no comment
SELECT *
FROM sales_flat_order
WHERE entity_id = :order_id
