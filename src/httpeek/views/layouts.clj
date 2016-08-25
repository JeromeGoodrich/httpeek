(ns httpeek.views.layouts
  (require [hiccup.page :as page]
           [httpeek.views.components :refer :all]
           [httpeek.views.index :as index]
           [httpeek.views.inspect :as inspect]
           [httpeek.views.not-found :as not-found]))

(defn- wrap-layout [component]
  (page/html5
    (head)
    component
    (footer)))

(defn index-page [flash]
  (wrap-layout (index/index-html flash)))

(defn inspect-bin-page [bin-id host requests]
  (wrap-layout (inspect/inspect-html bin-id host requests)))

(defn not-found-page []
  (wrap-layout (not-found/not-found-html)))


