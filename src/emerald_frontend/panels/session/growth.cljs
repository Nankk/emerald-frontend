(ns emerald-frontend.panels.session.growth
  (:require
   [emerald-frontend.util.css :refer [vw]]
   [emerald-frontend.util.js :as util.js]
   [emerald-frontend.const :as const]
   [emerald-frontend.components.button :refer [checkbox]]
   [emerald-frontend.subs :as subs]
   [emerald-frontend.util.http :as http]
   [emerald-frontend.panels.session.util :refer [<update-char]]
   [emerald-frontend.util.async :refer [go? <?]]
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [clojure.string :as str]
   [goog.string :refer [format]]
   [goog.string.format]
   [emerald-frontend.components.panel :as panel]
   [emerald-frontend.components.list :as list]
   [emerald-frontend.util.js :refer [by-id]]
   [emerald-frontend.macros.util :refer [when-let*]]
   [clojure.set :as set]
   [emerald-frontend.macros.util :as util]))

(def param-key->str
  {:dex "器用度"
   :agi "敏捷度"
   :str "筋力"
   :con "生命力"
   :int "知力"
   :pow "精神力"})

(defn- summary []
  (when-let [char @(rf/subscribe [::subs/cur-char])]
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
           [:tr [:td "Exp"] [:td (:free exp)]]]]]]])))

(defn move-elem [coll idx diff]
  (let [from-v (get coll idx)
        to     (min (max 0 (+ idx diff)) (dec (count coll)))
        to-v   (get coll to)]
    (assoc coll idx to-v to from-v)))

(def param-probabilities
  (map #(/ % 21) (reverse (map inc (range 6)))))

(defn growth-counts [ordered-param-keys achievements]
  (def achievements 6)
  (def ordered-param-keys [:dex :agi :int :pow :str :con])
  (let [growths              (vec (map #(Math/round (* achievements %)) param-probabilities))
        diff                 (- achievements (reduce + growths))
        growths-fraction-rev
        (case diff
          0  growths
          1  (let [min-add (apply min growths)
                   add-idx (first (keep-indexed #(when (= %2 min-add) %1) growths))]
               (update growths add-idx inc))
          -1 (let [min-sub (apply #(min 1 %) growths)
                   sub-idx (last (keep-indexed #(when (= %2 min-sub) %1) growths))]
               (update growths sub-idx dec))
          (throw (js/Error. "Erroneous fraction: " diff)))]
    (zipmap ordered-param-keys growths-fraction-rev)))

(def params-up-at-once
  (let [params    (reagent/atom (vec (keys param-key->str)))
        cur-param (reagent/atom (first @params))]
    (fn []
      (when-let [char @(rf/subscribe [::subs/cur-char])]
        [:div.base-container.hstack-justify.vw-70.vh-40
         [:div.hstack
          (into [:div.base-container.vw-25.h-100]
                (for [param @params]
                  [:div.base-container
                   (merge {:on-click #(reset! cur-param param)}
                          (when (= param @cur-param)
                            {:style {:border-radius (vw 1)
                                     :background-color "#306088"}}))
                   (param-key->str param)]))
          [:div
           [:div.base-container
            {:on-click (fn []
                         (swap! params
                                (fn [ps]
                                  (let [idx (first (keep-indexed #(when (= %2 @cur-param) %1) ps))]
                                    (move-elem ps idx -1)))))}
            "▲"]
           [:div.base-container
            {:on-click (fn []
                         (swap! params
                                (fn [ps]
                                  (let [idx (first (keep-indexed #(when (= %2 @cur-param) %1) ps))]
                                    (move-elem ps idx 1)))))}
            "▼"]]]
         [:div.vstack-justify.vw-35.h-100
          [:div
           [:div.text-center.font-size-5.py-4 "自動成長割り振り"]
           [:div "優先的に成長させたいパラメータが上に来るように並べ替えてください。"]
           [:div "確率に基づいて分配されます。"]]
          [:div.hstack-justify
           [:div]
           [:a.nes-btn.is-primary
            {:style {:width (vw 10)}
             :on-click
             (fn []
               (let [growths (growth-counts @params (get-in char [:status :achievements :free]))]
                 (<update-char
                  (-> char
                      (update-in ,, [:parameters :growth] #(merge-with + % growths))
                      (assoc-in ,, [:status :achievements :used]
                                   (get-in char [:status :achievements :total])))
                  (char :idx))
                 (panel/close-modal :growth/params-up-at-once)))}
            "確定"]]]]))))

(def parameter-upgrader
  (let [buffed? (reagent/atom true)]
    (fn []
      (when-let [char @(rf/subscribe [::subs/cur-char])]
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
              [:div
               [:a.nes-btn.is-primary
                {:class    (if upgradable?
                             "bounce-p"
                             "is-disabled")
                 :on-click #(when upgradable? (panel/show-modal :growth/params-up))}
                [:div "成長"]
                [:div.font-size-3 {:style {:width (vw 9)}}
                 (if (pos? upgrade-rest)
                   (str "あと" upgrade-rest "回")
                   "できません")]]
               (when upgradable?
                 [:a.text-center.d-block.mt-2.font-size-2
                  {:on-click #(panel/show-modal :growth/params-up-at-once)
                   :style {:background-color "rgba(0, 0, 0, 0.5)"
                           :color            "#209cee"}}
                  "一括で振り分ける？"])])]])))))

(defonce cur-ability (reagent/atom nil))

(defn ->hex [num]
  (cond
    (> num 255) "FF"
    (< num 0)   "00"
    :else       (let [hex-raw (.toString (int num) 16)
                      hex     (if (= (count hex-raw) 1) (str "0" hex-raw) hex-raw)]
                  hex)))

(def ability-upgrader
  (let [exp-table    (reagent/atom nil)]
    (go? (reset! exp-table (:body (<? (http/<get-data "/tables/exp")))))
    (fn []
      (when-let* [char         @(rf/subscribe [::subs/cur-char])
                  exp-table    @exp-table]
        (let [abl-list const/abilities
              cur-lvs  (:abilities char)]
          [:div.base-container.hstack.h-100
           ;; Header
           [:div.border-right.px-3.mr-3.h-100
            "技能"]
           ;; Flexbox
           (into [:div.vstack.flex-wrap.font-size-3 {:style {:height (vw 31)}}]
                 (for [{:keys [name]} abl-list
                       :let
                       [lv (or (cur-lvs (keyword name)) 0)
                        type (:table (first (filter #(= (% :name) name) abl-list)))
                        required (type (first (filter #(= (% :level) (inc lv)) exp-table)))
                        upgradable? (and (>= (get-in char [:status :exp :free]) required)
                                         (< lv 15))
                        bg-color (if (>= lv 15)
                                   "#997700"
                                   (let [v   (* 40 (Math/sqrt lv))
                                         rgb [0.0 0.5 1.0]]
                                     (apply str "#" (map #(->hex (+ 30 (* v %))) rgb))))]]
                   ;; Each ability
                   [:a.base-container.px-3.py-2.m-1
                    {:style    {:width            (vw 19.4)
                                :height           (vw 6)
                                :opacity          (if (or upgradable?
                                                          (>= lv 15)) 1 0.5)
                                :border-radius    (vw 1)
                                :background-color bg-color}
                     :on-click #(when upgradable?
                                  (reset! cur-ability {:name     name
                                                       :lv       lv
                                                       :required required})
                                  (panel/show-modal :growth/ability-up))}
                    [:div.position-relative
                     [:div.hstack-vcenter
                      ;; Icon
                      [:div.mr-2 {:style {:background-color "#888888"
                                          :width            (vw 3)
                                          :height           (vw 3)}}]
                      ;; Content
                      [:div.w-100
                       [:div name]
                       [:div.hstack-justify
                        [:div (if (>= lv 15)
                                "★MASTER"
                                (str "必要:" required))]
                        [:div.font-size-3 (str "Lv " lv)]]]]
                     (when upgradable?
                       [:div.position-absolute.bounce-p.font-size-3
                        {:style {:top     (vw -0.1) :right (vw -0.5)
                                 :opacity 0.8
                                 :color   "#eeee22"}}
                        [:span "Up!"]
                        [:i.fas.fa-angle-double-up]])]]))])))))

(defn- container []
  [:div#growth
   [:div.base-container.grid-container
    {:style {:width (vw 90) :height (vw 50)}}
      ;;; Upper
    [:div.upper.hstack-justify.h-100
     [summary]
     [parameter-upgrader]]
      ;;; Lower
    [:div.lower.w-100.h-100.font-size-4
     [ability-upgrader]]]])

(def params-up
  ;; `targets` prevents arbitrary selection by infinite retry
  (let [targets (reagent/atom nil)]
    (fn []
      (when-not @targets
        (reset! targets (repeatedly 2 #(inc (rand-int 6)))))
      (when-let [char @(rf/subscribe [::subs/cur-char])]
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
                                (char :idx))
                               (panel/close-modal :growth/params-up)
                               (reset! targets nil))})
                         [{:text     "やめる"
                           :on-click #(panel/close-modal :growth/params-up)}]))]])))))

(defonce skills-table (reagent/atom nil))

(def skill-type->str
  {:master  "武器/防具習熟"
   :attack  "攻撃系"
   :defense "防御系"
   :magic   "魔法系"
   :other   "その他技能系"})

(defn magical-acquirable? [char {:keys [req-lv abilities] :as skill}]
  (let [magi-abl-names (map :name (filter
                                   #(get-in % [:category :magician])
                                   const/abilities))]
    (some
     true?
     (for [ability abilities]
       (let [num          (js/parseInt (second (or (re-find #"([0-9]+)種" ability) [nil "1"])))
             matures      (filter (fn [[key lv]]
                                    (and (some #{(name key)} magi-abl-names)
                                         (>= lv req-lv)))
                                  (-> char :abilities))
             mature-count (count
                           (filter (fn [[key lv]]
                                     (and (some #{(name key)} magi-abl-names)
                                          (>= lv req-lv)))
                                   (-> char :abilities)))]
         (>= mature-count num))))))

(defn acquirable? [char skill]
  (let [{:keys [req-lv dependents abilities]} skill
        {{{:keys [manual auto]} :general} :skills}    char
        existing-skills                               (concat (or manual []) (or auto []))]
    (and (>= (-> char :status :level) req-lv)
         (if-not (empty? dependents) (every? #(some #{%} existing-skills) dependents) true)
         ;; Ability check (special treatment for "one of magical skills")
         (if-not (empty? abilities)
           (if (some #(re-find #"魔法使い系" %) abilities)
             (magical-acquirable? char skill)
             (every? #(>= (get-in char [:abilities (keyword %)]) req-lv) abilities))
           true))))

(defn skill-acquisition-tab [char cur-type available-only?]
  (let [x-pos   (atom 0)
        x-shift (util.js/vw 15)]
    [:div.tab
     [:div.hstack-vcenter
      [:div.vw-10.text-center "カテゴリ"]
      [:div.hstack-justify-vcenter {:style {:width (vw 37)}}
       [:div {:on-click (fn []
                          (swap! x-pos #(max 0 (- % x-shift)))
                          (.scrollTo (by-id "tabs") @x-pos 0))} "◀"]
       (into
        [:div#tabs.hstack.flex-nowrap.overflow-hidden {:style {:width (vw 30)}}]
        (for [[k s] skill-type->str]
          [:div.base-container.text-nowrap
           (when (not= k @cur-type)
             {:class    "border-0"
              :on-click #(reset! cur-type k)})
           s]))
       [:div {:on-click (fn []
                          (swap! x-pos #(min (-> (by-id "tabs") .-scrollWidth) (+ % x-shift)))
                          (.scrollTo (by-id "tabs") @x-pos 0))} "▶"]]]
     [:div.hstack-vcenter-justify.px-3
      [:div]
      [checkbox {:checked  true
                 :on-click #(swap! available-only? not)}
       [:div.font-size-3 "習得可能な技能のみ表示"]]]]))

(defn skill-acquisition-list [char cur-type cur-skill available-only?]
  (into
   [:div.vstack.list.h-100.overflow-auto]
   (for [skill (->> @skills-table
                    (filter #(and (some #{(name @cur-type)} (% :categories))
                                  (not (% :auto?))
                                  (not (% :replace))) ,,))]
     ;; Each skill
     [:div.base-container.hstack-justify
      {:on-click #(reset! cur-skill skill)
       :style
       {:border-radius    (vw 1)
        :background-color (when (= (get @cur-skill :name) (skill :name)) "#306088")
        :opacity          (when-not (acquirable? char skill) 0.5)
        :display          (when (or (some #{(skill :name)}
                                          (get-in char [:skills :general :manual]))
                                    (and @available-only?
                                         (not (acquirable? char skill)))) "none")}}
      [:div (skill :name)]])))

(defn skill-acquisition-bottom [char cur-skill]
  [:div.description.font-size-3
   [:hr]
   [:div.hstack-justify.p-3.h-100
    ;; Description
    [:div.overflow-auto {:style {:width  (vw 37)
                                 :height (vw 8.9)}}
     (when-let [{:keys [req-lv dependents abilities]} @cur-skill]
       [:div
        [:div (str "《" (get @cur-skill :name) "》")]
        [:div.pb-2 (get @cur-skill :abstract)]
        [:div.font-size-2 {:style {:color "#aaa"}}
         [:div
          (str  "必要："
                (when abilities
                  (str/replace (str/join "、" abilities)
                               #"、([^、]+$)"
                               #(str "、または" (%1 1))))
                " Lv " req-lv)]
         [:div (when dependents
                 (str
                  "前提特技："
                  (str/replace (str/join "、" (filter some? dependents))
                               #"、([^、]+$)"
                               #(str "、または" (%1 1)))))]]])]
    ;; Determine
    [:div.hstack-vcenter-hcenter.h-100 {:style {:min-width (vw 8)}}
     [:button.nes-btn.is-primary.text-nowrap
      {:class    (when (or (not @cur-skill)
                           (not (acquirable? char @cur-skill))) "is-disabled")
       :on-click (fn []
                   (<update-char
                    (update-in char [:skills :general :manual] conj (@cur-skill :name))
                    (char :idx))
                   (panel/close-modal :growth/skill-acquisition))}
      "決定"]]]])

(def skill-acquisition
  (let [cur-type        (reagent/atom :master)
        cur-skill       (reagent/atom nil)
        available-only? (reagent/atom true)]
    (def cs @cur-skill)
    (fn []
      (when-let [char @(rf/subscribe [::subs/cur-char])]
        [:div#skill-acquisition.base-container.font-size-4
         {:style {:width  (vw 50)
                  :height (vw 50)}}
         [:div.grid-container.h-100
          [skill-acquisition-tab char cur-type available-only?]
          [skill-acquisition-list char cur-type cur-skill available-only?]
          [skill-acquisition-bottom char cur-skill]]]))))

(def ability-up
  (let []
    (go? (reset! skills-table (:body (<? (http/<get-data "/tables/skills")))))
    (fn []
      (when-let* [ability      @cur-ability
                  char         @(rf/subscribe [::subs/cur-char])
                  skills-table @skills-table]
        (let [{:keys [name lv required]} ability
              cur-exp                    (-> char :status :exp :free)]
          [:div.base-container.vw-45
           [:div.font-size-5.px-5 (str name " Lv " lv " → Lv " (inc lv))]
           [:hr.mb-4]
           [:div.font-size-4.px-8.mb-5
            [:div.hstack-justify [:div "現在経験点"] [:div cur-exp]]
            [:div.hstack-justify [:div "必要経験点"] [:div required]]
            [:div.hstack-justify [:div "レベルアップ後"] [:div (- cur-exp required)]]]
           [:div.hstack-vcenter-hcenter
            [list/choice
             :direction :horizontal
             :choices [{:text "決定"
                        :on-click
                        (fn []
                          (let [new-char (-> char
                                             (update-in ,, [:abilities (keyword name)] #(inc (or % 0)))
                                             (update-in ,, [:status :exp :used] + required))]
                            (<update-char new-char (:idx new-char))
                            (when (and (> (inc lv) (-> char :status :level))
                                       (= (mod (inc lv) 2) 1))
                              (panel/show-modal :growth/skill-acquisition))
                            (panel/close-modal :growth/ability-up)))}
                       {:text "やめる" :on-click #(panel/close-modal :growth/ability-up)}]]]])))))

(defn main []
  [:div
   [container]
   [panel/modal :growth/params-up [(vw 10) (vw 65)] [params-up]]
   [panel/modal :growth/params-up-at-once [(vw 8) (vw 14)] [params-up-at-once]]
   [panel/modal :growth/ability-up [(vw 14) (vw 24)] [ability-up]]
   [panel/modal :growth/skill-acquisition [(vw 2) (vw 25)]
    {:backdropper-rigid? true}
    [skill-acquisition]]]
  #_[params-up-at-once])

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
                   (recur (str/replace s px (format base-str (* base num)))
                          (rest -m))))))))

