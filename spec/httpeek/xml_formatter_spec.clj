(ns httpeek.xml-formatter-spec
  (:require [httpeek.xml-formatter :refer :all]
            [clojure.tools.logging :as log]
            [speclj.core :refer :all]))

(describe "httpeek.xml-formatter"
  (context "formatting a proper xml string"
    (it "formats the xml string"
      (let [formatted-xml (format-xml "<root><child>aaa</child><child/></root>")]
        (should= {:body "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>\n  <child>aaa</child>\n  <child/>\n</root>\n"}
                 formatted-xml)))

    (it "it returns an error message if the xml is malformed"
      (log/log-capture! *ns*)
      (let [formatted-xml (log/spyf :info "XML Parsing Error %s in test" (format-xml "not-an-xml-string"))]
        (should= {:body "not-an-xml-string" :warning "Malformed XML in the request body"}
                 formatted-xml)))))
