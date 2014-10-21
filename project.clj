(defproject ec.datil/factora "0.1.0-SNAPSHOT"
  :description "Plataforma de factura electronica para Ecuador."
  :url "https://github.com/datilmedia/factora"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.xml "0.0.8"]
                 
                 [ec.datil/xml-validation "0.1.0-SNAPSHOT"]
                 [clj-time "0.8.0"]
                 [cheshire "5.3.1"]
                 [com.novemberain/validateur "2.3.1"]
                 [prismatic/schema "0.3.0"]
                 ; [com.taoensso/tower "3.0.2"]
                 [datil/schema-rosetta "0.1.0-SNAPSHOT"]
                 ; [cddr/integrity "0.3.1-SNAPSHOT"]
                 
                 [io.pedestal/pedestal.service "0.3.0"]

                 ;; Remove this line and uncomment the next line to
                 ;; use Tomcat instead of Jetty:
                 [io.pedestal/pedestal.jetty "0.3.0"]
                 ;; [io.pedestal/pedestal.tomcat "0.3.0"]

                 [ch.qos.logback/logback-classic "1.1.2" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.7"]
                 [org.slf4j/jcl-over-slf4j "1.7.7"]
                 [org.slf4j/log4j-over-slf4j "1.7.7"]]
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  :plugins [[codox "0.8.10"]]
  :profiles {:dev {:aliases {"run-dev" ["trampoline" "run" "-m" "factora.rest.server/run-dev"]}
                   :dependencies [[io.pedestal/pedestal.service-tools "0.3.0"]]
                   :plugins [[com.jakemccrary/lein-test-refresh "0.5.2"]]}}
  :main ^{:skip-aot true} factora.rest.server)
