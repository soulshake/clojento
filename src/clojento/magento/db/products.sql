-- :name product-entities :? :*
SELECT
  entity_id as id,
  sku as sku,
  created_at as `date-created`,
  updated_at as `date-updated`,
  type_id as type,
  attribute_set_id as `attribute-set`
FROM catalog_product_entity
WHERE entity_type_id = 4 and entity_id IN (:v*:product-ids)

-- :name product-websites :? :*
SELECT
  product_id as id,
  website_id as `website-id`
FROM catalog_product_website
WHERE product_id IN (:v*:product-ids)

-- :name product-stock :? :*
SELECT
  product_id as id,
  website_id as `website-id`,
  stock_id as `stock-id`,
  qty as `qty`,
  stock_status as `stock-status`
FROM cataloginventory_stock_status
WHERE product_id IN (:v*:product-ids)

-- :name product-attributes-varchar :? :*
SELECT
  entity_id as id,
  attribute_id as `attribute-id`,
  store_id as `store-id`,
  value as value
FROM catalog_product_entity_varchar
WHERE entity_type_id = 4
AND entity_id IN (:v*:product-ids)

-- :name product-attributes-text :? :*
SELECT
  entity_id as id,
  attribute_id as `attribute-id`,
  store_id as `store-id`,
  value as value
FROM catalog_product_entity_text
WHERE entity_type_id = 4
AND entity_id IN (:v*:product-ids)

-- :name product-attributes-datetime :? :*
SELECT
  entity_id as id,
  attribute_id as `attribute-id`,
  store_id as `store-id`,
  value as value
FROM catalog_product_entity_datetime
WHERE entity_type_id = 4
AND entity_id IN (:v*:product-ids)

-- :name product-attributes-int :? :*
SELECT *
FROM catalog_product_entity_int
WHERE entity_type_id = 4
AND entity_id IN (:v*:product-ids)

-- :name product-attributes-decimal :? :*
SELECT *
FROM catalog_product_entity_decimal
WHERE entity_type_id = 4
AND entity_id IN (:v*:product-ids)

-- :name product-url-keys :? :*
SELECT *
FROM catalog_product_entity_url_key
WHERE entity_type_id = 4
AND entity_id IN (:v*:product-ids)

-- :name product-links :? :*
SELECT *
FROM catalog_product_link
WHERE product_id IN (:v*:product-ids)
