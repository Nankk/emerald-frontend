(ns emerald-frontend.style.global
  (:require
   [garden.core :as g]
   [emerald-frontend.style.colors :as c]))

(defn css []
  (g/css
   [:html {:height "100%"
           :width "100%"
           :font-size "4.5vw"}]
   [:body {:height "100%"
           :width "100%"
           :font-family "PixelMplus10"
           :background-color (c/base 1)
           :color (c/base 7)}]
   [:#app {:height "100%"
           :width "100%"
           :background-color (c/base 1)}]
   ;; Font sizes are relative to viewport height
   [:h1 {:font-size "5vw!important"
         :margin "0.1rem"}]
   [:h2 {:font-size "4.5vw!important"
         :margin "0.08rem"}]
   [:h3 {:font-size "4vw!important"
         :margin "0.07rem"}]
   [:h4 {:font-size "3.5vw!important"
         :margin "0.06rem"}]
   [:h5 {:font-size "3vw!important"
         :margin "0.05rem"}]
   [:p  {:font-size "2vh!important"}]
   ))
