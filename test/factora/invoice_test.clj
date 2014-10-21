(ns factora.invoice-test
  (:require [clojure.test :refer :all]
            [factora.invoice :as inv]
            [factora.core :as factora]
            [factora.test-utils :as tutils]
            [clj-time.format :as f]
            [clj-time.local :as l]
            [cheshire.core :refer [generate-string parse-string]])
  (:import [clojure.lang ExceptionInfo]))

;; Fixtures

(def invoice
  {:ambiente 1
   :tipo_emision 2
   :fecha_emision "17/09/2014"
   :secuencial 10
   :guia_remision "123-123-123456789"
   :emisor {:ruc "0910000000001"
            :razon_social "Datilmedia S.A."
            :nombre_comercial "Datilmedia S.A."
            :direccion "VICTOR EMILIO ESTRADA 112 Y CIRCUNVALACION NORTE"
            :obligado_contabilidad true
            :establecimiento {:codigo "001"
                              :direccion "VICTOR EMILIO ESTRADA 112 Y CIRCUNVALACION NORTE"
                              :punto_emisor "001"}}
   :comprador {:razon_social "Juan Antonio Plaza"
               :identificacion "0924447956"
               :tipo_id "05"}
   :moneda "DOLAR"
   :totales {:subtotal 100.0
             :descuento 0
             :propina 0
             :impuestos
             [{:codigo 2
               :codigo_porcentaje "2"
               :tarifa 12.0
               :base_imponible 100.0
               :valor 12.0}]
             :importe_total 112.0}
   :items [{:detalle "Coca Cola"
            :codigo_principal "ABC"
            :codigo_auxiliar "123"
            :cantidad 1
            :precio 100
            :descuento 0
            :subtotal 100.0
            :impuestos [{:codigo 2
                         :codigo_porcentaje "2"
                         :base_imponible 100.0
                         :tarifa 12.00
                         :valor 12.0}]}]
   :info_adicional {:dato_adic_1 "valor_1"
                    :dato_adic_2 "valor_2"}})

;; Tests

(deftest when-invoice-map-is-valid
  (let [new-invoice (inv/build-invoice invoice)]
    (testing "the resulting map should contain the key :clave_acceso"
      (is (contains? new-invoice :clave_acceso)))
    
    (testing "the access key (clave de acceso) should be 49 characters long"
      (is (= (count (:clave_acceso new-invoice)) 49)))))

(deftest when-invoice-map-is-missing-required-keys
  (testing "it should return an Exception"
    (are [ex new-invoice] (thrown? ex new-invoice)
         ExceptionInfo (inv/build-invoice (dissoc invoice :emisor))
         ExceptionInfo (inv/build-invoice (dissoc invoice :items))
         ExceptionInfo (inv/build-invoice (dissoc invoice :totales))
         ExceptionInfo (inv/build-invoice (dissoc invoice :emisor))
         ExceptionInfo (inv/build-invoice (dissoc invoice :comprador))
         ExceptionInfo (inv/build-invoice (dissoc invoice :secuencial))
         ExceptionInfo (inv/build-invoice (dissoc invoice :ambiente))
         ExceptionInfo (inv/build-invoice (dissoc invoice :fecha_emision))
         ExceptionInfo (inv/build-invoice (dissoc invoice :moneda)))))

(defn new-invoice-ex-data
  [invoice]
  (try
    (inv/build-invoice invoice)
    (catch ExceptionInfo ei
      (ex-data ei))))

(deftest create-invoice-when-missing-key-emisor
  (testing "the raised exception should include an info map with the errors"
    (is (= (get-in (new-invoice-ex-data (dissoc invoice :emisor))
                   [:errors :emisor])
           "missing required key"))))

(deftest when-creating-a-valid-invoice
  ; (println "--------")
  ; (println (ppxml (factora/factura (inv/invoice-edn (new-invoice-ex-data invoice)))))
  ; (println (valid-invoice-xml? (factora/factura (inv/invoice-edn invoice))))
  ; (println "--------")
  (testing "edn-xml generation"
    (is (tutils/valid-invoice-xml? (factora/factura (inv/invoice-edn
                                                      (new-invoice-ex-data invoice)))))))
