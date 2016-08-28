(ns httpeek.views.index
  (require [httpeek.core :as core]
           [hiccup.core :as h]
           [httpeek.views.components :refer :all]))

(def bin-response-headers-input
  (nested-grid {:grid-class "bin-response-headers"}
                            (cell {:id "header-name-input" :col "4"}
                                  (formfield {:input-name "header-name[]" :id "bin-response-header-name" :html-text "Header Name"}))
                            (cell {:id "header-value-input" :col "4"}
                                  (formfield {:input-name "header-value[]" :id "bin-response-header-value" :html-text "Header Value"}))
                            (cell {:col "2" :cell-class "header-buttons"}
                                  (button {:btn-type "button" :id "add-headers-button" :html-text "Add Headers"}))
                            (cell {:col "2" :cell-class "header-buttons"}
                                  (button {:btn-type "button" :id "remove-headers-button" :html-text "Remove Headers"}))))


(def create-bin-form-content
  (list (formfield {:input-name "status" :id "bin-response-status" :html-text "Status"})
  [:div {:class "header-form"} bin-response-headers-input]
  (nested-grid {} (cell {:col "8"} (formfield {:input-name "body" :id "bin-response-body" :html-text "Body" :textarea true})))
  (nested-grid {} (cell {:col "4"} (formfield {:input-name "expiration" :id "expiration" :html-text "Hours Until Expiration"})))
  (nested-grid {} (cell {:col "6"} (button {:btn-type "submit" :html-text "Create Bin" :icon "add"})
                        [:input {:type "checkbox" :value "true" :name "private-bin"} (h/h "make this bin private")]))))

(def create-bin-form
  (grid (cell {:col "12"}
              (form {:id "create-bin-form" :action "/bins" :method "post"}
                    create-bin-form-content))))

(defn- index-card [flash]
  (card {:card-class "index-card"
         :title "HTTPeek: take a peek at HTTP requests"
         :supporting-text "HTTPeek is an application created to help users capture and read HTTP requests in a user friendly way.
                          To get started, create a 'bin' below. This bin will provide you with a unique URL to make requests to. You can also
                          specify what type of HTTP response you want requests to that bin to return. Bins are ephemeral an only last a maximum
                          of 24 hours."}
    (card-actions
      (for [message flash] [:p message])
    create-bin-form)))

(def list-bin-history
  [:ul
   (let [list-of-ids (map :id (core/get-bins {:limit 50}))]
     (for [id list-of-ids]
       [:li
        [:a.mdl-button.mdl-button--colored  {:href (h/h (format "/bin/%s/inspect" id))} (h/h (str "Bin: " id))]]))])

(def history-card
  (card {:card-class "index-card"
         :title "Bin History"}
        (card-actions list-bin-history)))

(defn index-html [flash]
  [:body
   navbar
   [:main.mdl-layout__content
    (index-card flash)
    history-card]])
