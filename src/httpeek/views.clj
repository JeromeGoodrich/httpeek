(ns httpeek.views
  (require [hiccup.page :as page]
           [hiccup.element :as elem]
           [camel-snake-kebab.core :as csk]
           [httpeek.core :as core]
           [httpeek.content-type-presenter :as presenter]
           [hiccup.core :as h]))

(defn- navbar []
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

(defn- footer []
  [:footer.mdl-mini-footer
   [:div.mdl-mini-footer__left-section
    [:div.mdl-logo (h/h "HTTPeek")]]])

(defn- head []
  [:head (page/include-css "https://fonts.googleapis.com/icon?family=Material+Icons"
                           "https://code.getmdl.io/1.1.3/material.light_blue-indigo.min.css"
                           "/css/main.css")
   (page/include-js "https://code.getmdl.io/1.1.3/material.min.js"
                    "/js/toggle_formats.js"
                    "/js/add_headers.js")])

(def create-bin-form
  [:div.mdl-grid
   [:div.mdl-cell.mdl-cell--12-col
    [:form#creat-bin-form {:action "/bins" :method "post"}
     [:div.mdl-textfield.mdl-js-textfield
      [:input#bin-response-status.mdl-textfield__input {:type "text" :name "status"}]
      [:label.mdl-textfield__label {:for "bin-response-status"} (h/h "Status")]]
     [:div#bin-response-headers.mdl-grid.mdl-grid--nesting
      [:div#header-name-input.mdl-cell.mdl-cell--4-col
       [:div.mdl-textfield.mdl-js-textfield
        [:input#bin-response-header-name.mdl-textfield__input {:type "text" :name "header-name[]"}]
        [:label.mdl-textfield__label {:for "bin-response-header-name"} (h/h "Header Name")]]]
      [:div#header-value-input.mdl-cell.mdl-cell--4-col
       [:div.mdl-textfield.mdl-js-textfield
        [:input#bin-response-header-value.mdl-textfield__input {:type "text" :name "header-value[]"}]
        [:label.mdl-textfield__label {:for "bin-response-header-value"} (h/h "Header Value")]]]
      [:div.mdl-cell.mdl-cell--4-col
       [:button#add-headers-button.mdl-button.mdl-js-button.mdl-button--primary {:type "button"} (h/h "Add Headers")]]]
     [:div.mdl-grid.mdl-grid--nesting
      [:div.mdl-cell.mdl-cell--8-col
       [:div#bin-response-body.mdl-textfield.mdl-js-textfield
        [:textarea#bin-response-body.mdl-textfield__input {:type "text" :name "body"}]
        [:label.mdl-textfield__label {:for "bin-response-body"} (h/h "Body")]]]]
     [:div.mdl-grid.mdl-grid--nesting
      [:div.mdl-cell.mdl-cell--4-col
       [:div#bin-response-body.mdl-textfield.mdl-js-textfield
        [:input#expiration.mdl-textfield__input {:type "text" :name "expiration"}]
        [:label.mdl-textfield__label {:for "expiration"} (h/h "Hours Until Expiration")]]]]
     [:div.mdl-grid.mdl-grid--nesting
      [:div.mdl-cell.mdl-cell--6-col
       [:button.mdl-button.mdl-js-button.mdl-button--primary {:type "submit"} (h/h "Create Bin")
        [:i.material-icons (h/h "add")]]
       [:input {:type "checkbox" :value "true" :name "private-bin"} (h/h "make this bin private")]]]]]])

(defn- index-card [flash]
  [:div.index-card.mdl-card.mdl-shadow--2dp
   [:div.mdl-card__title
    [:h2.mdl-card__title-text (h/h "HTTPeek: take a peek at HTTP requests")]]
   [:div.mdl-card__supporting-text (h/h "HTTPeek gives you a unique URL that will collect all requests made to it,
                                        and allow you to inspect those requests in a human friendly way.")]
   [:div.mdl-card__actions.mdl-card--border
    (for [message flash]
      [:p message])
    create-bin-form]])

(defn- list-bin-history []
  [:ul
   (let [list-of-ids (map :id (core/get-bins {:limit 50}))]
     (for [id list-of-ids]
       [:li
        [:a.mdl-button.mdl-button--colored  {:href (h/h (format "/bin/%s/inspect" id))} (h/h (str "Bin: " id))]]))])

(defn- history-card []
  [:div.index-card.mdl-card.mdl-shadow--2dp
   [:div.mdl-card__title
    [:h2.mdl-card__title-text (h/h "Bin History")]]
   [:div.mdl-card__actions.mdl-card--border
    (list-bin-history)]])

(defn- list-headers [headers]
  [:ul.mdl-list
   (map (fn [[k v]] [:li.mdl-list__item
                     [:p [:b (csk/->HTTP-Header-Case (name k)) ] (str ": " v)]]) headers)])

(defn- raw-headers [headers]
   (map (fn [[k v]] [:li
                     (str (csk/->HTTP-Header-Case (name k)) ": " v)]) headers))

(defn- display-raw-request [{:keys [request-method protocol uri headers body] :as full-request}]
  [:ul.raw-request
   [:li (str (clojure.string/upper-case request-method) " "
             uri " "
             (clojure.string/upper-case protocol) "\r\n")]
   (raw-headers headers)
   (if (not (empty? body))
   [:li body])])

(defn- request-body [body content-type]
  [:div.request-body {:data-type "request-body"}
   `[:div.mdl-cell.mdl-cell--6-col

     ~@(when-not (= body (:body (presenter/present-content-type content-type body)))

         [[:button.toggle-to-formatted.mdl-button.mdl-js-button.mdl-button--colored {:type "button" :style "display:none;"} (h/h "Formatted_Body")]
          [:button.toggle-to-raw.mdl-button.mdl-js-button.mdl-button--colored {:type "button" :style "display:block;"} (h/h "Raw_Body")]])

     [:div.formatted-body {:style "display:block;"}
      [:pre ~(h/h (:body (presenter/present-content-type content-type body)))]
      [:p ~(h/h (:warning (presenter/present-content-type content-type body)))]]
     [:div.raw-body {:style "display:none"}
      [:p ~(h/h body)]]]])

  (defn- request-card [request]
  (let [{:keys [created_at full_request]} request
        {{:keys [headers body
                 request-method
                 query-string]} :full_request} request]
    [:div.request-card.mdl-card.mdl-shadow--2dp
     [:div.mdl-card__title
      [:h2.mdl-card__title-text (h/h (clojure.string/upper-case request-method))]]
     [:div.mdl-card__supporting-text (h/h (str created_at))]
     [:div.request-content
      [:button.toggle-to-formatted.mdl-button.mdl-js-button.mdl-button--colored {:type "button" :style "display:none;"} (h/h "Formatted Request")]
      [:button.toggle-to-raw.mdl-button.mdl-js-button.mdl-button--colored {:type "button" :style "display:block;"} (h/h "Raw Request")]
      [:div.formatted-content {:style "display:block;"}
       [:div.mdl-card__actions.mdl-card--border
        [:div.mdl-grid
         [:div.mdl-cell.mdl-cell--6-col
          [:h4 (h/h (str "Query Params"))]]]
        [:div.mdl-grid
         [:div.mdl-cell.mdl-cell--6-col
          [:p (h/h query-string)]]]
        [:div.mdl-grid
         [:div.mdl-cell.mdl-cell--6-col
          [:h4 (h/h (str "Headers"))]]
         [:div.mdl-cell.mdl-cell--6-col
          [:h4 (h/h (str "Request Body"))]]]
        [:div.mdl-grid
         [:div.mdl-cell.mdl-cell--6-col (list-headers headers)]
         (if-let [content-type (:content-type headers)]
           (request-body body content-type))]]]
      [:div.raw-content {:style "display:none;"}
       [:div.mdl-card__actions.mdl-card--border
        [:div.mdl-grid
         [:div.mdl-cell.mdl-cell--8-col
          [:h4 (h/h (str "Raw Request"))]]]
        [:div.mdl-grid
         [:div.mdl-cell.mdl-cell--8-col (display-raw-request full_request)]]]]]]))

(defn- index-html [flash]
  [:body
   (navbar)
   [:main.mdl-layout__content
    (index-card flash)
    (history-card)]])

(defn- not-found-html []
  (page/html5
    [:div
     [:h1 (h/h "Sorry! The Page You were looking for cannot be found")]]))

(defn- code-example-card [host id]
  [:div.index-card.mdl-card.mdl-shadow--2dp
   [:div.mdl-card__title
    [:h2.mdl-card__title-text (h/h "make a request to get started")]]
   [:div.mdl-card__actions.mdl-card--border
    [:code (h/h (format "curl -X POST -d foo=bar %s/bin/%s" host id))]]])

(defn- inspect-html [id host requests]
  [:body
   (navbar)
   [:main.mdl-layout__content
    [:h4.bin-title (h/h (format "http://%s/bin/%s" host id))]
    [:form.bin-title {:action (h/h (format "/bin/%s/delete" id)) :method "POST"}
     [:button.mdl-button.mdl-js-button.mdl.button--fab.mdl-button--colored {:type "submit"} (h/h "Delete Bin")
      [:i.material-icons (h/h "delete")]]]
   (if (= (count requests) 0)
     (code-example-card host id)
     (for [request requests]
       (request-card request)))]])

(defn- wrap-layout [component]
  (page/html5
    (head)
    component
    (footer)))

(defn index-page [flash]
  (wrap-layout (index-html flash)))

(defn inspect-bin-page [bin-id host requests]
  (wrap-layout (inspect-html bin-id host requests)))

(defn not-found-page []
  (wrap-layout (not-found-html)))
