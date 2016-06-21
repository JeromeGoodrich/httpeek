(ns httpeek.views.content
  (require [hiccup.page :as page]
           [hiccup.form :as form]))

(defn index []
  (page/html5
    [:div
     [:h1 "hello world"]
     (form/form-to [:post "/bins"]
        (form/submit-button "create new request bin"))]))

(defn not-found []
  (page/html5
    [:div
     [:h1 "Sorry! The Page You were looking for cannot be found"]]))

(defn inspect [requests]
  (page/html5
    [:div
     [:h1 "YO"]]
    [:div requests]))
