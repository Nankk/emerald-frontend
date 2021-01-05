(ns emerald-frontend.panels.home
  (:require [emerald-frontend.components.list :as list]
            [emerald-frontend.components.panel :as panel]
            [emerald-frontend.components.container :as container]
            [cljs.core.async :refer [<! go]]
            [emerald-frontend.util.http :as http]
            [emerald-frontend.macros.async :refer [defan defan-]]
            [reagent.core :as reagent]

            [emerald-frontend.const :as const]))

(defonce *cur-character-idx (reagent/atom nil))

(defonce *characters-summary (reagent/atom nil))

(defan- <load-characters-summary []
  (let [res (<! (http/<get-data "/characters/summary"))]
    (reset! *characters-summary (res :body))
    true))

(defn character-summary [summary]
  [:div.nes-container.is-dark.is-rounded
   [:div.d-flex.flex-row
    [:div.nes-container.is-dark.is-rounded {:style {:width "10vw" :height "10vw" :padding "1vw"}}
     ;; Picture area
     (if-let [image (summary :image)]
       [:img.profile-image.w-100.h-100 {:src (str const/server-url "/characters/" (summary :idx) "/image")}]
       [:h6 "Default image"])]
    (let [{:keys [age species]
           {fname :first mname :middle lname :last} :name}
          summary]
      [:div
       [:h4 (str fname (when-not (empty? mname) (str "・" mname)) (when-not (empty? lname) (str "・" lname))
                 "（" age "）")]
       [:h5 (str "[" (species :name) "]")]])]])

(defn- summary-list
  "type :start/:show/:delete"
  [type]
  [:div.nes-container.is-dark.is-rounded {:style {:width "50vw" :height "50vw"}}
   [:div.overflow-auto.w-100.h-100
    (into [:div] (for [summary (sort-by :idx @*characters-summary)]
                   ^{:key (summary :uuid)}
                   [:div {:on-click (fn []
                                      (reset! *cur-character-idx (summary :idx))
                                      (case type
                                        :start (panel/show-modal :home-not-implemented)
                                        :show (panel/show-modal :home-not-implemented)
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

(defn- main []
  [:div#home.w-100.h-100.p-2
   [:div.nes-container.is-dark.is-rounded
    [list/choice
     :choices [{:text "ぼうけんをはじめる" :on-click #(do
                                                        (<load-characters-summary)
                                                        (panel/show-modal :home-start))}
               {:text "キャラクターをつくる" :on-click #(panel/show-modal :home-create)}
               {:text "キャラクターをみる" :on-click #(do
                                                        (<load-characters-summary)
                                                        (panel/show-modal :home-summary))}
               {:text "キャラクターをけす" :on-click #(do
                                                        (<load-characters-summary)
                                                        (panel/show-modal :home-delete))}]]]
   [:div
    [panel/modal :home-create ["15vw" "40vw"]
     [:div.nes-container.is-dark.is-rounded
      [list/choice
       :choices [{:text "自分でポイント振り分け" :on-click #(panel/show-modal :home-not-implemented)}
                 {:text "人物像から決める" :on-click #(container/switch-panels :home :cc-nature-species :up)}
                 {:text "テンプレート" :on-click #(panel/show-modal :home-not-implemented)}]]]]]
   [panel/modal :home-start ["5vw" "30vw"] [summary-list :start]]
   [panel/modal :home-summary ["5vw" "30vw"] [summary-list :show]]
   [panel/modal :home-delete ["5vw" "30vw"] [summary-list :delete]]
   [panel/modal :home-delete-confirmation ["15vw" "40vw"] {:backdropper-rigid? true} [character-delete-confirmation]]
   [panel/modal :home-not-implemented ["15vw" "40vw"]
    [:div.nes-container.is-dark.is-rounded
     [:div "まだ できてないよ！"]]]])
