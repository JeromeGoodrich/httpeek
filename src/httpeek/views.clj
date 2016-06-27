(ns httpeek.views
  (require [hiccup.page :as page]
           [hiccup.form :as form]
           [httpeek.core :as core]
           [cheshire.core :as json]
           [hiccup.element :as elem]
           [hiccup.core :as h]))

(defn navbar []
  [:div.mdl-layout.mdl-layout--fixed-header
   [:header.mdl-layout__header
    [:div.mdl-layout__header-row
     [:span.mdl-layout-title (h/h "HTTPeek")]
     [:div.mdl-layout-spacer]
     [:nav.mdl-navigation
      [:a.mdl-navigation__link {:href "/"} (h/h "Home")]]]]
   [:div.mdl-layout__drawer
    [:span.mdl-layout-title (h/h "HTTPeek")]
    [:nav.mdl-navigation.mdl-layout--large-screen-only
     [:a.mdl-navigation__link {:href "/"} (h/h "Home")]]]])

(defn footer []
  [:footer.mdl-mini-footer
   [:div.mdl-mini-footer__left-section
    [:div.mdl-logo (h/h "HTTPeek")]]])

(defn head []
  [:head (page/include-css "https://fonts.googleapis.com/icon?family=Material+Icons"
                           "https://code.getmdl.io/1.1.3/material.light_blue-indigo.min.css"
                           "/css/main.css")])

(defn index-card []
  [:div.index-card.mdl-card.mdl-shadow--2dp
   [:div.mdl-card__title
    [:h2.mdl-card__title-text (h/h "HTTPeek: take a peek at HTTP requests")]]
   [:div.mdl-card__supporting-text (h/h "HTTPeek gives you a unique URL that will collect all requests made to it,
                                        and allow you to inspect those requests in a human friendly way.")]
   [:div.mdl-card__actions.mdl-card--border
    [:form {:action "/bins" :method "post"}
     [:button.mdl-button.mdl.button--fab.mdl-button--colored (h/h "Create Bin")
      [:i.material-icons (h/h "add")]]]]])

(defn list-bin-history []
  [:ul
   (let [list-of-ids (map :id (core/all-bins))]
     (for [id list-of-ids]
       [:li
        [:a.mdl-button.mdl-button--colored  {:href (str "/bin/" id "?inspect")} (h/h (str "Bin: " id))]]))])

(defn history-card []
  [:div.index-card.mdl-card.mdl-shadow--2dp
   [:div.mdl-card__title
    [:h2.mdl-card__title-text (h/h "Bin History")]]
   [:div.mdl-card__actions.mdl-card--border
    (list-bin-history)]])

(defn list-headers [headers]
  [:ul.mdl-list
   (map (fn [[k v]] [:li.mdl-list__item (str (name k) ": " v)]) headers)])

(defn request-card [request]
  (let [created_at (:created_at request)
        headers (:headers (:body request))
        content-length (:content-length (:body request))
        form-params (:form-params (:body request))
        query-params (:query-params (:body request))
        uri (:uri (:body request))
        request-method (:request-method (:body request))]
    [:div.request-card.mdl-card.mdl-shadow--2dp
     [:div.mdl-card__title
      [:h2.mdl-card__title-text (h/h (str request-method))]
      [:h3.mdl-card__subtitle-text (h/h (str uri query-params))]]
     [:div.mdl-card__supporting-text (h/h (str created_at))]
     [:div.mdl-card__actions.mdl-card--border
      [:div.mdl-grid
       [:div.mdl-cell.mdl-cell--4-col (str "Form/Post Params")]
       [:div.mdl-cell.mdl-cell--6-col (str "Headers")]]
      [:div.mdl-grid
       [:div.mdl-cell.mdl-cell--4-col (str form-params)]
       [:div.mdl-cell.mdl-cell--6-col (list-headers headers)]]]]))

(defn index []
  [:body
   (navbar)
   [:main.mdl-layout__content
    (index-card)
    (history-card)]])

(defn not-found []
  (page/html5
    [:div
     [:h1 "Sorry! The Page You were looking for cannot be found"]]))

(defn code-example-card []
  [:div.index-card.mdl-card.mdl-shadow--2dp
   [:div.mdl-card__title
    [:h2.mdl-card__title-text (h/h "make a request to get started")]]
   [:div.mdl-card__actions.mdl-card--border
    [:code (h/h "curl -X POST -d foo=bar 'this bin url'")]]])

(defn inspect [id requests]
  [:body
   (navbar)
   [:main.mdl-layout__content
    [:h3.bin-title (h/h (str "Bin-Url: /bin/" id))]]
   (if (= (count requests) 0)
     (code-example-card)
     (for [request requests]
       (request-card request)))])

(defn layout [component]
  (page/html5
    (head)
    component
    (footer)))
