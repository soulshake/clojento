(ns clojento.views.products
  (:require [clojento.views.common :as common])
  (:use [noir.core :only [defpage url-for]]
        [noir.response :only [redirect]]
        [hiccup.core :only [html]]))

(defpage index "/products" {}
	(common/layout
	[:h1 "List All Products"]
	[:hr]
	[:div [:a {:href (url-for show {:id 1})} "product 1"]]
	[:div [:a {:href (url-for show {:id 2})} "product 2"]]
	[:div [:a {:href (url-for show {:id 3})} "product 3"]]))

(defpage show "/product/:id" {id :id}
	(common/layout
	[:h1 (str "Product " id " Detail Page")]
	[:hr]))
