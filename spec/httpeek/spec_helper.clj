(ns httpeek.spec-helper
  (:require [clojure.java.jdbc :as j]
            [httpeek.db :as db]))

(defn reset-db []
  (do (j/execute! db/db ["DELETE FROM requests;"])
    (j/execute! db/db ["DELETE FROM bins;"])))
