(ns httpeek.handler
  (:use compojure.core)
  (:require [ring.util.response :as response]
            [compojure.route :as route]))

(def bins (ref {}))

(def id-counter (atom 0))

(defn add-new-bin [_]
  (let [id (swap! id-counter inc)
        id (Long/toString id 36)
        bin-url (str "/bin/" id)]
    (dosync (alter bins assoc bin-url {}))))

(defn new-bin-handler [request]
  (add-new-bin (:uri request))
  {:status 201 :headers {}})


(defn bin-handler [request]
  (let [params (:query-string request)
        url (:uri request)]
  (if (= params "inspect")
    (response/redirect (str url "?" params) 302))))

(defroutes app-routes
           (GET "/" [_] (->(response/resource-response "index.html" {:root "public"})
                         (response/content-type "text/html")))
           (POST "/bins" request (new-bin-handler request))
           (GET  "/bin/:id" request (bin-handler request))
           (route/not-found "Page not Found"))

(def app*
  app-routes)

