(ns httpeek.handler
  (:use compojure.core)
  (:require [ring.util.response :as response]
            [compojure.route :as route]))

(defroutes app-routes
           (GET "/" [] (->(response/resource-response "index.html" {:root "public"})
                         (response/content-type "text/html")))
           (route/not-found "Page not Found"))

(def app*
  app-routes)

