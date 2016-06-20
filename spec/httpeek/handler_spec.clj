(ns httpeek.handler-spec
  (:require [speclj.core :refer :all]
            [httpeek.handler :refer :all]
            [ring.mock.request :as mock]
            [clojure.java.io :as io]
            [httpeek.spec-helper :as helper]
            [httpeek.db :as db]))

(describe "handlers"
  (after (helper/reset-db))

  (context "GET /"

    (it "should return a status of 200"
      (let [response (app* (mock/request :get "/"))]
        (should= 200
               (:status response))))

    (it "should be an html page"
      (let [response (app* (mock/request :get "/"))]
        (should= "text/html; charset=utf-8"
               (get-in response [:headers "Content-Type"])))))

  (context "Not Found"
    (it "returns a status of 404"
      (let [response (app* (mock/request :get "/foo"))]
        (should= 404
               (:status response)))))

    (it "should be an html page"
      (let [response (app* (mock/request :get "/"))]
        (should= "text/html; charset=utf-8"
               (get-in response [:headers "Content-Type"]))))

  (context "POST /bins"
    (context "a bin is created successfully"
      (it "redirects to the bin's inspect page"
        (let [response (app* (mock/request :post "/bins"))
              bin (->> (db/all-bins) (sort-by :created-at) last)]
          (should= 302 (:status response))
          (should= (format "/bin/%s?inspect" (:id bin)) (get-in response [:headers "Location"]))))))

  (context  "GET /bin/:id"
    (context "the bin exists and inspect params are provided"
      (it "returns a 200 status"
        (let  [bin-id (db/create)
               response (app* (mock/request :get (str "/bin/" bin-id) "inspect"))]
          (should= 200 (:status response)))))

    (context "the bin exists and inspect params are not given"
      (it "returns a status of 200"
        (let [bin-id (db/create)
              response (app* (mock/request :get (str "/bin/" bin-id)))]
          (should= 200 (:status response)))))))

    (context "the bin does not exist and inspect params are provided"
      (it "returns a 404 status"
        (let  [response (app* (mock/request :get "/bin/not-an-id" "inspect"))]
          (should= 404 (:status response))))

    (context "the bin does not exist and inspect params are not given"
      (it "returns a 404 status"
        (db/create)
        (let  [response (app* (mock/request :get "/bin/not-an-id"))]
          (should= 404 (:status response))))))
