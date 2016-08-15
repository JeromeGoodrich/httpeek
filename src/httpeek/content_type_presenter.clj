(ns httpeek.content-type-presenter
  (:require [httpeek.json-formatter :as json]
            [httpeek.form-urlencoded-validator :as url]
            [httpeek.xml-formatter :as xml]))

(def display-content-map
  {"application/json" json/format-json
   "text/xml" xml/format-xml
   "application/xml" xml/format-xml
   "application/x-www-form-urlencoded" url/validate-form-urlencoded})

(defn present-content-type [content-type body]
  (if-let [formatting-function (get display-content-map content-type)]
    (formatting-function body)
    body))
