(ns httpeek.core-spec
  (:require [speclj.core :refer :all]
            [httpeek.core :refer :all]))

(describe "Core"
  (it "verifies that an id is valid"
    (let [bin-id (str (create-bin))
          invalid-bin-id 123456789]
      (should (is-valid-id? bin-id))
      (should-not (is-valid-id? invalid-bin-id))))

  (it "gets the requests for a given bin"
    (let [bin-id (str (create-bin))
          request1-id (add-request bin-id {:foo "bar"})
          request2-id (add-request bin-id {:bar "foo"})]
      (should= 2 (count (get-requests bin-id)))
      (should= request1-id (:id (first(get-requests bin-id))))
      (should= request2-id (:id (last (get-requests bin-id)))))))
