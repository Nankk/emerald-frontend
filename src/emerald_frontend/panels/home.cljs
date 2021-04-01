(ns emerald-frontend.panels.home
  (:require [emerald-frontend.components.list :as list]
            [emerald-frontend.components.panel :as panel]
            [emerald-frontend.components.container :as container]
            [cljs.core.async :refer [<! go]]
            [emerald-frontend.util.http :as http]
            [emerald-frontend.macros.async :refer [defan defan-]]
            [reagent.core :as reagent]
            [emerald-frontend.const :as const]
            [emerald-frontend.util.css :refer [vw]]
            [emerald-frontend.panels.character-status :as status]))

(defonce *cur-character-idx (reagent/atom nil))

(defonce *characters-summary (reagent/atom nil))

(defan- <load-characters-summary []
  (let [res (<! (http/<get-data "/characters/summary"))]
    (reset! *characters-summary (res :body))
    true))

(defn character-summary [summary]
  [:div.nes-container.is-dark.is-rounded
   [:div.d-flex.flex-row
    [:div.nes-container.is-dark.is-rounded {:style {:width (vw 10) :height (vw 10) :padding (vw 1)}}
     ;; Picture area
     [:img.profile-image.w-100.h-100 {:src (str const/server-url "/characters/" (summary :idx) "/image")}]]
    (let [{:keys                                    [age species]
           {fname :first mname :middle lname :last} :name}
          summary]
      [:div
       [:h4 (str fname (when-not (empty? mname) (str " " mname)) (when-not (empty? lname) (str " " lname))
                 "（" age "）")]
       [:h5 (str "[" (species :name) "]")]])]])

(defonce *cur-character (reagent/atom nil))

(defn- reload-cur-char []
  (println "reload-cur-char")
  (go (reset! *cur-character (:body (<! (http/<get-data (str "/characters/" @*cur-character-idx)))))))

(defn- summary-list
  "type :start/:show/:delete"
  [type]
  [:div.nes-container.is-dark.is-rounded {:style {:width (vw 50) :height (vw 50)}}
   [:div.overflow-auto.w-100.h-100
    (into [:div] (for [summary (sort-by :idx @*characters-summary)]
                   ^{:key (summary :uuid)}
                   [:div {:on-click (fn []
                                      (reset! *cur-character-idx (summary :idx))
                                      (case type
                                        :start  (panel/show-modal :home-not-implemented)
                                        :show   (do (reload-cur-char)
                                                    (panel/show-modal :home-show-status))
                                        :delete (panel/show-modal :home-delete-confirmation)))}
                    [character-summary summary]]))]])

(defonce *delete-confirmation-depth (reagent/atom 0))

(defn- character-delete-confirmation []
  [:div.nes-container.is-dark.is-rounded
   (case @*delete-confirmation-depth
     3 [:h3 "キャラクターを削除しています…"]
     4 [:div
        [:h3 "削除しました。"]
        [:button.nes-btn.is-primary {:on-click #(do (reset! *delete-confirmation-depth 0)
                                                    (panel/close-modal :home-delete-confirmation))}
         "OK"]]
     [list/choice
      :header (case @*delete-confirmation-depth
                0 [:h3 "キャラクターをけしますか？"]
                1 [:h3 "ほんとうにけしますよ？"]
                2 [:h3 {:style {:color "#ff0000"}} "こうかいしませんね？"])
      :choices [{:text "はい" :on-click #(case @*delete-confirmation-depth
                                           2 (go
                                               (reset! *delete-confirmation-depth 3)
                                               (<! (http/<delete (str "/characters/" @*cur-character-idx)))
                                               (<! (<load-characters-summary))
                                               (reset! *delete-confirmation-depth 4))
                                           (swap! *delete-confirmation-depth inc))}
                {:text "いいえ" :on-click #(do (reset! *delete-confirmation-depth 0)
                                               (panel/close-modal :home-delete-confirmation))}]])])

(defn character-show-status []
  (let [char @*cur-character]
    (if char
      [status/main char @*cur-character-idx]
      [:div])))

(defn main []
  [:div#home.w-100.h-100.p-2
   [:div.nes-container.is-dark.is-rounded
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
     [:div.nes-container.is-dark.is-rounded
      [list/choice
       :choices [{:text "自分でポイント振り分け" :on-click #(panel/show-modal :home-not-implemented)}
                 {:text "人物像から決める" :on-click #(container/switch-panels :home :cc-nature-species :up)}
                 {:text "テンプレート" :on-click #(panel/show-modal :home-not-implemented)}]]]]]
   [panel/modal :home-start [(vw 5) (vw 30)] [summary-list :start]]
   [panel/modal :home-summary [(vw 5) (vw 30)] [summary-list :show]]
   [panel/modal :home-delete [(vw 5) (vw 30)] [summary-list :delete]]
   [panel/modal :home-delete-confirmation [(vw 15) (vw 40)] {:backdropper-rigid? true} [character-delete-confirmation]]
   [panel/modal :home-show-status [(vw 3) (vw 5)] [character-show-status]]
   [panel/modal :home-not-implemented [(vw 15) (vw 40)]
    [:div.nes-container.is-dark.is-rounded
     [:div "まだ できてないよ！"]]]])
