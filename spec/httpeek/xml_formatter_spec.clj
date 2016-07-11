(ns httpeek.xml-formatter-spec
  (:require [httpeek.xml-formatter :refer :all]
            [speclj.core :refer :all]))

(describe "httpeek.xml-formatter"
  (context "formatting a proper xml string"
    (it "formats the xml string"
      (let [formatted-xml (format-xml "<root><child>aaa</child><child/></root>")]
        (should= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>\n  <child>aaa</child>\n  <child/>\n</root>\n"
                 formatted-xml)))

    (it "it returns an error message if the xml is malformed"
      (let [formatted-xml (format-xml "not-an-xml-string")]
        (should= "Malformed XML in the request body"
                 formatted-xml)))))

