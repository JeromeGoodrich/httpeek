(ns httpeek.xml-formatter
  (:require [httpeek.core :as core])
  (:import [java.io StringReader StringWriter]
           [javax.xml.transform TransformerFactory OutputKeys]
           [org.xml.sax SAXParseException]
           [javax.xml.transform.stream StreamSource StreamResult]))

(def xml-transformer
  (delay (let [transformer (.newTransformer (TransformerFactory/newInstance))]
    (doseq [[prop val] {OutputKeys/INDENT "yes"
                        OutputKeys/METHOD "xml"
                        "{http://xml.apache.org/xslt}indent-amount" "2"}]
      (.setOutputProperty transformer prop val))
    transformer)))

(defn- ppxml [xml-str]
  (let [in  (StreamSource. (StringReader. xml-str))
        out (StreamResult. (StringWriter.))
        transformer @xml-transformer]
    (.transform transformer in out)
    (str (.getWriter out))))

(defn format-xml [xml-string]
  (core/with-error-handling "Malformed XML in the request body"
                            (ppxml xml-string)))


