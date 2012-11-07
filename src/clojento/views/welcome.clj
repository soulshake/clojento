(ns clojento.views.welcome
  (:require [clojento.views.common :as common])
  (:use [noir.core :only [defpage url-for]]
        [noir.response :only [redirect]]
        [hiccup.core :only [html]]))

(defpage root "/" {}
  (redirect (url-for dashboard)))

(defpage dashboard "/welcome" {}
  (common/layout
    [:h1 "Welcome to clojento"]
    [:hr]))
