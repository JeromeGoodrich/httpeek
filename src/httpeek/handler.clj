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

(defn handle-inspecting-bin [id session]
  (let [requested-bin (core/find-bin-by-id id)
        private? (:private requested-bin)
        permitted? (some #{id} (:private-bins session))]
    (if requested-bin
      (if (and private? (not permitted?))
        (response/status (response/response "private bin") 403)
        (views/inspect-bin-page id (core/get-requests id)))
    (response/not-found (views/not-found-page)))))

(defn parse-request-to-bin [request]
  (let [id (str->uuid (get-in request [:params :id]))
        body (json/encode (dissoc request :body))
        requested-bin (core/find-bin-by-id id)]
    {:requested-bin requested-bin
     :body body}))

(defn- add-request-to-bin [id request-body]
  (core/add-request id request-body)
  (response/response "ok"))

(defn route-request-to-bin [bin-id {:keys [inspect? private? permitted? body]}]
  (if inspect?
    (if (and private? (not permitted?))
      (response/status (response/response "private bin") 403)
      (views/inspect-bin-page bin-id (core/get-requests bin-id)))
    (add-request-to-bin bin-id body)))

(defn handle-requests-to-bin [{:keys [requested-bin body] :as parsed-request}]
  (if-let [bin-id (:id requested-bin)]
    (add-request-to-bin bin-id body)
    (response/not-found (views/not-found-page))))

(defn handle-creating-bin [form-params]
  (let [private? (boolean (get form-params "private-bin-checkbox"))
        bin-id (core/create-bin {:private private?})]
    (if private?
      (-> (response/redirect (format "/bin/%s/inspect" bin-id))
          (assoc-in [:session] {:private-bins [bin-id]}))
      (response/redirect (format "/bin/%s/inspect" bin-id)))))

(defn handle-deleting-bin [id]
  (if (core/find-bin-by-id id)
   (do (-> id
    str->uuid
    (core/delete-bin))
   (response/redirect "/" 302))
 (response/not-found (views/not-found-page))))

(defroutes app-routes
  (GET "/" [] (views/index-page))
  (POST "/bins" {form-params :form-params} (handle-creating-bin form-params))
  (GET "/bin/:id/inspect" [id :as {session :session}] (handle-inspecting-bin (str->uuid id) session))
  (ANY "/bin/:id" request (-> request
                            (parse-request-to-bin)
                            (handle-requests-to-bin)))
  (DELETE "/bin/:id/delete" [id] (handle-deleting-bin id))
  (GET "/api/bins/:id" [id] (-> id
                                str->uuid
                                core/find-bin-by-id
                                json/encode
                                response/response))
  (route/resources "/")
  (route/not-found (views/not-found-page)))

(def app*
  (-> app-routes
    (middleware/wrap-params)
    (session/wrap-session)))
