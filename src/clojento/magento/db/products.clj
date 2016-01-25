(ns clojento.magento.db.products
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns     "clojento/magento/db/products.sql")
(hugsql/def-sqlvec-fns "clojento/magento/db/products.sql")
