(ns httpeek.views
  (require [hiccup.page :as page]
           [httpeek.core :as core]
           [hiccup.core :as h]))

(defn navbar []
  [:div.mdl-layout.mdl-layout--fixed-header
   [:header.mdl-layout__header
    [:div.mdl-layout__header-row
     [:span.mdl-layout-title (h/h "HTTPeek")]
     [:div.mdl-layout-spacer]
     [:nav.mdl-navigation
      [:a.mdl-navigation__link {:href (h/h "/")} (h/h "Home")]]]]
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
     [:button.mdl-button.mdl.button--fab.mdl-button--colored {:type "submit"} (h/h "Create Bin")
      [:i.material-icons (h/h "add")]]
     [:input {:type "checkbox" :value "true" :name "private-bin?"} (h/h "make this bin private")]]]])

(defn list-bin-history []
  [:ul
   (let [list-of-ids (map :id (core/get-bins {:limit 50}))]
     (for [id list-of-ids]
       [:li
        [:a.mdl-button.mdl-button--colored  {:href (h/h (format "/bin/%s/inspect" id))} (h/h (str "Bin: " id))]]))])

(defn history-card []
  [:div.index-card.mdl-card.mdl-shadow--2dp
   [:div.mdl-card__title
    [:h2.mdl-card__title-text (h/h "Bin History")]]
   [:div.mdl-card__actions.mdl-card--border
    (list-bin-history)]])

(defn list-headers [headers]
  [:ul.mdl-list
   (map (fn [[k v]] [:li.mdl-list__item
                     [:b (name k) ] (str ": " v)]) headers)])

(defn request-card [request]
  (let [{:keys [created_at]} request
        {{:keys [headers content-length form-params
                 query-params uri request-method
                 json-params xml-params]} :full_request} request
        display-content {"application/json" (cheshire.core/encode json-params {:pretty true})
                         "text/xml" xml-params
                         "application/xml" xml-params
                         "application/x-www-form-urlencoded" form-params}]
    [:div.request-card.mdl-card.mdl-shadow--2dp
     [:div.mdl-card__title
      [:h2.mdl-card__title-text (h/h (clojure.string/upper-case request-method))]]
     [:div.mdl-card__supporting-text (h/h (str created_at))]
     [:div.mdl-card__actions.mdl-card--border
      [:div.mdl-grid
       [:div.mdl-cell.mdl-cell--6-col
        [:h4 (h/h (str "Headers"))]]
       [:div.mdl-cell.mdl-cell--6-col
        [:h4 (h/h (str "Request Body"))]]]
      [:div.mdl-grid
       [:div.mdl-cell.mdl-cell--6-col (list-headers headers)]
       (if-let [content-type (:content-type headers)]
         [:div.mdl-cell.mdl-cell--6-col
          [:pre (h/h (get display-content content-type))]])]]]))

(defn index-html []
  [:body
   (navbar)
   [:main.mdl-layout__content
    (index-card)
    (history-card)]])

(defn not-found-html []
  (page/html5
    [:div
     [:h1 (h/h "Sorry! The Page You were looking for cannot be found")]]))

(defn code-example-card []
  [:div.index-card.mdl-card.mdl-shadow--2dp
   [:div.mdl-card__title
    [:h2.mdl-card__title-text (h/h "make a request to get started")]]
   [:div.mdl-card__actions.mdl-card--border
    [:code (h/h "curl -X POST -d foo=bar 'this bin url'")]]])

(defn inspect-html [id requests]
  [:body
   (navbar)
   [:main.mdl-layout__content
    [:h3.bin-title (h/h (str "Bin-Url: /bin/" id))]]
   (if (= (count requests) 0)
     (code-example-card)
     (for [request requests]
       (request-card request)))])

(defn wrap-layout [component]
  (page/html5
    (head)
    component
    (footer)))

(defn index-page []
  (wrap-layout (index-html )))

(defn inspect-bin-page [bin-id requests]
  (wrap-layout (inspect-html bin-id requests)))

(defn not-found-page []
  (wrap-layout (not-found-html)))
