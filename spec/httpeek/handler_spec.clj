(ns httpeek.handler-spec
  (:require [speclj.core :refer :all]
            [httpeek.handler :refer :all]
            [ring.mock.request :as mock]
            [clojure.java.io :as io]))

(describe "GET /"
  (it "should return a status of 200"
    (let [response (app* (mock/request :get "/"))]
      (should= 200
             (:status response))))

  (it "should be an html page"
    (let [response (app* (mock/request :get "/"))]
      (should= "text/html"
             (get-in response [:headers "Content-Type"]))))

  (it "should load index.html as the body"
    (let [response (app* (mock/request :get "/"))]
      (should= "index.html"
             (.toString(.getFileName(.toPath(:body response))))))))

(describe "Not Found"
  (it "returns a status of 404"
    (let [response (app* (mock/request :get "/foo"))]
      (should= 404
             (:status response)))))

(describe "POST /bins"
  (context "the bin is created sucessfully"
    (it "should return a status of 200"
      (let [response (app* (mock/request :post "/bins"))]
        (should= 201
                 (:status response))))

    (it "should create a new bin"
      (let [response (app* (mock/request :post "/bins"))]
        (println @bins)
        (should-contain "/bin/1"
                        (keys @bins))))

    (it "should redirect to the bin's inspect-page"
      (let [response (app* (mock/request :get "/bin/1" "inspect"))]
        (should= 302
                 (:status response))))))
