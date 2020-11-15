(ns emerald-frontend.views
  (:require [emerald-frontend.components.panel :as panel]
            [emerald-frontend.subs :as subs]
            [re-frame.core :as rf]))

(defn main-panel []
  [:div.d-flex.flex-row.align-items-center.justify-content-center
   {:style {:width "100%" :height "100%"}}
   [:div#main-panel {:style
                     {:width  (str @(rf/subscribe [::subs/panel-width]) "px")
                      :height (str @(rf/subscribe [::subs/panel-height]) "px")}}
    [:h1 "This is h1."]
    [:h2 "This is h2."]
    [:h3 "This is h3."]
    [:h4 "This is h4."]
    [:h5 "This is h5."]
    [:button.nes-btn.is-primary
     [:h1 "ぼうけんをはじめろ"]]
    [panel/panel
     :style {:background-color "#004499"}
     :children [[:div "aaa"] [:div "aa2"]]]
    [panel/panel
     :children ["bbb"]]]])
