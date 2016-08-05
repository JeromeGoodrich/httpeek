(ns httpeek.core
  (:require [httpeek.db :as db]
            [validateur.validation :as v]
            [clj-time.core :as t]
            [cheshire.core :as json]))

(defmacro with-error-handling [default form]
  `(try
     ~form
     (catch Exception e#
       ~default)))

(defn validate-expiration [time-to-expiration]
  (let [validator (v/validation-set
                    (v/numericality-of :time-to-expiration
                                       :only-integer true
                                       :gte 1
                                       :lte 24
                                       :message-fn (fn [type _ _ & args]
                                                     (if (some #{type} [:gte :lte :only-integer])
                                                       "expiration time must be an integer between 1 and 24"))))]
    (v/errors :time-to-expiration (validator time-to-expiration))))

(defn- format-errors [errors-map]
  (let [status-errors (v/errors :status errors-map)
        header-errors (v/errors :headers errors-map)]
    (clojure.set/union status-errors header-errors)))

(defn validate-response [response]
  (let [validator (v/validation-set
                (v/presence-of :status
                               :message "status can't be blank")
                (v/presence-of :headers
                               :message "headers must have header name and value"))]
    (format-errors (validator response))))

(def default-response
  (json/encode {:status 200
                :headers {}
                :body "ok"}))

(defn create-bin [{:keys [private response time-to-expiration],
                   :or {private false
                        response default-response
                        time-to-expiration 24}}]
  (with-error-handling nil
    (db/create-bin {:private private
                    :response response
                    :expiration (t/from-now (t/hours time-to-expiration))})))

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
