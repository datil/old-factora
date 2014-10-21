(ns factora.test-utils
  (:require [uk.me.rkd.xml-validation :as xmlv]))

(import 'javax.xml.XMLConstants)
(import 'org.xml.sax.SAXException)
(import 'javax.xml.validation.SchemaFactory)
(import 'java.io.File)
(import 'java.io.StringReader)
(import 'javax.xml.transform.stream.StreamSource)

(defn create-validation-fn [schema]
  (let [validator (.newValidator
      (.newSchema
       (SchemaFactory/newInstance XMLConstants/W3C_XML_SCHEMA_NS_URI)
       (StreamSource. (File. schema))
       ))]
    (fn [xmldoc]
      (try
        (.validate validator
                   (StreamSource. (StringReader. xmldoc)))
        true
        (catch SAXException e
          (println "Validation Exception:\n\n" e "\n\n")
          false)))))

(defn valid-invoice-xml? [xml]
  ((create-validation-fn "resources/factura_v1.0.0.xsd") xml))

(defn ppxml [xml]
  (let [in (javax.xml.transform.stream.StreamSource.
            (java.io.StringReader. xml))
        writer (java.io.StringWriter.)
        out (javax.xml.transform.stream.StreamResult. writer)
        transformer (.newTransformer 
                     (javax.xml.transform.TransformerFactory/newInstance))]
    (.setOutputProperty transformer 
                        javax.xml.transform.OutputKeys/INDENT "yes")
    (.setOutputProperty transformer 
                        "{http://xml.apache.org/xslt}indent-amount" "2")
    (.setOutputProperty transformer 
                        javax.xml.transform.OutputKeys/METHOD "xml")
    (.transform transformer in out)
    (-> out .getWriter .toString)))