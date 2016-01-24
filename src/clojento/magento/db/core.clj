(ns clojento.magento.db.core
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns     "clojento/magento/db/core.sql")
(hugsql/def-sqlvec-fns "clojento/magento/db/core.sql")
