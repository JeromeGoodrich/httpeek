(ns httpeek.core
  (:require [httpeek.db :as db]))

(defmacro with-error-handling [default form]
  `(try
    ~form
    (catch Exception e#
      ~default)))

(defn create-bin [private-bin?]
  (db/create-bin (:private private-bin?)))

(defn find-bin-by-id [id]
  (with-error-handling nil
    (db/find-bin-by-id id)))

(defn get-requests [bin-id]
    (with-error-handling []
      (db/find-requests-by-bin-id bin-id)))

(defn add-request [bin-id full-json-request]
  (with-error-handling nil
    (db/add-request bin-id full-json-request)))

(defn all-bins []
  (take 50 (db/all-bins)))

(defn all-requests []
  (db/all-requests))

(defn delete-bin [bin-id]
  (with-error-handling nil
    (db/delete-bin bin-id)))
