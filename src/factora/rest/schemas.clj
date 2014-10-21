(ns factora.rest.schemas
  (:require [schema.core :as s]
            [clj-time.format :as f]
            [clj-time.local :as l]
            [factora.utils :refer [valid-date length]]))

(s/defschema Invoice
  "Estructura de una Factura"
  {:ambiente s/Int
   :tipo_emision s/Int
   :fecha_emision (s/pred valid-date)
   :emisor {:ruc (and s/Str
                     (s/pred (length = 13)))
            :razon_social (and s/Str
                               (s/pred (and (length <= 300)
                                            (length > 0))))
            ; (s/maybe (s/both s/Str
            ;                  (s/pred (length <= 300)))) :nombre_comercial
            :direccion (and s/Str
                               (s/pred (length <= 300)))}})

(def factura
  {:ambiente 1
   :tipo_emision 2
   :fecha_emision "17/09/2014"
   :emisor {:ruc "0910000000001"
            :razon_social "Datilmedia S.A."
            :nombre_comercial "Datilmedia S.A."
            :direccion "VICTOR EMILIO ESTRADA 112 Y CIRCUNVALACION NORTE"
            :obligado_contabilidad true
            :establecimiento {:codigo "001"
                              :direccion "VICTOR EMILIO ESTRADA 112 Y CIRCUNVALACION NORTE"
                              :punto_venta "1"}}
   :comprador {:razon_social "Juan Antonio Plaza"
               :identificacion "0924447956"
               :tipo_id "04"}
   :moneda "DOLAR"
   :totales {:subtotal 100.0
             :descuento 0
             :propina 0
             :impuestos
             [{:codigo 2
               :codigo_porcentaje 2
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
                         :codigo_porcentaje 2
                         :base_imponible 100.0
                         :tarifa 12.00
                         :valor 12.0}]}]
   :info_adicional {:dato_adic_1 "valor_1"
                    :dato_adic_2 "valor_2"}})