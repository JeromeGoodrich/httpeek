(ns httpeek.core-spec
  (:require [speclj.core :refer :all]
            [httpeek.core :refer :all]
            [cheshire.core :as json]
            [httpeek.spec-helper :as helper]))

(describe "httpeek.core"
  (after (helper/reset-db))

  (context "When validating user-inputted expiration time"
    (context "And the expiration time is less than 1 or greater than 24 hours"
      (it "returns a map of errors"
       (let [less-than-time-range-error (validate-expiration {:time-to-expiration 0})
             greater-than-time-range-error (validate-expiration {:time-to-expiration 50})]
         (should= #{"expiration time must be an integer between 1 and 24"} less-than-time-range-error)
         (should= #{"expiration time must be an integer between 1 and 24"} greater-than-time-range-error)))))

  (context "When validating a user-inputted response"
    (context "And the response-map is well-formed"
      (it "returns nil"
        (should-be-nil (validate-response (json/decode default-response true)))))

    (context "And the response-map is malformed"
      (it "returns a map of errors when no status is nil or empty"
        (let [status-nil-errors (validate-response {:status nil :headers {} :body ""})
              status-empty-errors (validate-response {:status "" :headers {} :body ""})]
          (should= #{"status can't be blank"} status-nil-errors)
          (should= #{"status can't be blank"} status-empty-errors)))

      (it "returns a map of errors when headers are nil"
        (let [headers-nil-errors (validate-response {:status 200 :headers nil :body "ok"})]
          (should= #{"headers must have header name and value"} headers-nil-errors)))))

  (context "When creating a bin"
    (context "And no options are specified"
      (it "increases the bin count by 1"
        (let [bin-count (count (get-bins {:limit 50}))]
          (create-bin {})
          (should= (+ 1 bin-count) (count (get-bins {:limit 50})))))

      (it "has the expected default values"
        (let [bin (find-bin-by-id (create-bin {}))
              default-response (json/decode default-response true)]
          (should= false (:private bin))
          (should= (:status default-response) (:status (:response bin)))
          (should= (:headers default-response) (:headers (:response bin)))
          (should= (:body default-response) (:body (:response bin))))))

    (context "And options are specified"
      (it "increases the bin count by 1"
        (let [bin-count (count (get-bins {:limit 50}))]
          (create-bin {:private false :response helper/bin-response})
          (should= (+ 1 bin-count)
                   (count (get-bins {:limit 50})))))

      (it "it does not create the bin if the arguments are wrong"
        (let [bin-count (count (get-bins {:limit 50}))
              malformed-bin (create-bin {:private nil :response helper/bin-response})
              another-malformed-bin (create-bin {:private false :response "not a json string"})]
          (should= bin-count (count (get-bins {:limit 50})))
          (should-be-nil malformed-bin)
          (should-be-nil another-malformed-bin)))))


  (context "When retreiving multiple bins"
    (it "retrieves a limited amount of bins"
      (let [bins (repeatedly 75 #(create-bin {:private false
                                              :response helper/bin-response}))]
        (should= (- (count bins) 25)
                 (count (get-bins {:limit 50})))))

    (it "returns 0 for an invalid limit"
      (should= 0 (get-bins {:limit "not-a-limit"}))))

  (context "When retrieving a single bin"
    (context "And the bin exists"
      (it "returns the bin-id"
        (let [bin-id (create-bin {:private false :response helper/bin-response})]
          (should= bin-id (:id (find-bin-by-id bin-id))))))

    (context "And the bin doesn not exist"
      (it "returns nil"
        (let [bin-id "not a valid id"]
          (should-be-nil (find-bin-by-id bin-id))))))

  (context "When adding a request to a bin"
    (context "And the bin exists"
      (it "increases the request count by 1"
        (let [bin-id (create-bin {:private false :response helper/bin-response})
              request-count (count (all-requests))]
          (add-request bin-id (json/encode {:foo "bar"}))
          (should= (+ 1 request-count)
                 (count (all-requests)))))

    (it "returns a request-id"
      (let [bin-id (create-bin {:private false :response helper/bin-response})
            request-id (add-request bin-id (json/encode {:foo "bar"}))]
        (should-not-be-nil request-id))))

  (context "And the request is invalid"
    (it "doesn't increase the  request count"
      (let [bin-id (create-bin {:private false :response helper/bin-response})
            invalid-full-request '(1 2 3)
            request-count (count (all-requests))]
        (add-request bin-id invalid-full-request)
        (should= request-count
                 (count (all-requests)))))

    (it "returns nil"
      (let [bin-id (create-bin {:private false :response helper/bin-response})
            invalid-full-request '(1 2 3)
            request-id (add-request bin-id invalid-full-request)]
        (should-be-nil request-id))))

  (context "And the bin doesn't exist"
    (it "doesn't increase the request count"
      (let [invalid-bin-id "not a valid id"
            request-count (count (all-requests))]
        (add-request invalid-bin-id (json/encode {:foo "bar"}))
        (should= request-count
                 (count (all-requests)))))

    (it "returns nil"
      (let [invalid-bin-id "not a valid id"
            request-id (add-request invalid-bin-id (json/encode {:foo "bar"}))]
        (should-be-nil request-id)))))


  (context "When getting the requests of a bin"
    (it "returns all the requests related to a bin"
      (let [bin-id (create-bin {:private false :response helper/bin-response})]
        (add-request bin-id (json/encode {:foo "bar"}))
        (add-request bin-id (json/encode {:fizz "buzz"}))
        (should= 2 (count (get-requests bin-id)))))

    (it "returns an empty vector if the bin doesn't exist"
      (let [bin-id "not a valid id"]
        (add-request bin-id {:foo "bar"})
        (add-request bin-id {:fizz "buzz"})
        (should= [] (get-requests bin-id)))))

  (context " When deleting an existing bin"
    (it "reduces the bin count by 1"
      (let [bin-id (create-bin {:private false :response helper/bin-response})
            bin-count (count (get-bins {:limit 50}))]
        (delete-bin bin-id)
        (should= (- bin-count 1) (count (get-bins {:limit 50})))
        (should-be-nil (find-bin-by-id bin-id))))

    (it "only deletes requests associated with the deleted bin"
      (let [bin-id (create-bin {:private false :response helper/bin-response})
            first-request-id (add-request bin-id (json/encode {:position "first"}))
            second-request-id (add-request bin-id (json/encode {:position "second"}))]
        (delete-bin "some-other-bin-id")
        (should-not (empty? (get-requests bin-id)))))

  (context "deleting a non-existent bin"
    (it "returns nil"
        (should-be-nil (delete-bin -1))))))
