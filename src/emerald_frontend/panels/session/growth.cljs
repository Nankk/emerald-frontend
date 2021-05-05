(ns emerald-frontend.panels.session.growth
  (:require
   [emerald-frontend.util.css :refer [vw]]
   [emerald-frontend.const :as const]
   [emerald-frontend.components.button :refer [checkbox]]
   [emerald-frontend.subs :as subs]
   [emerald-frontend.util.http :as http]
   [emerald-frontend.panels.session.util :refer [<update-char]]
   [cljs.core.async :refer [<! go]]
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [clojure.string :as str]
   [goog.string :as gstring]
   [goog.string.format]
   [emerald-frontend.components.panel :as panel]
   [emerald-frontend.components.list :as list]
   [clojure.set :as set]))

(def param-key->str
  {:dex "器用度"
   :agi "敏捷度"
   :str "筋力"
   :con "生命力"
   :int "知力"
   :pow "精神力"})

(defn- summary [char]
  (let [idx (:idx char)
        {{{:keys [first middle last]} :name}     :personality
         {:keys [level max-hp max-mp hp mp exp]} :status}
        char]
    [:div.font-size-3.px-1.h-100.hstack-vcenter
     {:style {:width (vw 35)}}
     [:div.hstack-vcenter
      [:img.profile-image.mx-3
       {:src   (str const/server-url "/characters/" idx "/image")
        :style {:width (vw 12) :height (vw 12)}}]
      [:div
       [:div (str first " " middle " " last)] ; Name
       [:table.nes-table.is-dark.w-50
        [:tbody
         [:tr [:td "Lv"] [:td level]]
         [:tr [:td "HP"] [:td (str hp "/" max-hp)]]
         [:tr [:td "MP"] [:td (str mp "/" max-mp)]]
         [:tr [:td "Exp"] [:td (:free exp)]]]]]]]))

(def parameter-upgrader
  (let [buffed? (reagent/atom true)]
    (fn [char]
      (let [{{:keys [total total-buffed]} :parameters} char]
        ;; Header
        [:div.base-container.hstack.font-size-4.vw-50
         [:div.border-right.px-3
          [:div "能力値"]
          [:div.font-size-3
           [checkbox {:scale    1.0
                      :checked  true
                      :on-click (fn [current] (swap! buffed? not))}
            "含修正"]]]
         ;; Table
         [:div.hstack-vcenter
          [:table.nes-table.is-dark
           (into [:tbody] (for [ps (partition 2 [:dex :agi :str :con :int :pow])]
                            (->> (for [p ps]
                                   [[:td (param-key->str p)]
                                    [:td.center (if @buffed?
                                                  (total-buffed p)
                                                  (total p))]])
                                 (apply concat)
                                 (into [:tr]))))]
          ;; Upgrade button
          (let [upgrade-rest (get-in char [:status :achievements :free])
                upgradable?  (not= upgrade-rest 0)]
            [:button.nes-btn.is-primary
             {:class (if upgradable?
                       "bounce-p"
                       "is-disabled")
              :on-click #(when upgradable?
                           (panel/show-modal :growth/params-up))}
             [:div "成長"]
             [:div.font-size-3 {:style {:width (vw 9)}}
              (if (pos? upgrade-rest)
                (str "あと" upgrade-rest "回")
                "できません")]])]]))))

(defonce cur-ability (reagent/atom nil))

(def ability-upgrader
  (let [exp-table (reagent/atom nil)]
    (go (reset! exp-table (:body (<! (http/<get-data "/tables/exp")))))
    (fn [char]
      (when @exp-table
        (let [abl-list const/abilities
              cur-lvs  (into {} (for [{:keys [name level]} (:abilities char)] [name level]))
              table    @exp-table]
          [:div.base-container.hstack.h-100
           ;; Header
           [:div.border-right.px-3.mr-3.h-100
            "技能"]
           ;; Flexbox
           (into [:div.vstack.flex-wrap.font-size-3 {:style {:height (vw 31)}}]
                 (for [{:keys [name]} abl-list
                       :let           [lv (or (cur-lvs name) 0)
                                       type (:table (first (filter #(= (% :name) name) abl-list)))
                                       required (type (first (filter #(= (% :level) (inc lv)) table)))
                                       upgradable? (>= (get-in char [:status :exp :free]) required)]]
                   ;;; Each ability
                   [:div.base-container.px-3.py-2.m-1
                    {:style    {:width   (vw 19.4)
                                :height  (vw 6)
                                :opacity (if upgradable? 1 0.5)}
                     :on-click #(do
                                  (reset! cur-ability {:name     name
                                                       :lv       lv
                                                       :required required})
                                  (panel/show-modal :growth/ability-up))}
                    [:div.hstack-vcenter
                     ;; Icon
                     [:div.mr-2 {:style {:background-color "#888888"
                                         :width            (vw 3)
                                         :height           (vw 3)}}]
                     ;; Content
                     [:div.w-100
                      [:div name]
                      [:div.hstack-justify
                       [:div (str "必要:" required)]
                       [:div.font-size-3 (str "Lv " lv)]]]]]))])))))

(defn- container [char]
  (when char
    [:div#growth
     [:div.base-container.grid-container
      {:style {:width (vw 90) :height (vw 50)}}
      ;;; Upper
      [:div.upper.hstack-justify.h-100
       [summary char]
       [parameter-upgrader char]]
      ;;; Lower
      [:div.lower.w-100.h-100.font-size-4
       [ability-upgrader char]]]]))

(def params-up
  ;; `targets` prevents arbitrary selection by infinite retry
  (let [targets (reagent/atom nil)]
    (fn [char]
      (when-not @targets
        (reset! targets (repeatedly 2 #(inc (rand-int 6)))))
      (let [param-names (vals param-key->str)
            choices     (set (map #(nth param-names (dec %)) @targets))]
        [:div.base-container.vw-25.font-size-5
         [:div "成長するものを選択"]
         [:hr.my-3]
         [list/choice
          :choices
          (vec (concat (for [c choices]
                         {:text c
                          :on-click
                          #(let [str->param-key (set/map-invert param-key->str)]
                             (<update-char
                              (-> char
                                  (update-in ,, [:parameters :growth (str->param-key c)] inc)
                                  (update-in ,, [:status :achievements :used] inc))
                              (char :idx)
                              {:wait-message "通信中…"})
                             (panel/close-modal :growth/params-up)
                             (reset! targets nil))})
                       [{:text     "やめる"
                         :on-click #(panel/close-modal :growth/params-up)}]))]]))))

(defn ability-up [char]
  (when-let [ability @cur-ability]
    (let [{:keys [name lv required]} ability
          cur-exp                    (-> char :status :exp :free)]
      [:div.base-container.vw-45
       [:div.font-size-5.px-5 (str name " Lv " lv " → Lv " (inc lv))]
       [:hr.mb-4]
       [:div.font-size-4.px-8
        [:div.hstack-justify [:div "現在経験点"] [:div cur-exp]]
        [:div.hstack-justify [:div "必要経験点"] [:div required]]
        [:div.hstack-justify [:div "レベルアップ後"] [:div (- cur-exp required)]]]
       [list/choice
        :choices [{:text "決定"
                   :on-click
                   (fn []
                     (let [idx (first (keep-indexed
                                       #(when (= (%2 :name) name) %1)
                                       (char :abilities)))]
                       (println (-> char
                                    (update ,, :abilities assoc idx {:name name :level (inc lv)})
                                    (update-in ,, [:status :exp :used] + required)))
                       ;; (<update-char
                       ;;  (-> char
                       ;;      (update ,, :abilities assoc idx {:name name :level (inc lv)})
                       ;;      (update-in ,, [:status :exp :used] + required))
                       ;;  (:idx char))
                       ))}
                  {:text "やめる" :on-click #(panel/close-modal :growth/ability-up)}]]]
      )))

(defn main [char]
  [:div
   [container char]
   [panel/modal :growth/params-up [(vw 10) (vw 70)] [params-up char]]
   [panel/modal :growth/ability-up [(vw 20) (vw 30)] [ability-up char]]]
  )

(comment
  ;; For px-style css conversion to zoomable (development only)
  (let [base     0.2
        base-str "calc(%.1f * min(1vw, 1vh * 16 / 9))"
        m        (for [i (reverse (range 20))]
                   [(str (* 2 i) "px") i])
        _ (println m)
        raw "2px 2px,4px 2px,6px 2px,8px 2px,10px 2px,12px 2px,14px 2px,18px 2px #fff,20px 2px #fff,2px 4px,16px 4px #fff,18px 4px #fff,20px 4px #fff,2px 6px #fff,14px 6px #fff,16px 6px #fff,2px 8px #fff,4px 8px #fff,12px 8px #fff,14px 8px #fff,2px 10px,4px 10px #fff,6px 10px #fff,10px 10px #fff,12px 10px #fff,16px 10px,2px 12px,6px 12px #fff,8px 12px #fff,10px 12px #fff,16px 12px,2px 14px,8px 14px #fff,16px 14px,2px 16px,4px 16px,6px 16px,8px 16px,10px 16px,12px 16px,14px 16px,16px 16px"]
    (println (loop [s  raw
                    -m m]
               (if (empty? -m)
                 s
                 (let [[px num] (first -m)]
                   (recur (str/replace s px (gstring/format base-str (* base num)))
                          (rest -m))))))))

