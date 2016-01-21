(defproject clojento "0.2.0-SNAPSHOT"
  :description "Clojure Magento Tools"
  :license {:name "The MIT License (MIT)"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.3"]
                 [clj-time "0.11.0"]
                 [com.layerware/hugsql-core "0.3.1"]
                 [com.layerware/hugsql-adapter-clojure-jdbc "0.3.1"]
                 [com.stuartsierra/component "0.3.0"]
                 [funcool/clojure.jdbc "0.6.1"]
                 [hikari-cp "1.4.0"]
                 [mysql/mysql-connector-java "5.1.37"]
                 [org.slf4j/log4j-over-slf4j "1.7.13"]
                 [org.slf4j/jul-to-slf4j "1.7.13"]
                 [org.slf4j/jcl-over-slf4j "1.7.13"]
                 [ragtime "0.5.2"]
                 [yesql "0.4.2"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [midje "1.8.2"]
                                  [com.h2database/h2 "1.4.190"]]
                   :plugins [[lein-midje "3.2"]]
                   :source-paths ["dev"]}})
