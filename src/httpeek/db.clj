(ns httpeek.db
  (:require [clojure.java.jdbc :as j]
            [cheshire.core :as json]))

(def db {:classname "com.postgresql.jdbc.Driver"
         :subprotocol "postgresql"
         :subname "//localhost:5432/httpeek"
         :user "httpeek"
         :password ""})

(extend-protocol j/IResultSetReadColumn
  org.postgresql.util.PGobject
  (result-set-read-column [pgobj metadata idx]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "jsonb" (json/decode value true)
        :else value))))

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

(defn find-by [table column value]
  (j/query db (str "SELECT * FROM " table " WHERE " column "='" value "';")))
