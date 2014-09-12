(ns factora.core
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
  [fact]
  (let [info-tributaria (:info-tributaria fact)
        info-factura (:info-factura fact)
        tags (xml/sexp-as-element fact)
        _ (println (xml/indent-str tags))]
    (xml/emit-str tags)))
