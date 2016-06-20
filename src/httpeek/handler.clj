(ns httpeek.handler
  (:use compojure.core)
  (:require [ring.util.response :as response]
            [compojure.route :as route]
            [httpeek.views.content :as content]
            [httpeek.db :as db]))

(defn create-bin []
  (let [bin-id (db/create)]
    (response/redirect (str "/bin/" bin-id "?inspect"))))

(defn handle-bin-get [request]
  (if (some #(= (get-in request [:params :id]) %) (map #(str (:id %)) (db/all-bins)))
    (if (= (:query-string request) "inspect")
      (content/inspect)
      (response/response"ok"))
    (response/not-found (content/not-found))))

(defroutes app-routes
  (GET "/" [] (content/index))
  (POST "/bins" [] (create-bin))
  (GET "/bin/:id" request (handle-bin-get request))
  (route/not-found (content/not-found)))

(def app*
  app-routes)

