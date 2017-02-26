(defproject distmarkets-service "0.1.0-SNAPSHOT"
  :description "Distribtued Markets Hackathon"
  :url "http://tidwell.com"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [org.clojure/java.classpath "0.2.3"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/core.async "0.2.395"]
                 [mount "0.1.11"]
                 [clj-http "3.4.1"]
                 [cheshire "5.6.3"]
                 [aero "1.1.2"]
                 [aleph "0.4.2-alpha12"]
                 [digest "1.4.5"]
                 [while-let "0.2.0"]
                 [bidi "2.0.16"]
                 [yada "1.2.0"
                  :exclusions [ring-swagger
                               com.fasterxml.jackson.core/jackson-databind]]
                 [com.taoensso/carmine "2.15.1"]
                 [metosin/ring-swagger "0.22.12"]
                 [com.taoensso/encore "2.89.0"]
                 [ymilky/franzy "0.0.1"]
                 [org.apache.kafka/kafka-clients "0.10.2.0"]
                 [kafka-clj "4.0.0"]
                 [com.taoensso/timbre "4.8.0"]]
  :plugins [[lein-cljfmt "0.5.6"]
            [lein-ancient "0.6.10"]]
  :main ^:skip-aot distmarkets-service.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             
             :dev
             {:repl-options {:init-ns user
                             :caught  clj-stacktrace.repl/pst+}
              :source-paths ["src" "test" "dev"]
              :dependencies [[org.clojure/tools.namespace "0.2.11"]
                             [clj-stacktrace "0.2.8"]]
              :test         {:jvm-opts ["-Xms768m" "-Xms768m"]}}})
