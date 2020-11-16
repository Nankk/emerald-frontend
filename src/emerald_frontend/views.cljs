(ns emerald-frontend.views
  (:require [emerald-frontend.components.core :as comp]
            [emerald-frontend.subs :as subs]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

(def ratom (reagent/atom 10))

(defn- r-test []
  (let [num @ratom]
    [:button.nes-btn.is-primary
     {:on-click #(swap! ratom inc)}
     @ratom]))

(defn- panel1 []
  [:div
   [:div "Konichiwa."]
   [:button.nes-btn.is-primary {:on-click #(comp/switch-panels :panel1 :panel2 :up)}
    "▼"]])

(defn- panel2 []
  [:div
   [:div "Howdy."]
   [:button.nes-btn.is-primary {:on-click #(comp/switch-panels :panel2 :panel1 :down)}
    "▲"]])

(defn- tp-test []
  (comp/transient-container
   {:panel1 [panel1]
    :panel2 [panel2]}))

(defn- test-showcase []
  [:div
   [:h1 "This is h1."]
   [:h2 "This is h2."]
   [:h3 "This is h3."]
   [:h4 "This is h4."]
   [:h5 "This is h5."]
   [:button.nes-btn.is-primary
    [:h1 "ぼうけんをはじめろ"]]
   [comp/test-panel
    :style {:background-color "#004499"}
    :children [[:div "aaa"] [:div "aa2"]]]
   [comp/test-panel
    :children ["bbb"]]])

(defn main-panel []
  [:div.d-flex.flex-row.align-items-center.justify-content-center
   {:style {:width "100%" :height "100%"}}
   [:div#main-panel {:style
                     {:width  (str @(rf/subscribe [::subs/panel-width]) "px")
                      :height (str @(rf/subscribe [::subs/panel-height]) "px")}}
    [tp-test]]])
