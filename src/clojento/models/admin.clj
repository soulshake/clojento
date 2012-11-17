(ns clojento.models.admin
	(:require [clojento.models.common :as common])
	(:use [korma.core]))

(def role-fields (fields [:role_id :id] [:role_name :name] [:role_type :type]))
(defentity role
	(table :admin_role)
	(database common/db))

(defentity user
	(table :admin_user)
	(database common/db))

(def all-users (select user))
(def all-roles (select role role-fields))
