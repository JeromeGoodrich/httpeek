(ns httpeek.handler
  (:use compojure.core)
  (:require [ring.util.response :as response]
            [compojure.route :as route]
            [httpeek.views.content :as content]
            [httpeek.bin :as bin]
            [httpeek.request :as req]))

(defn new-bin-handler []
  (bin/create)
  (let [bin-id (:id (first (bin/last-inserted)))]
  (response/redirect (str "bin/" bin-id "?inspect"))))

(defn bin-handler [request]
 (if (= (:query-string request) "inspect")
    (content/inspect))
    (do (req/create)
      (response/response "ok")))


(defroutes app-routes
           (GET "/" [] (content/index))
           (POST "/bins" []  (new-bin-handler))
           (GET "/bin/:id" request (bin-handler request))
           (route/not-found (content/not-found)))

(def app*
  app-routes)

