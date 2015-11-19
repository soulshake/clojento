-- name: check
-- no comment
SELECT 'passed' as check

-- name: websites
-- no comment
SELECT website_id as id, code
FROM core_website

-- name: stores
-- no comment
SELECT store_id as id, code
FROM core_store

-- name: product-by-id
-- no comment
SELECT *
FROM catalog_product_entity
WHERE entity_id = :product_id

-- name: all-product-ids
-- no comment (TODO what about bundles? TODO maybe use catalog_product_super_link table)
SELECT entity_id
FROM catalog_product_entity      p
JOIN catalog_product_entity_int  pv ON pv.entity_id = p.entity_id AND pv.attribute_id = :visibility_attribute_id AND pv.store_id = 0 AND pv.value > 1

-- name: product-and-variants-by-id
-- no comment
SELECT p.*, l.parent_id as parent_id, l.product_id as child_id
FROM catalog_product_entity p
LEFT OUTER JOIN catalog_product_super_link l ON l.product_id = p.entity_id
WHERE p.entity_id = :product_id OR l.parent_id = :product_id
ORDER BY l.product_id;

-- name: variants
-- find variants of product or if product is a variant (lookup by parent id or child id)
SELECT l.parent_id as parent_id, l.product_id as child_id, p.sku as child_sku, p.updated_at as child_updated_at
FROM catalog_product_super_link l
LEFT JOIN catalog_product_entity p ON l.product_id = p.entity_id
WHERE l.parent_id = :product_id OR l.product_id = :product_id

-- name: product-websites
-- no comment
SELECT * FROM catalog_product_website WHERE product_id IN (:product_ids)

-- name: product-stock-status
-- no comment
SELECT * FROM cataloginventory_stock_status WHERE product_id IN (:product_ids)

-- name: product-attributes-varchar
-- no comment
SELECT *
FROM catalog_product_entity_varchar
WHERE entity_type_id = 4
AND entity_id IN (:product_ids)

-- name: product-attributes-text
-- no comment
SELECT *
FROM catalog_product_entity_text
WHERE entity_type_id = 4
AND entity_id IN (:product_ids)

-- name: product-attributes-datetime
-- no comment
SELECT *
FROM catalog_product_entity_datetime
WHERE entity_type_id = 4
AND entity_id IN (:product_ids)

-- name: product-attributes-int
-- no comment
SELECT *
FROM catalog_product_entity_int
WHERE entity_type_id = 4
AND entity_id IN (:product_ids)

-- name: product-attributes-decimal
-- no comment
SELECT *
FROM catalog_product_entity_decimal
WHERE entity_type_id = 4
AND entity_id IN (:product_ids)

-- name: product-url-keys
-- no comment
SELECT *
FROM catalog_product_entity_url_key
WHERE entity_type_id = 4
AND entity_id IN (:product_ids)

-- name: product-links
-- no comment
SELECT *
FROM catalog_product_link
WHERE product_id IN (:product_ids)

-- name: order-by-id
-- no comment
SELECT *
FROM sales_flat_order
WHERE entity_id = :order_id
