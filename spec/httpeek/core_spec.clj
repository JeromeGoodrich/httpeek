(ns httpeek.core-spec
  (:require [speclj.core :refer :all]
            [httpeek.core :refer :all]
            [cheshire.core :as json]
            [httpeek.spec-helper :as helper]))

(describe "httpeek.core"
  (after (helper/reset-db))

  (context "retrieving bins"
    (it "returns a set amount of bins"
      (let [bins (repeatedly 50 #(create-bin {:private false}))]
        (should= (count bins) (count (get-bins {:limit 50})))))

    (it "returns 0 for an invalid limit"
      (should= 0 (get-bins {:limit "not-a-limit"}))))

  (context "creating a bin"
    (it "increases the bin count by 1"
      (let [bin-count (count (get-bins {:limit 50}))]
      (create-bin {:private false})
      (should= (+ 1 bin-count)
               (count (get-bins {:limit 50}))))))

  (context "finding a bin"
    (it "finds an existing bin"
      (let [bin-id (create-bin {:private false})]
        (should= bin-id (:id (find-bin-by-id bin-id)))))

    (it "returns nil if the bin doesn't exist"
      (let [bin-id "not a valid id"]
        (should-be-nil (find-bin-by-id bin-id)))))

  (context "adding a request to an exisiting bin"
    (it "increases the request count by 1"
      (let [bin-id (create-bin {:private false})
            request-count (count (all-requests))]
        (add-request bin-id (json/encode {:foo "bar"}))
        (should= (+ 1 request-count)
                 (count (all-requests)))))

    (it "should return not nil"
      (let [bin-id (create-bin {:private false})
            request-id (add-request bin-id (json/encode {:foo "bar"}))]
        (should-not-be-nil request-id))))

  (context "adding an invalid request to an existing bin"
    (it "doesn't increase the  request count"
      (let [bin-id (create-bin {:private false})
            invalid-full-request '(1 2 3)
            request-count (count (all-requests))]
        (add-request bin-id invalid-full-request)
        (should= request-count
                 (count (all-requests)))))

    (it "should return nil"
      (let [bin-id (create-bin {:private false})
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
      (let [bin-id (create-bin {:private false})]
        (add-request bin-id (json/encode {:foo "bar"}))
        (add-request bin-id (json/encode {:fizz "buzz"}))
        (should= 2 (count (get-requests bin-id)))))

    (it "returns an empty vector if the bin doesn't exist"
      (let [bin-id "not a valid id"]
        (add-request bin-id {:foo "bar"})
        (add-request bin-id {:fizz "buzz"})
        (should= [] (get-requests bin-id)))))

  (context "deleting an existing bin"
    (it "deletes the bin"
      (let [bin-id (create-bin {:private false})
            bin-count (count (get-bins {:limit 50}))]
        (delete-bin bin-id)
        (should= (- bin-count 1) (count (get-bins {:limit 50})))
        (should-be-nil (find-bin-by-id bin-id)))))

  (context "deleting a non-existent bin"
    (it "returns nil"
        (should-be-nil (delete-bin -1)))))
