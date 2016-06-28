(ns httpeek.handler
  (:use compojure.core)
  (:require [ring.util.response :as response]
            [compojure.route :as route]
            [httpeek.core :as core]
            [cheshire.core :as json]
            [httpeek.views :as views]))

(defn- str->uuid [uuid-string]
  (core/with-error-handling nil
    (java.util.UUID/fromString uuid-string)))

(defn- parse-bin-request [request]
  (let [id (str->uuid (get-in request [:params :id]))
        inspect (= (:query-string request) "inspect")
        body (json/encode (dissoc request :body))]
    (assoc {} :id id :inspect inspect :body body)))

(defn- add-request-to-bin [id request-body]
  (core/add-request id request-body)
  (response/response "ok"))

(defn- set-response-for-bin [id inspect body]
  (if-let [bin-id (:id (core/find-bin-by-id id))]
    (if inspect
      (views/inspect-bin-page bin-id (core/get-requests bin-id))
      (add-request-to-bin bin-id body))
    (response/not-found (views/not-found-page))))

(defn handle-getting-bin [request]
  (set-response-for-bin (:id request)
                        (:inspect request)
                        (:body request)))

(defn handle-creating-bin []
  (->> (core/create-bin)
    (format "/bin/%s?inspect")
    (response/redirect)))

(defroutes app-routes
  (GET "/" [] (views/index-page))
  (POST "/bins" [] (handle-creating-bin))
  (ANY "/bin/:id" request (-> request
                            (parse-bin-request)
                            (handle-getting-bin)))
  (route/resources "/")
  (route/not-found (views/not-found-page)))

(def app*
  app-routes)
