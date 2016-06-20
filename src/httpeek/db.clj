(ns httpeek.db
  (:require [clojure.java.jdbc :as j]))

(def db {:classname "com.postgresql.jdbc.Driver"
         :subprotocol "postgresql"
         :subname "//localhost:5432/httpeek"
         :user "httpeek"
         :password ""})

(defn create []
  (-> (j/query db "INSERT INTO bins DEFAULT VALUES returning id;")
    first
    :id))

(defn all-bins []
  (j/query db
    ["SELECT * FROM bins;"]))

(defn all-requests []
  (j/query db
    ["SELECT * FROM requests;"]))

(defn add-request [bin-id body]
  (-> (j/query db (str "INSERT INTO requests(body, bin_id) VALUES('" body "', '" bin-id "') returning id;"))
    first
    :id))

(defn find-request [id]
  (-> (j/query db (str "SELECT * FROM requests WHERE id=" id";"))
    first))
