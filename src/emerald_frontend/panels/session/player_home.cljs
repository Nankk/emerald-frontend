(ns emerald-frontend.panels.session.player-home
  (:require [emerald-frontend.events :as events]
            [emerald-frontend.subs :as subs]
            [emerald-frontend.util.http :as http]
            [emerald-frontend.const :as const]
            [emerald-frontend.util.css :refer [vw]]
            [emerald-frontend.panels.character-status :as status]
            [emerald-frontend.components.panel :as panel]
            [cljs.core.async :refer [<! go]]
            [re-frame.core :as rf]
            [reagent.core :as reagent]
            [emerald-frontend.components.list :as list]))

(defonce cur-char (reagent/atom nil))
(defonce cur-char-idx (reagent/atom nil))

(defn screen [char]
  [:div.screen
   [:div.w-100.h-100
    [:img.screen-image {:src "img/dev/dragon-wars.png"}]]])

(defn title [char]
  [:div.title.font-size-3.w-100.h-100
   [:div.d-flex.flex-column.justify-content-center.w-100.h-100
    [:div "Emerald"]
    [:div "aaa"]]])

(defn command [char]
  [:div.command
   [:div.nes-container.is-dark.is-rounded.w-100.h-100.font-size-4
    (let [icon-size (vw 5)
          gap       (vw 1)
          gap-icon  (vw 1.8)
          choices   [["もちもの" #()]
                     ["まほう" #()]
                     ["そうび" #()]
                     ["つよさ" #(panel/show-modal :player/show-status)]]]
      (into [:div.d-flex.flex-column]
            (for [c choices]
              [:div.d-flex.flex-row.align-items-center {:style {:margin gap} :on-click (c 1)}
               [:img {:style {:margin-right gap-icon :width icon-size :height icon-size}}]
               [:div (c 0)]])))]])

(defn status-summary [char]
  (let [{{{:keys [first middle last]} :name}           :personality
         {:keys [level max-hp hp max-mp mp gamel exp]} :status} char]
    [:div.status-summary
     [:div.nes-container.is-dark.is-rounded.w-100.h-100
      [:div.d-flex.flex-row.align-items-center.w-100.h-100
       [:img.profile-image
        {:src   (str const/server-url "/characters/" @cur-char-idx "/image")
         :style {:width (vw 10) :height (vw 10)}}]
       [:div.d-flex.flex-column.justify-content-between
        [:div (str first " " middle " " last)]
        [:table.nes-table.is-dark
         [:tbody
          [:tr [:td "Lv"] [:td level] [:td ""] [:td ""]]
          [:tr [:td "HP"] [:td (str hp "/" max-hp)] [:td "G"] [:td gamel]]
          [:tr [:td "MP"] [:td (str mp "/" max-mp)] [:td "Exp"] [:td exp]]]]
        ]]]]))

(defn notes [char]
  [:div.notes.font-size-4
   [:div.nes-container.is-dark.is-rounded.w-100.h-100
    [:div.font-size-5 "メモ欄"]
    [:div "クソゲーRTA、はっじまっるよー！"]
    [:div "まずはキャラ作成からです！"]]])

(defn settings []
  [:div.nes-container.is-dark.is-rounded {:style {:width (vw 33) :height (vw 40)}}
   [list/choice
    :choices [{:text "セッションから離脱" :on-click #(rf/dispatch-sync [::events/move-panel-to :home])}]]
   ])

(defn exit []
  [:div.nes-container.is-dark.is-rounded
   [list/choice
    :header [:h3 "終了しますか？"]
    :choices [{:text "はい" :on-click #(rf/dispatch-sync [::events/move-panel-to :home])}
              {:text "いいえ" :on-click #(panel/close-modal :player/exit)}]]])

(defn panels []
  (when @cur-char
    (let [char @cur-char]
      [:div.w-100.h-100 {:style {:position "relative"}}
       [:div#player-home.w-100.h-100
        [:div.grid-container.w-100.h-100.font-size-5
         [screen char]
         [title char]
         [command char]
         [status-summary char]
         [notes char]]]
       [:div.font-size-4
        [:div {:on-click #(panel/show-modal :player/exit)}
         [:i.fas.fa-sign-out-alt {:style {:position "absolute" :top (vw 1.5) :left (vw 93)}}]]
        [:div {:on-click #(panel/show-modal :player/settings)}
         [:i.fas.fa-cog {:style {:position "absolute" :top (vw 1.5) :left (vw 96)}}]]]
       [panel/modal :player/show-status [(vw 3) (vw 5)] [status/main char @cur-char-idx]]
       [panel/modal :player/exit [(vw 2) (vw 70)] [exit]]
       [panel/modal :player/settings [(vw 2) (vw 60)] [settings]]
       ])))

(defn main []
  (let [char-idx @(rf/subscribe [::subs/cur-char-idx])]
    (go
      (reset! cur-char-idx char-idx)
      (reset! cur-char (:body (<! (http/<get-data (str "/characters/" char-idx))))))
    [panels]))
