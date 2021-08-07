(ns emerald-frontend.util.css
  (:require [emerald-frontend.subs :as subs]
            [re-frame.core :as rf]))

(def aspect-ratio [16 9])

(defn vw
  "Returns css text specifying the correct relative fraction considering current container size."
  [percent]
  (str "calc(" (/ percent 100) " * min(100vw, 100vh * " (aspect-ratio 0) " / " (aspect-ratio 1) "))"))

(defn px->vw
  "Convert fixed pixel to relative fraction to the app's viewport width."
  [px]
  (let [w    @(rf/subscribe [::subs/panel-width])
        h    @(rf/subscribe [::subs/panel-height])
        frac (/ px (min w (/ (* h (aspect-ratio 0)) (aspect-ratio 1))))]
    (str "calc(" frac " * min(100vw, 100vh * " (aspect-ratio 0) " / " (aspect-ratio 1) "))")))
