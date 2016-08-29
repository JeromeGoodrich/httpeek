(ns httpeek.core-spec
  (:require [speclj.core :refer :all]
            [httpeek.core :refer :all]
            [cheshire.core :as json]
            [httpeek.spec-helper :as helper]))

(describe "httpeek.core"
  (after (helper/reset-db))

  (context "When converting a UUID"
    (it "converts a valid string to a UUID"
      (let [bin-id (create-bin {:private false :response helper/bin-response})
            string (str bin-id)]
        (should= bin-id (str->uuid string))))

    (it "it returns nil for a non-uuid string"
      (let [string "not-a-valid-uuid"]
        (should= nil (str->uuid string)))))


  (context "When validating user-inputted expiration time"
    (context "And the expiration time is less than 1 hour"
      (it "returns a map of errors"
       (let [less-than-time-range-error (validate-expiration {:time-to-expiration 0})]
         (should= #{"expiration time must be an integer between 1 and 24"} less-than-time-range-error))))

    (context "And the expiration time is greater than 24 hours"
      (it "returns a map of errors"
       (let [greater-than-time-range-error (validate-expiration {:time-to-expiration 50})]
         (should= #{"expiration time must be an integer between 1 and 24"} greater-than-time-range-error))))

    (context "And the expiration time is not an integer"
      (it "returns a map of errors"
       (let [not-an-integer-error (validate-expiration {:time-to-expiration 4.5})]
         (should= #{"expiration time must be an integer between 1 and 24"}  not-an-integer-error))))

    (context "And the expiration time is a string"
      (it "returns a map of errors"
       (let [string-error (validate-expiration {:time-to-expiration "foo"})]
         (should= #{"expiration time must be an integer between 1 and 24"} string-error)))))

  (context "When validating a user-inputted response"
    (context "And the response-map is well-formed"
      (it "returns nil"
        (should-be-nil (validate-response default-response))))

    (context "And the status is nil"
      (it "returns a map of errors"
        (let [status-nil-error (validate-response {:status nil :headers {} :body ""})]
          (should= #{"status must be a 3 digit number"} status-nil-error))))

    (context "And the status is blank"
      (it "returns a map of errors"
        (let [status-blank-error (validate-response {:status "" :headers {} :body ""})]
          (should= #{"status must be a 3 digit number"} status-blank-error))))

    (context "And the status is a string"
      (it "returns a map of errors"
        (let [status-string-error (validate-response {:status "not a number" :headers {} :body ""})]
          (should= #{"status must be a 3 digit number"} status-string-error))))

    (context "And the status is less than 3 digits"
      (it "returns a map of errors"
        (let [status-less-than-three-digits-error (validate-response {:status 4 :headers {} :body ""})]
          (should= #{"status must be a 3 digit number"} status-less-than-three-digits-error))))

    (context "And the status is more than 3 digits"
      (it "returns a map of errors"
        (let [status-more-than-three-digits-error (validate-response {:status 600000 :headers {} :body ""})]
          (should= #{"status must be a 3 digit number"} status-more-than-three-digits-error))))

    (context "And the status is not an integer"
      (it "returns a map of errors"
        (let [status-not-integer-error (validate-response {:status 400.5 :headers {} :body ""})]
          (should= #{"status must be a 3 digit number"} status-not-integer-error)))))

  (context "When creating a bin"
    (context "And no options are specified"
      (it "increases the bin count by 1"
        (let [bin-count (count (get-bins {:limit 50}))]
          (create-bin {})
          (should= (+ 1 bin-count) (count (get-bins {:limit 50})))))

      (it "has the expected default values"
        (let [bin (find-bin-by-id (create-bin {}))]
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

    (it "returns an empty vector for an invalid limit"
      (should= [] (get-bins {:limit "not-a-limit"}))))

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
          (add-request bin-id {:foo "bar"})
          (should= (+ 1 request-count)
                 (count (all-requests)))))

    (it "returns a request-id"
      (let [bin-id (create-bin {:private false :response helper/bin-response})
            request-id (add-request bin-id {:foo "bar"})]
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
        (add-request invalid-bin-id {:foo "bar"})
        (should= request-count
                 (count (all-requests)))))

    (it "returns nil"
      (let [invalid-bin-id "not a valid id"
            request-id (add-request invalid-bin-id {:foo "bar"})]
        (should-be-nil request-id)))))


  (context "When getting the requests of a bin"
    (it "returns all the requests related to a bin"
      (let [bin-id (create-bin {:private false :response helper/bin-response})]
        (add-request bin-id {:foo "bar"})
        (add-request bin-id {:fizz "buzz"})
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
            first-request-id (add-request bin-id {:position "first"})
            second-request-id (add-request bin-id {:position "second"})]
        (delete-bin "some-other-bin-id")
        (should-not (empty? (get-requests bin-id)))))

  (context "deleting a non-existent bin"
    (it "returns nil"
        (should-be-nil (delete-bin -1))))))
