(ns httpeek.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as response]))

(defroutes app-routes
           (GET "/" [] (->(response/resource-response "index.html" {:root "public"})
                         (response/content-type "text/html")))
           (route/not-found "Page not Found"))

(def app*
  (handler/site app-routes))

