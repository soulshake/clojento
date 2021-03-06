-- required for mysql:
-- (otherwise it will auto_increment ids that are 0)
-- SET GLOBAL sql_mode='NO_AUTO_VALUE_ON_ZERO';


INSERT INTO `core_website` (`website_id`, `code`, `name`, `sort_order`, `default_group_id`, `is_default`, `is_staging`, `master_login`, `master_password`, `visibility`)
VALUES
  (0, 'admin', 'Admin', 0, 0, 0, 0, '', '', ''),
  (1, 'website_1', 'Website 1', 0, 1, 1, 0, '', '', ''),
  (2, 'website_2', 'Website 2', 1, 2, 0, 0, '', '', '');

--;;

INSERT INTO `core_store_group` (`group_id`, `website_id`, `name`, `root_category_id`, `default_store_id`)
VALUES
  (0, 0, 'Default', 0, 0),
  (1, 1, 'Store Group 1', 2, 1);

--;;

INSERT INTO `core_store` (`store_id`, `code`, `website_id`, `group_id`, `name`, `sort_order`, `is_active`)
VALUES
  (0, 'admin', 0, 0, 'Admin', 0, 1),
  (1, 'store_1', 1, 1, 'Store 1', 0, 1);

--;;

INSERT INTO `eav_entity_type` (`entity_type_id`, `entity_type_code`, `entity_model`, `attribute_model`, `entity_table`, `value_table_prefix`, `entity_id_field`, `is_data_sharing`, `data_sharing_key`, `default_attribute_set_id`, `increment_model`, `increment_per_store`, `increment_pad_length`, `increment_pad_char`, `additional_attribute_table`, `entity_attribute_collection`)
VALUES
  (1, 'customer', 'customer/customer', 'customer/attribute', 'customer/entity', '', '', 1, 'default', 1, 'eav/entity_increment_numeric', 0, 8, '0', 'customer/eav_attribute', 'customer/attribute_collection'),
  (2, 'customer_address', 'customer/address', 'customer/attribute', 'customer/address_entity', '', '', 1, 'default', 2, '', 0, 8, '0', 'customer/eav_attribute', 'customer/address_attribute_collection'),
  (3, 'catalog_category', 'catalog/category', 'catalog/resource_eav_attribute', 'catalog/category', '', '', 1, 'default', 3, '', 0, 8, '0', 'catalog/eav_attribute', 'catalog/category_attribute_collection'),
  (4, 'catalog_product', 'catalog/product', 'catalog/resource_eav_attribute', 'catalog/product', '', '', 1, 'default', 4, '', 0, 8, '0', 'catalog/eav_attribute', 'catalog/product_attribute_collection'),
  (5, 'order', 'sales/order', '', 'sales/order', '', '', 1, 'default', 0, 'eav/entity_increment_numeric', 1, 8, '0', '', ''),
  (6, 'invoice', 'sales/order_invoice', '', 'sales/invoice', '', '', 1, 'default', 0, 'eav/entity_increment_numeric', 1, 8, '0', '', ''),
  (7, 'creditmemo', 'sales/order_creditmemo', '', 'sales/creditmemo', '', '', 1, 'default', 0, 'eav/entity_increment_numeric', 1, 8, '0', '', ''),
  (8, 'shipment', 'sales/order_shipment', '', 'sales/shipment', '', '', 1, 'default', 0, 'eav/entity_increment_numeric', 1, 8, '0', '', '');

--;;

INSERT INTO `eav_attribute_set` (`attribute_set_id`, `entity_type_id`, `attribute_set_name`, `sort_order`)
VALUES
  (1, 1, 'Default', 1),
  (2, 2, 'Default', 1),
  (3, 3, 'Default', 1),
  (4, 4, 'Default', 1),
  (5, 5, 'Default', 1),
  (6, 6, 'Default', 1),
  (7, 7, 'Default', 1),
  (8, 8, 'Default', 1);

--;;

INSERT INTO `eav_attribute` (`attribute_id`, `entity_type_id`, `attribute_code`, `attribute_model`, `backend_model`, `backend_type`, `backend_table`, `frontend_model`, `frontend_input`, `frontend_label`, `frontend_class`, `source_model`, `is_required`, `is_user_defined`, `default_value`, `is_unique`, `note`)
VALUES
  (60, 4, 'name', NULL, '', 'varchar', '', '', 'text', 'Name', '', '', 1, 0, '', 0, ''),
  (61, 4, 'description', NULL, '', 'text', '', '', 'textarea', 'Description', '', '', 0, 0, '', 0, ''),
  (62, 4, 'short_description', NULL, '', 'text', '', '', 'textarea', 'Short Description', '', '', 0, 0, '', 0, ''),
  (66, 4, 'special_from_date', NULL, 'catalog/product_attribute_backend_startdate', 'datetime', '', '', 'date', 'Special Price From Date', '', '', 0, 0, '', 0, ''),
  (67, 4, 'special_to_date', NULL, 'eav/entity_attribute_backend_datetime', 'datetime', '', '', 'date', 'Special Price To Date', '', '', 0, 0, '', 0, ''),
  (71, 4, 'meta_title', NULL, '', 'varchar', '', '', 'text', 'Meta Title', '', '', 0, 0, '', 0, ''),
  (73, 4, 'meta_description', NULL, '', 'varchar', '', '', 'textarea', 'Meta Description', 'validate-length maximum-length-255', '', 0, 0, '', 0, 'Maximum 255 chars');

--;;

INSERT INTO `catalog_product_entity` (`entity_id`, `entity_type_id`, `attribute_set_id`, `type_id`, `sku`, `has_options`, `required_options`, `created_at`, `updated_at`, `activation_date`, `expiry_date`)
VALUES
  (1, 4, 4, 'simple',       'sku-1',   0, 0, '2011-11-21 14:02:58+00:00', '2013-11-14 10:17:49+00:00', NULL, NULL),
  (2, 4, 4, 'configurable', 'sku-2',   1, 1, '2011-11-21 14:04:03+00:00', '2012-02-23 16:38:05+00:00', NULL, NULL),
  (3, 4, 4, 'simple',       'sku-2.1', 0, 0, '2011-11-21 14:04:00+00:00', '2012-02-23 16:38:00+00:00', NULL, NULL),
  (4, 4, 4, 'simple',       'sku-2.2', 0, 0, '2011-11-21 14:04:01+00:00', '2012-02-23 16:38:01+00:00', NULL, NULL),
  (5, 4, 4, 'simple',       'sku-2.3', 0, 0, '2011-11-21 14:04:02+00:00', '2012-02-23 16:38:02+00:00', NULL, NULL),
  (6, 4, 4, 'configurable', 'sku-6',   0, 0, '2011-11-21 14:05:00+00:00', '2012-02-23 16:39:00+00:00', NULL, NULL);

--;;

INSERT INTO `catalog_product_super_link` (`product_id`, `parent_id`)
VALUES
  (3, 2),
  (4, 2),
  (5, 2);

--;;

INSERT INTO `catalog_product_website` (`product_id`, `website_id`)
VALUES
  (1, 1),
  (1, 2),
  (2, 1),
  (3, 1),
  (4, 1),
  (5, 1),
  (6, 2);

--;;

INSERT INTO `cataloginventory_stock` (`stock_id`, `stock_name`)
VALUES
  (1, 'Default');

--;;

INSERT INTO `cataloginventory_stock_status` (`product_id`, `website_id`, `stock_id`, `qty`, `stock_status`)
VALUES
  (1, 1, 1, 2.0000, 1),
  (1, 2, 1, 0.0000, 0);

--;;

INSERT INTO `catalog_product_entity_varchar` (`entity_type_id`, `attribute_id`, `store_id`, `entity_id`, `value`)
VALUES
  (4, 60, 0, 1, 'Simple Product 1'),
  (4, 71, 0, 1, NULL),
  (4, 73, 0, 1, 'This is the simple product with id 1');

--;;

INSERT INTO `catalog_product_entity_text` (`entity_type_id`, `attribute_id`, `store_id`, `entity_id`, `value`)
VALUES
  (4, 61, 0, 1, 'This is the long description for product 1'),
  (4, 62, 0, 1, 'This is the short description for product 1');

--;;

INSERT INTO `catalog_product_entity_datetime` (`entity_type_id`, `attribute_id`, `store_id`, `entity_id`, `value`)
VALUES
  (4, 66, 1, 1, '2015-06-26 12:02:57+00:00'),
  (4, 67, 1, 1, '2015-07-01 13:35:47+00:00');

-- required for mysql:
-- SET GLOBAL sql_mode='';
