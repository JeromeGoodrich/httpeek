(ns httpeek.json-formatter
  (:require [cheshire.core :as json]
            [httpeek.core :as core]))

(defn- beautify-json [json-string]
  (-> json-string
    json/decode
    (json/encode {:pretty true})))

(defn format-json [json-string]
  (core/with-error-handling {:body json-string :warning "Malformed JSON in the request body"}
                            {:body (beautify-json json-string)}))
