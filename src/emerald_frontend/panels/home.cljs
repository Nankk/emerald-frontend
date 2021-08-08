(ns emerald-frontend.panels.home
  (:require [emerald-frontend.components.list :as list]
            [emerald-frontend.components.panel :as panel]
            [emerald-frontend.components.container :as container]
            [emerald-frontend.panels.cc-from-nature :as nature]
            [cljs.core.async :refer [<! go]]
            [emerald-frontend.util.http :as http]
            [emerald-frontend.util.async :refer [go? <?]]
            [reagent.core :as reagent]
            [emerald-frontend.const :as const]
            [emerald-frontend.util.css :refer [vw]]
            [emerald-frontend.panels.character-status :as status]
            [re-frame.core :as rf]
            [emerald-frontend.events :as events]
            [emerald-frontend.subs :as subs]))

(defonce *characters-summary (reagent/atom nil))

(defn- <load-characters-summary []
  (go?
   (let [res (<? (http/<get-data "/characters/summary"))]
     (reset! *characters-summary (res :body))
     true)))

(defn character-summary [summary]
  [:div.base-container
   [:div.hstack
    [:div.base-container {:style {:width (vw 10) :height (vw 10) :padding (vw 1)}}
     ;; Picture area
     [:img.profile-image.w-100.h-100 {:src (str const/server-url "/characters/" (summary :idx) "/image")}]]
    (let [{:keys                                    [age species]
           {fname :first mname :middle lname :last} :name}
          summary]
      [:div
       [:div.font-size-4 (str fname (when-not (empty? mname) (str " " mname)) (when-not (empty? lname) (str " " lname))
                              "（" age "）")]
       [:div.font-size-3 (str "[" (species :name) "]")]])]])

(defn- reload-cur-char []
  (println "reload-cur-char")
  (go? (let [char (:body (<? (http/<get-data (str "/characters/" @(rf/subscribe [::subs/cur-char-idx])))))]
         (rf/dispatch-sync [::events/set-cur-char char]))))

(defn- summary-list
  "type :start/:show/:delete"
  [type]
  [:div.base-container {:style {:width (vw 50) :height (vw 50)}}
   [:div.overflow-auto.w-100.h-100
    (into [:div] (for [summary (sort-by :idx @*characters-summary)]
                   ^{:key (summary :uuid)}
                   [:a
                    {:on-click
                     (fn []
                       (rf/dispatch-sync [::events/set-cur-char-idx (summary :idx)])
                       (reload-cur-char)
                       (case type
                         :start  (rf/dispatch-sync [::events/move-panel-to :session-player])
                         :show   (panel/show-modal :home-show-status)
                         :delete (panel/show-modal :home-delete-confirmation)))}
                    [character-summary summary]]))]])

(defonce *delete-confirmation-depth (reagent/atom 0))

(defn- character-delete-confirmation []
  [:div.base-container
   (case @*delete-confirmation-depth
     3 [:div.font-size-5 "キャラクターを削除しています…"]
     4 [:div.vstack-vcenter-hcenter
        [:div.font-size-5 "削除しました。"]
        [:button.nes-btn.is-primary {:on-click #(do (reset! *delete-confirmation-depth 0)
                                                    (panel/close-modal :home-delete-confirmation))}
         "OK"]]
     [list/choice
      :header (case @*delete-confirmation-depth
                0 [:div.font-size-5 "キャラクターをけしますか？"]
                1 [:div.font-size-5 "ほんとうにけしますよ？"]
                2 [:div.font-size-5 {:style {:color "#ff0000"}} "こうかいしませんね？"])
      :choices [{:text "はい"
                 :on-click
                 #(case @*delete-confirmation-depth
                    2 (go
                        (reset! *delete-confirmation-depth 3)
                        (<? (http/<delete (str "/characters/" @(rf/subscribe [::subs/cur-char-idx]))))
                        (<? (<load-characters-summary))
                        (reset! *delete-confirmation-depth 4))
                    (swap! *delete-confirmation-depth inc))}
                {:text "いいえ" :on-click #(do (reset! *delete-confirmation-depth 0)
                                               (panel/close-modal :home-delete-confirmation))}]])])

(defn character-show-status []
  (let [char     @(rf/subscribe [::subs/cur-char])
        char-idx @(rf/subscribe [::subs/cur-char-idx])]
    (if char
      [status/main char char-idx]
      [:div])))

(defn start []
  [:div#home.w-100.h-100.p-2
   [:div.base-container
    [list/choice
     :choices [{:text "ぼうけんをはじめる" :on-click #(do (<load-characters-summary)
                                                          (panel/show-modal :home-start))}
               {:text "キャラクターをつくる" :on-click #(panel/show-modal :home-create)}
               {:text "キャラクターをみる" :on-click #(do (<load-characters-summary)
                                                          (panel/show-modal :home-summary))}
               {:text "キャラクターをけす" :on-click #(do (<load-characters-summary)
                                                          (panel/show-modal :home-delete))}]]]
   [:div
    [panel/modal :home-create [(vw 15) (vw 40)]
     [:div.base-container
      [list/choice
       :choices [{:text "自分でポイント振り分け" :on-click #(panel/show-modal :home-not-implemented)}
                 {:text "人物像から決める" :on-click #(container/switch-panels :home :cc-nature/species :up)}
                 {:text "テンプレート" :on-click #(panel/show-modal :home-not-implemented)}]]]]]
   [panel/modal :home-start [(vw 5) (vw 30)] [summary-list :start]]
   [panel/modal :home-summary [(vw 5) (vw 30)] [summary-list :show]]
   [panel/modal :home-delete [(vw 5) (vw 30)] [summary-list :delete]]
   [panel/modal :home-delete-confirmation [(vw 15) (vw 40)] {:backdropper-rigid? true} [character-delete-confirmation]]
   [panel/modal :home-show-status [(vw 3) (vw 5)] [character-show-status]]
   [panel/modal :home-not-implemented [(vw 15) (vw 40)]
    [:div.base-container
     [:div "まだ できてないよ！"]]]])

(defn main []
  [:div.w-100.h-100
   [container/transient-container
    :home
    {:home                          [start]
     :cc-nature/species             [nature/species-main]
     :cc-nature/job                 [nature/job-main]
     :cc-nature/born                [nature/born-main]
     :cc-nature/infancy             [nature/infancy-main]
     :cc-nature/happening-infancy   [nature/happening-main :infancy]
     :cc-nature/childhood           [nature/childhood-main]
     :cc-nature/happening-childhood [nature/happening-main :childhood]
     :cc-nature/adolescence         [nature/adolescence-main]
     :cc-nature/reason              [nature/reason-main]
     :cc-nature/name                [nature/name-main]
     :cc-nature/high-lv             [nature/high-lv-main]
     :cc-nature/confirmation        [nature/confirmation-main]
     }]])
