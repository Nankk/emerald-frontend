(ns emerald-frontend.views
  (:require [emerald-frontend.components.container :as container]
            [emerald-frontend.panels.home :as home]
            [emerald-frontend.subs :as subs]
            [emerald-frontend.panels.session.player-home :as player]
            [emerald-frontend.util.css :refer [vw]]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

(defn loading []
  (let [loading? @(rf/subscribe [::subs/loading?])]
    [:div.w-100.h-100.d-flex.flex-column.align-items-center.justify-content-center.position-absolute
     {:class (when-not loading? "fade-out-fast")
      :style {:z-index          999
              :top              0
              :left             0
              :background-color "rgba(0, 0, 0, 0.7)"}}
     [:div.d-flex.flex-row.align-items-end.pb-5
      [:img.pixelated.mr-4 {:src   "img/sandglass.gif"
                            :style {:width (vw 7) :height (vw 7)}}]
      [:div "Now loading..."]]
     (let [content @(rf/subscribe [::subs/loading-message])]
       (cond
         (string? content) [:div.font-size-4.w-50.text-wrap.text-center content]
         (vector? content) content
         :else             [:div content]))
     [:div {:style {:height (vw 5)}}]]))

(defn main-panel []
  [:div.d-flex.flex-row.align-items-center.justify-content-center.w-100.h-100
   [:div#main-panel.position-relative
    {:style
     {:width  (str @(rf/subscribe [::subs/panel-width]) "px")
      :height (str @(rf/subscribe [::subs/panel-height]) "px")}}
    (case @(rf/subscribe [::subs/cur-panel])
      :home           [home/main]
      :session-player [player/main])
    [loading]]])
