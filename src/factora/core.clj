(ns factora.core
  "API público para emisión de comprobantes electrónicos."
  (:require [clojure.data.xml :as xml]))

;; Testing
;; http://stackoverflow.com/questions/17138518/convert-hyphenated-string-to-camelcase
(defn hyphenated-name-to-camel-case-name [^String method-name]
  (clojure.string/replace method-name #"-(\w)"
                          #(clojure.string/upper-case
                            (do
                              (println %1)
                              (first %1)))))

(defn factura
  "Dada una factura en formato EDN produce un XML
   de acuerdo al XSD de SRI"
  [fact]
  (let [tags (xml/sexp-as-element fact)]
    (xml/emit-str tags)))
