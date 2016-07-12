(ns httpeek.content-type-presenter-spec
  (:require [speclj.core :refer :all]
            [httpeek.content-type-presenter :refer :all]))

(describe "httpeek.content-type-presenter"
  (context "presenting the body of a supported content-type"
    (it "correctly presents JSON"
      (with-redefs [display-content-map {"application/json" (constantly "ok")}]
        (should= "ok" (present-content-type "application/json" "body"))))

    (it "correctly presents XML"
      (with-redefs [display-content-map {"application/xml" (constantly "ok")}]
        (should= "ok" (present-content-type "application/xml" "body"))))

    (it "correctly presents x-www-form-url-encoded"
      (with-redefs [display-content-map {"application/x-www-form-urlencoded" (constantly "ok")}]
        (should= "ok" (present-content-type "application/x-www-form-urlencoded" "body"))))

    (it "presents the raw if it doesn't recognize the content-type"
        (should= "body" (present-content-type "foo/bar" "body")))))
