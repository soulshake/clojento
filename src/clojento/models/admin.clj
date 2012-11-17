(ns clojento.models.admin
	(:require [clojento.models.common :as common])
	(:use [korma.core]))

(defentity role
	(table :admin_role)
	(database common/db))

(defentity user
	(table :admin_user)
	(database common/db))

