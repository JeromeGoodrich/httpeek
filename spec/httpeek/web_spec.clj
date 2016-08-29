(ns httpeek.web-spec
  (:require [speclj.core :refer :all]
            [httpeek.router :refer :all]
            [httpeek.web :refer :all]
            [ring.mock.request :as mock]
            [httpeek.spec-helper :as helper]
            [clj-time.core :as t]
            [ring.util.io :as r-io]
            [ring.util.codec :as codec]
            [cheshire.core :as json]
            [httpeek.core :as core])
  (:import java.io.ByteArrayInputStream))

(describe "httpeek.handler"
  (after (helper/reset-db))

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

    (defn- encode-form-data [{:strs [status header-name[] header-value[] body expiration]
                        :or {status ""
                             header-name[] ""
                             header-value[] ""
                             body ""
                             expiration ""} :as form-data}]
      (codec/form-encode form-data))

    (context "When a bin is successfully created"
      (context "And an expiration time is specified"
        (it "creates a bin with the expected expiration time"
          (let [response (create-bin-response (encode-form-data {"status" 200 "expiration" 7}))
                bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)
                before (t/minus (t/from-now (t/hours 7)) (t/seconds 1))
                after (t/plus (t/from-now (t/hours 7)) (t/seconds 1))]
            (should (t/within? before after (:expiration bin))))))

      (context "And only a status is specified for the response"
        (it "creates a bin with the correct response attributes"
          (let [response (create-bin-response (encode-form-data {"status" 500}))
                bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should= 500 (:status (:response bin)))
            (should= {} (:headers (:response bin)))
            (should= nil (:body (:response bin))))))

      (context "And a status and one header are specified for the response"
        (it "creates a bin with the correct response attributes"
          (let [response (create-bin-response (encode-form-data {"status" 500
                                                                 "header-name[]" "foo"
                                                                 "header-value[]" "bar"}))
                bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should= 500 (:status (:response bin)))
            (should= {:foo "bar"} (:headers (:response bin)))
            (should= nil (:body (:response bin))))))

      (context "And a status and headers are specified for the response"
        (it "creates a bin with the correct response attributes"
          (let [response (create-bin-response (encode-form-data {"status" 500
                                                                 "header-name[]" ["foo" "baz"]
                                                                 "header-value[]" ["bar" "buzz"]}))
                bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should= 500 (:status (:response bin)))
            (should= {:foo "bar" :baz "buzz"} (:headers (:response bin)))
            (should= nil (:body (:response bin))))))

      (context "And status, headers and body are specified for the response"
        (it "creates a bin with the correct response attributes"
          (let [response (create-bin-response (encode-form-data {"status" 500
                                                                 "header-name[]" ["foo" "baz"]
                                                                 "header-value[]" ["bar" "buzz"]
                                                                 "body" "some text"}))
                bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should= 500 (:status (:response bin)))
            (should= {:foo "bar" :baz "buzz"} (:headers (:response bin)))
            (should= "some text" (:body (:response bin))))))

      (context "With valid response attributes"
        (it "redirects to the bin's inspect page"
          (let [response (create-bin-response (encode-form-data {"status" 500}))
                bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should= 302 (:status response))
            (should= (format "/bin/%s/inspect" (:id bin)) (get-in response [:headers "Location"]))))))

    (context "When a bin creation attempt is unsuccessful"
      (context "And an invalid expiration time is entered"
        (it "doesn't create the bin"
          (let [response (create-bin-response (encode-form-data {"status" 500
                                                                 "header-name[]" ["foo" "baz"]
                                                                 "header-value[]" ["bar" "buzz"]
                                                                 "body" "some text"
                                                                 "expiration" 100}))
                bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should-be-nil bin)))

        (it "redirects to the form's page with an error"
          (let [response (create-bin-response (encode-form-data {"status" 500
                                                                 "header-name[]" ["foo" "baz"]
                                                                 "header-value[]" ["bar" "buzz"]
                                                                 "body" "some text"
                                                                 "expiration" 100}))]
            (should= 302 (:status response))
            (should-contain "expiration time must be an integer between 1 and 24" (:flash response)))))

      (context "and no status is specified in the response"
        (it "doesn't create the bin"
          (let [response (create-bin-response (encode-form-data {"header-name[]" ["foo"]
                                                                 "header-value[]" ["bar" "buzz"]
                                                                 "body" "some text"
                                                                 "expiration" 7}))

                bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should-be-nil bin)))

        (it "redirects to the form's page with an error message"
          (let [response (create-bin-response (encode-form-data {"header-name[]" ["foo" "baz"]
                                                                 "header-value[]" ["bar" "buzz"]
                                                                 "body" "some text"
                                                                 "expiration" 7}))
                bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should= 302 (:status response))
            (should-contain "status must be a 3 digit number" (:flash response)))))

      (context "and headers are not filled out properly"
        (it "doesn't create the bin"
          (let [response (create-bin-response (encode-form-data {"status" 200
                                                                 "header-name[]" ["foo" "baz"]
                                                                 "header-value[]" ["bar"]
                                                                 "body" "some text"
                                                                 "expiration" 7}))
                bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should-be-nil bin)))

        (it "redirects to the form's page with an error message"
          (let [response (create-bin-response (encode-form-data {"status" 200
                                                                 "header-name[]" ["foo" "baz"]
                                                                 "header-value[]" ["bar"]
                                                                 "body" "some text"
                                                                 "expiration" 7}))
                bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should= 302 (:status response))
            (should-contain "headers must have header name and value" (:flash response)))))))

  (context  "GET /bin/:id/inspect"
    (context "an existing private bin with no requests"
      (it "returns a 200 status"
        (let  [bin-id (core/create-bin {:private true :response helper/bin-response})]
          (let [response (web-routes (assoc (mock/request :get (format "/bin/%s/inspect" bin-id)) :session {:private-bins [bin-id]}))]
            (should= 200 (:status response))
            (should-contain "curl -X" (:body response))))))

    (context "an existing public bin with no requests"
      (it "returns a 200 status"
        (let  [bin-id (core/create-bin {:private false :response helper/bin-response})]
          (let [response (web-routes (mock/request :get (format "/bin/%s/inspect" bin-id)))]
            (should= 200 (:status response))
            (should-contain "curl -X" (:body response))))))

    (context "a private bin from a different session"
      (it "returns a 403 status"
        (let  [bin-id (core/create-bin {:private true :response helper/bin-response})
               response (web-routes (mock/request :get (format "/bin/%s/inspect" bin-id)))]
          (should= 403 (:status response)))))

    (context "an existing private bin with a request added"
      (it "returns a 200 status"
        (let  [bin-id (core/create-bin {:private true :response helper/bin-response})
               valid-request (web-routes (assoc (mock/request :get (format "/bin/%s" bin-id)) :protocol "HTTP/1.1"))
               response (web-routes (assoc (mock/request :get (format "/bin/%s/inspect" bin-id)) :session {:private-bins [bin-id]}))]
          (should= 200 (:status response)))))

    (context "an existing public bin with a request added"
      (it "returns a 200 status"
        (let  [bin-id (core/create-bin {:private false :response helper/bin-response})
               valid-request (web-routes (assoc (mock/request :get (format "/bin/%s" bin-id)) :protocol "HTTP/1.1"))
               response (web-routes (mock/request :get (format "/bin/%s/inspect" bin-id)))]
          (should= 200 (:status response)))))

    (context "a non-existent bin"
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
          (should= "hello world" (slurp (:body response))))))

    (context "Other request with existing bin"
      (it "returns the bin's set response"
        (let [bin-id (core/create-bin {:private false :response helper/bin-response})
              response (app* (mock/request :post (format "/bin/%s" bin-id)))]
          (should= 500 (:status response))
          (should= {"foo" "bar"} (:headers response))
          (should= "hello world" (slurp (:body response))))))

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
