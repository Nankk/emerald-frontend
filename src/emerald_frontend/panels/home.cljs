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

(def debug-char
  {:equipments {},
   :accessories
   {:head [],
    :foot [],
    :neck [],
    :face [],
    :back [],
    :waist [],
    :others [],
    :left-hand [],
    :ear [],
    :right-hand []},
   :abilities [{:name "ファイター", :level 2}
               {:name "グラップラー", :level 1}
               {:name "フェンサー", :level 1}
               {:name "シューター", :level 1}
               {:name "ソーサラー", :level 1}
               {:name "コンジャラー", :level 1}
               {:name "プリースト", :level 1}
               {:name "フェアリーテイマー", :level 1}
               {:name "マギテック", :level 1}
               {:name "デーモンルーラー", :level 1}
               {:name "ドルイド", :level 1}
               {:name "グリモワール", :level 1}
               {:name "スカウト", :level 1}
               {:name "レンジャー", :level 1}
               {:name "セージ", :level 1}
               {:name "エンハンサー", :level 1}
               {:name "バード", :level 1}
               {:name "ライダー", :level 1}
               {:name "アルケミスト", :level 1}
               {:name "ウォーリーダー", :level 1}
               {:name "ミスティック", :level 1}],
   :reference-values
   {:damage-bonus 5,
    :defense 0,
    :hit 4,
    :dodge 4,
    :resistance {:con 5, :pow 4},
    :magical-power {},
    :mobility {:limited 3, :normal 16, :sprint 48},
    :packages
    {:scout {:dex 0, :agi 0, :int 0},
     :ranger {:dex 3, :agi 3, :int 3},
     :sage {:int 0}}},
   :skills {:general ["防具習熟A/非金属鎧"]},
   :status
   {:level 2, :max-hp 26, :max-mp 14, :hp 26, :mp 14, :exp 1000, :honor 0},
   :personality
   {:age 20,
    :name {:middle "", :first "メイトリウス", :last "コルゲンバリ"},
    :species {:name "ナイトメア", :origin "ドワーフ"},
    :gender "male"},
   :options
   {:nature
    {:infancy
     {:happening {:abstract "家族を失った（父）"},
      :event
      {:abstract "山野を己の庭として",
       :detail "野を駆け、森を抜け、山を越える。自然の環境は、きみのあらゆる能力を試し、同時に鍛え上げた。"}},
     :adolescence
     {:departure-reason {:abstract "家族をすべて失ったので"},
      :event
      {:abstract "戦いの訓練を受けた",
       :detail "兵士としてか、道場でか。きみは戦いの訓練を積んだ時期がある。そこで、的確な防具の扱いについて学ぶことができた。"}},
     :born
     {:abstract "鉱山の子",
      :detail
      "鍛冶に使う鉱物や宝石を採掘する工夫の家に生まれる。母親は死にかける。暗闇の中を明かりもなく生活する環境にきみは馴染めず、 惨めに育つ。 "},
     :childhood
     {:happening {:abstract "蛮族の血が流れている"},
      :event {:abstract "自然の中で学ぶ", :detail "自然の中で生き、自然から生を得た。"}}}},
   :items [],
   :languages
   [{:name "交易共通語", :oral true, :literal true}
    {:name "ドワーフ語", :oral true, :literal true}
    {:name "メソポタミア語", :oral false, :literal true}
    {:name "ミ＝語", :oral true, :literal false}
    {:name "語語の紅茶", :oral true, :literal false}
    {:name "ミ＝ゴの紅茶", :oral true, :literal false}
    {:name "みさくら語", :oral true, :literal true}
    {:name "ヒババンゴ", :oral true, :literal true}
    {:name "マタンゴ", :oral true, :literal true}
    {:name "地方語(関西)", :oral true, :literal true}],
   :parameters
   {:initial {:int 5, :pow 5, :str 4, :con 4, :dex 8, :agi 8},
    :base {:mind 8, :technique 7, :physique 15},
    :growth {:int 1, :pow 1, :str 1, :con 1, :dex 1, :agi 1},
    :total {:int 14, :pow 14, :str 20, :con 20, :dex 16, :agi 16},
    :bonus {:int 2, :pow 2, :str 3, :con 3, :dex 2, :agi 2}}})

(defn character-show-status []
  [status/main debug-char 4]
  ;; (let [char @*cur-character]
  ;;   (if char
  ;;     [status/main char]
  ;;     [:div]))
  )

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
