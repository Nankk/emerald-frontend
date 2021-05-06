(ns emerald-frontend.style.global
  (:require
   [garden.core :as g]
   [garden.stylesheet :refer [at-keyframes]]
   [emerald-frontend.style.colors :as c]
   [emerald-frontend.util.css :refer [vw]]
   [clojure.string :as str]))

(defn nonlinear-sizes
  "Returns non-linearly increasing font size set.

  | n         | Curvature (1 = linear)             |
  | min       | Minimum font size (relative to vw) |
  | max       | Maximum font size (relative to vw) |
  | max-steps | Total numbers of font sizes        |
  "
  [n min max max-steps]
  (let [steps (range 1 (inc max-steps))]
    (for [i steps]
      [i
       (+ min
          (* (Math/pow (/ (- i (first steps)) (- (last steps) (first steps))) n)
             (- max min)))])))

(defn outermost []
  [[:html {:height    "100%"
           :width     "100%"
           :font-size (vw 4.5)
           }]
   [:body {:height           "100%"
           :width            "100%"
           :background-color (c/base 1)
           :color            (c/base 7)}]
   [:#app {:height           "100%"
           :width            "100%"
           :background-color (c/base 1)}]])

(def nes-container-dark
  {:border-style "solid"
   :border-width (vw 0.3)
   :border-color "white"
   :margin  "0px"
   :padding (vw 1)
   :color "#ffffff"
   :background-color "#212529"
   :border-image-slice 3
   :border-image-width 3
   :border-image-repeat "stretch"
   :border-image-source "url('data:image/svg+xml;utf8,<?xml version=\"1.0\" encoding=\"UTF-8\" ?><svg version=\"1.1\" width=\"8\" height=\"8\" xmlns=\"http://www.w3.org/2000/svg\"><path d=\"M3 1 h1 v1 h-1 z M4 1 h1 v1 h-1 z M2 2 h1 v1 h-1 z M5 2 h1 v1 h-1 z M1 3 h1 v1 h-1 z M6 3 h1 v1 h-1 z M1 4 h1 v1 h-1 z M6 4 h1 v1 h-1 z M2 5 h1 v1 h-1 z M5 5 h1 v1 h-1 z M3 6 h1 v1 h-1 z M4 6 h1 v1 h-1 z\" fill=\"rgb(255,255,255)\" /></svg>')"
   :border-image-outset 0})

(defn containers []
  [[[:.base-container nes-container-dark]
    [:.base-container-full (merge nes-container-dark {:width "100%" :height "100%"})]]
   ;; hstack/vstack (shorthand for flex-row/column considering alignment & justification)
   ;; e.g.) .hstack-hcenter-vcenter .hstack-vcenter-justify .vstack-justify
   (let [align-center   {:align-items "center"}
         justify        {:justify-content "space-between"}
         justify-center {:justify-content "center"}]
     (for [dir  ["h" "v"]
           :let [opposite (if (= dir "h") "v" "h")
                 base     {:display        "flex"
                           :flex-direction (if (= dir "h") "row" "column")}]]
       (for [[aabr astyle] [[nil {}]
                            [(str opposite "center") align-center]]
             [jabr jstyle] [[nil {}]
                            ["justify" justify]
                            [(str dir "center") justify-center]]
             :let          [style (merge base astyle jstyle)]]
         [[(keyword (str "." dir "stack" (when aabr (str "-" aabr)) (when jabr (str "-" jabr))))
           style]
          (when (and aabr jabr) ; Allow swapped writing
            [(keyword (str "." dir "stack" (when jabr (str "-" jabr)) (when aabr (str "-" aabr))))
             style])])))])

(defn font-size []
  ;;  h1-h5 & nonlinear-sizes
  [(for [[lv relwidth] (nonlinear-sizes 1.3 1 5 10)]
     [(keyword (str ".font-size-" lv)) {:font-size (vw relwidth)}])
   [:p  {:font-size (vw 2.0)}]])

(defn margins-and-paddings []
  ;; Margins & paddings override
  (for [m-or-p           ["m" "p"]
        [acro postfixes] [["t" ["-top"]]
                          ["l" ["-left"]]
                          ["r" ["-right"]]
                          ["b" ["-bottom"]]
                          ["" [""]]
                          ["x" ["-left" "-right"]]
                          ["y" ["-top" "-bottom"]]]
        [lv relwidth]    (nonlinear-sizes 1.3 0.1 5 10)]
    [(keyword (str "." m-or-p acro "-" lv))
     (for [postfix postfixes]
       {(keyword (str (if (= m-or-p "m")
                        "margin"
                        "padding") postfix)) (str (vw relwidth) " !important")})]))

(defn width-and-height []
  ;; Width & height override
  (for [i (range 5 101 5)]
    [[(keyword (str ".vw-" i)) {:width (str (vw i) " !important")}]
     [(keyword (str ".vh-" i)) {:height (str (vw i) " !important")}]]))

(defn nes-override []
  [[:.nes-container.is-rounded {:margin  "0px"
                                :padding (vw 1)}]
   [:label {:margin "0px"}]
   [:input :.nes-input :.is-dark {:padding (str "0 " (vw 1))}]
   [:.nes-btn {:font-size (vw 2.0)
               :vertical-align "middle"
               :text-align "center"
               :height "auto"
               :padding (str (vw 0.5) (vw 0.8))}]
   [:.nes-btn.is-small {:font-size (vw 1.5)
                        :vertical-align "middle"
                        :text-align "center"
                        :height "auto"
                        :padding (str (vw 0.3) (vw 0.5))}]])

(defn auxiliaries []
  [[:hr {:width            "100%"
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
   [:textarea.is-dark {:resize "none"
                       :background-color "#212529"
                       :color "#FFFFFF"}]])

(defn animations []
  [;; Bounce
   (vec (for [[name amplitude] [["bounce" 0.7]
                                ["bounce-f" 1.5]
                                ["bounce-ff" 3]
                                ["bounce-p" 0.4]
                                ["bounce-pp" 0.2]]]
          [[(keyword (str "." name)) {:animation (str name " 1s infinite")}]
           (at-keyframes
            name
            [:0% {:transform                 (str "translateY(" (vw (- (/ amplitude 2))) ")")
                  :animation-timing-function "ease-in"}]
            [:50% {:transform                 (str "translateY(" (vw (/ amplitude 2)) ")")
                   :animation-timing-function "ease-out"}]
            [:100% {:transform (str "translateY(" (vw (- (/ amplitude 2))) ")")}])]))
   ;; Transition
   (for [[dir from to] [["up" 56 0]
                        ["down" -56 0]
                        ["right" -100 0]
                        ["left" 100 0]]
         type          ["in" "out"]
         :let          [name (str dir "-" type)
                        xy   (if (or (= dir "up") (= dir "down")) "Y" "X")]]
     [[(keyword (str "." name)) {:visibility (if (= type "out") "hidden" "visible")
                                 :animation  (str name " 0.5s linear 0s")}]
      (at-keyframes
       name
       [:0% {:visibility "visible"
             :transform
             (str "translate" xy "(" (if (= type "in") from to) "vw)")}]
       [:100% {:transform
               (str "translate" xy "(" (if (= type "in") to (- from)) "vw)")}])])
   ;; Fade in & out
   (for [[speed time] [["-fastest" 0.15]
                       ["-faster" 0.25]
                       ["-fast" 0.5]
                       ["" 1.0]
                       ["-slow" 1.5]
                       ["-slower" 2.0]
                       ["-slowest" 3.0]]]
     [[(keyword (str ".fade-in" speed)) {:animation (str "fade-in " time "s linear")}]
      [(keyword (str ".fade-out" speed)) {:opacity   0
                                          :transform (str "translateY(" (vw 56) ")")
                                          :animation (str "fade-out " time "s linear")}]])
   (at-keyframes "fade-in"
                 [:0% {:opacity 0}]
                 [:100% {:opacity 100}])
   (at-keyframes "fade-out"
                 ;; What a miserable, horrifying and NASTY implementation
                 [:0% {:opacity 100 :transform "translateY(0px)"}]
                 [:100% {:opacity 0 :transform "translateY(0px)"}])])

(defn css []
  (g/css
   (outermost)
   (containers)
   (font-size)
   (margins-and-paddings)
   (width-and-height)
   (auxiliaries)
   (nes-override)
   (animations)))
