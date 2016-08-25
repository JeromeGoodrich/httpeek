(ns httpeek.views.inspect
  (require [camel-snake-kebab.core :as csk]
           [httpeek.views.components :refer :all]
           [httpeek.content-type-presenter :as presenter]
           [hiccup.core :as h]))

(defn- code-example-card [host id]
  (card {:card-class "index-card"
         :title "make a request to get started"}
        (card-actions
          [:code (h/h (format "curl -X POST -d foo=bar %s/bin/%s" host id))])))

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
         [(button {:btn-type "button" :btn-class "toggle-to-formatted" :style "display:none;" :html-text "Formatted Body"})
          (button {:btn-type "button" :btn-class "toggle-to-raw" :style "display:block;" :html-text "Raw Body"})])
     [:div.formatted-body {:style "display:block;"}
      [:pre ~(h/h (:body (presenter/present-content-type content-type body)))]
      [:p ~(h/h (:warning (presenter/present-content-type content-type body)))]]
     [:div.raw-body {:style "display:none"}
      [:p ~(h/h body)]]]])


(defn- formatted-request [{:keys [query-string headers body]}]
  [:div.formatted-content {:style "display:block;"}
   (card-actions
     (grid (cell {:col "6"} [:h4 (h/h (str "Query Params"))]))
     (grid (cell {:col "6"} [:p (h/h query-string)]))
     (grid (cell {:col "6"} [:h4 (h/h (str "Headers"))])
           (cell {:col "6"} [:h4 (h/h (str "Request Body"))]))
     (grid (cell {:col "6"} (list-headers headers))
           (if-let [content-type (:content-type headers)]
             (request-body body content-type))))])

(defn- raw-request [full_request]
  [:div.raw-content {:style "display:none;"}
    (card-actions
    (grid (cell {:col "8"} [:h4 (h/h (str "Raw Request"))]))
    (grid (cell {:col "8"} (display-raw-request full_request))))])

(defn- request-card [request]
  (let [{:keys [created_at full_request]} request
        {{:keys [request-method]} :full_request} request]
    (card {:card-class "request-card"
           :title (clojure.string/upper-case request-method)
           :supporting-text (str created_at)}
     [:div.request-content
      (button {:btn-type "button" :style "display:none;" :btn-class "toggle-to-formatted" :html-text "Formatted Request"})
      (button {:btn-type "button" :style "display:block;" :btn-class "toggle-to-raw" :html-text "Raw Request"})
      (formatted-request full_request)
      (raw-request full_request)
     ])))

(defn inspect-html [id host requests]
  [:body
   (navbar)
   [:main.mdl-layout__content
    [:h4#bin-title (h/h (format "http://%s/bin/%s" host id))]
    (form {:action (h/h (format "/bin/%s/delete" id)) :method "POST" :id "bin-title"}
          [(button {:type "submit" :html-text "Delete Bin" :icon "delete"})])
   (if (= (count requests) 0)
     (code-example-card host id)
     (for [request requests]
       (request-card request)))]])


