(ns clojento.views.admin
  (:require [clojento.models.admin :as admin] [clojento.views.common :as common])
  (:use [noir.core :only [defpage url-for]]
        [noir.response :only [redirect]]
        [korma.core :only [select fields]]
        [hiccup.core :only [html]]))

(defpage user_show "/admin/user/:id" {id :id}
	(common/layout
	[:h1 (str "User " id " Detail Page")]
	[:hr]))

(defpage users_index "/admin/users" {} (common/layout
	[:h1 "Users"]
	[:hr]
	(let [users (select admin/user) user (first users)]
		[:table
			[:thead [:tr
				[:th "id"]
				[:th "username"]
				[:th "firstname"]
				[:th "lastname"]
			]]
			[:tbody
				[:tr
					[:td (get user :user_id)]
					[:td [:a {:href (url-for user_show {:id (get user :user_id)})} (get user :username)]]
					[:td (get user :firstname)]
					[:td (get user :lastname)]
				]
			]
		])))

(defpage role_show "/admin/role/:id" {id :id}
	(common/layout
	[:h1 (str "Role " id " Detail Page")]
	[:hr]))

(defpage roles_index "/admin/roles" {} (common/layout
	[:h1 "Roles"]
	[:hr]
	(let [roles (select admin/role) role (first roles)]
		[:table
			[:thead [:tr
				[:th "id"]
				[:th "name"]
				[:th "type"]
			]]
			[:tbody
				[:tr
					[:td (get role :role_id)]
					[:td [:a {:href (url-for role_show {:id (get role :role_id)})} (get role :role_name)]]
					[:td (get role :role_type)]
				]
			]
		])))

