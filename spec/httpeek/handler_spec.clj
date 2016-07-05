(ns httpeek.handler-spec
  (:require [speclj.core :refer :all]
            [httpeek.handler :refer :all]
            [ring.mock.request :as mock]
            [httpeek.spec-helper :as helper]
            [cheshire.core :as json]
            [httpeek.core :as core]))

(describe "httpeek.handler"
  (after (helper/reset-db))

  (context "/api"
    (context "POST /bins"
      (it "returns a json response for creating a bin"
        (let [response (app* (assoc-in (mock/request :post "/api/bins") [:headers "host"] "localhost"))
              bin-id (->> (core/all-bins) (sort-by :creaed-at) last :id)
              body  (json/decode (:body response))]
          (should= 200 (:status response))
          (should= "application/json" (get-in response [:headers "Content-Type"]))
          (should= (format "http://localhost/bin/%s" bin-id) (get body "bin-url")))))

    (context "GET /bins/:id"
      (it "returns a json response for the bin"
        (let [id (core/create-bin {:private false})
              response (app* (mock/request :get (format "/api/bins/%s" id)))
              body (json/decode (:body response))]
          (should= 200 (:status response))
          (should= "application/json" (get-in response [:headers "Content-Type"]))
          (should= (str id) (get body "id"))))))

  (context "GET /"
    (it "should return a status of 200"
      (let [response (app* (mock/request :get "/"))]
        (should= 200
                 (:status response)))))

  (context "Not Found"
    (it "returns a status of 404"
      (let [response (app* (mock/request :get "/foo"))]
        (should= 404
                 (:status response)))))

  (context "POST /bins"
    (context "a bin is created successfully"
      (it "redirects to the bin's inspect page"
        (let [response (app* (mock/request :post "/bins"))
              bin (->> (core/all-bins) (sort-by :created-at) last)]
          (should= 302 (:status response))
          (should= (format "/bin/%s/inspect" (:id bin)) (get-in response [:headers "Location"]))))))

  (context  "GET /bin/:id/inspect"
    (context "getting an existing private bin with no requests"
      (it "returns a 200 status"
        (let  [bin-id (core/create-bin {:private true})]
          (let [response (app-routes (assoc (mock/request :get (format "/bin/%s/inspect" bin-id)) :session {:private-bins [bin-id]}))]
            (should= 200 (:status response))
            (should-contain "curl -X" (:body response))))))

    (context "getting an existing public bin with no requests"
      (it "returns a 200 status"
        (let  [bin-id (core/create-bin {:private false})]
          (let [response (app-routes (mock/request :get (format "/bin/%s/inspect" bin-id)))]
            (should= 200 (:status response))
            (should-contain "curl -X" (:body response))))))

    (context "attempting to access private bin from a different session"
      (it "returns a 403 status"
        (let  [bin-id (core/create-bin {:private true})
               response (app-routes (mock/request :get (format "/bin/%s/inspect" bin-id)))]
          (should= 403 (:status response)))))

    (context "getting an existing private bin with request added"
      (it "returns a 200 status"
        (let  [bin-id (core/create-bin {:private true})
               valid-request (app-routes (mock/header (mock/request :get (format "/bin/%s" bin-id)) :foo "bar"))
               response (app-routes (assoc (mock/request :get (format "/bin/%s/inspect" bin-id)) :session {:private-bins [bin-id]}))]
          (should= 200 (:status response))
          (should-contain "foo: bar" (:body response)))))

    (context "getting an existing public bin with request added"
      (it "returns a 200 status"
        (let  [bin-id (core/create-bin {:private false})
               valid-request (app-routes (mock/header (mock/request :get (format "/bin/%s" bin-id)) :foo "bar"))
               response (app-routes (mock/request :get (format "/bin/%s/inspect" bin-id)))]
          (should= 200 (:status response))
          (should-contain "foo: bar" (:body response)))))

    (context "getting a non-existent bin"
      (it "returns a 404 status"
        (let  [response (app* (mock/request :get "/bin/not-an-id/inspect"))]
          (should= 404 (:status response)))))

    (context "submitting other requests to a non-existent bin"
      (it "returns a 404 status"
        (let  [response (app* (mock/request :patch "/bin/not-an-id/inspect"))]
          (should= 404 (:status response))))))

  (context "ANY /bin/:id"
    (context "GET request with existing bin"
      (it "returns a status of 200"
        (let [bin-id (core/create-bin {:private false})
              response (app* (mock/request :get (format "/bin/%s" bin-id)))]
          (should= 200 (:status response)))))

    (context "Other request with existing bin"
      (it "returns a status of 200"
        (let [bin-id (core/create-bin {:private false})
              response (app* (mock/request :post (format "/bin/%s" bin-id)))]
          (should= 200 (:status response)))))

    (context "GET request with non-existent bin"
      (it "returns a 404 status"
        (let  [response (app* (mock/request :get "/bin/not-an-id"))]
          (should= 404 (:status response)))))

    (context "Other requests with non-existent bin"
      (it "returns a 404 status"
        (let  [response (app* (mock/request :put (str "/bin/not-an-id")))]
          (should= 404 (:status response))))))

 (context "DELETE /bin/:id/delete"
   (context "DELETE request to existing bin"
     (it "redirects to the index page"
      (let [bin-id (core/create-bin {:private false})
            response (app* (mock/request :delete (str "/bin/" bin-id "/delete")))]
        (should= 302 (:status response)))))

   (context "DELETE request to non-existent bin"
     (it "returns a 404 status"
      (let [response (app* (mock/request :delete (str "/bin/" -1 "/delete")))]
        (should= 404 (:status response)))))))
