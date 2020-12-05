(ns emerald-frontend.panels.cc-from-nature
  (:require [cljs.core.async :refer [go <!]]
            [emerald-frontend.components.container :as container]
            [emerald-frontend.components.list :as list]
            [emerald-frontend.const :as const]
            [emerald-frontend.events :as events]
            [emerald-frontend.subs :as subs]
            [emerald-frontend.macros.async :refer [defan defan-]]
            [emerald-frontend.util.http :as http]
            [re-frame.core :as rf]
            [reagent.core :as reagent]
            [emerald-frontend.components.panel :as panel]))

;;; Childhood ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce *cur-childhood (reagent/atom nil))
(defonce *childhood-data (reagent/atom {}))

(defan- <load-childhood []
  (println "<load-childhood")
  (let [res (<! (http/<get-data "/cbb/childhood"))]
    (reset! *childhood-data (res :body))
    true))

(defn- childhood-detail []
  (when-let [b @*cur-childhood]
    [:div.nes-container.is-dark.is-rounded {:style {:width "70vw" :height "30vw"}}
     [:h3 (b :title)]
     [:h3 (b :description)]
     [:hr]
     [list/choice
      :header [:h3 "これでいいですか？"]
      :choices [{:text "はい" :on-click #()}
                {:text "いいえ" :on-click #(panel/close-modal :childhood-detail)}]
      :direction :horizontal]]))

(defn- childhood-list []
  [:div.nes-container.is-dark.is-rounded.experience-list
   {:style {:width "50vw" :height "40vw"}}
   [:div.overflow-auto.w-100.h-100
    [list/choice
     :choices (into []
                    (for [b (@*childhood-data :人間-戦士系)] ^{:key (b :title)}
                      {:text (b :title) :on-click (fn []
                                                    (reset! *cur-childhood b)
                                                    (panel/show-modal :childhood-detail))}))]]])

(defn- random-childhood []
  (let [childhoods (@*childhood-data :人間-戦士系)]
    (childhoods (rand-int (count childhoods)))))

(defn childhood-main []
  (<load-childhood)
  [:div#cc-nature-childhood.w-100.h-100.p-1 {:style {:position "relative"}}
   [:div.grid-container.w-100.h-100
    ;; Header
    [:button.nes-btn.aux-left {:on-click #(container/switch-panels :cc-nature-childhood :cc-nature-job :right)} [:h3 "◁"]]
    [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "少年期の経験を決定してください"]]
    ;; Body
    [:div.nes-container.is-dark.is-rounded.w-100.h-100.selector
     [list/choice
      :choices [{:text "ランダム" :on-click (fn []
                                              (reset! *cur-childhood (random-childhood))
                                              (panel/show-modal :childhood-detail))}
                {:text "自分で選ぶ" :on-click #(panel/show-modal :childhood-experience-list)}]]]
    [panel/modal :childhood-experience-list ["15vw" "35vw"]
     [childhood-list]]]
   [panel/modal :childhood-detail ["20vw" "15vw"]
    [childhood-detail]]
   ])

;;; Infancy ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce *cur-infancy (reagent/atom nil))
(defonce *infancy-data (reagent/atom {}))

(defan- <load-infancy []
  (println "<load-infancy")
  (let [res (<! (http/<get-data "/cbb/infancy"))]
    (reset! *infancy-data (res :body))
    true))

(defn- infancy-detail []
  (when-let [b @*cur-infancy]
    [:div.nes-container.is-dark.is-rounded {:style {:width "70vw" :height "30vw"}}
     [:h3 (b :title)]
     [:h3 (b :description)]
     [:hr]
     [list/choice
      :header [:h3 "これでいいですか？"]
      :choices [{:text "はい" :on-click #()}
                {:text "いいえ" :on-click #(panel/close-modal :infancy-detail)}]
      :direction :horizontal]]))

(defn- infancy-list []
  [:div.nes-container.is-dark.is-rounded.experience-list
   {:style {:width "50vw" :height "40vw"}}
   [:div.overflow-auto.w-100.h-100
    [list/choice
     :choices (into []
                    (for [b (@*infancy-data :人間-戦士系)] ^{:key (b :title)}
                      {:text (b :title) :on-click (fn []
                                                    (reset! *cur-infancy b)
                                                    (panel/show-modal :infancy-detail))}))]]])

(defn- random-infancy []
  (let [infancys (@*infancy-data :人間-戦士系)]
    (infancys (rand-int (count infancys)))))

(defn infancy-main []
  (<load-infancy)
  [:div#cc-nature-infancy.w-100.h-100.p-1 {:style {:position "relative"}}
   [:div.grid-container.w-100.h-100
    ;; Header
    [:button.nes-btn.aux-left {:on-click #(container/switch-panels :cc-nature-infancy :cc-nature-job :right)} [:h3 "◁"]]
    [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "幼少期の経験を決定してください"]]
    ;; Body
    [:div.nes-container.is-dark.is-rounded.w-100.h-100.selector
     [list/choice
      :choices [{:text "ランダム" :on-click (fn []
                                              (reset! *cur-infancy (random-infancy))
                                              (panel/show-modal :infancy-detail))}
                {:text "自分で選ぶ" :on-click #(panel/show-modal :infancy-experience-list)}]]]
    [panel/modal :infancy-experience-list ["15vw" "35vw"]
     [infancy-list]]]
   [panel/modal :infancy-detail ["20vw" "15vw"]
    [infancy-detail]]
   ])

;;; Born ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce *origin (reagent/atom ""))
(defonce *freedom (reagent/atom ""))
(defonce *cur-born (reagent/atom nil))
(defonce *born-data (reagent/atom {}))

(defan- <load-born []
  (println "<load-born")
  (let [res (<! (http/<get-data "/cbb/born"))]
    (reset! *born-data (res :body))
    (println (res :body))
    true))

(defn- nightmare-origin [origins]
  [:div
   [:hr]
   [list/choice
    :header [:h4 "出自"]
    :direction :horizontal
    :choices-size :h4
    :choices (into [] (for [origin origins]
                        {:text     origin
                         :on-click #(reset! *origin origin)}))]])

(defn- nightmare-freedom [freedoms]
  [:div
   [list/choice
    :header [:h4 "自由になれた理由"]
    :direction :horizontal
    :choices-size :h4
    :choices (into [] (for [freedom freedoms]
                        {:text     (freedom :title)
                         :on-click #(reset! *freedom freedom)}))]
   [:h4 ((first (filter #(= (% :title) (@*freedom :title)) freedoms)) :description)]])

(defn- nightmare-opts [born]
  (when (= @(rf/subscribe [::subs/cur-character-param [:species]]) "ナイトメア")
    [:div
     (let [origins (set (map #(nth % 1) (filter #(re-find #"origin-" (name (% 0))) born)))]
       (reset! *origin (first origins))
       [nightmare-origin origins])
     (when-let [freedoms (born :自由になれた理由)]
       (reset! *freedom (first freedoms))
       [nightmare-freedom freedoms])]))

(defn- born-detail []
  (when-let [born @*cur-born]
    [:div.nes-container.is-dark.is-rounded {:style {:width "90vw"}}
     [:h3 (born :title)]
     [:h4 (born :description)]
     (reset! *origin nil)
     (reset! *freedom nil)
     [nightmare-opts born]
     [:hr]
     [list/choice
      :header [:h3 "これでいいですか？"]
      :choices [{:text "はい"
                 :on-click
                 #(do
                    (rf/dispatch-sync
                     [::events/set-character-param
                      [:born]
                      {:title (str (born :title)
                                   (when @*origin
                                     (str "（出自：" @*origin "）")))
                       :description (str (born :description)
                                         (when @*freedom
                                           (str "（自由になれた理由 [" (@*freedom :title) "]："
                                                (@*freedom :description))))}])
                    (container/switch-panels :cc-nature-born :cc-nature-childhood :left))}
                {:text     "いいえ"
                 :on-click #(panel/close-modal :born-detail)}]
      :direction :horizontal]]))

(defn- born-list []
  (let [cur-species @(rf/subscribe [::subs/cur-character-param [:species]])]
    [:div.nes-container.is-dark.is-rounded.experience-list
     {:style {:width "50vw" :height "40vw"}}
     [:div.overflow-auto.w-100.h-100
      [list/choice
       :choices (into []
                      (for [b (@*born-data (keyword cur-species))] ^{:key (b :title)}
                        {:text (b :title) :on-click (fn []
                                                      (reset! *cur-born b)
                                                      (panel/show-modal :born-detail))}))]]]))

(defn- random-born []
  (let [cur-species @(rf/subscribe [::subs/cur-character-param [:species]])
        borns       (@*born-data (keyword cur-species))]
    (borns (rand-int (count borns)))))

(defn born-main []
  (<load-born)
  [:div#cc-nature-born.w-100.h-100.p-1 {:style {:position "relative"}}
   [:div.grid-container.w-100.h-100
    ;; Header
    [:button.nes-btn.aux-left {:on-click #(container/switch-panels :cc-nature-born :cc-nature-job :right)} [:h3 "◁"]]
    [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "生まれを決定してください"]]
    ;; Body
    [:div.nes-container.is-dark.is-rounded.w-100.h-100.selector
     [list/choice
      :choices [{:text "ランダム" :on-click (fn []
                                              (reset! *cur-born (random-born))
                                              (panel/show-modal :born-detail))}
                {:text "自分で選ぶ" :on-click #(panel/show-modal :born-experience-list)}]]]]
   [panel/modal :born-experience-list ["15vw" "35vw"]
    [born-list]]
   [panel/modal :born-detail ["10vw" "5vw"]
    [born-detail]]
   ])

;;; Job ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce ^:private *cur-job (reagent/atom nil))

(defn- aptitude-test [species job]
  (let [species-list const/species-list
        aptitudes    (get-in species-list [species :aptitude])]
    (some? (some #{job} aptitudes))))

(defn- job-icon [job]
  (let [has-aptitude? (aptitude-test (@(rf/subscribe [::subs/cur-character]) :species) (job 0))]
    [:button.nes-btn {:class (when-not has-aptitude? "is-disabled")
                      :style {:height "10vw" :width "10vw" :background-color "#bbbbbb" :margin "1vw" :color "#222222"}
                      :on-click (fn []
                                  (when has-aptitude?
                                    (rf/dispatch-sync [::events/set-character-param [:job] (job 0)])
                                    (reset! *cur-job job)))}
     [:h3 (job 0)]]))

(defn job-main []
  [:div#cc-nature-job.w-100.h-100.p-1
   [:div.grid-container.w-100.h-100
    ;; Header
    [:button.nes-btn.aux-left {:on-click #(container/switch-panels :cc-nature-job :cc-nature-species :right)} [:h3 "◁"]]
    [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "職業を選んでください"]]
    ;; Icons
    (into [:div.d-flex.flex-row.flex-wrap.align-items-center.justify-content-center.w-100.h-100.icons]
          (for [job const/jobs] ^{:key (job 0)} [job-icon job]))
    ;; Description
    [:div.nes-container.is-dark.is-rounded.w-100.h-100.description
     (when-let [cj @*cur-job]
       [:div
        [:h3 (cj 0)]
        [:h4 (get-in cj [1 :description])]])]
    ;; Button
    [:div.nes-btn.is-primary.aux-right {:style {:height "7vw"}
                                        :on-click #(container/switch-panels :cc-nature-job :cc-nature-born :left)}
     [:h3 "決定"]]
    ]])

;;; Species ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private cols-count 3)
(defonce ^:private *head-idx (reagent/atom 0))
(def ^:private idx->pos-class
  ["panel-left" "panel-center" "panel-right"])

(defn- shift-head-idx [cur diff]
  (let [len      (count const/species-list)
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
                (rf/dispatch-sync [::events/set-character-param [:species] (species 0)])
                (container/switch-panels :cc-nature-species :cc-nature-job :left))}
   [:div.h-25 [:h4 (species 0)]]
   [:hr]
   [:div.h-25 [:h4 ((species 1) :feature)]]
   [:hr]
   [:div.h-25 [:h4 ((species 1) :description)]]])

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
    (map-indexed #(species-panel %1 %2) (take cols-count (subvec (vec const/species-list) @*head-idx)))
    ]])
