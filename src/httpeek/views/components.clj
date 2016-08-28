(ns httpeek.views.components
  (require [hiccup.page :as page]
           [hiccup.def :refer :all]
           [hiccup.core :as h]))

(def head
  [:head (page/include-css "https://fonts.googleapis.com/icon?family=Material+Icons"
                           "https://code.getmdl.io/1.1.3/material.light_blue-indigo.min.css"
                           "/css/main.css")
   (page/include-js "https://code.getmdl.io/1.1.3/material.min.js"
                    "/js/toggle_formats.js"
                    "/js/toggle_header_fields.js")])

(def navbar
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

(def footer
  [:footer.mdl-mini-footer
   [:div.mdl-mini-footer__left-section
    [:div.mdl-logo (h/h "HTTPeek")]]])

(defn formfield  [{:keys [input-name id html-text textarea]}]
  [:div.mdl-textfield.mdl-js-textfield
   (if textarea
     [:textarea.mdl-textfield__input {:type "text" :name input-name :id id}]
     [:input.mdl-textfield__input {:type "text" :name input-name :id id}])
   [:label.mdl-textfield__label {:for id} (h/h html-text)]])

(defn button [{:keys [btn-type id btn-class html-text style icon]}]
  [:button.mdl-button.mdl-js-button.mdl-button--primary {:type btn-type :id id :class btn-class :style style} (h/h html-text)
   (if icon
     [:i.material-icons (h/h icon)])])

(defn nested-grid [{:keys [grid-id grid-class]} & content]
  [:div.mdl-grid.mdl-grid--nesting {:id grid-id :class grid-class} content])

(defn cell [{:keys [id col cell-class]} & content]
  [:div.mdl-cell {:id id :class (format "mdl-cell--%s-col %s" col cell-class)}
   content])

(defn form [{:keys [id action method]} content]
  [:form {:id id :action action :method method}
   content])

(defn grid [& content]
  [:div.mdl-grid
   content])

(defn card [{:keys [card-class title supporting-text]} & content]
 [:div.mdl-card.mdl-shadow--2dp {:class card-class}
   [:div.mdl-card__title
    [:h2.mdl-card__title-text (h/h title)]]
    (if supporting-text
      [:div.mdl-card__supporting-text (h/h supporting-text)])
   content])

(defn card-actions [& content]
  [:div.mdl-card__actions.mdl-card--border
   content])
