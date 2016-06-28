(ns httpeek.core
  (:require [httpeek.db :as db]))

(defn create-bin []
  (db/create-bin))

(defn find-bin-by-id [id]
  (try
    (db/find-bin-by-id id)
    (catch Throwable e
      nil)))

(defn get-requests [bin-id]
  (try
    (db/find-requests-by-bin-id bin-id)
    (catch Throwable e
      nil)))

(defn add-request [bin-id full-json-request]
  (try
    (db/add-request bin-id full-json-request)
    (catch Throwable e
      nil)))

(defn all-bins []
  (db/all-bins))

(defn all-requests []
  (db/all-requests))
