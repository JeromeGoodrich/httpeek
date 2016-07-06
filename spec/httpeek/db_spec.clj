(ns httpeek.db-spec
  (require [speclj.core :refer :all]
           [httpeek.db :refer :all]
           [httpeek.spec-helper :as helper]
           [cheshire.core :as json]))

(describe "httpeek.db"
  (after (helper/reset-db))

  (context "creating a bin"
    (it "adds a new public bin record to the bin table"
      (let [bin-count (count (all-bins))
            bin-id(create-bin false)]
        (should= (+ 1 bin-count)
                 (count (all-bins)))))

    (it "adds a new private bin record to the bin table"
      (let [bin-count (count (all-bins))
            bin-id(create-bin false)]
        (should= (+ 1 bin-count)
                 (count (all-bins))))))

  (context "finding a bin"
    (it "finds a public bin"
      (let [bin-id (create-bin false)]
        (should= bin-id (:id (find-bin-by-id bin-id)))
        (should= false (:private (find-bin-by-id bin-id)))))

    (it "finds a private bin"
      (let [bin-id (create-bin true)]
        (should= bin-id (:id (find-bin-by-id bin-id)))
        (should= true (:private (find-bin-by-id bin-id))))))


  (context "a request is created successfully"
    (it "adds a request to an existing bin"
      (let [bin-id (create-bin false)
            full-request (json/encode {:foo "bar"})
            request-count (count (all-requests))]
        (let [request-id (add-request bin-id full-request)]
        (should= (+ 1 request-count)
                 (count (all-requests))))))

    (it "has the correct details"
      (let [bin-id (create-bin false)
            full-request (json/encode {:foo "bar"})
            request-id (add-request bin-id (json/encode full-request))
            request (find-request-by-id request-id)]
        (should= request-id (:id request))
        (should= full-request (:full_request request))
        (should= bin-id (:bin_id request))))

    (it "should be associated with a current bin-id"
      (let [bin-id (create-bin false)
            first-request-id (add-request bin-id (json/encode {:position "first"}))
            second-request-id (add-request bin-id (json/encode {:position "second"}))
            requests (find-requests-by-bin-id bin-id)]
        (should= 2 (count requests))
        (should= first-request-id (:id (first requests)))
        (should= {:position "first"} (:full_request (first requests)))
        (should= second-request-id (:id (second requests)))
        (should= {:position "second"} (:full_request (second requests)))
        )))

  (context "deleting a bin"
    (it "removes a record from the bin table"
      (let [bin-id (create-bin false)
            bin-count (count (all-bins))]
        (delete-bin bin-id)
        (should= (- 1 bin-count) (count (all-bins)))
        (should-be-nil (find-bin-by-id bin-id))))

    (it "returns the count of bins deleted"
      (let [bin-id (create-bin false)
            fake-bin-id nil]
        (should= 1 (delete-bin bin-id))
        (should= 0 (delete-bin fake-bin-id))))))
