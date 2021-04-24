(ns emerald-frontend.style.cc-from-nature
  (:require [garden.core :as g]))

(defn css []
  (g/css
   [:#cc-nature-species
    [:.grid-container {:display               "grid"
                       :grid-template-columns "minmax(0, 0.5fr) minmax(0, 1fr) minmax(0, 1fr) minmax(0, 1fr) minmax(0, 0.5fr)"
                       :grid-template-rows    "minmax(0, 0.3fr) minmax(0, 1.7fr)"
                       :gap                   "0.2rem 0.2rem"
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
   [:#cc-nature-job
    [:.grid-container {:display               "grid"
                       :grid-template-columns "minmax(0, 0.5fr) minmax(0, 3fr) minmax(0, 0.5fr)"
                       :grid-template-rows    "minmax(0, 0.3fr) minmax(0, 1fr) minmax(0, 0.7fr)"
                       :gap                   "0.2rem 0.2rem"
                       :grid-template-areas
                       (pr-str "aux-left title ."
                               ". icons ."
                               ". description aux-right")}]
    [:.icons {:grid-area "icons"}]
    [:.aux-left {:grid-area "aux-left"}]
    [:.description {:grid-area "description"}]
    [:.aux-right {:grid-area "aux-right"}]
    [:.title {:grid-area "title"}]]

   [:#cc-nature-born
    :#cc-nature-infancy
    :#cc-nature-childhood
    :#cc-nature-adolescence
    :#cc-nature-happening
    :#cc-nature-reason
    [:.grid-container {:display               "grid"
                       :grid-template-columns "minmax(0, 0.5fr) minmax(0, 1fr) minmax(0, 2fr) minmax(0, 0.5fr)"
                       :grid-template-rows    "minmax(0, 0.3fr) minmax(0, 0.8fr) minmax(0, 1.2fr)"
                       :gap                   "0.2rem 0.2rem"
                       :grid-template-areas
                       (pr-str "aux-left title title ."
                               ". selector experience-list ."
                               ". image experience-list .")}]
    [:.aux-left {:grid-area "aux-left"}]
    [:.title {:grid-area "title"}]
    [:.experience-list {:grid-area "experience-list"}]
    [:.selector {:grid-area "selector"}]
    [:.image {:grid-area "image"}]]
   [:#cc-nature-name
    [:.grid-container {:display               "grid"
                       :grid-template-columns "minmax(0, 0.5fr) minmax(0, 3fr) minmax(0, 0.5fr)"
                       :grid-template-rows    "minmax(0, 0.3fr) minmax(0, 1.4fr) minmax(0, 0.3fr)"
                       :gap                   "0.2rem 0.2rem"
                       :grid-template-areas
                       (pr-str "aux-left title ."
                               ". main ."
                               ". . .")}]

    [:.main {:grid-area "main"}]
    [:.title {:grid-area "title"}]
    [:.aux-left {:grid-area "aux-left"}]]
   [:#cc-nature-high-lv
    [:.grid-container {:display               "grid"
                       :grid-template-columns "minmax(0, 0.5fr) minmax(0, 3fr) minmax(0, 0.5fr)"
                       :grid-template-rows    "minmax(0, 0.3fr) minmax(0, 1.4fr) minmax(0, 0.3fr)"
                       :gap                   "0.2rem 0.2rem"
                       :grid-template-areas
                       (pr-str "aux-left title ."
                               ". main ."
                               ". main aux-right")}]

    [:.main {:grid-area "main"}]
    [:.title {:grid-area "title"}]
    [:.aux-left {:grid-area "aux-left"}]
    [:.aux-right {:grid-area "aux-right"}]]
   [:#cc-nature-confirmation
    [:.grid-container {:display               "grid"
                       :grid-template-columns "minmax(0, 0.5fr) minmax(0, 3fr) minmax(0, 0.5fr)"
                       :grid-template-rows    "minmax(0, 0.3fr) minmax(0, 1.7fr)"
                       :gap                   "0.2rem 0.2rem"
                       :grid-template-areas
                       (pr-str ". title ."
                               ". main .")}]

    [:.main {:grid-area "main"}]
    [:.title {:grid-area "title"}]]
   [:.inner-grid-container {:display               "grid"
                            :grid-template-columns "minmax(0, 7fr) minmax(0, 3fr)"
                            :grid-template-rows    "minmax(0, 4fr) minmax(0, 5fr)"
                            :gap                   "0.2rem 0.2rem"
                            :grid-template-areas
                            (pr-str "params biography"
                                    "skills biography")}]
   [:.params {:grid-area "params"}]
   [:.biography {:grid-area  "biography"}]
   [:.skills {:grid-area "skills"}]
   ))
