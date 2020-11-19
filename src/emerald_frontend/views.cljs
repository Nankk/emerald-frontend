(ns emerald-frontend.views
  (:require [emerald-frontend.components.container :as container]
            [emerald-frontend.components.list :as list]
            [emerald-frontend.components.panel :as panel]
            [emerald-frontend.subs :as subs]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

(defn- modal-test []
  [:div
   [:div "ほにゃらら"]
   [:button.nes-btn.is-primary {:on-click #(panel/show-modal :make-your-choice)}
    "いでよ モーダル!"]
   [panel/modal :make-your-choice [10 10]
    [:div.nes-container.is-dark.is-rounded
     [list/choice
      :header [:h4 "ゴキブリ が あらわれた!"]
      :choices [{:text "たたかう" :on-click #(panel/show-modal :confirm)}
                {:text "にげる" :on-click #(panel/show-modal :confirm)}]]]
    [panel/modal :confirm ["20vw" "20vw"]
     [:div.nes-container.is-dark.is-rounded
      [list/choice
       :header [:h4 "よろしいですか?"]
       :choices [{:text "はい" :on-click #(container/switch-panels :panel2 :panel3 :left)}
                 {:text "いいえ" :on-click #(panel/close-modal :confirm)}]]]]]])

(defn- panel1 []
  [:div
   [:div "Konichiwa."]
   [:button.nes-btn.is-primary {:on-click #(container/switch-panels :panel1 :panel2 :up)}
    "▼"]])

(defn- panel2 []
  [:div.w-100.h-100
   [:button.nes-btn.is-primary {:on-click #(container/switch-panels :panel2 :panel1 :down)}
    "▲"]
   [:button.nes-btn.is-primary {:on-click #(container/switch-panels :panel2 :panel3 :left)}
    "▶"]
   [:h3 "Howdy."]
   [modal-test]])

(defn- panel3 []
  [:div.nes-container.is-dark.is-rounded
   [:h2 "Konyanyachiwa."]
   [:button.nes-btn.is-primary {:on-click #(container/switch-panels :panel3 :panel2 :right)}
    "◀"]
   [:button.nes-btn.is-primary {:on-click #(container/switch-panels :panel3 :panel1 :down)}
    "▲"]])

(defn- tp-test []
  (container/transient-container
   {:panel1 [panel1]
    :panel2 [panel2]
    :panel3 [panel3]}))

(defn- showcase []
  [:div
   [:h1 "This is h1."]
   [:h2 "This is h2."]
   [:h3 "This is h3."]
   [:h4 "This is h4."]
   [:h5 "This is h5."]
   [:button.nes-btn.is-primary
    [:h1 "ぼうけんをはじめろ"]]
   [panel/test-panel
    :style {:background-color "#004499"}
    :children [[:div "aaa"] [:div "aa2"]]]
   [panel/test-panel
    :children ["bbb"]]])

(defn main-panel []
  [:div.d-flex.flex-row.align-items-center.justify-content-center
   {:style {:width "100%" :height "100%"}}
   [:div#main-panel {:style
                     {:width  (str @(rf/subscribe [::subs/panel-width]) "px")
                      :height (str @(rf/subscribe [::subs/panel-height]) "px")}}
    [tp-test]]])
