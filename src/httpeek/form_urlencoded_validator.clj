(ns httpeek.form-urlencoded-validator
  (:require [ring.util.codec :as codec]))

(defn validate-form-urlencoded [urlencoded-string]
  (if (= urlencoded-string (codec/form-decode urlencoded-string))
    {:body urlencoded-string :warning "Malformed form data in the request body"}
    {:body urlencoded-string}))

