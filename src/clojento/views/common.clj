(ns clojento.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page-helpers :only [include-css include-js html5]]))

(defpartial foundation-js []
            (include-js "js/foundation/jquery.js"
                        "js/foundation/jquery.cookie.js"
                        "js/foundation/jquery.event.move.js"
                        "js/foundation/jquery.event.swipe.js"
                        "js/foundation/jquery.foundation.accordion.js"
                        "js/foundation/jquery.foundation.alerts.js"
                        "js/foundation/jquery.foundation.buttons.js"
                        "js/foundation/jquery.foundation.clearing.js"
                        "js/foundation/jquery.foundation.forms.js"
                        "js/foundation/jquery.foundation.joyride.js"
                        "js/foundation/jquery.foundation.magellan.js"
                        "js/foundation/jquery.foundation.mediaQueryToggle.js"
                        "js/foundation/jquery.foundation.navigation.js"
                        "js/foundation/jquery.foundation.orbit.js"
                        "js/foundation/jquery.foundation.reveal.js"
                        "js/foundation/jquery.foundation.tabs.js"
                        "js/foundation/jquery.foundation.tooltips.js"
                        "js/foundation/jquery.foundation.topbar.js"
                        "js/foundation/jquery.placeholder.js"
                        "js/foundation/app.js"))
            
(defpartial layout [& content]
            (html5
              [:head
               [:title "clojento"]
               [:link {:href "/css/app.css" :media "screen, projector, print" :rel "stylesheet" :type "text/css"} ]]
              [:body
               [:div.row [:div.twelve.columns content]]
               (foundation-js)]))
