(ns factora.invoice
  (:require [clojure.data.xml :as xml]
            [schema.core :as s]
            [factora.utils :as utils]
            [schema-rosetta.core :as rosetta]
            [validateur.validation :as v :refer [validation-set
                                                 presence-of
                                                 nested
                                                 compose-sets]]))

(s/defschema Tax
  {:codigo (s/enum 2 3 5)
   :codigo_porcentaje s/Str
   :base_imponible utils/ValidAmount
   :tarifa utils/ValidAmount
   :valor utils/ValidAmount})

(s/defschema Invoice
  "Estructura de una Factura"
  {:ambiente s/Int
   :secuencial s/Int
   :tipo_emision (s/enum 1 2)
   :comprador {:razon_social (s/both s/Str
                                     (s/pred (utils/length > 0) 'min-length)
                                     (s/pred (utils/length <= 300) 'max-length))
               :identificacion (s/both s/Str
                                       (s/pred (utils/length <= 300) 'max-length)
                                       (s/pred (utils/length > 0) 'min-length))
               ;; 04 RUC
               ;; 05 Cédula
               ;; 06 Pasaporte
               ;; 07 Venta a consumidor final
               ;; 08 ID del exterior
               ;; 09 Placa
               :tipo_id (s/enum "04" "05" "06" "07" "08" "09")}
   :items [{:detalle (s/both s/Str
                             (s/pred (utils/length > 0) 'min-length)
                             (s/pred (utils/length <= 300) 'max-length))
            :codigo_principal (s/both s/Str
                                      (s/pred (utils/length > 0) 'min-length-1)
                                      (s/pred (utils/length <= 25) 'max-length-25))
            :codigo_auxiliar (s/maybe (s/both s/Str
                                              (s/pred (utils/length > 0) 'min-length)
                                              (s/pred (utils/length <= 25) 'max-length)))
            :cantidad utils/ValidAmount
            :precio utils/ValidAmount
            :descuento (s/maybe utils/ValidAmount)
            :subtotal utils/ValidAmount
            :impuestos [{;; 2 IVA
                         ;; 3 ICE
                         ;; 5 IRBPNR
                         :codigo (s/enum 2 3 5)
                         :codigo_porcentaje s/Str
                         :base_imponible utils/ValidAmount
                         :tarifa utils/ValidAmount
                         :valor utils/ValidAmount}]}]
   :moneda s/Str
   (s/optional-key :guia_remision) (s/pred #(re-matches #"[0-9]{3}-[0-9]{3}-[0-9]{9}" %)
                                           'valid)
   :totales {:subtotal utils/ValidAmount
             :descuento utils/ValidAmount
             :propina utils/ValidAmount
             :impuestos
             [Tax]
             :importe_total utils/ValidAmount}
   (s/optional-key :info_adicional) (s/pred map? 'map?)
   :fecha_emision (s/pred utils/valid-date 'a-valid-date)
   :emisor {:ruc (s/both s/Str
                         (s/pred (utils/length = 13) 'of-length-13))
            :razon_social (s/both s/Str
                                  (s/pred (utils/length <= 300) 'max-length-300)
                                  (s/pred (utils/length > 0) 'min-length-1))
            :nombre_comercial (s/maybe (s/both s/Str
                                               (s/pred (utils/length <= 300) 'max-length-300)))
            :direccion (s/both s/Str
                               (s/pred (utils/length <= 300) 'max-length-300))
            :obligado_contabilidad s/Bool
            :establecimiento {:codigo (s/both s/Str
                                              (s/pred (utils/length = 3) 'length-equal-to-3))
                              :direccion (s/both s/Str
                                                 (s/pred (utils/length > 1) 'min-length-1)
                                                 (s/pred (utils/length <= 300) 'max-length-300))
                              :punto_emisor (s/both s/Str
                                                    (s/pred (utils/length = 3) 'of-length-3))}}})

(def validator
  (compose-sets
    (validation-set
      (presence-of [:ambiente :tipo_emision :fecha_emision :emisor]
                   :message "es requerido"))
    (nested :emisor 
            (validation-set
              (presence-of [:ruc
                            :razon_social
                            :direccion
                            :obligado_contabilidad
                            :establecimiento])))))

(defn build-error-msg [field error]
  (str (name field) " " error))

(defn error-msgs-partial [field errors]
  (map (partial build-error-msg field) errors))

(defn grouped-messages [fields errors]
  (map #(error-msgs-partial % errors) fields))

(defn error-map [errors]
  (map #(if (keyword? (key %))
          (error-msgs-partial (key %) (val %))
          (grouped-messages (key %) (val %)))
       errors))

(defn build-invoice [invoice]
  (try
    (s/validate Invoice invoice)
    (assoc invoice :clave_acceso (utils/gen-access-key invoice "01" 1))
    (catch RuntimeException e
      (throw (ex-info "Invoice does not match the schema"
                      {:system_errors (s/check Invoice invoice)
                       :errors (rosetta/human-check Invoice invoice)})))))

(defn sum [& args]
  (apply + (filter #(not (nil? %)) args)))

(defn group-taxes [tax-list {:keys [valor base_imponible] :as impuesto}]
  (-> tax-list
      (update-in [(:codigo impuesto) :base_imponible] sum base_imponible)
      (update-in [(:codigo impuesto) :total] sum valor)))

(defn calculate-totals
  "Recorre los ítems y calcula el total de cada impuesto
   :items [{
      :detalle \"Coca Cola\"
      :codigo_principal \"ABC\"
      :codigo_auxiliar \"123\"
      :cantidad 1
      :precio 1.5
      :impuestos [{
        :codigo 2,
        :codigo_porcentaje 2,
        :base_imponible 1.5,
        :tarifa 12.00
        :valor 0.18
      }]
    }]"
  [{:keys [items] :as factura}]
  (reduce group-taxes {} (flatten (map :impuestos items))))

(declare item-detail-edn tax-total-edn)

(defn invoice-edn [{:keys [emisor comprador ambiente items
                           tipo_emision secuencial totales] :as factura}]
  [:factura {:xmlns:ds "http://www.w3.org/2000/09/xmldsig#"
           :xmlns:xsi "http://www.w3.org/2001/XMLSchema-instance"
           :xsi:noNamespaceSchemaLocation "factura_v1.0.0.xsd"
           :id "comprobante"
           :version "Text"}
   [:infoTributaria {}
    [:ambiente {} ambiente]
    [:tipoEmision {} tipo_emision]
    [:razonSocial {} (:razon_social emisor)]
    [:nombreComercial {} (:nombre_comercial emisor)]
    [:ruc {} (:ruc emisor)]
    [:claveAcceso {} (:clave_acceso factura)]
    [:codDoc {} "01"]
    [:estab {} (get-in emisor [:establecimiento :codigo])]
    [:ptoEmi {} (get-in emisor [:establecimiento :punto_emisor])]
    [:secuencial (format "%09d" secuencial)]
    [:dirMatriz {} (:direccion emisor)]]
   [:infoFactura {}
    [:fechaEmision {} (:fecha_emision factura)]
    [:dirEstablecimiento {} (get-in emisor [:establecimiento :direccion])]
    (when (:contribuyente_especial emisor)
      [:contribuyenteEspecial {} (:contribuyente_especial emisor)])
    [:obligadoContabilidad {} (if (:obligado_contabilidad emisor)
                                "SI" "NO")]
    [:tipoIdentificacionComprador {} (:tipo_id comprador)]
    (when (:guia_remision factura)
      [:guiaRemision {} (:guia_remision factura)])
    [:razonSocialComprador {} (:razon_social comprador)]
    [:identificacionComprador {} (:identificacion comprador)]
    [:totalSinImpuestos {} (utils/format-amount (:subtotal totales))]
    [:totalDescuento {} (utils/format-amount (:descuento totales))]
    (into [:totalConImpuestos {}] (mapv tax-total-edn (:impuestos totales)))
    [:propina {} (utils/format-amount (:propina totales))]
    [:importeTotal {} (utils/format-amount (:importe_total totales))]
    [:moneda {} (:moneda factura)]]
   (into [:detalles {}] (mapv item-detail-edn items))
   [:infoAdicional {}
    [:campoAdicional {:nombre "Info"} "Info"]]])

(defn tax-total-edn [{:keys [descuento_adicional] :as impuesto}]
  [:totalImpuesto {}
   [:codigo {} (:codigo impuesto)]
   [:codigoPorcentaje {} (:codigo_porcentaje impuesto)]
   (when descuento_adicional
     [:descuentoAdicional {} (utils/format-amount descuento_adicional)])
   [:baseImponible {} (utils/format-amount (:base_imponible impuesto))]
   [:valor {} (utils/format-amount (:valor impuesto))]])

(defn tax-edn [impuesto]
  [:impuesto {}
   [:codigo {} (:codigo impuesto)]
   [:codigoPorcentaje {} (:codigo_porcentaje impuesto)]
   [:tarifa {} (utils/format-amount (:tarifa impuesto))]
   [:baseImponible {} (utils/format-amount (:base_imponible impuesto))]
   [:valor {} (utils/format-amount (:valor impuesto))]])

(defn item-detail-edn [{:keys [codigo_principal codigo_auxiliar detalle cantidad
                               precio descuento subtotal impuestos]
                     :as item}]
  [:detalle {}
   [:codigoPrincipal {} codigo_principal]
   [:codigoAuxiliar {} codigo_auxiliar]
   [:descripcion {} detalle]
   [:cantidad {} (utils/format-amount cantidad)]
   [:precioUnitario (utils/format-amount precio)]
   [:descuento {} (utils/format-amount descuento)]
   [:precioTotalSinImpuesto {} (utils/format-amount precio)]
   [:detallesAdicionales {}
    [:detAdicional {:nombre "D" :valor "X"}]]
   (into [:impuestos {}] (mapv tax-edn impuestos))])
