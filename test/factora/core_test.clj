(ns factora.core-test
  (:require [clojure.test :refer :all]
            [factora.core :refer :all]
            [uk.me.rkd.xml-validation :as xmlv]))

(defn read-from-file [filename]
  (with-open [r (java.io.PushbackReader.
                 (clojure.java.io/reader filename))]
    (binding [*read-eval* false]
      (read r))))

(deftest factura-test
  (testing "Dado un mapa representando una factura, emite XML conforme al XSD"
    (let [factura-valida? (xmlv/create-validation-fn "resources/factura_v1.0.0.xsd")]
     (is (= true
            (factura-valida? (factura
                              (read-from-file "resources/factura.edn"))))))))
