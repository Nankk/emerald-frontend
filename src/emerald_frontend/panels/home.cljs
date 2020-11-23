(ns emerald-frontend.panels.home
  (:require [emerald-frontend.components.list :as list]
            [emerald-frontend.components.panel :as panel]
            [emerald-frontend.components.container :as container]))

(defn- main []
  [:div#home.w-100.h-100.p-2
   [:div.nes-container.is-dark.is-rounded
    [list/choice
     :choices [{:text "ぼうけんをはじめる" :on-click #(panel/show-modal :home-not-implemented)}
               {:text "キャラクターをつくる" :on-click #(panel/show-modal :home-create)}
               {:text "キャラクターをみる" :on-click #(panel/show-modal :home-not-implemented)}
               {:text "キャラクターをけす" :on-click #(panel/show-modal :home-not-implemented)}]]]
   (let [pos ["15vw" "40vw"]]
     [:div
      [panel/modal :home-create pos
       [:div.nes-container.is-dark.is-rounded
        [list/choice
         :choices [{:text "自分でポイント振り分け" :on-click #(panel/show-modal :home-not-implemented)}
                   {:text "人物像から決める" :on-click #(container/switch-panels :home :cc-nature-species :up)}
                   {:text "テンプレート" :on-click #(panel/show-modal :home-not-implemented)}]]]]
      [panel/modal :home-not-implemented pos
       [:div.nes-container.is-dark.is-rounded
        [:div "まだ できてないよ！"]]]])])
