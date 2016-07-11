(ns httpeek.db
  (:require [clojure.java.jdbc :as j]
            [cheshire.core :as json]
            [environ.core :refer [env]]))

(def db
  {:classname (env :db-classname)
   :subprotocol (env :db-subprotocol)
   :subname (env :db-subname)
   :user (env :db-user)
   :password (env :db-password)})

(extend-protocol j/IResultSetReadColumn
  org.postgresql.util.PGobject
  (result-set-read-column [pgobj metadata idx]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "jsonb" (json/decode value true)
        :else value))))

(defn create-bin
  ([private-bin?]
  (-> (j/query db (str "INSERT INTO bins VALUES(DEFAULT, '" private-bin? "', DEFAULT, DEFAULT) returning id;"))
    first
    :id))
  ([private-bin? custom-response]
  (-> (j/query db (str "INSERT INTO bins VALUES(DEFAULT, '" private-bin? "', '" custom-response "', DEFAULT) returning id;"))
    first
    :id)))

(defn get-bins [limit]
  (j/query db (str "SELECT * FROM bins LIMIT " limit ";")))

(defn all-requests []
  (j/query db
    ["SELECT * FROM requests;"]))

(defn add-request [bin-id full-request]
  (-> (j/query db (str "INSERT INTO requests(full_request, bin_id) VALUES('" full-request "', '" bin-id "') returning id;"))
    first
    :id))

(defn find-bin-by-id [id]
  (-> (j/query db (str "SELECT * FROM bins WHERE id='"id "';"))
    first))

(defn find-request-by-id [id]
  (-> (j/query db (str "SELECT * FROM requests WHERE id='"id "';"))
    first))

(defn find-requests-by-bin-id [bin-id]
  (j/query db (str "SELECT * FROM requests WHERE bin_id='"bin-id "';")))

(defn delete-bin [bin-id]
  (first (j/with-db-transaction [db db]
                         (j/delete! db :requests ["bin_id = ?" bin-id])
                         (j/delete! db :bins ["id = ?" bin-id]))))
