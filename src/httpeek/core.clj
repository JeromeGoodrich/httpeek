(ns httpeek.core
  (:require [httpeek.db :as db]
            [cheshire.core :as json]))

(defmacro with-error-handling [default form]
  `(try
     ~form
     (catch Exception e#
       ~default)))

(def default-response
  (json/encode {:status 200
                :headers {}
                :body "ok"}))

(defn create-bin [{:keys [private response],
                   :or {private false
                        response default-response}}]
  (with-error-handling nil
    (db/create-bin {:private private
                    :response response})))

(defn find-bin-by-id [id]
  (with-error-handling nil
    (db/find-bin-by-id id)))

(defn get-requests [bin-id]
    (with-error-handling []
      (db/find-requests-by-bin-id bin-id)))

(defn add-request [bin-id full-json-request]
  (with-error-handling nil
    (db/add-request bin-id full-json-request)))

(defn get-bins [limit]
  (with-error-handling 0
    (db/get-bins (:limit limit))))

(defn all-requests []
  (db/all-requests))

(defn delete-bin [bin-id]
  (with-error-handling nil
    (db/delete-bin bin-id)))
