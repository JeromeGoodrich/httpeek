(ns httpeek.form-urlencoded-validator-spec
  (:require [speclj.core :refer :all]
            [httpeek.form-urlencoded-validator :refer :all]))

(describe "httpeek.form-urlencoded-validator"
  (context "When validating a well-formed form-urlencoded string"
    (it "return a map with no warning"
      (let [urlencoded-string "foo=bar&baz[]=hello&baz[]=world&buzz="
            validated-string (validate-form-urlencoded urlencoded-string)]
        (should= urlencoded-string (:body validated-string))
        (should-be-nil (:warning validated-string)))))

  (context "When validating a malformed string"
    (it "returns a map with a warning"
      (let [some-string "{/this/ string && is malformed!}}?{:w"
            validated-string (validate-form-urlencoded some-string)]
        (should= some-string (:body validated-string))
        (should= "Malformed form data in the request body"(:warning validated-string))))))

