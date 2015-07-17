;(ns clojento.views.products
;  (:require [clojento.views.common :as common])
;  (:use [noir.core :only [defpage url-for]]
;        [noir.response :only [redirect]]
;        [hiccup.core :only [html]]))
;
;(defpage index "/products" {} (common/layout
;	[:h1 "Products"]
;	[:hr]
;	[:table
;		[:thead [:tr
;			[:th "id"]
;			[:th "sku"]
;			[:th "name"]
;			[:th "type"]
;		]]
;		[:tbody
;			[:tr
;				[:td "1"]
;				[:td [:a {:href (url-for show {:id 1})} "123.4567.8.9"]]
;				[:td [:a {:href (url-for show {:id 1})} "product 1"]]
;				[:td "configurable"]
;			]
;			[:tr
;				[:td "2"]
;				[:td [:a {:href (url-for show {:id 2})} "123.1234.8.9"]]
;				[:td [:a {:href (url-for show {:id 2})} "product 2"]]
;				[:td "bundle"]
;			]
;			[:tr
;				[:td "3"]
;				[:td [:a {:href (url-for show {:id 3})} "123.1111.8.9"]]
;				[:td [:a {:href (url-for show {:id 3})} "product 3"]]
;				[:td "simple"]
;			]
;		]
;	]))
;
;(defpage all "/products/all" {} (common/layout
;	[:h1 "List All Products"]
;	[:hr]
;	[:table
;		[:thead [:tr
;			[:th "id"]
;			[:th "sku"]
;			[:th "name"]
;			[:th "type"]
;		]]
;		[:tbody
;			[:tr
;				[:td "1"]
;				[:td [:a {:href (url-for show {:id 1})} "123.4567.8.9"]]
;				[:td [:a {:href (url-for show {:id 1})} "product 1"]]
;				[:td "configurable"]
;			]
;			[:tr
;				[:td "2"]
;				[:td [:a {:href (url-for show {:id 2})} "123.1234.8.9"]]
;				[:td [:a {:href (url-for show {:id 2})} "product 2"]]
;				[:td "bundle"]
;			]
;			[:tr
;				[:td "3"]
;				[:td [:a {:href (url-for show {:id 3})} "123.1111.8.9"]]
;				[:td [:a {:href (url-for show {:id 3})} "product 3"]]
;				[:td "simple"]
;			]
;		]
;	]))
;
;
;(defpage show "/product/:id" {id :id}
;	(common/layout
;	[:h1 (str "Product " id " Detail Page")]
;	[:hr]))
;
