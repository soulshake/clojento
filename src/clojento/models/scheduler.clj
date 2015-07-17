;(ns clojento.models.scheduler
;	(:require [clojento.models.common :as common])
;	(:use [korma.core]))
;
;(defentity job
;	(table :cron_schedule)
;	(database common/db))
;
;(def base (-> (select* job)
;	(fields [:schedule_id :id] [:job_code :code] :status :messages :created_at :scheduled_at :executed_at :finished_at)
;	(order :scheduled_at)))
;
;
;(def jobs (select base))
