(ns httpeek.core
  (:require [httpeek.db :as db]
            [cheshire.core :as json]))

(defn create-bin []
  (db/create))

(defn- str-to-uuid [uuid-string]
  (java.util.UUID/fromString uuid-string))

(defn is-valid-id? [string-id]
  (boolean (some #(= string-id %)(map #(str (:id %))(db/all-bins)))))

(defn get-requests [bin-id]
  (db/find-by "requests" "bin_id" (str-to-uuid bin-id)))

(defn add-request [id request-body]
  (db/add-request id (json/generate-string request-body)))

(defn all-bins []
  (db/all-bins))
