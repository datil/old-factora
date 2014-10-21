(ns factora.rest.service-test
  (:require [clojure.test :refer :all]
            [io.pedestal.test :refer :all]
            [io.pedestal.http :as bootstrap]
            [factora.rest.service :as service]
            [cheshire.core :refer [generate-string parse-string]]))

;; Fixtures


(def service
  (::bootstrap/service-fn (bootstrap/create-servlet service/service)))


; (is (=
;             (:headers (response-for service :post "/api/invoices"
;                                     :body (generate-string bill)
;                                     :headers {"Content-Type" "application/json"}))
;             {"Content-Type" "application/json;charset=UTF-8"
;              "Strict-Transport-Security" "max-age=31536000; includeSubdomains"
;              "X-Frame-Options" "DENY"
;              "X-Content-Type-Options" "nosniff"
;              "X-XSS-Protection" "1; mode=block"}))