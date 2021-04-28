(ns emerald-frontend.style.session.growth
  (:require [garden.core :as g]
            [emerald-frontend.style.colors :as c]
            [emerald-frontend.util.css :refer [vw]]))

(defn css []
  (g/css
   [:#growth
    [:.grid-container
     {:display "grid"
      :grid-template-columns "1fr"
      :grid-template-rows "3fr 8fr"
      :gap (str (vw 1) " " (vw 1))
      :grid-template-areas (pr-str "upper"
                                   "lower")}]
    [:.upper {:grid-area "upper"}]
    [:.lower {:grid-area "lower"}]
    ]))
