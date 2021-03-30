(ns emerald-frontend.style.global
  (:require
   [garden.core :as g]
   [emerald-frontend.style.colors :as c]
   [emerald-frontend.util.css :refer [vw]]))

(defn css []
  (g/css
   [:html {:height "100%"
           :width "100%"
           :font-size (vw 4.5)
           }]
   [:body {:height "100%"
           :width "100%"
           :font-family "PixelMplus10"
           :background-color (c/base 1)
           :color (c/base 7)}]
   [:#app {:height "100%"
           :width "100%"
           :background-color (c/base 1)}]

   [:h1 {:font-size (vw 4.0)
         :margin "3vw"}]
   [:h2 {:font-size (vw 3.0)
         :margin "0.08rem"}]
   [:h3 {:font-size (vw 2.5)
         :margin "0.07rem"}]
   [:h4 {:font-size (vw 2.0)
         :margin "0.06rem"}]
   [:h5 {:font-size (vw 1.5)
         :margin "0.5vw"}]
   [:h6 {:font-size (vw 1.0)
         :margin "0.5vw"}]
   [:p  {:font-size (vw 2.0)}]

   ;; Aux
   [:hr {:width "100%"
         :height "0.1vw"
         :background-color "#ffffff"
         :margin "0"
         }]
   [:.hidden-input {:width    "0.1px"
                    :height   "0.1px"
                    :opacity  "0"
                    :overflow "hidden"
                    :position "absolute"
                    :z-index  "-1"}]
   [:.profile-image {:style {:object-fit "contain"}
                     ;; :image-rendering "pixelated"
                     }]

   ;; For nes components
   [:.nes-container.is-rounded {:margin "0px"
                                :padding "1vw"}]
   [:label {:margin "0px"}]
   [:input :.nes-input :.is-dark {:padding "0vw 1vw"}]
   ))
