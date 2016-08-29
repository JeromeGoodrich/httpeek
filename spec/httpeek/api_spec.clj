(ns httpeek.api-spec
  (:require [speclj.core :refer :all]
            [httpeek.api :refer :all]
            [httpeek.router :refer :all]
            [ring.mock.request :as mock]
            [ring.util.io :as r-io]
            [httpeek.spec-helper :as helper]
            [cheshire.core :as json]
            [httpeek.core :as core]))

(describe "httpeek.api"
  (after (helper/reset-db))

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
              request (core/add-request bin-id (mock/request :get (format "/bin/%s" bin-id)))
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
          (let  [response (api-create-bin-response (json/encode {:status 200
                                                                 :headers {}
                                                                 :body [1 2 3 4 5]}))
                bin-id (->> (core/get-bins {:limit 50}) (sort-by :created_at) last :id)
                body (json/decode (:body response))]
            (should= 200 (:status response))
            (should= "application/json; charset=utf-8" (get-in response [:headers "Content-Type"]))
            (should= (format "http://localhost/bin/%s" bin-id) (get body "bin-url"))))

        (it "Created a bin with the correct response attributes"
          (api-create-bin-response (json/encode {:status 200
                                                 :headers {"foo" "bar"}
                                                 :body [1 2 3 4 5]}))
          (let [bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should= 200 (:status (:response bin)))
            (should= {:foo "bar"} (:headers (:response bin)))
            (should= [1 2 3 4 5] (:body (:response bin))))))

      (context "When a bin creation attempt is unsuccessful"
        (it "doesn't create a bin if the body is not a vector of integers"
          (api-create-bin-response (json/encode {:status 303 :headers {} :body "not-an-array-of-ints"}))
          (let [bin (->> (core/get-bins {:limit 50}) (sort-by :created-at) last)]
            (should-be-nil bin)))

        (it "returns an error message if the body is not a vector of integers"
          (let [response (api-create-bin-response (json/encode {:status 303 :headers {} :body "not-an-array-of-ints"}))]
            (should= "Bad Request" (:body response))))

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
