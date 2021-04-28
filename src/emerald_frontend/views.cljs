(ns emerald-frontend.views
  (:require [emerald-frontend.components.container :as container]
            [emerald-frontend.panels.home :as home]
            [emerald-frontend.subs :as subs]
            [emerald-frontend.panels.session.player-home :as player]
            [emerald-frontend.util.css :refer [vw]]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

(defn main-panel []
  [:div.d-flex.flex-row.align-items-center.justify-content-center.w-100.h-100
   [:div#main-panel {:style
                     {:width    (str @(rf/subscribe [::subs/panel-width]) "px")
                      :height   (str @(rf/subscribe [::subs/panel-height]) "px")
                      :position "relative"}}
    (case @(rf/subscribe [::subs/cur-panel])
      :home           [home/main]
      :session-player [player/main])
    (when @(rf/subscribe [::subs/loading?])
      [:div.w-100.h-100.d-flex.flex-column.align-items-center.justify-content-center
       {:style {:position         "absolute"
                :background-color "rgba(0, 0, 0, 0.5)"}}
       [:img.pixelated {:src "img/sandglass.gif"
                        :style {:width (vw 5) :height (vw 5)}}]
       [:div {:style {:height (vw 5)}}]])]])
