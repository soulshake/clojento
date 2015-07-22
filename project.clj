(defproject clojento "0.2.0-SNAPSHOT"
  :description "Clojure Magento Tools"
  :license {:name "The MIT License (MIT)"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/java.jdbc "0.3.7"]
                 [ch.qos.logback/logback-classic "1.1.3"]
                 [com.stuartsierra/component "0.2.3"]
                 [com.taoensso/timbre "4.0.2"]
                 [hikari-cp "1.2.4" :exclusions [com.zaxxer/HikariCP]]
                 [com.zaxxer/HikariCP-java6 "2.3.9"]
                 [mysql/mysql-connector-java "5.1.35"]
                 [yesql "0.4.0"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]]
                   :source-paths ["dev"]}})
