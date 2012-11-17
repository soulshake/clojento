(ns clojento.views.admin
  (:require [clojento.models.admin :as admin] [clojento.views.common :as common])
  (:use [noir.core :only [defpage defpartial url-for]]
        [noir.response :only [redirect]]
        [hiccup.core :only [html]]))

(defpage user_show "/admin/user/:id" {id :id}
	(common/layout
	[:h1 (str "User " id " Detail Page")]
	[:hr]))

(defpartial user-line [{:keys [user_id username firstname lastname]}]
	[:tr
		[:td user_id]
		[:td [:a {:href (url-for user_show {:id user_id})} username]]
		[:td firstname]
		[:td lastname]
	])


(defpage users_index "/admin/users" {} (common/layout
	[:h1 "Users"]
	[:hr]
	(let [users admin/all-users]
		[:table
			[:thead [:tr
				[:th "id"]
				[:th "username"]
				[:th "firstname"]
				[:th "lastname"]
			]]
			[:tbody (map user-line users) ]
		])))

(defpage role_show "/admin/role/:id" {id :id}
	(common/layout
	[:h1 (str "Role " id " Detail Page")]
	[:hr]))

(defpartial role-line [{:keys [id name type]}]
	[:tr
		[:td id]
		[:td [:a {:href (url-for role_show {:id id})} name]]
		[:td type]
	])

(defpage roles_index "/admin/roles" {} (common/layout
	[:h1 "Roles"]
	[:hr]
	(let [roles admin/all-roles]
		[:table
			[:thead [:tr
				[:th "id"]
				[:th "name"]
				[:th "type"]
			]]
			[:tbody (map role-line roles) ]
		])))

