(ns httpeek.db-spec
  (require [speclj.core :refer :all]
           [ring.mock.request :as mock]
           [httpeek.db :as db]
           [cheshire.core :as json]))

(describe "db operations"
  (context "a bin is created successfully"
    (it "adds a new bin record to the bin table"
      (let [bin-count (count (db/all-bins))]
        (db/create)
        (should= (+ 1 bin-count)
                 (count (db/all-bins))))))

  (context "a successful request"
    (it "adds a request to an existing bin"
      (let [bin-id (db/create)
            request-body (json/generate-string (mock/request :get (str "/bin/" bin-id)))
            request-count (count (db/all-requests))]
        (db/add-request bin-id request-body)
        (should= (+ 1 request-count)
                 (count (db/all-requests)))))

    (it "should be associated with a current bin-id"
      (let [bin-id (db/create)
            request-body (json/generate-string (mock/request :get (str "/bin/" bin-id)))
            request-id (db/add-request bin-id request-body)]
        (should= bin-id (:bin_id (first (db/find-by "requests" "id" request-id))))))))

