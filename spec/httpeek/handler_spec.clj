(ns httpeek.handler-spec
  (:require [speclj.core :refer :all]
            [httpeek.handler :refer :all]
            [ring.mock.request :as mock]
            [httpeek.spec-helper :as helper]
            [cheshire.core :as json]
            [httpeek.core :as core]))

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
    (context "a bin is created successfully"
      (it "redirects to the bin's inspect page"
        (let [response (app* (mock/request :post "/bins"))
              bin (->> (core/all-bins) (sort-by :created-at) last)]
          (should= 302 (:status response))
          (should= (format "/bin/%s?inspect" (:id bin)) (get-in response [:headers "Location"]))))))

  (context  "ANY /bin/:id?inspect"
    (context "get request with existing bin and no requests"
      (it "returns a 200 status"
        (app* {:cookies {}
               :remote-addr "localhost"
               :params {},
               :route-params {}
               :headers {"host" "localhost"}
               :server-port 80
               :form-params {}
               :compojure/route [:post "/bins"]
               :session/key nil
               :query-params {}
               :uri "/bins"
               :server-name "localhost"
               :query-string nil
               :scheme :http
               :request-method :post,
               :session {}})
        (let  [bin (->> (core/all-bins) (sort-by :created-at) last)
               bin-id (:id bin)
               response (app* (mock/request :get (str "/bin/" bin-id) "inspect"))]
          (should= 200 (:status response))
          (should-contain "curl -X" (:body response)))))

    (context "a private bin cannot be accessed by an out-of-session user"
      (it "returns a 404 status"
        (app* {:cookies {}
               :remote-addr "localhost"
               :params {},
               :route-params {}
               :headers {"host" "localhost"}
               :server-port 80
               :form-params {"private-bin-checkbox" true}
               :compojure/route [:post "/bins"]
               :session/key nil
               :query-params {}
               :uri "/bins"
               :server-name "localhost"
               :query-string nil
               :scheme :http
               :request-method :post
               :session {}})
        (let  [bin (->> (core/all-bins) (sort-by :created-at) last)
               bin-id (:id bin)
               response (app* (mock/request :get (str "/bin/" bin-id) "inspect"))]
          (should= 403 (:status response)))))

    (context "a private bin cannot be accessed by an out-of-session user"
      (it "returns a 404 status"
        (let [create-bin-response (app* {:cookies {}
                                         :remote-addr "localhost"
                                         :params {},
                                         :route-params {}
                                         :headers {"host" "localhost"}
                                         :server-port 80
                                         :form-params {"private-bin-checkbox" "true"}
                                         :compojure/route [:post "/bins"]
                                         :session/key nil
                                         :query-params {}
                                         :uri "/bins"
                                         :server-name "localhost"
                                         :query-string nil
                                         :scheme :http
                                         :request-method :post
                                         :session {}})
                                  bin (->> (core/all-bins) (sort-by :created-at) last)
                                  bin-id (:id bin)
                                  response (app* {:cookies {"ring-session" {:value "some-key"}}
                                                  :remote-addr "localhost"
                                                  :params {},
                                                  :route-params {}
                                                  :headers {"host" "localhost"}
                                                  :server-port 80
                                                  :compojure/route [:get "/bin/:id"]
                                                  :session/key nil
                                                  :query-params {}
                                                  :uri (str "/bin/" bin-id)
                                                  :server-name "localhost"
                                                  :query-string "inspect"
                                                  :scheme :http
                                                  :request-method :get
                                                  :session {:private-bins [bin-id]}})]
          (should= 403 (:status response)))))


    (context "GET request with existing bin and request added"
      (it "returns a 200 status"
        (let  [bin-id (core/create-bin false)
               valid-request (app* (mock/header (mock/request :get (str "/bin/" bin-id)) :foo "bar"))
               response (app* (mock/request :get (str "/bin/" bin-id) "inspect"))]
          (should= 200 (:status response))
          (should-contain "foo: bar" (:body response)))))

    (context "GET request with non-existent bin"
      (it "returns a 404 status"
        (let  [response (app* (mock/request :get "/bin/not-an-id" "inspect"))]
          (should= 404 (:status response)))))

(context "Other requests with non-existent bin"
  (it "returns a 404 status"
    (let  [response (app* (mock/request :patch (str "/bin/not-an-id") "inspect"))]
      (should= 404 (:status response))))))

(context "ANY /bin/:id"
  (context "GET request with existing bin"
    (it "returns a status of 200"
      (let [bin-id (core/create-bin false)
            response (app* (mock/request :get (str "/bin/" bin-id)))]
        (should= 200 (:status response)))))

  (context "Other request with existing bin"
    (it "returns a status of 200"
      (let [bin-id (core/create-bin false)
            response (app* (mock/request :post (str "/bin/" bin-id)))]
        (should= 200 (:status response)))))

  (context "GET request with non-existent bin"
    (it "returns a 404 status"
      (let  [response (app* (mock/request :get "/bin/not-an-id"))]
        (should= 404 (:status response)))))

  (context "Other requests with existing bin"
    (it "returns a 404 status"
      (let  [response (app* (mock/request :put (str "/bin/not-an-id")))]
        (should= 404 (:status response)))))))

