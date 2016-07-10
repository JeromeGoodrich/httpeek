(ns httpeek.views
  (require [hiccup.page :as page]
           [httpeek.core :as core]
           [hiccup.core :as h])
  (:import [java.io StringReader StringWriter]
           [javax.xml.transform TransformerFactory OutputKeys]
           [org.xml.sax SAXParseException]
           [javax.xml.transform.stream StreamSource StreamResult]))

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
                           "/css/main.css")
   (page/include-js "https://code.getmdl.io/1.1.3/material.min.js")])

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
                     [:p [:b (name k) ] (str ": " v)]]) headers)])

(defn ppxml [xml-str]
  (let [in  (StreamSource. (StringReader. xml-str))
        out (StreamResult. (StringWriter.))
        transformer (.newTransformer
                     (TransformerFactory/newInstance))]
    (doseq [[prop val] {OutputKeys/INDENT "yes"
                        OutputKeys/METHOD "xml"
                        "{http://xml.apache.org/xslt}indent-amount" "2"}]
      (.setOutputProperty transformer prop val))
    (.transform transformer in out)
    (str (.getWriter out))))

(defn- format-xml [xml-string]
  (core/with-error-handling "Malformed XML in the request body"
                            (ppxml xml-string)))

(defn- format-json [body]
  (-> body
    cheshire.core/decode
    (cheshire.core/encode {:pretty true})))

(def display-content-map
  {"application/json" format-json
   "text/xml" format-xml
   "application/xml" format-xml
   "application/x-www-form-urlencoded" identity})

(defn- display-content [content-type body]
  ((get display-content-map content-type) body))

(defn raw-headers [headers]
   (map (fn [[k v]] [:li
                     (str (name k) ": " v)]) headers))

(defn display-raw-request [{:keys [request-method protocol uri headers body] :as full-request}]
  [:ul.raw-request
   [:li (str (clojure.string/upper-case request-method) " "
             uri " "
             (clojure.string/upper-case protocol) "\r\n")]
   (raw-headers headers)
   (if (not (empty? body))
   [:li body])])

(defn request-card [request]
  (let [{:keys [created_at full_request]} request
        {{:keys [headers body
                 request-method]} :full_request} request]
    [:div.request-card.mdl-card.mdl-shadow--2dp
     [:div.mdl-card__title
      [:h2.mdl-card__title-text (h/h (clojure.string/upper-case request-method))]]
     [:div.mdl-card__supporting-text (h/h (str created_at))]
     [:div.mdl-tabs.mdl-js-tabs
      [:div.mdl-tabs__tab-bar
       [:a.mdl-tabs__tab.is-active {:href "#formatted-request"} "Formatted-Request"]
       [:a.mdl-tabs__tab {:href "#raw-request"} "Raw-Request"]]
      [:div#formatted-request.mdl-tabs__panel.is-active
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
            [:pre (h/h (display-content content-type body))]])]]]
      [:div#raw-request.mdl-tabs__panel
        [:div.mdl-card__actions.mdl-card--border
        [:div.mdl-grid
         [:div.mdl-cell.mdl-cell--8-col
          [:h4 (h/h (str "Raw Request"))]]]
        [:div.mdl-grid
         [:div.mdl-cell.mdl-cell--8-col (display-raw-request full_request)]]]]]]))

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

(defn code-example-card [host id]
  [:div.index-card.mdl-card.mdl-shadow--2dp
   [:div.mdl-card__title
    [:h2.mdl-card__title-text (h/h "make a request to get started")]]
   [:div.mdl-card__actions.mdl-card--border
    [:code (h/h (format "curl -X POST -d foo=bar %s/bin/%s" host id))]]])

(defn inspect-html [id host requests]
  [:body
   (navbar)
   [:main.mdl-layout__content
    [:h4.bin-title (h/h (format "URL: %s/bin/%s" host id))]
    [:form.bin-title {:action (h/h (format "/bin/%s/delete" id)) :method "get"}
     [:button.mdl-button.mdl-js-button.mdl.button--fab.mdl-button--colored {:type "submit"} (h/h "Delete Bin")
      [:i.material-icons (h/h "delete")]]]
   (if (= (count requests) 0)
     (code-example-card host id)
     (for [request requests]
       (request-card request)))]])

(defn wrap-layout [component]
  (page/html5
    (head)
    component
    (footer)))

(defn index-page []
  (wrap-layout (index-html )))

(defn inspect-bin-page [bin-id host requests]
  (wrap-layout (inspect-html bin-id host requests)))

(defn not-found-page []
  (wrap-layout (not-found-html)))
