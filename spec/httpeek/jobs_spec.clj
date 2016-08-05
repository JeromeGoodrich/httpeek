(ns httpeek.jobs-spec
  (:require [speclj.core :refer :all]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.scheduler :as qs]
            [httpeek.jobs :refer :all])
  (:import [httpeek.jobs DeleteExpired]))

(describe "httpeek.jobs"

  (context "When scheduling a job"
    (it "actually schedules the job"
      (let [trigger (configure-trigger {:trigger-key "trigger.ms"
                                        :schedule sched-once-every-200-ms})
            job (build-job {:job-type DeleteExpired
                            :job-key "mock-job"})
            scheduler (schedule job trigger)]
        (should (qs/scheduled? scheduler (j/key "mock-job")))))))
