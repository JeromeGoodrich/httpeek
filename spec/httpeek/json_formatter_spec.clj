(ns httpeek.json-formatter-spec
  (:require [speclj.core :refer :all]
            [httpeek.json-formatter :refer :all]))

(describe "httpeek.json-formatter"
  (context "formatting a json string"
    (it "correctly formats a proper json string"
      (let  [formatted-json (format-json "{\"foo\":\"bar\"}")]
        (should= {:body "{\n  \"foo\" : \"bar\"\n}"}
                 formatted-json)))

    (it "returns a error message if the json string is malformed"
      (let  [formatted-json (format-json "not-a-json-string")]
        (should= {:body "not-a-json-string" :warning "Malformed JSON in the request body"}
                 formatted-json)))))
