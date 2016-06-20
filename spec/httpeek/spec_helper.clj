(ns httpeek.spec-helper
  (:require [clojure.java.jdbc :as j]
            [httpeek.bin :as db]))

(defn reset-db []
  (j/execute! db/db ["DELETE FROM bins;"]))
