(ns httpeek.db-spec
  (require [speclj.core :refer :all]
           [httpeek.db :refer :all]
           [httpeek.spec-helper :as helper]
           [clj-time.core :as t]
           [clj-time.coerce :as ct]
           [cheshire.core :as json]))

(describe "httpeek.db"
  (after (helper/reset-db))

  (context "When deleting expired bins"
    (it "deletes the bins"
      (let [bin-id (create-bin {:private false
                           :response helper/bin-response
                           :expiration (t/ago (t/hours 24))})]
        (should-not-be-nil (find-bin-by-id bin-id))
        (delete-expired-bins)
        (should-be-nil (find-bin-by-id bin-id))))

    (it "only deletes expired bins"
      (let [expired-bin-id (create-bin {:private false
                           :response helper/bin-response
                           :expiration (t/ago (t/hours 24))})
            unexpired-bin-id (create-bin {:private false
                                          :response helper/bin-response
                                          :expiration (t/from-now (t/hours 16))})]
        (delete-expired-bins)
        (should= unexpired-bin-id (:id (find-bin-by-id unexpired-bin-id)))
        (should-be-nil (find-bin-by-id expired-bin-id)))))

  (context "When retrieving bins"
    (it "gets a specified number of bins"
      (let [bins (repeatedly 50 #(create-bin {:private false
                                              :response helper/bin-response
                                              :expiration (t/from-now (t/hours 24))}))]
        (should= (count bins) (count (get-bins 50)))))

    (it "only retrieves unexpired bins"
      (let [bin (create-bin {:private false
                             :response helper/bin-response
                             :expiration (t/from-now (t/hours 24))})
            expired-bin (create-bin {:private false
                                     :response helper/bin-response
                                     :expiration (t/ago (t/hours 24))})]
        (should-contain bin (map #(get % :id) (get-bins 10)))
        (should-not-contain expired-bin (map #(get % :id) (get-bins 10))))))

  (context "When creating a bin"
    (it "adds a new public bin record to the bin table"
      (let [bin-count (count (get-bins 50))
            bin-id(create-bin {:private false
                               :response helper/bin-response
                               :expiration (t/from-now (t/hours 24))})]
        (should= (+ 1 bin-count)
                 (count (get-bins 50)))))

    (it "adds a new private bin record to the bin table"
      (let [bin-count (count (get-bins 50))
            bin-id(create-bin {:private true
                               :response helper/bin-response
                               :expiration (t/from-now (t/hours 24))})]
        (should= (+ 1 bin-count)
                 (count (get-bins 50))))))

  (context "When finding a bin"
    (it "finds a public bin with a custom response"
      (let [expiration (t/from-now (t/hours 7))
            bin-id (create-bin {:private false
                                :response  helper/bin-response
                                :expiration expiration})]
        (should= bin-id (:id (find-bin-by-id bin-id)))
        (should= 500 (:status (:response (find-bin-by-id bin-id))))
        (should= {:foo "bar"} (:headers (:response (find-bin-by-id bin-id))))
        (should= "hello world" (:body (:response (find-bin-by-id bin-id))))
        (should= (ct/to-string expiration)
                 (ct/to-string (ct/from-sql-time (:expiration (find-bin-by-id bin-id)))))))

    (it "finds a private bin"
      (let [bin-id (create-bin {:private true
                                :response helper/bin-response
                                :expiration (t/from-now (t/hours 24))})]
        (should= bin-id (:id (find-bin-by-id bin-id)))
        (should= true (:private (find-bin-by-id bin-id))))))


  (context "When a request is created successfully"
    (it "adds a request to an existing bin"
      (let [bin-id (create-bin {:private false
                                :response helper/bin-response
                                :expiration (t/from-now (t/hours 24))})
            full-request (json/encode {:foo "bar"})
            request-count (count (all-requests))]
        (let [request-id (add-request bin-id full-request)]
        (should= (+ 1 request-count)
                 (count (all-requests))))))

    (it "has the correct details"
      (let [bin-id (create-bin {:private false
                                :response helper/bin-response
                                :expiration (t/from-now (t/hours 24))})
            full-request (json/encode {:foo "bar"})
            request-id (add-request bin-id (json/encode full-request))
            request (find-request-by-id request-id)]
        (should= request-id (:id request))
        (should= full-request (:full_request request))
        (should= bin-id (:bin_id request))))

    (it "should be associated with a current bin-id"
      (let [bin-id (create-bin {:private false
                                :response helper/bin-response
                                :expiration (t/from-now (t/hours 24))})
            first-request-id (add-request bin-id (json/encode {:position "first"}))
            second-request-id (add-request bin-id (json/encode {:position "second"}))
            requests (find-requests-by-bin-id bin-id)]
        (should= 2 (count requests))
        (should= first-request-id (:id (first requests)))
        (should= {:position "first"} (:full_request (first requests)))
        (should= second-request-id (:id (second requests)))
        (should= {:position "second"} (:full_request (second requests))))))

  (context "When deleting a bin"
    (it "removes a record from the bin table"
      (let [bin-id (create-bin {:private false
                                :response helper/bin-response
                                :expiration (t/from-now (t/hours 24))})
            bin-count (count (get-bins 50))]
        (delete-bin bin-id)
        (should= (- 1 bin-count) (count (get-bins 50)))
        (should-be-nil (find-bin-by-id bin-id))))

    (it "returns the count of bins deleted"
      (let [bin-id (create-bin {:private false
                                :response helper/bin-response
                                :expiration (t/from-now (t/hours 24))})
            fake-bin-id nil]
        (should= 1 (delete-bin bin-id))
        (should= 0 (delete-bin fake-bin-id))))

    (it "deletes all requests associated with a deleted bin"
      (let [bin-id (create-bin {:private false
                                :response helper/bin-response
                                :expiration (t/from-now (t/hours 24))})
            first-request-id (add-request bin-id (json/encode {:position "first"}))
            second-request-id (add-request bin-id (json/encode {:position "second"}))]
        (delete-bin bin-id)
        (should-be-nil (find-request-by-id first-request-id))
        (should-be-nil (find-request-by-id second-request-id))))))
