(ns httpeek.core-spec
  (:require [speclj.core :refer :all]
            [httpeek.core :refer :all]
            [cheshire.core :as json]
            [httpeek.spec-helper :as helper]))

(describe "httpeek.core"
  (after (helper/reset-db))

  (context "creating a bin"
    (it "increases the bin count by 1"
      (let [bin-count (count (all-bins))]
      (create-bin)
      (should= (+ 1 bin-count)
               (count (all-bins))))))

  (context "finding a bin"
    (it "finds an existing bin"
      (let [bin-id (create-bin)]
        (should= bin-id (:id (find-bin-by-id bin-id)))))

    (it "returns nil if the bin doesn't exist"
      (let [bin-id "not a valid id"]
        (should-be-nil (find-bin-by-id bin-id)))))

  (context "adding a request to an exisiting bin"
    (it "increases the request count by 1"
      (let [bin-id (create-bin)
            request-count (count (all-requests))]
        (add-request bin-id (json/encode {:foo "bar"}))
        (should= (+ 1 request-count)
                 (count (all-requests)))))

    (it "should return not nil"
      (let [bin-id (create-bin)
            request-id (add-request bin-id (json/encode {:foo "bar"}))]
        (should-not-be-nil request-id))))

  (context "adding an invalid request to an existing bin"
    (it "doesn't increase the  request count"
      (let [bin-id (create-bin)
            invalid-full-request '(1 2 3)
            request-count (count (all-requests))]
        (add-request bin-id invalid-full-request)
        (should= request-count
                 (count (all-requests)))))

    (it "should return nil"
      (let [bin-id (create-bin)
            invalid-full-request '(1 2 3)
            request-id (add-request bin-id invalid-full-request)]
        (should-be-nil request-id))))

  (context "adding a request to an invalid bin"
    (it "doesn't increase the request count"
      (let [invalid-bin-id "not a valid id"
            request-count (count (all-requests))]
        (add-request invalid-bin-id (json/encode {:foo "bar"}))
        (should= request-count
                 (count (all-requests)))))

    (it "should return nil"
      (let [invalid-bin-id "not a valid id"
            request-id (add-request invalid-bin-id (json/encode {:foo "bar"}))]
        (should-be-nil request-id))))


  (context "getting the requests of a bin"
    (it "returns all the requests related to a bin"
      (let [bin-id (create-bin)]
        (add-request bin-id (json/encode {:foo "bar"}))
        (add-request bin-id (json/encode {:fizz "buzz"}))
        (should= 2 (count (get-requests bin-id)))))

    (it "returns an empty vector if the bin doesn't exist"
      (let [bin-id "not a valid id"]
        (add-request bin-id {:foo "bar"})
        (add-request bin-id {:fizz "buzz"})
        (should= [] (get-requests bin-id))))))
