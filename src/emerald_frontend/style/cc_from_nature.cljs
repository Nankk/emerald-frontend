(ns emerald-frontend.style.cc-from-nature
  (:require [garden.core :as g]))

(defn css []
  (g/css
   [:#cc-nature-species
    [:.grid-container {:display "grid"
                       :grid-template-columns "0.5fr 1fr 1fr 1fr 0.5fr"
                       :grid-template-rows "0.3fr 1.7fr"
                       :gap "0.2rem 0.2rem"
                       :grid-template-areas
                       (pr-str "aux-left title title title aux-right"
                               "button-left panel-left panel-center panel-right button-right")}]
    [:.title {:grid-area "title"}]
    [:.panel-left {:grid-area "panel-left"}]
    [:.panel-center {:grid-area "panel-center"}]
    [:.panel-right {:grid-area "panel-right"}]
    [:.button-left {:grid-area "button-left"}]
    [:.button-right {:grid-area "button-right"}]
    [:.aux-left {:grid-area "aux-left"}]
    [:.aux-right {:grid-area "aux-right"}]]
   [:#cc-nature-role
    [:.grid-container {:display "grid"
                       :grid-template-columns "0.5fr 3fr 0.5fr"
                       :grid-template-rows "0.3fr 1fr 0.7fr"
                       :gap "0.2rem 0.2rem"
                       :grid-template-areas
                       (pr-str "aux-left title ."
                               ". icons ."
                               ". description aux-right")}]
    [:.icons {:grid-area "icons"}]
    [:.aux-left {:grid-area "aux-left"}]
    [:.description {:grid-area "description"}]
    [:.aux-right {:grid-area "aux-right"}]
    [:.title {:grid-area "title"}]]

   [:.grid-container {:display "grid"
                      :grid-template-columns "0.5fr 1fr 2fr 0.5fr"
                      :grid-template-rows "0.3fr 0.8fr 1.2fr"
                      :gap "0.2rem 0.2rem"
                      :grid-template-areas
                      (pr-str "aux-left title title ."
                              ". selector experience-list ."
                              ". image experience-list .")}]
   [:.aux-left {:grid-area "aux-left"}]
   [:.title {:grid-area "title"}]
   [:.experience-list {:grid-area "experience-list"}]
   [:.selector {:grid-area "selector"}]
   [:.image {:grid-area "image"}]
   ))
