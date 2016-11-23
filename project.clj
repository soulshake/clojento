(defproject clojento "0.2.0-SNAPSHOT"
  :description "Clojure Magento Tools"
  :license {:name "The MIT License (MIT)"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.3"]
                 [clj-time "0.11.0"]
                 [com.layerware/hugsql-core "0.3.1"]
                 [com.layerware/hugsql-adapter-clojure-jdbc "0.3.1"]
                 [funcool/clojure.jdbc "0.6.1"]
                 [functionalbytes/mount-lite "0.9.8"]
                 [hikari-cp "1.5.0"]
                 [mysql/mysql-connector-java "5.1.38"]
                 [org.slf4j/log4j-over-slf4j "1.7.14"]
                 [org.slf4j/jul-to-slf4j "1.7.14"]
                 [org.slf4j/jcl-over-slf4j "1.7.14"]
                 [ragtime "0.5.2"]
                 [robert/hooke "1.3.0"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.3.0-alpha3"]
                                  [com.h2database/h2 "1.4.191"]]
                   :source-paths ["dev"]}})
