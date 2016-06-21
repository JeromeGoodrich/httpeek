(ns httpeek.handler
  (:use compojure.core)
  (:require [ring.util.response :as response]
            [ring.util.request :as req]
            [compojure.route :as route]
            [httpeek.views.content :as content]
            [httpeek.db :as db]
            [ring.middleware.json :as ring-json]
            [cheshire.core :as json]))

(defn create-bin []
  (let [bin-id (db/create)]
    (response/redirect (str "/bin/" bin-id "?inspect"))))

(defn handle-bin-get [request]
  (if (some #(= (get-in request [:params :id]) %) (map #(str (:id %)) (db/all-bins)))
    (if (= (:query-string request) "inspect")
      (content/inspect (db/find-requests-by "bin_id" (java.util.UUID/fromString (get-in request [:params :id]))))
      (do (db/add-request (get-in request [:params :id]) (json/generate-string (dissoc request :body)))
          (response/response"ok")))
    (response/not-found (content/not-found))))

(defroutes app-routes
  (GET "/" [] (content/index))
  (POST "/bins" [] (create-bin))
  (GET "/bin/:id" request (handle-bin-get request))
  (route/not-found (content/not-found)))

(def app*
  app-routes)

