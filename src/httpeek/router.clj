(ns httpeek.router
  (:use compojure.core)
  (:require [ring.middleware.session :as session]
            [ring.middleware.flash :as flash]
            [ring.middleware.json :as ring-json]
            [httpeek.core :as core]
            [httpeek.web :as web]
            [httpeek.api :as api]))

(def app*
  (routes (-> api/api-routes
            (wrap-routes ring-json/wrap-json-body {:keywords? true})
            (ring-json/wrap-json-response))
          (-> web/web-routes
            (flash/wrap-flash)
            (session/wrap-session))))
