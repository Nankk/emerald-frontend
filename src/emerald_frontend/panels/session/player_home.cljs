(ns emerald-frontend.panels.session.player-home
  (:require [emerald-frontend.events :as events]
            [emerald-frontend.subs :as subs]
            [emerald-frontend.util.http :as http]
            [emerald-frontend.const :as const]
            [emerald-frontend.util.css :refer [vw]]
            [emerald-frontend.panels.character-status :as status]
            [emerald-frontend.panels.session.items :as items]
            [emerald-frontend.components.panel :as panel]
            [emerald-frontend.panels.session.growth :as growth]
            [emerald-frontend.util.async :refer [go? <?]]
            [re-frame.core :as rf]
            [reagent.core :as reagent]
            [emerald-frontend.components.list :as list]
            [emerald-frontend.panels.session.shop :as shop]))

(defn screen []
  (when-let [char @(rf/subscribe [::subs/cur-char])]
    [:div.screen
     [:div.vstack-vcenter-hcenter.w-100.h-100.font-size-5
      [:div.mb-4 "セッションに参加していません"]
      [:button.nes-btn.is-primary
       "参加する！"]]]))

(defn title []
  [:div.title.font-size-5.w-100.h-100
   [:div.w-100.h-100.px-5.pt-3
    [:div "Emerald"]
    [:div "aaa"]]])

(defn command []
  [:div.command
   [:div.base-container-full.font-size-4
    (let [icon-size (vw 3)
          gap       (vw 1)
          gap-icon  (vw 1.8)
          choices   [["もちもの" #(panel/show-modal :player/show-items)]
                     ["まほう" #()]
                     ["そうび" #()]
                     ["つよさ" #(panel/show-modal :player/show-status)]
                     ["よろずや" #(panel/show-modal :player/shop)]]]
      (into [:div.vstack]
            (for [c choices]
              [:a.hstack-vcenter {:style {:margin gap} :on-click (c 1)}
               [:img {:style {:margin-right gap-icon :width icon-size :height icon-size}}]
               [:div (c 0)]])))]])

(defn params-upgradable? [char]
  (> (get-in char [:status :achievements :free]) 0))

(defn ability-upgradable? [char exp-table]
  (let [abl-list     const/abilities
        cur-lvs      (into {} (for [{:keys [name level]} (:abilities char)] [name level]))
        able-or-nots (for [{:keys [name]} abl-list
                           :let           [lv (or (cur-lvs name) 0)
                                           type (:table (first (filter #(= (% :name) name) abl-list)))
                                           required (type (first (filter #(= (% :level) (inc lv)) exp-table)))]]
                       (>= (get-in char [:status :exp :free]) required))]
    (some true? able-or-nots)))

(def lv-uppable-notifier
  (let [exp-table (reagent/atom nil)]
    (go? (reset! exp-table (:body (<? (http/<get-data "/tables/exp")))))
    (fn []
      (when-let [char @(rf/subscribe [::subs/cur-char])]
        (when (and @exp-table
                   (or (params-upgradable? char)
                       (ability-upgradable? char @exp-table)))
          [:div.position-absolute.bounce.font-size-5
           {:style {:padding          (str "0 " (vw 1))
                    :top              0
                    :left             0
                    :background-color "rgba(30, 70, 140, 0.5)"}}
           "UP⬆"])))))

(defn status-summary []
  (when-let [char @(rf/subscribe [::subs/cur-char])]
    (let [idx (char :idx)
          {{{:keys [first middle last]} :name}       :personality
           {:keys [level max-hp hp max-mp mp g exp]} :status} char]
      [:div.status-summary.font-size-3
       [:div.base-container-full
        [:div.hstack-vcenter.w-100.h-100
         [:a.position-relative
          {:on-click #(panel/show-modal :player/growth)}
          [lv-uppable-notifier]
          [:img.profile-image
           {:src      (str const/server-url "/characters/" idx "/image")
            :style    {:width (vw 10) :height (vw 10)}}]]
         [:div.vstack-justify
          [:div.pb-3 (str first " " middle " " last)]
          [:table.nes-table.is-dark
           [:tbody
            [:tr [:td "Lv"] [:td level] [:td ""] [:td ""]]
            [:tr [:td "HP"] [:td (str hp "/" max-hp)] [:td "G"] [:td g]]
            [:tr [:td "MP"] [:td (str mp "/" max-mp)] [:td "Exp"] [:td (:free exp)]]]]]]]])))

(defn nature->str [nature]
  (let [{{b-abst :abstract b-detail :detail}                                 :born
         {{ie-abst :abstract ie-detail :detail} :event
          {ih-abst :abstract}                   :happening}        :infancy
         {{ce-abst :abstract ce-detail :detail} :event
          {ch-abst :abstract}                   :happening}        :childhood
         {{ae-abst :abstract ae-detail :detail} :event
          {ar-abst :abstract}                   :departure-reason} :adolescence} nature]
    (str "生まれ：" b-abst "\n"
         b-detail "\n\n"
         "幼少期の経験：" ie-abst "\n"
         "" ie-detail "\n\n"
         "幼少期のハプニング：" ih-abst "\n\n"
         "少年期の経験：" ce-abst "\n"
         "" ce-detail "\n\n"
         "少年期のハプニング：" ch-abst "\n\n"
         "青年期の経験：" ae-abst "\n"
         "" ae-detail "\n\n"
         "冒険に出た理由：" ar-abst "\n")))

(defn notes []
  (when-let [char @(rf/subscribe [::subs/cur-char])]
    (let [{{:keys [notes nature]} :options} char]
      [:div.notes.font-size-2
       [:div.base-container-full.p-3
        [:div "メモ欄"]
        [:hr.w-100]
        [:textarea.is-dark.border-0.pt-3.w-100.vh-10
         {:default-value (nature->str (-> char :options :nature))}]]])))

(defn settings []
  [:div.base-container {:style {:width (vw 33) :height (vw 40)}}
   [list/choice
    :choices [{:text "セッションから離脱" :on-click #(rf/dispatch-sync [::events/move-panel-to :home])}]]
   ])

(defn exit []
  [:div.base-container
   [list/choice
    :header [:div.font-size-5 "ホームに戻りますか？"]
    :choices [{:text "はい" :on-click #(rf/dispatch-sync [::events/move-panel-to :home])}
              {:text "いいえ" :on-click #(panel/close-modal :player/exit)}]]])

(defn panels []
  (when-let [char @(rf/subscribe [::subs/cur-char])]
    [:div.w-100.h-100 {:style {:position "relative"}}
     [:div#player-home.w-100.h-100
      [:div.grid-container.w-100.h-100.font-size-2
       [screen]
       [title]
       [command]
       [status-summary]
       [notes]]]
     [:div.font-size-4
      [:div {:on-click #(panel/show-modal :player/exit)}
       [:i.fas.fa-sign-out-alt {:style {:position "absolute" :top (vw 1.5) :left (vw 93)}}]]
      [:div {:on-click #(panel/show-modal :player/settings)}
       [:i.fas.fa-cog {:style {:position "absolute" :top (vw 1.5) :left (vw 96)}}]]]
     [panel/modal :player/show-items [(vw 1.5) (vw 3)] [items/main]]
     [panel/modal :player/show-status [(vw 1.5) (vw 3)] [status/main]]
     [panel/modal :player/exit [(vw 2) (vw 70)] [exit]]
     [panel/modal :player/settings [(vw 2) (vw 60)] [settings]]
     [panel/modal :player/shop [(vw 2) (vw 2)] [shop/main]]
     ]))

(defn main []
  (let []
    [:div.w-100.h-100.p-4
     [panels]
     [panel/modal :player/growth [(vw 3) (vw 4.5)] [growth/main]]]
    ;; [items/main]
    ))
