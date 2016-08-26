(ns httpeek.views.not-found
  (require [hiccup.core :as h]
           [httpeek.views.components :refer :all]))

(def not-found-html
   [:body navbar
    [:div
     [:h1 (h/h "Sorry! The Page You were looking for cannot be found")]]])
