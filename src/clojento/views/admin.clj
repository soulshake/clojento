;(ns clojento.views.admin
;  (:require [clojento.models.admin :as admin] [clojento.views.common :as common] clojento.models.scheduler)
;  (:use [noir.core :only [defpage defpartial url-for]]
;        [noir.response :only [redirect]]
;        [hiccup.core :only [html]]))
;
;(defpage user_show "/admin/user/:id" {id :id}
;	(common/layout
;	[:h1 (str "User " id " Detail Page")]
;	[:hr]))
;
;(defpartial user-line [{:keys [user_id username firstname lastname]}]
;	[:tr
;		[:td user_id]
;		[:td [:a {:href (url-for user_show {:id user_id})} username]]
;		[:td firstname]
;		[:td lastname]
;	])
;
;(defpage users_index "/admin/users" {} (common/layout
;	[:h1 "Users"]
;	[:hr]
;	(let [users admin/all-users]
;		[:table
;			[:thead [:tr
;				[:th "id"]
;				[:th "username"]
;				[:th "firstname"]
;				[:th "lastname"]
;			]]
;			[:tbody (map user-line users) ]
;		])))
;
;(defpage role_show "/admin/role/:id" {id :id}
;	(common/layout
;	[:h1 (str "Role " id " Detail Page")]
;	[:hr]))
;
;(defpartial role-line [{:keys [id name type]}]
;	[:tr
;		[:td id]
;		[:td [:a {:href (url-for role_show {:id id})} name]]
;		[:td type]
;	])
;
;(defpage roles_index "/admin/roles" {} (common/layout
;	[:h1 "Roles"]
;	[:hr]
;	(let [roles admin/all-roles]
;		[:table
;			[:thead [:tr
;				[:th "id"]
;				[:th "name"]
;				[:th "type"]
;			]]
;			[:tbody (map role-line roles) ]
;		])))
;
;(defpartial job-line [{:keys [id code status created_at scheduled_at executed_at finished_at]}]
;	[:tr
;		[:td id]
;		[:td code]
;		[:td (if (= status "success") [:span.success.round.label status] [:span.secondary.round.label status])]
;		[:td created_at]
;		[:td scheduled_at]
;		[:td executed_at]
;		[:td finished_at]])
;
;(defpage scheduler-jobs "/scheduler/jobs" {} (common/layout
;	[:h1 "Scheduler > Jobs"]
;	[:hr]
;	[:table
;		[:thead [:tr
;			[:th "id"]
;			[:th "code"]
;			[:th "status"]
;			[:th "created_at"]
;			[:th "scheduled_at"]
;			[:th "executed_at"]
;			[:th "finished_at"]
;		]]
;		[:tbody (map job-line clojento.models.scheduler/jobs) ]
;		]))
;
;(defn job-values [job] 
;	(clojure.string/join "\t" (map job [:id :code :status :created_at :scheduled_at :executed_at :finished_at])))
;
;(defpage "/data/jobs.tsv" {} (noir.response/content-type "text/tab-separated-values; charset=utf-8" 
;	(clojure.string/join "\n"
;		(cons
;			(clojure.string/join "\t" ["id" "code" "status" "created_at" "scheduled_at" "executed_at" "finished_at"])
;			(map job-values clojento.models.scheduler/jobs)))))
;
;
