(ns httpeek.views.not-found
  (require [hiccup.core :as h]))

(defn not-found-html []
    [:div
     [:h1 (h/h "Sorry! The Page You were looking for cannot be found")]])
