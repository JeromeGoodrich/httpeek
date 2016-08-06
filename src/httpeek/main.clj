(ns httpeek.main
  (:require [httpeek.jobs :as jobs]
            [httpeek.handler :refer [app*]])
(:import [httpeek.jobs DeleteExpired]))

(defn -main [& args]
  (let [trigger (jobs/configure-trigger {:trigger-key "trigger.ms"
                                         :schedule jobs/sched-once-every-200-ms})
        job (jobs/build-job {:job-type DeleteExpired :job-key "test"})]
    (jobs/schedule job trigger)))
