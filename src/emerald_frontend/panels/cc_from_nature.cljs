(ns emerald-frontend.panels.cc-from-nature
  (:require [cljs.core.async :refer [go <!]]
            [emerald-frontend.components.container :as container]
            [emerald-frontend.components.list :as list]
            [emerald-frontend.const :as const]
            [emerald-frontend.events :as events]
            [emerald-frontend.macros.async :refer [defan defan-]]
            [emerald-frontend.util.http :as http]
            [re-frame.core :as rf]
            [reagent.core :as reagent]
            [emerald-frontend.components.panel :as panel]))

;;; Born ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce *cur-born (reagent/atom nil))
(defonce *born-data (reagent/atom {}))

(defan- <load-born []
  (println "<load-born")
  (let [res (<! (http/<get-data "/cbb/born"))]
    (reset! *born-data (res :body))
    true))

(defn- detail []
  (when-let [b @*cur-born]
    [:div.nes-container.is-dark.is-rounded {:style {:width "70vw" :height "30vw"}}
     [:h3 (b :name)]
     [:h3 (b :description)]
     [:hr]
     [list/choice
      :header [:h3 "これでいいですか？"]
      :choices [{:text "はい" :on-click #()}
                {:text "いいえ" :on-click #(panel/close-modal :born-detail)}]
      :direction "horizontal"]]))

(defn- experience-list []
  [:div.nes-container.is-dark.is-rounded.experience-list
   {:style {:width "50vw" :height "40vw"}}
   [:div.overflow-auto.w-100.h-100
    [list/choice
     :choices (into []
                    (for [b (@*born-data :人間)] ^{:key (b :name)}
                      {:text (b :name) :on-click (fn []
                                                   (reset! *cur-born b)
                                                   (panel/show-modal :born-detail))}))]]])

(defn- random-born []
  (let [borns (@*born-data :人間)]
    (borns (rand-int (count borns)))))

(defn born-main []
  (<load-born)
  [:div#cc-nature-born.w-100.h-100.p-1 {:style {:position "relative"}}
   [:div.grid-container.w-100.h-100
    ;; Header
    [:button.nes-btn.aux-left {:on-click #(container/switch-panels :cc-nature-born :cc-nature-role :right)} [:h3 "◁"]]
    [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "生まれを決定してください"]]
    ;; Body
    [:div.nes-container.is-dark.is-rounded.w-100.h-100.selector
     [list/choice
      :choices [{:text "ランダム" :on-click (fn []
                                              (reset! *cur-born (random-born))
                                              (panel/show-modal :born-detail))}
                {:text "自分で選ぶ" :on-click #(panel/show-modal :born-experience-list)}]]]
    [panel/modal :born-experience-list ["15vw" "35vw"]
     [experience-list]]]
   [panel/modal :born-detail ["20vw" "15vw"]
    [detail]]
   ])

;;; Role ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce ^:private *cur-role (reagent/atom nil))

(defn- role-icon [role]
  [:button {:style {:height "10vw" :width "10vw" :background-color "#bbbbbb" :margin "1vw" :color "#222222"}
            :on-click (fn []
                        (rf/dispatch-sync [::events/set-character-param [:type] (role :name)])
                        (reset! *cur-role role))}
   [:h3 (role :name)]])

(defn role-main []
  [:div#cc-nature-role.w-100.h-100.p-1
   [:div.grid-container.w-100.h-100
    ;; Header
    [:button.nes-btn.aux-left {:on-click #(container/switch-panels :cc-nature-role :cc-nature-species :right)} [:h3 "◁"]]
    [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "職業を選んでください"]]
    ;; Icons
    (into [:div.d-flex.flex-row.flex-wrap.align-items-center.justify-content-center.w-100.h-100.icons]
          (for [role const/roles] ^{:key (role :name)} [role-icon role]))
    ;; Description
    [:div.nes-container.is-dark.is-rounded.w-100.h-100.description
     (when-let [cr @*cur-role] [:h4 (cr :description)])]
    ;; Button
    [:div.nes-btn.is-primary.aux-right {:style {:height "7vw"}
                                        :on-click #(container/switch-panels :cc-nature-role :cc-nature-born :left)}
     [:h3 "決定"]]
    ]])

;;; Species ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private cols-count 3)
(defonce ^:private *head-idx (reagent/atom 0))
(def ^:private idx->pos-class
  ["panel-left" "panel-center" "panel-right"])

(defn- shift-head-idx [cur diff]
  (let [len      (count const/species)
        next-raw (+ cur diff)]
    (cond
      (< next-raw 0)    (* (Math/floor (/ (dec len) cols-count)) cols-count)
      (>= next-raw len) 0
      :else             next-raw)))

(defn- species-panel [idx species]
  ^{:key idx}
  [:button.nes-container.is-dark.is-rounded.w-100.h-100
   {:class (idx->pos-class idx)
    :on-click (fn []
                (rf/dispatch-sync [::events/set-character-param [:species] (species :name)])
                (container/switch-panels :cc-nature-species :cc-nature-role :left))}
   [:div.h-25 [:h4 (species :name)]]
   [:hr]
   [:div.h-25 [:h4 (species :feature)]]
   [:hr]
   [:div.h-25 [:h4 (species :description)]]])

(defn species-main []
  [:div#cc-nature-species.w-100.h-100.p-1
   [:div.grid-container.w-100.h-100
    ;; Header
    [:button.nes-btn.aux-left {:on-click #(container/switch-panels :cc-nature-species :home :down)} [:h3 "△"]]
    [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "種族を選んでください"]]
    [:button.nes-btn.is-primary.aux-right {:on-click #(container/switch-panels :cc-from-nature :home :down)} [:h3 "♂"]]
    ;; Arrows
    [:div.d-flex.flex-row.align-items-center.justify-content-center.w-100.h-100.button-left
     [:button.nes-container.is-dark.is-rounded.text-center
      {:on-click (fn [] (swap! *head-idx #(shift-head-idx % (- cols-count))))} [:h3 "◀"]]]
    [:div.d-flex.flex-row.align-items-center.justify-content-center.w-100.h-100.button-right
     [:button.nes-container.is-dark.is-rounded.text-center
      {:on-click (fn [] (swap! *head-idx #(shift-head-idx % cols-count)))} [:h3 "▶"]]]
    ;; Panels
    (map-indexed #(species-panel %1 %2) (take cols-count (subvec const/species @*head-idx)))
    ]])
