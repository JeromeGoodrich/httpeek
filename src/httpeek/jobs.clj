(ns httpeek.jobs
  (:require [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.jobs :as j]
            [httpeek.db :as db]
            [clojurewerkz.quartzite.jobs :refer [defjob]]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.schedule.simple :as s]
            [clojurewerkz.quartzite.schedule.daily-interval :as di]))

(defjob DeleteExpired [_]
  (db/delete-expired-bins))

(defn build-job [job]
  (j/build
    (j/with-identity (j/key (:job-key job)))
    (j/of-type (:job-type job))))

(defn sched-everyday-at-midnight []
  (di/schedule
    (di/with-interval-in-days 1)
    (di/every-day)
    (di/starting-daily-at
      (di/time-of-day 00 00 00))))

(defn sched-once-every-200-ms []
  (s/schedule
    (s/with-interval-in-milliseconds 200)))

(defn configure-trigger [trigger]
  (t/build
    (t/with-identity (t/key (:trigger-key trigger)))
    (t/start-now)
    (t/with-schedule ((:schedule trigger)))))

(defn schedule [job trigger]
  (let [scheduler (-> (qs/initialize) qs/start)]
    (qs/schedule scheduler job trigger)
    scheduler))
