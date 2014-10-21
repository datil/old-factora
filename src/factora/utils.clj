(ns factora.utils
  "Funciones utilitarias de uso común"
  (:require [clj-time.format :as f]
            [clj-time.local :as l]
            [clj-time.coerce :as c]
            [schema.core :as s]))

(defn partial-checksum [group] (reduce + 0 (map * group (range 2 8))))

(defn check-digit [access-key]
  (let [c (partition 6 6 nil (map #(Integer/parseInt (str %)) (reverse access-key)))
        digit (reduce + 0 (map partial-checksum c))
        m (mod digit 11)]
    (if (> m 1) (- 11 m) m)))

;; Tipos de comprobante
;; Comprobante
;; Factura           | 01
;; Nota de crédito   | 04
;; Nota de débito    | 05
;; Guía de remisión  | 06
;; Retención         | 07
(defn gen-access-key
  "El tipo de emisión puede ser:
     Emisión Nomal: 1
     Emisión por Indisponibilidad del Sistema: 2"
  [{:keys [emisor ambiente secuencial] :as comprobante} tipo-comprobante tipo_emision]
  (let [ahora (l/local-now)
        fecha (f/unparse (f/formatter "ddMMyyyy") ahora)
        ruc (:ruc emisor)
        secuencial-str (format "%09d" secuencial)
        codigo (get-in emisor [:establecimiento :codigo])
        punto-emisor (get-in emisor [:establecimiento :punto_emisor])
        serie (str codigo punto-emisor)
        ;codigo-numerico (f/unparse (f/formatter "emmssSSS") (l/local-now))
        ; Para generar el código numérico tomamos los últimos ocho dígitos de la
        ; hora actual en milisegundos
        ahora-epoch (c/to-long ahora)
        codigo-numerico (subs (str ahora-epoch)
                              (- (count (str ahora-epoch)) 8))
        clave (str fecha tipo-comprobante ruc ambiente serie secuencial-str codigo-numerico tipo_emision)
        verificador (check-digit clave)]
    (str clave verificador)))

(defn format-amount [amount]
  (format "%.02f" (float amount)))

;; Schema utils

(def ValidAmount
  (s/pred #(or
             (integer? %)
             (and (float? %)
                (<= (count (subs (str %)
                                 (+ (.indexOf (str %) ".") 1)))
                    2)))
          'valid-amount))

(defn valid-date [fecha]
  (try
    (f/parse (f/formatter "dd/MM/yyyy") fecha)
    true
    (catch Exception e
      false)))

(defn length
  "Retorna un predicado que verifica la longitud especificada"
  [fn-comparacion longitud]
  (fn [valor] (fn-comparacion (count valor) longitud)))