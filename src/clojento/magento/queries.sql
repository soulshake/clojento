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

-- name: all-product-ids
-- no comment (TODO what about bundles? TODO maybe use catalog_product_super_link table)
SELECT entity_id
FROM catalog_product_entity      p
JOIN catalog_product_entity_int  pv ON pv.entity_id = p.entity_id AND pv.attribute_id = :visibility_attribute_id AND pv.store_id = 0 AND pv.value > 1

-- name: variants
-- find variants of product or if product is a variant (lookup by product id or variant id)
SELECT l.parent_id as product_id, l.product_id as variant_id
FROM catalog_product_super_link l
WHERE l.parent_id = :product_id OR l.product_id = :variant_id

-- name: product-entities
-- no comment
SELECT
  entity_id as id,
  sku as sku,
  created_at as `date-created`,
  updated_at as `date-updated`,
  type_id as type,
  attribute_set_id as `attribute-set`
FROM catalog_product_entity
WHERE entity_type_id = 4 and entity_id IN (:product_ids)

-- name: product-websites
-- no comment
SELECT
  product_id as id,
  website_id as `website-id`
FROM catalog_product_website
WHERE product_id IN (:product_ids)

-- name: product-stock
-- no comment
SELECT
  product_id as id,
  website_id as `website-id`,
  stock_id as `stock-id`,
  qty as `qty`,
  stock_status as `stock-status`
FROM cataloginventory_stock_status
WHERE product_id IN (:product_ids)

-- name: product-attributes-varchar
-- no comment
SELECT
  entity_id as id,
  attribute_id as `attribute-id`,
  store_id as `store-id`,
  value as value
FROM catalog_product_entity_varchar
WHERE entity_type_id = 4
AND entity_id IN (:product_ids)

-- name: product-attributes-text
-- no comment
SELECT
  entity_id as id,
  attribute_id as `attribute-id`,
  store_id as `store-id`,
  value as value
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
