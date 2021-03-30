(ns emerald-frontend.panels.character-status
  (:require
   [emerald-frontend.util.css :refer [vw]]
   [emerald-frontend.const :as const]
   [reagent.core :as reagent]
   ))

(def param-key->str
  {:dex "器用度"
   :agi "敏捷度"
   :str "筋力"
   :con "生命力"
   :int "知力"
   :pow "精神力"})

(def param->base
  {:dex :technique
   :agi :technique
   :str :physique
   :con :physique
   :int :mind
   :pow :mind})

(defonce ^:private page-idx (reagent/atom 0))

(defn- move-page [diff]
  (swap! page-idx #(Math/abs (rem (+ diff %) 2)))
  (println @page-idx))

(defn- status-main [char idx]
  [:div#character-status-main
   (let [{{:keys [age gender impurity]}                       :personality
          {{:keys [first middle last]} :name
           {:keys [name head origin]}  :species}              :personality
          {:keys [level max-hp max-mp hp mp exp honor gamel]} :status
          {:keys [weapons shield armor]}                      :equipment
          {:keys [base initial growth total bonus]}           :parameters
          {{:keys [scout ranger sage]} :packages}             :reference-values} char]
     [:div.nes-container.is-dark.is-rounded.grid-container {:style {:width (vw 90) :height (vw 50)}}
      [:div.aux-left
       [:div.nes-container.is-dark.is-rounded.d-flex.flex-row.align-items-center.justify-content-center
        {:style {:height (vw 7)}
         :on-click #(move-page -1)}
        [:div "◁"]]]
      [:div.aux-right
       [:div.nes-container.is-dark.is-rounded.d-flex.flex-row.align-items-center.justify-content-center
        {:style {:height (vw 7)}
         :on-click #(move-page 1)}
        [:div "▷"]]]
      [:div.status {:style {:font-size (vw 1.5)}}
       [:div.nes-container.is-dark.is-rounded.h-100
        [:div (str first " " middle " " last)] ; Name
        [:div (str "（" (when (or head origin) (str head origin "の")) name "）")] ; Species
        [:div.d-flex.flex-row.align-items-center
         [:img.profile-image
          {:src   (str const/server-url "/characters/" idx "/image")
           :style {:width "50%" :height "50%"}}]
         [:table.nes-table.is-dark.w-50
          [:tbody
           [:tr [:td "Lv"] [:td level]]
           [:tr [:td "HP"] [:td (str hp "/" max-hp)]]
           [:tr [:td "MP"] [:td (str mp "/" max-mp)]]
           [:tr [:td "EXP"] [:td exp]]
           [:tr [:td "G"] [:td gamel]]]]]]]
      [:div.personality {:style {:font-size (vw 1.5)}}
       [:div.nes-container.is-dark.is-rounded.h-100
        [:table.nes-table.is-dark.w-100
         [:tbody
          [:tr [:td "性別"] [:td (case gender "male" "男" "female" "女" "LGBTTIQQ2SA")]]
          [:tr [:td "年齢"] [:td age]]
          [:tr [:td "穢れ"] [:td impurity]]
          [:tr [:td "名誉点"] [:td honor]]
          ]]]]
      [:div.equipments {:style {:font-size (vw 1.5)}}
       [:div.nes-container.is-dark.is-rounded.h-100
        [:table.nes-table.is-dark.w-100
         [:tbody
          [:tr [:td "武器"] [:td (when weapons
                                   (into [:div]
                                         (for [w weapons]
                                           [:div (w :name)])))]]
          [:tr [:td "盾"] [:td (:name shield)]]
          [:tr [:td "鎧"] [:td (:name armor)]]
          ]]]]
      [:div.parameters {:style {:font-size (vw 1.5)}}
       [:div.nes-container.is-dark.is-rounded.h-100
        [:table.nes-table.is-dark.w-100
         [:thead
          [:tr [:th "能力値"] [:th "基礎"] [:th "初期"] [:th "成長"] [:th "計(含修正)"] [:th "ボーナス"]]]
         (into [:tbody] (keep-indexed (fn [i p]
                                        [:tr
                                         [:td (param-key->str p)]
                                         (when (= (rem i 2) 0)
                                           [:td.center {:rowspan "2"
                                                        :style   {:vertical-align "middle"}}
                                            (base (param->base p))])
                                         [:td.center (initial p)]
                                         [:td.center (growth p)]
                                         [:td.center (total p)]
                                         [:td.center (bonus p)]])
                                      [:dex :agi :str :con :int :pow]))]]]
      [:div.package {:style {:font-size (vw 1.5)}}
       [:div.nes-container.is-dark.is-rounded.h-100
        [:table.nes-table.is-dark.w-100
         [:thead
          [:tr [:th "判定パッケージ"] [:th "技巧"] [:th "運動"] [:th "観察"] [:th "知識"]]]
         [:tbody
          [:tr [:td "スカウト"] [:td (scout :dex)] [:td (scout :agi)] [:td (scout :int)] [:td "-"]]
          [:tr [:td "レンジャー"] [:td (ranger :dex)] [:td (ranger :agi)] [:td (ranger :int)] [:td "-"]]
          [:tr [:td "セージ"] [:td "-"] [:td "-"] [:td "-"] [:td (sage :int)]]]]]]
      [:div.languages {:style {:font-size (vw 1.5)}}
       [:div.nes-container.is-dark.is-rounded.h-100
        (into [:div.d-flex.flex-row {:style {:width (vw 50) :overflow "auto"}}]
              (for [groups (let [rows 4] (partition rows rows (repeat {}) (char :languages)))]
                [:table.nes-table.is-dark.text-nowrap {:style {:min-width (vw 20)}}
                 [:thead
                  [:tr [:th "言語"] [:th "会話"] [:th "読文"]]]
                 (into [:tbody]
                       (for [l groups]
                         (let [->str #(if (boolean? %) (if % "◯" "-") %)]
                           [:tr
                            [:td (->str (l :name))]
                            [:td.center (->str (l :oral))]
                            [:td.center (->str (l :literal))]])))]))]]])])

(defn- status-skills [char idx]
  [:div#character-status-skills
   [:div.nes-container.is-dark.is-rounded.grid-container {:style {:width (vw 90) :height (vw 50)}}
    [:div.aux-left
     [:div.nes-container.is-dark.is-rounded.d-flex.flex-row.align-items-center.justify-content-center
      {:style {:height (vw 7)}
       :on-click #(move-page -1)}
      [:div "◁"]]]
    [:div.aux-right
     [:div.nes-container.is-dark.is-rounded.d-flex.flex-row.align-items-center.justify-content-center
      {:style {:height (vw 7)}
       :on-click #(move-page 1)}
      [:div "▷"]]]
    [:div.abilities {:style {:font-size (vw 1.5)}}
     [:div.nes-container.is-dark.is-rounded.h-100
      [:h4.ml-2 "技能"]
      (into [:div.d-flex.flex-row]
            (for [groups (let [rows 7] (partition rows rows (repeat {}) (char :abilities)))]
              [:table.nes-table.is-dark.text-nowrap {:style {:min-width (vw 20)}}
               (into [:tbody]
                     (for [a groups]
                       [:tr
                        [:td (a :name)]
                        [:td.center (a :level)]]))]))]]
    [:div.skills
     ]
    ]])

(defn main [char idx]
  (case @page-idx
    0 [status-main char idx]
    1 [status-skills char idx]))
