(defproject clojento "0.2.0-SNAPSHOT"
  :description "Clojure Magento Tools"
  :license {:name "The MIT License (MIT)"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [ch.qos.logback/logback-classic "1.1.3"]
                 [com.stuartsierra/component "0.3.0"]
                 [com.taoensso/timbre "4.1.4"]
                ;  [funcool/clojure.jdbc "0.5.1"]
                 [funcool/clojure.jdbc "0.6.1"]
                 [hikari-cp "1.4.0"]
                 [mysql/mysql-connector-java "5.1.37"]
                 [yesql "0.4.2"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [midje "1.8.2"]
                                  [com.h2database/h2 "1.4.190"]]
                   :source-paths ["dev"]}})
