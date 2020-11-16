(ns emerald-frontend.style.main-panel
  (:require [garden.core :as g]
            [emerald-frontend.style.colors :as c]))

(defn css []
  (g/css
   [:#main-panel {:background-color (c/base 2)
                  :overflow "hidden"}
    ]))
