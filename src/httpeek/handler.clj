(ns httpeek.handler
  (:use compojure.core)
  (:require [ring.util.response :as response]
            [ring.middleware.params :as middleware]
            [ring.middleware.session :as session]
            [compojure.route :as route]
            [httpeek.core :as core]
            [cheshire.core :as json]
            [httpeek.views :as views]))

(defn- str->uuid [uuid-string]
  (core/with-error-handling nil
                            (java.util.UUID/fromString uuid-string)))

(defn parse-request-to-bin [request]
  (let [id (str->uuid (get-in request [:params :id]))
        inspect (= (:query-string request) "inspect")
        private-bins (get-in request [:session :private-bins])
        body (json/encode (dissoc request :body))
        requested-bin (core/find-bin-by-id id)
        private? (:private requested-bin)
        permitted? (some #{id} private-bins)]
    (assoc {} :requested-bin requested-bin :inspect inspect :body body :private? private? :permitted? permitted?)))

(defn- add-request-to-bin [id request-body]
  (core/add-request id request-body)
  (response/response "ok"))

(defn route-request-to-bin [bin-id parsed-request]
  (let [{:keys [inspect private? permitted? body]} parsed-request]
    (if inspect
      (if (and private? (not permitted?))
        (response/status (response/response "private bin") 403)
        (views/inspect-bin-page bin-id (core/get-requests bin-id)))
      (add-request-to-bin bin-id body))))

(defn handle-requests-to-bin [parsed-request]
  (let [{:keys [requested-bin]} parsed-request]
    (if-let [bin-id (:id requested-bin)]
      (route-request-to-bin bin-id parsed-request)
      (response/not-found (views/not-found-page)))))

(defn handle-creating-bin [form-params]
  (let [private? (boolean (get form-params "private-bin-checkbox"))
        bin-id (core/create-bin {:private private?})]
    (if private?
      (-> (response/redirect (format "/bin/%s?inspect" bin-id))
          (assoc-in [:session] {:private-bins [bin-id]}))
      (response/redirect (format "/bin/%s?inspect" bin-id)))))

(defroutes app-routes
  (GET "/" [] (views/index-page))
  (POST "/bins" {form-params :form-params} (handle-creating-bin form-params))
  (ANY "/bin/:id" request (-> request
                            (parse-request-to-bin)
                            (handle-requests-to-bin)))
  (route/resources "/")
  (route/not-found (views/not-found-page)))

(def app*
  (-> app-routes
    (middleware/wrap-params)
    (session/wrap-session)))
