(ns httpeek.handler-spec
  (:require [speclj.core :refer :all]
            [httpeek.handler :refer :all]
            [ring.mock.request :as mock]
            [clojure.java.io :as io]))

(describe "GET /"
  (it "should return a status of 200"
    (let [response (app* (mock/request :get "/"))]
      (println response)
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
