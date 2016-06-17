(ns httpeek.bin
  (:require [clojure.java.jdbc :as j]))


(def db-spec {:classname "com.postgresql.jdbc.Driver"
                :subprotocol "postgresql"
                :subname "//localhost:5432/httpeek"
                :user "httpeek"
                :password ""})


(defn create []
  (j/db-do-commands db-spec true
    "INSERT INTO bins DEFAULT VALUES;"))

(defn last-inserted []
  (j/query db-spec
    ["SELECT id FROM bins WHERE created_at =(SELECT MAX(created_at) FROM bins);"]))

(defn all-bins []
  (j/query db-spec
    ["SELECT * FROM bins;"]))
