(defproject ec.datil/factora "0.1.0-SNAPSHOT"
  :description "Plataforma de factura electronica para Ecuador."
  :url "https://github.com/datilmedia/factora"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/data.xml "0.0.8"]
                 [ec.datil/xml-validation "0.1.0-SNAPSHOT"]]
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  :plugins [[codox "0.8.10"]])
