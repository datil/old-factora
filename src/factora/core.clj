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
  [factura]
  (let [tags (xml/sexp-as-element factura)]
    (xml/emit-str tags)))

(defn retencion
  "Dada una retencion en formato EDN produce un XML
   de acuerdo al XSD de SRI"
  [retencion]
  (let [tags (xml/sexp-as-element retencion)]
    (xml/emit-str tags)))
