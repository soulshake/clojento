(ns clojento.core
  (:require [clojento.config :as config]
            [clojento.magento :as magento]
            [clojento.magento.db :as magento-db]
            [clojure.tools.logging :as log]))

(log/debug "loading clojento.core namespace")
