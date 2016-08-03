(ns httpeek.handler-spec
  (:require [speclj.core :refer :all]
            [httpeek.handler :refer :all]
            [ring.mock.request :as mock]
            [httpeek.spec-helper :as helper]
            [ring.util.io :as r-io]
            [cheshire.core :as json]
            [httpeek.core :as core]))

(describe "httpeek.handler"
  (after (helper/reset-db))

  (context "/api"
    (context "GET /"
      (it "returns a json response of all existing bins"
        (let [bin-id (core/create-bin {:private false :response helper/bin-response})
              response (app* (mock/request :get "/api/"))
              body (json/decode (:body response))]
          (should= 200 (:status response))
          (should= "application/json; charset=utf-8" (get-in response [:headers "Content-Type"]))
          (should= (str bin-id) (get (first (get body "bins")) "id")))))

    (context "GET /bin/:id/inspect"
      (it "returns a json response for inspecting an existing bin"
        (let [bin-id (core/create-bin {:private false :response helper/bin-response})
              request (core/add-request bin-id (json/encode (mock/request :get (format "/bin/%s" bin-id))))
              response (app* (mock/request :get (format "/api/bin/%s/inspect" bin-id)))
              body (json/decode (:body response))]
          (should= 200 (:status response))
          (should= "application/json; charset=utf-8" (get-in response [:headers "Content-Type"]))
          (should= (str bin-id) (get (first (get body "requests")) "bin_id"))))

      (it "returns a json response for attempting to inspect a non-existent bin"
        (let [response (app* (mock/request :get "/api/bin/not-an-id/inspect"))]
          (should= 404 (:status response))
          (should= "application/json; charset=utf-8" (get-in response [:headers "Content-Type"])))))

    (context "DELETE /bin/:id/delete"
      (it "returns a json response for deleting an existing bin"
        (let [bin-id (core/create-bin {:private false :response helper/bin-response})
              response (app* (mock/request :delete (format "/api/bin/%s/delete" bin-id)))]
          (should= 200 (:status response))
          (should= "application/json; charset=utf-8" (get-in response [:headers "Content-Type"]))))

      (it "returns a json response for attempting to delete a non-existent bin"
        (let [response (app* (mock/request :delete "/api/bin/not-an-id/delete"))]
          (should= 404 (:status response))
          (should= "application/json; charset=utf-8" (get-in response [:headers "Content-Type"])))))

    (context "POST /bins"
      (defn- api-create-bin-response [json-request-body-string]
        (-> (mock/request :post "/api/bins")
          (assoc :body (r-io/string-input-stream json-request-body-string))
          (assoc-in [:headers "content-type"] "application/json")
          app*))

      (context "When a bin is successfully created"
        (it "returns a successful json response"
          (let  [response (api-create-bin-response helper/bin-response)
                bin-id (->> (core/get-bins {:limit 50}) (sort-by :created_at) last :id)
                body (json/decode (:body response))]
            (should= 200 (:status response))
            (should= "application/json; charset=utf-8" (get-in response [:headers "Content-Type"]))
            (should= (format "http://localhost/bin/%s" bin-id) (get body "bin-url"))))

        (it "Created a bin with the correct response attributes"
          (api-create-bin-response helper/bin-response)
          (let [bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should= 500 (:status (:response bin)))
            (should= {:foo "bar"} (:headers (:response bin)))
            (should= "hello world" (:body (:response bin))))))

      (context "When a bin creation attempt is unsuccessful"
        (it "doesn't create a bin if there is no status in the response-map"
          (api-create-bin-response (json/encode {:headers {} :body ""}))
          (let [bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should-be-nil bin)))

        (it "returns a status of 400"
          (let [response (api-create-bin-response (json/encode {:headers {} :body ""}))]
            (should= 400 (:status response))))))

    (context "not found"
      (it "returns a json response for a resource that can't be found"
        (let [response (app* (mock/request :get "/api/not-a-path"))]
          (should= 404 (:status response))
          (should= "application/json; charset=utf-8" (get-in response [:headers "Content-Type"]))))))

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
    (defn- create-bin-response [request-body-string]
      (-> (mock/request :post "/bins")
        (assoc :body (r-io/string-input-stream request-body-string))
        (assoc-in [:headers "content-type"] "application/x-www-form-urlencoded")
        app*))

    (context "When a bin is successfully created"
      (context "And only a status is specified for the response"
        (it "creates a bin with the correct response attributes"
          (let [response (create-bin-response "status=500&header-name[]=&header-value[]=&body=")
                bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should= 500 (:status (:response bin)))
            (should= {} (:headers (:response bin)))
            (should= "" (:body (:response bin))))))

      (context "And a status and one header are specified for the response"
        (it "creates a bin with the correct response attributes"
          (let [response (create-bin-response "status=500&header-name[]=foo&header-value[]=bar&body=")
                bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should= 500 (:status (:response bin)))
            (should= {:foo "bar"} (:headers (:response bin)))
            (should= "" (:body (:response bin))))))

      (context "And a status and headers are specified for the response"
        (it "creates a bin with the correct response attributes"
          (let [response (create-bin-response "status=500&header-name[]=foo&header-value[]=bar&header-name[]=baz&header-value[]=buzz&body=")
                bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should= 500 (:status (:response bin)))
            (should= {:foo "bar" :baz "buzz"} (:headers (:response bin)))
            (should= "" (:body (:response bin))))))

      (context "And status, headers and body are specified for the response"
        (it "creates a bin with the correct response attributes"
          (let [response (create-bin-response "status=500&header-name[]=foo&header-value[]=bar&header-name[]=baz&header-value[]=buzz&body=cash rules everything around me")
                bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should= 500 (:status (:response bin)))
            (should= {:foo "bar" :baz "buzz"} (:headers (:response bin)))
            (should= "cash rules everything around me" (:body (:response bin))))))

      (context "With valid response attributes"
        (it "redirects to the bin's inspect page"
          (let [response (create-bin-response "status=500&header-name[]=foo&header-value[]=bar&header-name[]=baz&header-value[]=buzz&body=cash rules everything around me")
                bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should= 302 (:status response))
            (should= (format "/bin/%s/inspect" (:id bin)) (get-in response [:headers "Location"]))))))

    (context "When a bin creation attempt is unsuccessful"
      (context "and no status is specified in the response"
        (it "doesn't create the bin"
          (let [response (create-bin-response "status=&header-name[]=foo&header-value[]=bar&header-name[]=baz&header-value[]=buzz&body=cash rules everything around me")
                bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should-be-nil bin)))

        (it "returns a 400 status"
          (let [response (create-bin-response "status=&header-name[]=foo&header-value[]=bar&header-name[]=baz&header-value[]=buzz&body=cash rules everything around me")
                bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should= 302 (:status response))
            (should-contain "status can't be blank" (:flash response)))))

      (context "and headers are not filled out properly"
        (it "doesn't create the bin"
          (let [response (create-bin-response "status=600&header-name[]=foo&header-value[]=bar&header-name[]=baz&header-value[]=&body=cash rules everything around me")
                bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should-be-nil bin)))

        (it "returns a 400 status"
          (let [response (create-bin-response "status=600&header-name[]=foo&header-value[]=bar&header-name[]=baz&header-value[]=&body=cash rules everything around me")
                bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should= 302 (:status response))
            (should-contain "headers must have header name and value" (:flash response)))))))

  (context  "GET /bin/:id/inspect"
    (context "getting an existing private bin with no requests"
      (it "returns a 200 status"
        (let  [bin-id (core/create-bin {:private true :response helper/bin-response})]
          (let [response (web-routes (assoc (mock/request :get (format "/bin/%s/inspect" bin-id)) :session {:private-bins [bin-id]}))]
            (should= 200 (:status response))
            (should-contain "curl -X" (:body response))))))

    (context "getting an existing public bin with no requests"
      (it "returns a 200 status"
        (let  [bin-id (core/create-bin {:private false :response helper/bin-response})]
          (let [response (web-routes (mock/request :get (format "/bin/%s/inspect" bin-id)))]
            (should= 200 (:status response))
            (should-contain "curl -X" (:body response))))))

    (context "attempting to access private bin from a different session"
      (it "returns a 403 status"
        (let  [bin-id (core/create-bin {:private true :response helper/bin-response})
               response (web-routes (mock/request :get (format "/bin/%s/inspect" bin-id)))]
          (should= 403 (:status response)))))

    (context "getting an existing private bin with request added"
      (it "returns a 200 status"
        (let  [bin-id (core/create-bin {:private true :response helper/bin-response})
               valid-request (web-routes (assoc (mock/request :get (format "/bin/%s" bin-id)) :protocol "HTTP/1.1"))
               response (web-routes (assoc (mock/request :get (format "/bin/%s/inspect" bin-id)) :session {:private-bins [bin-id]}))]
          (should= 200 (:status response)))))

    (context "getting an existing public bin with request added"
      (it "returns a 200 status"
        (let  [bin-id (core/create-bin {:private false :response helper/bin-response})
               valid-request (web-routes (assoc (mock/request :get (format "/bin/%s" bin-id)) :protocol "HTTP/1.1"))
               response (web-routes (mock/request :get (format "/bin/%s/inspect" bin-id)))]
          (should= 200 (:status response)))))

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
      (it "returns the bin's set response"
        (let [bin-id (core/create-bin {:private false :response helper/bin-response})
              response (app* (mock/request :get (format "/bin/%s" bin-id)))]
          (should= 500 (:status response))
          (should= {"foo" "bar"} (:headers response))
          (should= "hello world" (:body response)))))

    (context "Other request with existing bin"
      (it "returns the bin's set response"
        (let [bin-id (core/create-bin {:private false :response helper/bin-response})
              response (app* (mock/request :post (format "/bin/%s" bin-id)))]
          (should= 500 (:status response))
          (should= {"foo" "bar"} (:headers response))
          (should= "hello world" (:body response)))))

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
      (let [bin-id (core/create-bin {:private false :response helper/bin-response})
            response (app* (mock/request :post (str "/bin/" bin-id "/delete")))]
        (should= 302 (:status response)))))

   (context "DELETE request to non-existent bin"
     (it "returns a 404 status"
      (let [response (app* (mock/request :post (str "/bin/" -1 "/delete")))]
        (should= 404 (:status response)))))))
