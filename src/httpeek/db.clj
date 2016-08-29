(ns httpeek.db
  (:require [clojure.java.jdbc :as j]
            [cheshire.core :as json]
            [clj-time.jdbc]
            [environ.core :refer [env]]))

(def db (env :database-url))

(extend-protocol j/ISQLValue
  clojure.lang.IPersistentMap
  (sql-value [value]
    (doto (org.postgresql.util.PGobject.)
      (.setType "jsonb")
      (.setValue (json/encode value)))))

(extend-protocol j/IResultSetReadColumn
  org.postgresql.util.PGobject
  (result-set-read-column [pgobj metadata idx]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "jsonb" (json/decode value true)
        :else value))))

(defn delete-expired-bins []
  (j/delete! db :bins ["expiration < NOW()"]))

(defn create-bin [{:keys [private response expiration] :as bin-options}]
  (-> (j/query db ["INSERT INTO bins (private, response, expiration) VALUES (?, ?, ?) RETURNING id;" private response expiration])
    first
    :id))

(defn get-bins [limit]
  (j/query db ["SELECT * FROM bins WHERE expiration > NOW() LIMIT ?;" limit]))

(defn all-requests []
  (j/query db ["SELECT * FROM requests;"]))

(defn add-request [bin-id full-request]
  (-> (j/query db ["INSERT INTO requests(full_request, bin_id) VALUES(?, ?) RETURNING id;" full-request bin-id])
    first
    :id))

(defn find-bin-by-id [id]
  (-> (j/query db ["SELECT * FROM bins WHERE id= ?;" id])
    first))

(defn find-request-by-id [id]
  (-> (j/query db ["SELECT * FROM requests WHERE id= ?;" id])
    first))

(defn find-requests-by-bin-id [bin-id]
  (j/query db ["SELECT * FROM requests WHERE bin_id= ?;" bin-id]))

(defn delete-bin [bin-id]
  (first (j/with-db-transaction [db db]
                         (j/delete! db :requests ["bin_id = ?" bin-id])
                         (j/delete! db :bins ["id = ?" bin-id]))))
