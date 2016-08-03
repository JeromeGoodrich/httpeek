(ns httpeek.spec-helper
  (:require [clojure.java.jdbc :as j]
            [httpeek.db :as db]
            [cheshire.core :as json]))

(defn reset-db []
  (j/execute! db/db ["DELETE FROM requests;"])
    (j/execute! db/db ["DELETE FROM bins;"]))

(def bin-response
    (json/encode {:status 500
                  :headers {"foo" "bar"}
                  :body "hello world"}))
