(ns emerald-frontend.style.session.items
  (:require [garden.core :as g]
            [emerald-frontend.style.colors :as c]
            [emerald-frontend.util.css :refer [vw]]))

(defn css []
  (g/css
   [:#player-items
    [:.grid-container
     {:display               "grid"
      :grid-template-columns "1fr"
      :grid-template-rows    "7.3fr 2.7fr"
      :gap                   "0px 0px"
      :grid-template-areas   (pr-str "items"
                                     "description")}]
    [:.items {:grid-area "items"}]
    [:.description {:grid-area "description"}]]))
