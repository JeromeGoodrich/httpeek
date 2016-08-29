(ns httpeek.api
  (:use compojure.core)
  (:require [ring.util.response :as response]
            [ring.middleware.json :as ring-json]
            [clojure.walk :as walk]
            [compojure.route :as route]
            [httpeek.core :as core]))

(defn- handle-api-not-found [message]
  (response/not-found {:message message}))

(defn- validate-response-body [body]
  (if (and (vector? body) (every? integer? body))
    body
    nil))

(defn- format-response [response response-body]
  (let [formatted-response (-> (update response :headers walk/stringify-keys)
                         (assoc :body response-body))
        errors (core/validate-response formatted-response)]
    (if (empty? errors)
      formatted-response
      nil)))

(defn- response-config [response]
  (if-let [response-body (validate-response-body (:body response))]
    (format-response response response-body)
    nil))

(defn- handle-api-create-bin [body {:strs [host] :as headers}]
  (if-let [response (response-config body)]
    (let [bin-id (core/create-bin {:private false :response response})]
      (response/response {:bin-url (format "http://%s/bin/%s" host bin-id)
                          :inspect-url (format "http://%s/bin/%s/inspect" host bin-id)
                          :delete-url (format "http://%s/api/bin/%s/" host bin-id)}))
    {:status 400 :headers {} :body "Bad Request"}))

(defn- handle-api-delete-bin [id]
  (let [bin-id (core/str->uuid id)
        delete-count (core/delete-bin bin-id)]
    (if (< 0 delete-count)
      (response/response {:message (str "bin" bin-id "has been deleted")})
      (handle-api-not-found (format "The bin %s could not be deleted because it doesn't exist" id)))))

(defn- handle-api-inspect-bin [id]
  (if-let [bin-id (:id (core/find-bin-by-id (core/str->uuid id)))]
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
    (POST "/bins" {body :body headers :headers} (handle-api-create-bin body headers))
    (route/not-found (handle-api-not-found "This resource could not be found"))))


