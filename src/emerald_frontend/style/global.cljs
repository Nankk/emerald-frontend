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
   [:h1 {:font-size "1rem"
         :margin "0.1rem"}]
   [:h2 {:font-size "0.8rem"
         :margin "0.08rem"}]
   [:h3 {:font-size "0.7rem"
         :margin "0.07rem"}]
   [:h4 {:font-size "0.6rem"
         :margin "0.06rem"}]
   [:h5 {:font-size "0.5rem"
         :margin "0.05rem"}]
   [:p  {:font-size "3vh"}]
   ))
