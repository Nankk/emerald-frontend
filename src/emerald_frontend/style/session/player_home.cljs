(ns emerald-frontend.style.session.player-home
  (:require [garden.core :as g]
            [emerald-frontend.style.colors :as c]
            [emerald-frontend.util.css :refer [vw]]))

(defn css []
  (g/css
   [:#player-home
    [:.grid-container
     {:display "grid"
      :grid-template-columns "2.5fr 2.9fr 1.6fr"
      :grid-template-rows "1fr 3.5fr 1.5fr"
      :gap (str (vw 1) (vw 1))
      :grid-template-areas (pr-str "screen screen title"
                                   "screen screen command"
                                   "status-summary notes notes")}]
    [:.screen {:grid-area "screen"}]
    [:.status-summary {:grid-area "status-summary"}]
    [:.notes {:grid-area "notes"}]
    [:.command {:grid-area "command"}]
    [:.title {:grid-area "title"}]
    ]))
