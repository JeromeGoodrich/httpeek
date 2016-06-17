(ns httpeek.handler-spec
  (:require [speclj.core :refer :all]
            [httpeek.handler :refer :all]
            [ring.mock.request :as mock]
            [clojure.java.io :as io]
            [httpeek.bin :as bin]))

(describe "GET /"
  (it "should return a status of 200"
    (let [response (app* (mock/request :get "/"))]
      (should= 200
             (:status response))))

  (it "should be an html page"
    (let [response (app* (mock/request :get "/"))]
      (should= "text/html; charset=utf-8"
             (get-in response [:headers "Content-Type"])))))

(describe "Not Found"
  (it "returns a status of 404"
    (let [response (app* (mock/request :get "/foo"))]
      (should= 404
             (:status response)))))

  (it "should be an html page"
    (let [response (app* (mock/request :get "/"))]
      (should= "text/html; charset=utf-8"
             (get-in response [:headers "Content-Type"]))))

(describe "POST /bins"
  (context "a bin is created successfully"
    (it "redirects to the bin's inspect page"
      (let [response (app* (mock/request :post "/bins"))]
        (should= 302
                 (:status response))))))
;What does an unsuccessful request test look like. UUID is generated randomly in the DB the user isn't inputting anything.
; how to test that url is expected UUID?"

(describe  "GET /bin/:id"
  (context "the bin exists and inspect params are provided"
    (it "returns a status of 200"
      (let [bin-id (:id (first (bin/last-inserted)))
            response (app* (mock/request :get (str "/bin/" bin-id) "inspect"))]
        (should= 200
                 (:status response)))))

  (context "the bin exists and inspect params are not given"
    (it "returns a status of 200"
      (let [bin-id (:id (first (bin/last-inserted)))
            response (app* (mock/request :get (str "/bin/" bin-id)))]
        (should= 200
                 (:status response))))))
