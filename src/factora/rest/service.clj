(ns factora.rest.service
    (:require [io.pedestal.http :as bootstrap]
              [io.pedestal.http.route :as route]
              [io.pedestal.http.body-params :as body-params]
              [io.pedestal.http.route.definition :refer [defroutes]]
              [ring.util.response :as ring-resp]
              [factora.invoice :as invoice])
    (:import [clojure.lang ExceptionInfo]))


(defn home-page
  [request]
  (ring-resp/response "Home"))

(defn create-invoice
  [request]
  (let [invoice (:json-params request)]
    (try 
      (ring-resp/response (invoice/build-invoice invoice))
      (catch ExceptionInfo e
        (-> (ring-resp/response {:errores (:errors (ex-data e))})
            (ring-resp/status 400))))))

(defroutes routes
  [[["/" {:get home-page}
     ;; Set default interceptors for /about and any other paths under /
     ^:interceptors [(body-params/body-params) bootstrap/html-body]]
    ["/api"
     ^:interceptors [(body-params/body-params) bootstrap/json-body]
     ["/invoices" {:post [:post-invoice create-invoice]}]]]])

;; Consumed by factora.http.server/create-server
;; See bootstrap/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::bootstrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty or :tomcat (see comments in project.clj
              ;; to enable Tomcat)
              ;;::bootstrap/host "localhost"
              ::bootstrap/type :jetty
              ::bootstrap/port 8080})

