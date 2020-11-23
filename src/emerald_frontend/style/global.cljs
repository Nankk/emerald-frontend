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
   [:h1 {:font-size "4.0vw!important"
         :margin "3vw"}]
   [:h2 {:font-size "3.0vw!important"
         :margin "0.08rem"}]
   [:h3 {:font-size "2.5vw!important"
         :margin "0.07rem"}]
   [:h4 {:font-size "2.0vw!important"
         :margin "0.06rem"}]
   [:h5 {:font-size "1.5vw!important"
         :margin "0.5vw"}]
   [:h6 {:font-size "1.0vw!important"
         :margin "0.5vw"}]
   [:p  {:font-size "2vh!important"}]

   ;; Aux
   [:hr {:width "100%"
         :height "0.1vw"
         :background-color "#ffffff"
         :margin "0"
         }]

   ;; For nes components
   [:.nes-container.is-rounded {:margin "0px"
                                :padding "1vw 2vw"}]
   [:label {:margin "0px"}]
   ))
