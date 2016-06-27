(ns httpeek.handler
  (:use compojure.core)
  (:require [ring.util.response :as response]
            [compojure.route :as route]
            [httpeek.core :as core]
            [httpeek.views :as views]))

(defn- parse-bin-request [request]
  (let [parsed-request {}
        id (get-in request [:params :id])
        inspect (= (:query-string request) "inspect")
        body (dissoc request :body)]
    (assoc parsed-request :id id :inspect inspect :body body)))

(defn- add-request-to-bin [id request-body]
    (core/add-request id request-body)
    (response/response "ok"))

(defn- set-response-for-bin [id inspect body]
  (if (core/is-valid-id? id)
    (if inspect
      (views/layout (views/inspect id (core/get-requests id)))
      (add-request-to-bin id body))
    (response/not-found (views/not-found))))

(defn handle-bin-get [request]
  (set-response-for-bin (:id request)
                        (:inspect request)
                        (:body request)))

(defroutes app-routes
  (GET "/" [] (views/layout (views/index)))
  (POST "/bins" [] (response/redirect (format "/bin/%s?inspect" (core/create-bin))))
  (ANY "/bin/:id" request (handle-bin-get (parse-bin-request request)))
  (route/resources "/")
  (route/not-found (views/not-found)))

(def app*
  app-routes)
