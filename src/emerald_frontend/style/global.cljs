(ns emerald-frontend.style.global
  (:require
   [garden.core :as g]
   [garden.stylesheet :refer [at-keyframes]]
   [emerald-frontend.style.colors :as c]
   [emerald-frontend.util.css :refer [vw]]))

(defn css []
  (g/css
   [:html {:height    "100%"
           :width     "100%"
           :font-size (vw 4.5)
           }]
   [:body {:height           "100%"
           :width            "100%"
           :font-family      "PixelMplus10"
           :background-color (c/base 1)
           :color            (c/base 7)}]
   [:#app {:height           "100%"
           :width            "100%"
           :background-color (c/base 1)}]

   ;;  h1-h5 & font-sizes
   (for [[lv relwidth] {5 4.0, 4 3.0, 3 2.5, 2 2.0, 1 1.0}]
     [(keyword (str ".font-size-" lv)) {:font-size (vw relwidth)}])
   [:p  {:font-size (vw 2.0)}]

   ;; Margins override
   (for [[acro postfixes] [["t" ["-top"]
                            "l" ["-left"]
                            "r" ["-right"]
                            "b" ["-bottom"]
                            "" [""]
                            "x" ["-left" "-right"]
                            "y" ["-top" "-bottom"]]]
         [lv relwidth] {5 4.0, 4 3.0, 3 2.5, 2 2.0, 1 1.0, 0 0}]
     [(keyword (str "m" acro "-" lv))
      (for [postfix postfixes]
        {(keyword (str "margin" postfix)) (vw relwidth)})])

   ;; Aux
   [:hr {:width            "100%"
         :height           "0.1vw"
         :background-color "#ffffff"
         :margin           "0"
         }]
   [:.hidden-input {:width    "0.1px"
                    :height   "0.1px"
                    :opacity  "0"
                    :overflow "hidden"
                    :position "absolute"
                    :z-index  "-1"}]
   [:.profile-image {:object-fit "contain"
                     ;; :image-rendering "pixelated"
                     }]
   [:.pixelated {:image-rendering "pixelated"}]
   [:.screen-image {:object-fit       "contain"
                    :background-color "#000000"
                    :width            "100%"
                    :height           "100%"}]

   ;; For nes components
   [:.nes-container.is-rounded {:margin  "0px"
                                :padding "1vw"}]
   [:label {:margin "0px"}]
   [:input :.nes-input :.is-dark {:padding "0vw 1vw"}]

   ;;; Animations
   ;; Bounce
   (vec (for [[name amplitude] [["bounce" 0.7]
                                ["bounce-f" 1.5]
                                ["bounce-ff" 3]
                                ["bounce-p" 0.4]
                                ["bounce-pp" 0.2]]]
          [[(keyword (str "." name)) {:animation (str name " 1s infinite")}]
           (at-keyframes
            name
            [:0% {:transform       (str "translateY(" (vw (- (/ amplitude 2))) ")")
                  :timing-function "ease-in"}]
            [:50% {:transform       (str "translateY(" (vw (/ amplitude 2)) ")")
                   :timing-function "ease-out"}]
            [:100% {:transform (str "translateY(" (vw (- (/ amplitude 2))) ")")}])]))
   ;; Transition
   (for [[dir from to]  [["up" 56 0]
                         ["down" -56 0]
                         ["right" -100 0]
                         ["left" 100 0]]
         type ["in" "out"]
         :let [name (str dir "-" type)
               xy   (if (or (= dir "up") (= dir "down")) "Y" "X")]]
     [[(keyword (str "." name)) {:visibility (if (= type "out") "hidden" "visible")
                                 :animation (str name " 0.5s linear 0s")}]
      (at-keyframes
       name
       [:0% {:visibility "visible"
             :transform
             (str "translate" xy "(" (if (= type "in") from to) "vw)")}]
       [:100% {:transform
               (str "translate" xy "(" (if (= type "in") to (- from)) "vw)")}])])
   ;; Fade in & out
   (for [[speed time] [["-fast" 0.5] ["" 1.0] ["-slow" 1.5]]]
     [[(keyword (str ".fade-in" speed)) {:animation (str "fade-in " time "s linear")}]
      [(keyword (str ".fade-out" speed)) {:opacity 0
                                          :transform (str "translateY(" (vw 56) ")")
                                          :animation (str "fade-out " time "s linear")}]])
   (at-keyframes "fade-in"
                 [:0% {:opacity 0}]
                 [:100% {:opacity 100}])
   (at-keyframes "fade-out"
                 ;; What a miserable, horrifying and NASTY implementation
                 [:0% {:opacity 100 :transform "translateY(0px)"}]
                 [:100% {:opacity 0 :transform "translateY(0px)"}])
   ))
