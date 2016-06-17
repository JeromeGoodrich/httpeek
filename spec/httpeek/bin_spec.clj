(ns httpeek.bin-spec
  (require [speclj.core :refer :all]
           [httpeek.bin :as bin]))

(describe "create bin"
  (it "add's a new bin record to the bin table"
    (let [bin-count (count (bin/all-bins))]
    (bin/create)
    (should= (+ 1 bin-count)
             (count (bin/all-bins))))))
; wondering how to test whether the bin has the expected UUID since it's generated randomly by the db

