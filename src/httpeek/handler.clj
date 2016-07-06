(ns httpeek.handler
  (:use compojure.core)
  (:require [ring.util.response :as response]
            [ring.middleware.params :as middleware]
            [ring.middleware.session :as session]
            [ring.middleware.json :as ring-json]
            [compojure.route :as route]
            [httpeek.core :as core]
            [cheshire.core :as json]
            [httpeek.views :as views]))

(defn- str->uuid [uuid-string]
  (core/with-error-handling nil
                            (java.util.UUID/fromString uuid-string)))

(defn- handle-web-inspect-bin [id session]
  (let [requested-bin (core/find-bin-by-id id)
        private? (:private requested-bin)
        permitted? (some #{id} (:private-bins session))]
    (if requested-bin
      (if (and private? (not permitted?))
        (response/status (response/response "private bin") 403)
        (views/inspect-bin-page id (core/get-requests id)))
      (response/not-found (views/not-found-page)))))

(defn- parse-request-to-bin [request]
  (let [id (str->uuid (get-in request [:params :id]))
        body (json/encode (dissoc request :body))
        requested-bin (core/find-bin-by-id id)]
    {:requested-bin requested-bin
     :body body}))

(defn- add-request-to-bin [id request-body]
  (core/add-request id request-body)
  (response/response "ok"))

(defn- route-request-to-bin [{:keys [requested-bin body] :as parsed-request}]
  (if-let [bin-id (:id requested-bin)]
    (add-request-to-bin bin-id body)
    (response/not-found (views/not-found-page))))

(defn- handle-web-create-bin [form-params]
  (let [private? (boolean (get form-params "private-bin?"))
        bin-id (core/create-bin {:private private?})]
    (if private?
      (-> (response/redirect (format "/bin/%s/inspect" bin-id))
        (assoc-in [:session] {:private-bins [bin-id]}))
      (response/redirect (format "/bin/%s/inspect" bin-id)))))

(defn- handle-web-delete-bin [id]
  (if-let [bin-id (core/find-bin-by-id (str->uuid id))]
    (do (core/delete-bin bin-id)
      (response/redirect "/" 302))
    (response/not-found (views/not-found-page))))

(defn- handle-web-request-to-bin [request]
  (-> request
    (parse-request-to-bin)
    (route-request-to-bin)))

(defn- handle-api-not-found [message]
  (response/not-found {:message message}))

(defn- handle-api-get-bin [id]
  (if-let [bin-id (core/find-bin-by-id (str->uuid id))]
    (response/response {:bin-details bin-id})
    (handle-api-not-found (format "The bin %s does not exist" id))))

(defn- handle-api-create-bin [{:strs [host] :as headers}]
  (let [bin-id (core/create-bin {:private false})]
    (response/response {:bin-url (format "http://%s/bin/%s" host bin-id)
                        :inspect-url (format "http://%s/bin/%s/inspect" host bin-id)
                        :delete-url (format "http://%s/bin/%s/delete" host bin-id)})))

(defn- handle-api-delete-bin [id]
  (let [bin-id (str->uuid id)
        delete-count (core/delete-bin bin-id)]
    (if (< 0 delete-count)
      (response/response {:message (str "bin" bin-id "has been deleted")})
      (handle-api-not-found (format "The bin %s could not be deleted because it doesn't exist" id)))))

(defn- handle-api-inspect-bin [id]
  (if-let [bin-id (:id (core/find-bin-by-id id))]
    (response/response {:bin-id bin-id
                        :requests (core/get-requests bin-id)})
    (handle-api-not-found (format "The bin %s could not be found" id))))

(defn- handle-api-bin-index []
  (response/response {:bins (core/get-bins {:limit 50})}))

(defroutes api-routes
  (context "/api" []
    (GET "/" [] (handle-api-bin-index))
    (GET "/bin/:id/inspect" [id] (handle-api-inspect-bin id))
    (DELETE "/bin/:id/delete" [id] (handle-api-delete-bin id))
    (POST "/bins" {headers :headers} (handle-api-create-bin headers))
    (GET "/bin/:id" [id] (handle-api-get-bin id))
    (route/not-found (handle-api-not-found "This resource could not be found"))))

(defroutes web-routes
  (GET "/" [] (views/index-page))
  (POST "/bins" {form-params :form-params} (handle-web-create-bin form-params))
  (GET "/bin/:id/inspect" [id :as {session :session}] (handle-web-inspect-bin (str->uuid id) session))
  (ANY "/bin/:id" req (handle-web-request-to-bin req))
  (DELETE "/bin/:id/delete" [id] (handle-web-delete-bin id))
  (route/resources "/")
  (route/not-found (views/not-found-page)))

(def app*
  (routes (-> api-routes
            (wrap-routes ring-json/wrap-json-response)
            (ring-json/wrap-json-response))
          (-> web-routes
            (middleware/wrap-params)
            (session/wrap-session))))
