(ns httpeek.views.index
  (require [httpeek.core :as core]
           [hiccup.core :as h]
           [httpeek.views.components :refer :all]))

(defn- bin-response-headers-input []
  (nested-grid {:grid-id "bin-response-headers"}
               (cell {:id "header-name-input" :col "4"}
                     (formfield {:input-name "header-name[]" :id "bin-response-header-name" :html-text "Header Name"}))
               (cell {:id "header-value-input" :col "4"}
                     (formfield {:input-name "header-value[]" :id "bin-response-header-value" :html-text "Header Value"}))
               (cell {:col "4"} (button {:btn-type "button" :id "add-headers-button" :html-text "Add Headers"}))))

(def create-bin-form-content
  [(formfield {:input-name "status" :id "bin-response-status" :html-text "Status"})
  (bin-response-headers-input)
  (nested-grid {} (cell {:col "8"} (formfield {:input-name "body" :id "bin-response-body" :html-text "Body" :textarea true})))
  (nested-grid {} (cell {:col "4"} (formfield {:input-name "expiration" :id "expiration" :html-text "Hours Until Expiration"})))
  (nested-grid {} (cell {:col "6"} (button {:btn-type "submit" :html-text "Create Bin" :icon "add"})
                        [:input {:type "checkbox" :value "true" :name "private-bin"} (h/h "make this bin private")]))])

(def create-bin-form
  (grid (cell {:col "12"}
              (form {:id "create-bin-form" :action "/bins" :method "post"}
                    create-bin-form-content))))

(defn- index-card [flash]
  (card {:card-class "index-card"
         :title "HTTPeek: take a peek at HTTP requests"
         :supporting-text "HTTPeek gives you a unique URL that will collect all requests made to it,
                          and allow you to inspect those requests in a human friendly way."}
    (for [message flash]
      [:p message])
    create-bin-form))

(defn- list-bin-history []
  [:ul
   (let [list-of-ids (map :id (core/get-bins {:limit 50}))]
     (for [id list-of-ids]
       [:li
        [:a.mdl-button.mdl-button--colored  {:href (h/h (format "/bin/%s/inspect" id))} (h/h (str "Bin: " id))]]))])

(defn- history-card []
  (card {:card-class "index-card"
         :title "Bin History"}
        (card-actions (list-bin-history))))

(defn index-html [flash]
  [:body
   (navbar)
   [:main.mdl-layout__content
    (index-card flash)
    (history-card)]])
