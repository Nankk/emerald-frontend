(ns emerald-frontend.style.character-status
  (:require [garden.core :as g]
            [emerald-frontend.util.css :refer [vw]]
            ))

(def ^:private side-width (vw 6))

(defn css []
  (g/css
   [:#character-status-main
    [:.grid-container
     {:display "grid",
      :grid-template-columns (str side-width " 1fr 1.5fr " side-width),
      :grid-template-rows "1fr 1fr 1fr",
      :gap "0px 0px",
      :grid-template-areas (pr-str "aux-left  status parameters aux-right "
                                   "aux-left personality package aux-right"
                                   "aux-left equipments languages aux-right")}]
    [:.status {:grid-area "status"}]
    [:.personality {:grid-area "personality"}]
    [:.equipments {:grid-area "equipments"}]
    [:.parameters {:grid-area "parameters"}]
    [:.package {:grid-area "package"}]
    [:.languages {:grid-area "languages"}]
    [:.aux-left {:grid-area "aux-left"}]
    [:.aux-right {:grid-area "aux-right"}]]
   [:#character-status-skills
    [:.grid-container
     {:display "grid",
      :grid-template-columns (str side-width " 1fr " side-width),
      :grid-template-rows "1fr 1fr",
      :gap "0px 0px",
      :grid-template-areas (pr-str "aux-left abilities aux-right"
                                   "aux-left skills aux-right")}]
    [:.abilities {:grid-area "abilities"}]
    [:.skills {:grid-area "skills"}]
    [:.aux-left {:grid-area "aux-left"}]
    [:.aux-right {:grid-area "aux-right"}]]))
