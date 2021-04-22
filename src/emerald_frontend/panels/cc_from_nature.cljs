(ns emerald-frontend.panels.cc-from-nature
  (:require [async-interop.interop :refer-macros [<p!]]
            [cljs.core.async :refer [<! >! go chan]]
            [cljs.reader :as reader]
            [clojure.string :as str]
            [emerald-frontend.components.container :as container]
            [emerald-frontend.components.list :as list]
            [emerald-frontend.components.panel :as panel]
            [emerald-frontend.const :as const]
            [emerald-frontend.macros.async :refer [defan defan-]]
            [emerald-frontend.style.colors :as colors]
            [emerald-frontend.util.http :as http]
            [emerald-frontend.util.js :refer [by-id log]]
            [cljs-http.client :as client]
            [re-frame.core :as rf]
            [reagent.core :as reagent]
            [clojure.walk :as walk]
            [emerald-frontend.util.css :refer [vw]]))

(def char-default
  {:personality {:name    {:first "" :middle "" :last ""}
                 :age     20
                 :job     "操霊術師"
                 :gender  "male"
                 :species {:name "人間"}}
   :equipments  {}
   :accessories {:head       []
                 :foot       []
                 :neck       []
                 :face       []
                 :back       []
                 :waist      []
                 :others     []
                 :left-hand  []
                 :ear        []
                 :right-hand []}
   :abilities   [{:name "スカウト" :level 1}]
   :skills      {:general {:manual []}}
   :status      {:exp {:total 0 :used 0}
                 :honor 0}
   :options     {:nature {:born        {:name   "芸術の民"
                                        :detail "芸術に影響を受けやすい環境にあった。古来の舞踊を伝承する民族かもしれないし、スラムの壁に描かれた絵に天啓を見たのかもしれない。"}
                          :infancy     {:event     {:name   "神殿で過ごす"
                                                    :detail "神の教えを学び、数多くの奉仕に努めた。そのことで、数多くの知識や技術を身につけることができた。"
                                                    :type   "汎用魔法使い"}
                                        :happening {:name "身分に関係なく、立派な仕事に就けた"}}
                          :childhood   {:event     {:name   "フィールドワーク過多"
                                                    :detail "きみの師匠、あるいはきみの学んだ環境では、野外や遺跡に出向き、調査することを特に重視していた。きみはそちらにも才能があった。"}
                                        :happening {:name "一定期間の記憶がない"}}
                          :adolescence {:event            {:name   "鍛錬と根性"
                                                           :detail "気を失うほどの鍛錬の果てに手に入れた技は、多くの人を救うだろう。こつこつと、地道に、魔法をかける対象の数を2つ、3つと増やしていった。"}
                                        :departure-reason {:name "友達に勧められて"}}}}
   :items       []
   :languages   [{:name "エルフ語" :oral true :literal true}]
   :parameters  {:base    {:physique 1 :technique 1 :mind 1}
                 :initial {:dex 1 :agi 1 :str 1 :con 1 :int 1 :pow 1}
                 :growth  {:dex 1 :agi 1 :str 1 :con 1 :int 1 :pow 1}}})

(defonce cur-char (reagent/atom char-default))

;;; Confirmation ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(do

  (defonce *errors (atom nil))

  (defn- merge-abilities [vec1 vec2]
    (loop [abils vec1
           adds  vec2]
      (if-not (empty? adds)
        (let [add (first adds)]
          (recur (if (some #{(add :name)} (map :name abils))
                   (let [idx (first (keep-indexed #(when (= (%2 :name) (add :name)) %1) abils))]
                     (update-in abils [idx :level] #(+ % (add :level))))
                   (conj abils add))
                 (rest adds)))
        abils)))

  (defn- character-formatted
    "Returns a formatted character for character storage server.
    :additional-items are merged into main status and removed."
    [character]
    (let [c character
          c (cond-> c
              ;; Origin
              (get-in character [:personality :species :origin])
              (update ,, :languages (comp vec concat)
                         ((first
                           (filter #(= (% :name) (get-in character [:personality :species :origin]))
                                   const/species-list))
                          :languages))
              ;; Abilities
              (get-in character [:additional-items :abilities])
              (update ,, :abilities #(merge-abilities % (get-in character [:additional-items :abilities]))))
          c (update c :languages (comp vec concat) (get-in character [:additional-items :languages]))
          c (update-in c [:status :exp :total] #(+ % (get-in character [:additional-items :exp])))
          c (let [exp-diff (* (count (get-in character [:additional-items :abilities])) 500)]
              (update-in c [:status :exp :used] #(+ % exp-diff)))
          c (dissoc c :additional-items)
          c (walk/postwalk #(if (map? %) (into {} (remove (comp nil? second) %)) %) c)]
      c))

  (defn- confirmation-biography [character]
    (let [{{{{babst :abstract bdetail :detail}                                                :born
             {{iabst :abstract idetail :detail} :event {ihappen :abstract} :happening}        :infancy
             {{cabst :abstract cdetail :detail} :event {chappen :abstract} :happening}        :childhood
             {{aabst :abstract adetail :detail} :event {areason :abstract} :departure-reason} :adolescence} :nature} :options}
          ;; Deep-nested binding
          character]
      [:div.biography.w-100.h-100.overflow-auto
       [:div.nes-container.is-dark.is-rounded
        [:div
         [:h5 (str "生まれ：" babst)]
         [:hr]
         [:h5 bdetail]
         [:h5 "　"]
         [:h5 (str "幼少期：" iabst)]
         [:hr]
         [:h5 idetail]
         [:h5 "　"]
         [:h5 (str "幼少期のハプニング")]
         [:hr]
         [:h5 ihappen]
         [:h5 "　"]
         [:h5 (str "少年期：" cabst)]
         [:hr]
         [:h5 cdetail]
         [:h5 "　"]
         [:h5 (str "少年期のハプニング")]
         [:hr]
         [:h5 chappen]
         [:h5 "　"]
         [:h5 (str "青年期：" aabst)]
         [:hr]
         [:h5 adetail]
         [:h5 "　"]
         [:h5 "冒険に出た理由"]
         [:hr]
         [:h5 areason]]]]))

  (defn- confirmation-skills [character]
    [:div.skills.w-100.h-100
     [:div.nes-container.is-dark.is-rounded.w-100.h-100.d-flex.flex-row
      [:div {:style {:width        "33%"
                     :border-right (str (vw 0.4) " solid " (colors/base 10))}}
       [:h5 "技能"]
       [:hr]
       (into [:div]
             (for [ability (character :abilities)]
               [:div.d-flex.flex-row.justify-content-between.w-100
                [:h5 (ability :name)]
                [:h5 (str "Lv " (ability :level))]]))]
      [:div {:style {:width        "33%"
                     :border-right (str (vw 0.4) " solid " (colors/base 10))}}
       [:h5 "戦闘特技"]
       [:hr]
       (into [:div]
             (for [skill (get-in character [:skills :general :manual])]
               [:h5 skill]))]
      [:div {:style {:width "33%"}}
       [:h5 "言語"]
       [:hr]
       (into [:div]
             (for [language (character :languages)]
               [:h5 (str (language :name) " [" (when (language :oral) "話") (when (language :literal) "読") "]")]))]]])

  (defn- confirmation-params [character]
    [:div.params.w-100.h-100
     [:div.nes-container.is-dark.is-rounded.w-100.h-100.d-flex.flex-row
      [:div.nes-container.is-dark.is-rounded {:style {:width    (vw 15)    :height  (vw 15)
                                                      :position "relative" :padding (vw 1)}}
       ;; Picture area
       (if-let [image (get-in character [:options :image])]
         [:img.profile-image.w-100.h-100 {:src (str "data:" (image :type) ";base64," (image :base64))}]
         [:div "I am a default image!"])]
      [:div
       (let [{fname :first mname :middle lname :last} (get-in character [:personality :name])
             {species :name origin :origin}           (get-in character [:personality :species])]
         [:div
          [:h5 (str fname
                    (when-not (empty? mname) (str " " mname))
                    (when-not (empty? lname) (str " " lname))
                    " (" (get-in character [:personality :age]) ")")]
          [:h5 (str "[ " species (when origin (str "（" origin "）")) " ]")]])
       (let [{{{:keys [technique physique mind]}      :base
               {stg :str :keys [dex agi con int pow]} :initial} :parameters}
             character
             lv                   (apply max (map :level (character :abilities)))
             hp                   (+ (+ physique con) (* lv 3))
             magic-lv-total       (reduce + (get (filter #(some #{(% :name)} const/magical-abilities)
                                                         (character :abilities)) 1))
             mp                   (+ (+ mind pow) (* magic-lv-total 3))
             {:keys [total used]} (-> character :status :exp)]
         [:div.d-flex.flex-row.justify-content-between.w-100
          [:div.mr-2
           [:h5 (str "Lv " lv)]
           [:h5 (str "HP " hp "/" hp)]
           [:h5 (str "MP " mp "/" mp)]
           [:h5 (str "Exp " (- total used) "/" total)]]
          [:div
           [:h5 "　"]
           [:h5 (str "筋力 " (+ stg physique))]
           [:h5 (str "知力 " (+ int mind))]
           [:h5 (str "器用 " (+ dex technique))]]
          [:div
           [:h5 "　"]
           [:h5 (str "生命 " (+ con physique))]
           [:h5 (str "精神 " (+ pow mind))]
           [:h5 (str "敏捷 " (+ agi technique))]]])]]])

  (defan- register-character-to-server [character-formatted]
    (panel/show-modal :confirmation-register)
    (let [res (<! (http/<post-data "/characters" character-formatted))]
      (println "応答" res)
      (panel/close-modal :confirmation-register)
      (if (= (res :status) 200)
        (panel/show-modal :confirmation-complete)
        (do (reset! *errors res)
            (panel/show-modal :confirmation-failure)))
      true))

  (defn confirmation-main []
    (let [transient @cur-char
          character (character-formatted transient)]
      [:div#cc-nature-confirmation.w-100.h-100.p-1
       [:div.grid-container.w-100.h-100
        ;; Header
        [:div.nes-container.is-dark.is-rounded.w-100.title.d-flex.flex-row.justify-content-center.align-items-center
         [:h3 "このキャラクターを作成します"]
         [list/choice
          :direction :horizontal
          :choices-size :h4
          :choices [{:text "オッケー" :on-click #(register-character-to-server character)}
                    {:text "待った" :on-click #(container/switch-panels :cc-nature-confirmation :cc-nature-additional-exp :right)}]]]
        ;; Panel
        [:div.nes-container.is-dark.is-rounded.main
         [:div.inner-grid-container.w-100.h-100
          [confirmation-params character]
          [confirmation-skills character]
          [confirmation-biography character]]]]
       [panel/modal :confirmation-register [(vw 20) (vw 35)] {:backdropper-rigid? true}
        [:div.nes-container.is-dark.is-rounded
         [:h3 "キャラクターを登録しています…"]]]
       [panel/modal :confirmation-complete [(vw 20) (vw 35)] {:backdropper-rigid? true}
        [:div.nes-container.is-dark.is-rounded
         [:h3 "作成が完了しました！"]
         [:div.d-flex.flex-row.align-items-center.justify-content-center.w-100
          [:button.nes-btn.is-primary {:on-click #(do (panel/close-all-modals)
                                                      (reset! cur-char char-default)
                                                      (container/switch-panels :cc-nature-confirmation :home :down))}
           "OK"]]]]
       [panel/modal :confirmation-failure [(vw 20) (vw 35)]
        [:div.nes-container.is-dark.is-rounded
         [:h3 "作成に失敗しました。"]
         [:h5 @*errors]
         [:button.nes-btn.is-primary {:on-click #(panel/close-modal :confirmation-failure)}
          "OK"]]]])))


;;; Additional Exps ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(do

  (defonce high-lv-data (atom {}))

  (defan- <load-high-lv-data []
    (println "<load-high-lv-data")
    (let [res (<! (http/<get-data "/tables/high-level-character"))]
      (reset! high-lv-data (res :body))
      true))

  (defn additional-exp-main []
    (<load-high-lv-data)
    [:div#cc-nature-additional-exp.w-100.h-100.p-1
     [:div.grid-container.w-100.h-100
      ;; Header
      [:button.nes-btn.aux-left
       {:on-click #(container/switch-panels :cc-nature-additional-exp :cc-nature-name :right)}
       [:h3 "◁"]]
      [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "追加の成長情報を入力してください"]]
      ;; Panel
      [:div.nes-container.is-dark.is-rounded.main.font-size-4
       [:div "※高レベルキャラクターを作成する場合のみ入力してください。"]
       [:div "　レベル1または2のキャラクターを作成する場合入力は不要です。"]
       [:hr {:style {:margin (str (vw 1) " 0")}}]
       [:div.d-flex.flex-row.font-size-3
        [:div {:style {:width (vw 30)}}
         [:div.text-nowrap "テンプレート"]
         [:div.nes-select.is-dark
          (into [:select {:on-change
                          (fn [x]
                            (let [idx (js/parseInt (.. x -target -value))
                                  items (map vector
                                             [:achievements :additional-exp :g :honor :abyss-shard]
                                             ["成長回数" "追加経験点" "所持金" "名誉点" "アビスシャード"])]
                              (doseq [item items]
                                (set! (.. (by-id (str "additional-" (item 1))) -value)
                                      ((@high-lv-data idx) (item 0))))))}]
                (map-indexed
                 (fn [i data]
                   [:option {:value i}
                    (str "レベル" (data :level))])
                 @high-lv-data))]]
        (into [:div.w-100]
              (for [[text update-fn]
                    [["成長回数" (fn [num] (swap! cur-char #(assoc-in % [:status :achievements] num)))]
                     ["追加経験点" (fn [num] (swap! cur-char #(assoc-in % [:additional-items :exp] num)))]
                     ["所持金" (fn [num] (swap! cur-char #(assoc-in % [:status :g] num)))]
                     ["名誉点" (fn [num] (swap! cur-char #(assoc-in % [:status :honor] num)))]
                     ["アビスシャード" (fn [num] (swap! cur-char #(assoc-in % [:items] [{:uuid "36254798-5944-4699-b6d3-e458cbec8487"
                                                                                         :name "アビスシャード"
                                                                                         :count num}])))]]]
                [:div.d-flex.flex-row.align-items-center.justify-content-between.w-100
                 [:div text]
                 [:input.nes-input.is-dark
                  {:id            (str "additional-" text)
                   :style         {:width (vw 20) :height (vw 5)}
                   :type          "text"
                   :default-value "0"
                   :on-blur       #(let [v         (.. % -target -value)
                                         sanitized (if (js/isNaN v) 0 (js/parseInt v))]
                                     (update-fn sanitized)
                                     (set! (.. % -target -value) sanitized))}]]))]]
      [:button.nes-btn.is-primary.aux-right
       {:on-click #(container/switch-panels :cc-nature-additional-exp :cc-nature-confirmation :left)}
       [:h3 "決定"]]
      ]]))

;;; Name ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(do

  (defonce *name-data (atom {}))

  (defan- <load-name []
    (println "<load-name")
    (let [res (<! (http/<get-data "/cbb/name"))]
      (reset! *name-data (res :body))
      true))

  (defan- <get-image-size [file]
    (let [ch  (chan)
          img (js/Image.)]
      (set! (.-onload img)
            (fn []
              (.revokeObjectURL js/URL (.-src img))
              (go (>! ch {:width (.-width img) :height (.-height img)}))))
      (set! (.-src img) (.createObjectURL js/URL file))
      (<! ch)))

  (defan- <load-image [file]
    (if (<= (.-size file) const/upload-image-size-limit)
      (let [image (merge {:size      (.-size file)
                          :base64    (str/replace (<p! (js/toDataURLPromise file)) #"data.*base64," "")
                          :extension ((re-find #"\.([^\.]+)$" (.-name file)) 1)}
                         (<! (<get-image-size file)))]
        (swap! cur-char (fn [c] (assoc-in c [:options :image] image)))
        true)
      (do (panel/show-modal :name-error)
          false)))

  (defn- profile-image []
    [:div
     ;; Hidden input element
     [:input.hidden-input {:id        "file-selector"
                           :type      "file"
                           :name      "files[]"
                           :accept    "image/*" ; Image files only
                           :on-change (fn [e]
                                        (let [file (first (.. e -target -files))]
                                          (<load-image file)))}]
     ;; Image
     [:div.nes-container.is-dark.is-rounded {:style {:width    (vw 20)     :height  (vw 20)
                                                     :position "relative" :padding (vw 1)}}
      ;; Default style of input elements is ugly so using a label instead
      [:label {:for "file-selector"}
       [:div.nes-btn.is-secondary {:style {:width    (vw 5)      :height (vw 5)    :padding "0px"
                                           :position "absolute" :left   (vw -1.5) :top     (vw -1.5)}}
        [:i.fas.fa-cloud-upload-alt.fa-sm]]]
      ;; Picture area
      (if-let [image (get-in @cur-char [:options :image])]
        [:img.profile-image.w-100.h-100 {:src (str "data:" (image :type) ";base64," (image :base64))}]
        [:div "I am a default image!"])]
     ])

  (defn- random-name [gender species]
    (let [exact-species (or (species :origin) (species :name))
          species-key   (keyword (get-in @*name-data [:種族 0 (keyword exact-species)]))
          first-list    (@*name-data (if (= gender "male") :男性名 :女性名))
          last-list     (@*name-data :姓)
          first-name    (first-list (rand-int (count first-list)))
          last-name     (last-list (rand-int (count last-list)))]
      [(first-name species-key) (last-name species-key)]))

  (defn- name-input []
    (let [{fname :first lname :last} (get-in @cur-char [:personality :name])]
      [:div.w-100
       [:div.d-flex.flex-row.align-items-center.justify-content-end.w-100
        [:div.mr-2 "年齢"]
        [:input.nes-input.is-dark
         {:style         {:width (vw 20) :height (vw 5)}
          :type          "text"
          :placeholder   "Your age"
          :default-value (get-in @cur-char [:personality :age])
          :on-blur       #(let [v   (.. % -target -value)
                                age (if (js/isNaN v) 20 (js/parseInt v))]
                            (swap! cur-char (fn [c] (assoc-in c [:personality :age] age)))
                            (set! (.. % -target -value) age))}]]
       [:div.d-flex.flex-row.align-items-center.justify-content-end.w-100
        [:div.mr-2 "名"]
        [:input#cc-nature-name-first.nes-input.is-dark
         {:style         {:width (vw 30) :height (vw 5)}
          :type          "text"
          :placeholder   "First name"
          :default-value fname
          :on-blur       #(let [v (.. % -target -value)]
                            (swap! cur-char (fn [c] (assoc-in c [:personality :name :first] v)))
                            (set! (.. % -target -value) v))}]]
       [:div.d-flex.flex-row.align-items-center.justify-content-end.w-100
        [:div.mr-2 "姓"]
        [:input#cc-nature-name-last.nes-input.is-dark
         {:style         {:width (vw 30) :height (vw 5)}
          :type          "text"
          :placeholder   "Last name"
          :default-value lname
          :on-blur       #(let [v (.. % -target -value)]
                            (swap! cur-char (fn [c] (assoc-in c [:personality :name :last] v)))
                            (set! (.. % -target -value) v))}]]
       [:div.d-flex.flex-row.align-items-center.justify-content-end.w-100
        [:button.nes-btn.is-primary
         {:on-click
          #(do (let [[rand-first rand-last] (random-name
                                             (get-in @cur-char [:personality :gender])
                                             (get-in @cur-char [:personality :species]))]
                 (when (str/blank? fname)
                   (set! (-> js/document (.getElementById "cc-nature-name-first") .-value) rand-first))
                 (when (= "" lname)
                   (set! (-> js/document (.getElementById "cc-nature-name-last") .-value) rand-last)))
               (panel/show-modal :name-confirm))} "決定"]]]))

  (defn name-main []
    (<load-name)
    [:div#cc-nature-name.w-100.h-100.p-1
     [:div.grid-container.w-100.h-100
      ;; Header
      [:button.nes-btn.aux-left {:on-click #(container/switch-panels :cc-nature-name :cc-nature-reason :right)} [:h3 "◁"]]
      [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "年齢・名前を決めてください"]]
      ;; Panel
      [:div.nes-container.is-dark.is-rounded.main
       [:div.d-flex.flex-row.align-items-center.w-100.h-100
        [profile-image]
        [name-input]]]]
     [panel/modal :name-confirm [(vw 40) (vw 60)]
      [:div.nes-container.is-dark.is-rounded {:style {:width (vw 30) :height (vw 10)}}
       [list/choice
        :header [:h3 "これでいいですか？"]
        :direction :horizontal
        :choices [{:text     "はい"
                   :on-click #(let [fname (-> js/document (.getElementById "cc-nature-name-first") .-value)
                                    lname (-> js/document (.getElementById "cc-nature-name-last") .-value)]
                                (swap! cur-char (fn [c] (assoc-in c [:personality :name :first] fname)))
                                (swap! cur-char (fn [c] (assoc-in c [:personality :name :last] lname)))
                                (container/switch-panels :cc-nature-name :cc-nature-additional-exp :left))}
                  {:text "いいえ" :on-click #(panel/close-modal :name-confirm)}]]]]
     [panel/modal :name-error [(vw 20) (vw 30)]
      [:div.nes-container.is-dark.is-rounded {:style {:width (vw 40)}}
       [:h3 "アップロード可能なプロフィール画像の最大サイズは200KBです。"]
       [:button.nes-btn.is-primary {:on-click #(panel/close-modal :name-error)}
        "OK"]]]]))

;;; Departure reason ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(do

  (defonce *cur-reason (reagent/atom nil))
  (defonce *reason-data (reagent/atom {}))

  (defan- <load-reason []
    (println "<load-reason")
    (let [res (<! (http/<get-data "/cbb/departure-reason"))]
      (reset! *reason-data (res :body))
      true))

  (defn- reason-detail []
    (when-let [reason @*cur-reason]
      [:div.nes-container.is-dark.is-rounded {:style {:width (vw 90)}}
       [:h3 reason]
       [:hr]
       [list/choice
        :header [:h3 "これでいいですか？"]
        :choices [{:text "はい"
                   :on-click
                   #(do
                      (swap! cur-char (fn [c] (assoc-in c
                                                         [:options :nature :adolescence :departure-reason]
                                                         {:abstract reason})))
                      (container/switch-panels :cc-nature-reason :cc-nature-name :left))}
                  {:text "いいえ" :on-click #(panel/close-modal :reason-detail)}]
        :direction :horizontal]]))

  (defn- reason-list []
    [:div.nes-container.is-dark.is-rounded.experience-list {:style {:width (vw 50) :height (vw 40)}}
     [:div.overflow-auto.w-100.h-100
      [list/choice
       :choices (into []
                      (for [reason (@*reason-data :冒険に出た理由)]
                        ^{:key (reason :abstract)}
                        {:text (reason :abstract) :on-click (fn []
                                                              (reset! *cur-reason (:abstract reason))
                                                              (panel/show-modal :reason-detail))}))]]])

  (defn- random-reason []
    (let [reasons (@*reason-data :冒険に出た理由)]
      (:abstract (reasons (rand-int (count reasons))))))

  (defn reason-main []
    (<load-reason)
    [:div#cc-nature-reason.w-100.h-100.p-1 {:style {:position "relative"}}
     [:div.grid-container.w-100.h-100
      ;; Header
      [:button.nes-btn.aux-left {:on-click #(container/switch-panels :cc-nature-reason :cc-nature-adolescence :right)} [:h3 "◁"]]
      [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "冒険に出た理由を決定してください。"]]
      ;; Body
      [:div.nes-container.is-dark.is-rounded.w-100.h-100.selector
       [list/choice
        :choices [{:text "ランダム" :on-click (fn []
                                                (reset! *cur-reason (random-reason))
                                                (panel/show-modal :reason-detail))}
                  {:text "自分で選ぶ" :on-click #(panel/show-modal :reason-experience-list)}]]]]
     [panel/modal :reason-experience-list [(vw 15) (vw 35)] [reason-list]]
     [panel/modal :reason-detail [(vw 10) (vw 5)] [reason-detail]]]))

;;; Happenings ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(do

  (defonce *cur-happening  (reagent/atom nil))
  (defonce *sub-item        (reagent/atom nil))
  (defonce *happening-data (reagent/atom {}))

  (defan- <load-happening []
    (println "<load-happening")
    (let [res (<! (http/<get-data "/cbb/happening"))]
      (reset! *happening-data (res :body))
      true))

  (def multiple-sweethearts
    [(str (max (rand-int 6) 2) "人の異性")
     (str (max (rand-int 6) 2) "人の同性")
     (str (max (dec (rand-int 6)) 1) "人の異性と" (max (dec (rand-int 6)) 1) "人の同性")])

  (defn- happening-additional [happening]
    (let [key (case (happening :abstract)
                "何者かにさらわれた"            :誰によって
                "投獄されたことがある"          :投獄の理由
                "家族を失った"                  :失った家族
                "恋人/配偶者と悲しい別れをした" :別れ
                "複数の相手と恋愛をしている"    :複数
                "特定の装備を身につけられない"  :装備
                nil)]
      (if key
        (do
          (reset! *sub-item ((first (happening key)) :name))
          [list/choice
           :header [:h3 (str (name key) "：")]
           :direction :horizontal
           :choices (into [] (if (= key :複数) ; TODO why can't eval sexp in the string fuckfuckfuck
                               (for [ms multiple-sweethearts]
                                 {:text     ms
                                  :on-click #(reset! *sub-item ms)})
                               (for [item (happening key)]
                                 {:text     (item :name)
                                  :on-click #(reset! *sub-item (item :name))})))])
        [:div])))

  (defn- happening-result [happening]
    {:abstract (str (happening :abstract) (when @*sub-item (str "（" @*sub-item "）")))})

  (defn- close-all-modals []
    (doseq [key [:happening-detail
                 :happening-fortune-list
                 :happening-love-list
                 :happening-secret-list
                 :happening-set
                 :happening-tragedy-list]]
      (panel/close-modal key)))

  (defn- happening-detail
    "period :infancy/:childhood"
    [*target period]
    (when-let [happening @*target]
      (let [move-from (keyword (str "cc-nature-happening-" (name period)))
            move-to   (if (= period :infancy) :cc-nature-childhood :cc-nature-adolescence)]
        [:div.nes-container.is-dark.is-rounded {:style {:width (vw 70)}}
         [:h3 (happening :abstract)]
         [:h4 (happening :detail)]
         (reset! *sub-item nil)
         [happening-additional happening]
         [:hr]
         [list/choice
          :header [:h3 "これでいいですか？"]
          :choices [{:text     "はい"
                     :on-click #(do
                                  (swap! cur-char (fn [c] (assoc-in c
                                                                     [:options :nature period :happening]
                                                                     (happening-result happening))))
                                  (close-all-modals)
                                  (container/switch-panels move-from move-to :left))}
                    {:text "いいえ" :on-click #(panel/close-modal :happening-detail)}]
          :direction :horizontal]])))

  (defn- fortune-list [*target]
    [:div.nes-container.is-dark.is-rounded.experience-list {:style {:width (vw 90) :height (vw 30)}}
     [:div.overflow-auto.w-100.h-100
      [list/choice
       :choices (into []
                      (for [fortune (@*happening-data :幸運・幸福)]
                        ^{:key (fortune :abstract)}
                        {:text (fortune :abstract) :on-click (fn []
                                                               (reset! *target fortune)
                                                               (panel/show-modal :happening-detail))}))]]])

  (defn- tragedy-list [*target]
    [:div.nes-container.is-dark.is-rounded.experience-list {:style {:width (vw 90) :height (vw 30)}}
     [:div.overflow-auto.w-100.h-100
      [list/choice
       :choices (into []
                      (for [tragedy (@*happening-data :事故や悲劇)]
                        ^{:key (tragedy :abstract)}
                        {:text (tragedy :abstract) :on-click (fn []
                                                               (reset! *target tragedy)
                                                               (panel/show-modal :happening-detail))}))]]])

  (defn- love-list [*target]
    [:div.nes-container.is-dark.is-rounded.experience-list {:style {:width (vw 90) :height (vw 30)}}
     [:div.overflow-auto.w-100.h-100
      [list/choice
       :choices (into []
                      (for [love (@*happening-data :恋愛)]
                        ^{:key (love :abstract)}
                        {:text (love :abstract) :on-click (fn []
                                                            (reset! *target love)
                                                            (panel/show-modal :happening-detail))}))]]])

  (defn- secret-list [*target]
    [:div.nes-container.is-dark.is-rounded.experience-list {:style {:width (vw 90) :height (vw 30)}}
     [:div.overflow-auto.w-100.h-100
      [list/choice
       :choices (into []
                      (for [secret (@*happening-data :秘密の背景)]
                        ^{:key (secret :abstract)}
                        {:text (secret :abstract) :on-click (fn []
                                                              (reset! *target secret)
                                                              (panel/show-modal :happening-detail))}))]]])

  (defn- random-happening [period]
    (let [target-keys (if (= period :infancy)
                        [:幸運・幸福 :事故や悲劇]
                        [:幸運・幸福 :事故や悲劇 :恋愛 :秘密の背景])
          *targets    (atom (apply concat (for [k target-keys] (@*happening-data k))))]
      (when (= period :infancy)
        (swap! *targets concat (repeat (/ (count @*targets) 2) {:abstract "特になし"})))
      (nth @*targets (rand-int (count @*targets)))))

  (defn- happening-set [period]
    [:div.nes-container.is-dark.is-rounded
     [list/choice
      :choices (vec (let [base      [{:text "幸運・幸福" :on-click #(panel/show-modal :happening-fortune-list)}
                                     {:text "事故や悲劇" :on-click #(panel/show-modal :happening-tragedy-list)}]
                          infancy   [{:text "特になし" :on-click #(do (reset! *cur-happening {:abstract "特になし"})
                                                                      (panel/show-modal :happening-detail))}]
                          childhood [{:text "恋愛" :on-click #(panel/show-modal :happening-love-list)}
                                     {:text "秘密の背景" :on-click #(panel/show-modal :happening-secret-list)}]]
                      (cond-> base
                        (= period :infancy)   (concat ,, infancy)
                        (= period :childhood) (concat ,, childhood))))]])

  (defn happening-main
    "period :infancy/:childhood"
    [period]
    (<load-happening)
    [:div#cc-nature-happening.w-100.h-100.p-1 {:style {:position "relative"}}
     [:div.grid-container.w-100.h-100
      ;; Header
      [:button.nes-btn.aux-left {:on-click #(container/switch-panels
                                             (keyword (str "cc-nature-happening-" (name period)))
                                             (keyword (str "cc-nature-" (name period)))
                                             :right)} [:h3 "◁"]]
      [:div.nes-container.is-dark.is-rounded.w-100.text-center.title
       [:h3 (str (if (= period :infancy) "幼少期" "少年期") "に起きたハプニングを決定してください")]]
      ;; Body
      [:div.nes-container.is-dark.is-rounded.w-100.h-100.selector
       [list/choice
        :choices [{:text "ランダム" :on-click (fn []
                                                (reset! *cur-happening (random-happening period))
                                                (panel/show-modal :happening-detail))}
                  {:text "自分で選ぶ" :on-click #(panel/show-modal :happening-set)}]]]]
     [panel/modal :happening-set          [(vw 18) (vw 30)] [happening-set period]]
     [panel/modal :happening-fortune-list [(vw 15) (vw 5)]  [fortune-list *cur-happening]]
     [panel/modal :happening-tragedy-list [(vw 15) (vw 5)]  [tragedy-list *cur-happening]]
     [panel/modal :happening-love-list    [(vw 15) (vw 5)]  [love-list *cur-happening]]
     [panel/modal :happening-secret-list  [(vw 15) (vw 5)]  [secret-list *cur-happening]]
     [panel/modal :happening-detail       [(vw 20) (vw 15)] [happening-detail *cur-happening period]]]))

;;; Adolescence ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(do

  (defonce *cur-adolescence (reagent/atom nil))
  (defonce *adolescence-data (reagent/atom {}))
  (defonce *proficiency (reagent/atom nil))

  (defan- <load-adolescence []
    (println "<load-adolescence")
    (let [res (<! (http/<get-data "/cbb/adolescence"))]
      (reset! *adolescence-data (res :body))
      true))

  (defn- equipment-proficiency [candidates]
    [:div
     [:hr]
     [list/choice
      :header [:h4 "習熟対象："]
      :direction :horizontal
      :choices-size :h4
      :choices (into [] (for [c candidates]
                          {:text     (c :name)
                           :on-click #(reset! *proficiency (c :name))}))]])

  (defn- adolescence-sub [adolescence]
    (when-let [candidates (or (adolescence :武器)
                              (adolescence :防具))]
      (reset! *proficiency (:name (first candidates)))
      [equipment-proficiency candidates]))

  (defn- adolescence-detail []
    (when-let [adolescence @*cur-adolescence]
      [:div.nes-container.is-dark.is-rounded {:style {:width (vw 90)}}
       [:h3 (adolescence :abstract)]
       [:h4 (str "習得戦闘特技：" (or (adolescence :skill) "-"))]
       [adolescence-sub adolescence]
       [:h4 (adolescence :detail)]
       [:hr]
       [list/choice
        :header [:h3 "これでいいですか？"]
        :choices [{:text "はい"
                   :on-click
                   #(do
                      (swap! cur-char (fn [c] (assoc-in c
                                                         [:options :nature :adolescence :event]
                                                         {:abstract       (adolescence :abstract)
                                                          :detail (adolescence :detail)})))
                      (swap! cur-char (fn [c] (assoc-in c
                                                         [:skills :general :manual]
                                                         [(str/replace (adolescence :skill) #"\*\*" (or @*proficiency ""))])))
                      (container/switch-panels :cc-nature-adolescence :cc-nature-reason :left))}
                  {:text "いいえ" :on-click #(panel/close-modal :adolescence-detail)}]
        :direction :horizontal]]))

  (defn- adolescence-keys [job type]
    (if (= "遠隔支援系" (const/job->category job))
      [(keyword job)]
      [(keyword job) (keyword type)]))

  (defn- adolescence-list []
    [:div.nes-container.is-dark.is-rounded.experience-list
     {:style {:width (vw 50) :height (vw 40)}}
     [:div.overflow-auto.w-100.h-100
      [list/choice
       :choices (into []
                      (for [adolescence
                            (get-in @*adolescence-data
                                    (adolescence-keys
                                     (get-in @cur-char [:personality :job])
                                     (get-in @cur-char [:options :nature :infancy :event :type])))]
                        ^{:key (adolescence :abstract)}
                        {:text (adolescence :abstract) :on-click (fn []
                                                                   (reset! *cur-adolescence adolescence)
                                                                   (panel/show-modal :adolescence-detail))}))]]])

  (defn- random-adolescence []
    (let [adolescences (get-in @*adolescence-data
                               (adolescence-keys
                                (get-in @cur-char [:personality :job])
                                (get-in @cur-char [:options :nature :infancy :event :type])))]
      (adolescences (rand-int (count adolescences)))))

  (defn adolescence-main []
    (<load-adolescence)
    [:div#cc-nature-adolescence.w-100.h-100.p-1 {:style {:position "relative"}}
     [:div.grid-container.w-100.h-100
      ;; Header
      [:button.nes-btn.aux-left {:on-click #(container/switch-panels :cc-nature-adolescence :cc-nature-happening-childhood :right)} [:h3 "◁"]]
      [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "青年期の経験を決定してください"]]
      ;; Body
      [:div.nes-container.is-dark.is-rounded.w-100.h-100.selector
       [list/choice
        :choices [{:text "ランダム" :on-click (fn []
                                                (reset! *cur-adolescence (random-adolescence))
                                                (panel/show-modal :adolescence-detail))}
                  {:text "自分で選ぶ" :on-click #(panel/show-modal :adolescence-experience-list)}]]]]
     [panel/modal :adolescence-experience-list [(vw 15) (vw 35)] [adolescence-list]]
     [panel/modal :adolescence-detail [(vw 10) (vw 5)] [adolescence-detail]]
     ]))

;;; Childhood ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(do

  (defonce *cur-childhood (reagent/atom nil))
  (defonce *childhood-data (reagent/atom {}))
  (defonce *language (reagent/atom {}))

  (defan- <load-childhood []
    (println "<load-childhood")
    (let [res (<! (http/<get-data "/cbb/childhood"))]
      (reset! *childhood-data (res :body))
      true))

  (defn- language [languages]
    [:div
     [:hr]
     [list/choice
      :header [:h3 "言語："]
      :direction :horizontal
      :choices-size :h4
      :choices (into [] (for [language languages]
                          {:text     (const/language->str language)
                           :on-click #(reset! *language language)}))]
     [:h4 (@*language :description)]])

  (defn- childhood-detail []
    (when-let [childhood @*cur-childhood]
      [:div.nes-container.is-dark.is-rounded {:style {:width (vw 90)}}
       [:h3 (childhood :abstract)]
       [:h4 (str "習得技能：" (or (childhood :ability) "-"))]
       [:h4 (childhood :detail)]
       (reset! *language nil)
       (when-let [languages (childhood :言語)]
         (reset! *language (first (childhood :言語)))
         [language languages])
       [:hr]
       [list/choice
        :header [:h3 "これでいいですか？"]
        :choices [{:text "はい"
                   :on-click
                   #(do
                      (swap! cur-char (fn [c] (assoc-in c
                                                         [:options :nature :childhood :event]
                                                         {:abstract       (childhood :abstract)
                                                          :detail (childhood :detail)})))
                      (when-let [ability (childhood :ability)]
                        (swap! cur-char (fn [c] (assoc-in c
                                                           [:additional-items :abilities]
                                                           [{:name ability :level 1}]))))
                      (when @*language
                        (swap! cur-char (fn [c] (assoc-in c
                                                           [:additional-items :languages]
                                                           [@*language]))))
                      (container/switch-panels :cc-nature-childhood :cc-nature-happening-childhood :left))}
                  {:text "いいえ" :on-click #(panel/close-modal :childhood-detail)}]
        :direction :horizontal]]))

  (defn- childhood-list []
    [:div.nes-container.is-dark.is-rounded.experience-list
     {:style {:width (vw 50) :height (vw 40)}}
     [:div.overflow-auto.w-100.h-100
      [list/choice
       :choices (into []
                      (for [childhood (@*childhood-data
                                       (keyword
                                        (const/job->category
                                         (get-in @cur-char [:personality :job]))))]
                        ^{:key (childhood :abstract)}
                        {:text (childhood :abstract) :on-click (fn []
                                                                 (reset! *cur-childhood childhood)
                                                                 (panel/show-modal :childhood-detail))}))]]])

  (defn- random-childhood []
    (let [childhoods (@*childhood-data
                      (keyword
                       (const/job->category
                        (get-in @cur-char [:personality :job]))))]
      (childhoods (rand-int (count childhoods)))))

  (defn childhood-main []
    (<load-childhood)
    [:div#cc-nature-childhood.w-100.h-100.p-1 {:style {:position "relative"}}
     [:div.grid-container.w-100.h-100
      ;; Header
      [:button.nes-btn.aux-left {:on-click #(container/switch-panels :cc-nature-childhood :cc-nature-happening-infancy :right)} [:h3 "◁"]]
      [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "少年期の経験を決定してください"]]
      ;; Body
      [:div.nes-container.is-dark.is-rounded.w-100.h-100.selector
       [list/choice
        :choices [{:text "ランダム" :on-click (fn []
                                                (reset! *cur-childhood (random-childhood))
                                                (panel/show-modal :childhood-detail))}
                  {:text "自分で選ぶ" :on-click #(panel/show-modal :childhood-experience-list)}]]]]
     [panel/modal :childhood-experience-list [(vw 15) (vw 35)] [childhood-list]]
     [panel/modal :childhood-detail [(vw 10) (vw 5)] [childhood-detail]]
     ]))

;;; Infancy ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(do

  (defonce *cur-infancy  (reagent/atom nil))
  (defonce *infancy-data (reagent/atom {}))

  (defan- <load-infancy []
    (println "<load-infancy")
    (let [res (<! (http/<get-data "/cbb/infancy"))]
      (reset! *infancy-data (res :body))
      true))

  (defn- set-infancy-to-cur-character [infancy]
    (swap! cur-char (fn [c] (assoc-in c
                                       [:options :nature :infancy :event]
                                       {:abstract (infancy :abstract)
                                        :detail   (infancy :detail)
                                        :type     (infancy :type)})))
    (swap! cur-char (fn [c] (assoc-in c [:parameters :initial :dex] (infancy :A))))
    (swap! cur-char (fn [c] (assoc-in c [:parameters :initial :agi] (infancy :B))))
    (swap! cur-char (fn [c] (assoc-in c [:parameters :initial :str] (infancy :C))))
    (swap! cur-char (fn [c] (assoc-in c [:parameters :initial :con] (infancy :D))))
    (swap! cur-char (fn [c] (assoc-in c [:parameters :initial :int] (infancy :E))))
    (swap! cur-char (fn [c] (assoc-in c [:parameters :initial :pow] (infancy :F)))))

  (defn- infancy-detail []
    (when-let [infancy @*cur-infancy]
      [:div.nes-container.is-dark.is-rounded {:style {:width (vw 70)}}
       [:h3 (str (infancy :abstract) "（" (infancy :type) "）")]
       [:h4 (infancy :detail)]
       [:hr]
       [:h3 "能力値"]
       [:h3 (str "器" (infancy :A) " 敏" (infancy :B) " 筋" (infancy :C) " 生" (infancy :D) " 知" (infancy :E) " 精" (infancy :F))]
       [:hr]
       [list/choice
        :header [:h3 "これでいいですか？"]
        :choices [{:text "はい" :on-click #(do
                                             (set-infancy-to-cur-character infancy)
                                             (container/switch-panels :cc-nature-infancy :cc-nature-happening-infancy :left))}
                  {:text "いいえ" :on-click #(panel/close-modal :infancy-detail)}]
        :direction :horizontal]]))

  (defn- infancy-list []
    [:div.nes-container.is-dark.is-rounded.experience-list
     {:style {:width (vw 50) :height (vw 40)}}
     [:div.overflow-auto.w-100.h-100
      [list/choice
       :choices
       (vec
        (for [infancy (remove
                       #(re-find
                         (re-pattern (get-in @cur-char [:personality :job]))
                         (or (% :incompatible) ""))
                       (@*infancy-data
                        (keyword
                         (str (get-in @cur-char [:personality :species :name])
                              "-"
                              (const/job->category
                               (get-in @cur-char [:personality :job]))))))]
          {:text (infancy :abstract) :on-click (fn []
                                                 (reset! *cur-infancy infancy)
                                                 (panel/show-modal :infancy-detail))}))]]])

  (defn- random-infancy []
    (let [infancies (vec
                     (remove
                      #(re-find
                        (re-pattern (get-in @cur-char [:personality :job]))
                        (or (% :incompatible) ""))
                      (@*infancy-data
                       (keyword
                        (str (get-in @cur-char [:personality :species :name])
                             "-"
                             (const/job->category
                              (get-in @cur-char [:personality :job])))))))]
      (infancies (rand-int (count infancies)))))

  (defn infancy-main []
    (<load-infancy)
    [:div#cc-nature-infancy.w-100.h-100.p-1 {:style {:position "relative"}}
     [:div.grid-container.w-100.h-100
      ;; Header
      [:button.nes-btn.aux-left {:on-click #(container/switch-panels :cc-nature-infancy :cc-nature-born :right)} [:h3 "◁"]]
      [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "幼少期の経験を決定してください"]]
      ;; Body
      [:div.nes-container.is-dark.is-rounded.w-100.h-100.selector
       [list/choice
        :choices [{:text "ランダム" :on-click (fn []
                                                (reset! *cur-infancy (random-infancy))
                                                (panel/show-modal :infancy-detail))}
                  {:text "自分で選ぶ" :on-click #(panel/show-modal :infancy-experience-list)}]]]
      [panel/modal :infancy-experience-list [(vw 15) (vw 35)]
       [infancy-list]]]
     [panel/modal :infancy-detail [(vw 10) (vw 15)]
      [infancy-detail]]
     ]))

;;; Born ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(do

  (defonce *human-noble       (reagent/atom nil))
  (defonce *nightmare-origin  (reagent/atom nil))
  (defonce *nightmare-freedom (reagent/atom nil))
  (defonce *lycant-head       (reagent/atom nil))
  (defonce *cur-born          (reagent/atom nil))
  (defonce *born-data         (reagent/atom {}))

  (defan- <load-born []
    (println "<load-born")
    (let [res (<! (http/<get-data "/cbb/born"))]
      (reset! *born-data (res :body))
      true))

  ;; Options

  (defn- human-noble [reasons]
    [:div
     [:hr]
     [list/choice
      :header [:h4 "貴族が冒険に出た理由"]
      :direction :horizontal
      :choices-size :h4
      :choices (into [] (for [freedom reasons]
                          {:text     (freedom :abstract)
                           :on-click #(reset! *human-noble freedom)}))]
     [:h4 ((first (filter #(= (% :abstract) (@*human-noble :abstract)) reasons)) :detail)]])

  (defn- human-opts [born]
    (when (and (= (get-in @cur-char [:personality :species :name]) "人間")
               (= (born :abstract) "貴族(都市)"))
      [:div
       (when-let [reasons (born :貴族)]
         (reset! *human-noble (first reasons))
         [human-noble reasons])]))

  (defn- nightmare-origin [origins]
    [:div
     [:hr]
     [list/choice
      :header [:h4 "出自"]
      :direction :horizontal
      :choices-size :h4
      :choices (into [] (for [origin origins]
                          {:text     origin
                           :on-click #(reset! *nightmare-origin origin)}))]])

  (defn- nightmare-freedom [freedoms]
    [:div
     [list/choice
      :header [:h4 "自由になれた理由"]
      :direction :horizontal
      :choices-size :h4
      :choices (into [] (for [freedom freedoms]
                          {:text     (freedom :abstract)
                           :on-click #(reset! *nightmare-freedom freedom)}))]
     [:h4 ((first (filter #(= (% :abstract) (@*nightmare-freedom :abstract)) freedoms)) :detail)]])

  (defn- nightmare-opts [born]
    (when (= (get-in @cur-char [:personality :species :name]) "ナイトメア")
      [:div
       (let [origins (set (map #(nth % 1) (filter #(re-find #"origin-" (name (% 0))) born)))]
         (reset! *nightmare-origin (first origins))
         [nightmare-origin origins])
       (when-let [freedoms (born :自由になれた理由)]
         (reset! *nightmare-freedom (first freedoms))
         [nightmare-freedom freedoms])]))

  (defn- lycant-opts [born]
    (when (= (get-in @cur-char [:personality :species :name]) "リカント")
      (reset! *lycant-head (first (map :name (born :頭部))))
      [:div
       [:hr]
       [list/choice
        :header [:h3 "頭部の形状"]
        :direction :horizontal
        :choices-size :h4
        :choices (into [] (for [shape (map :name (born :頭部))]
                            {:text     shape
                             :on-click #(reset! *lycant-head shape)}))]]))

  ;; Main

  (defn- born-result [born]
    (let [detail (cond-> (born :detail)
                   @*human-noble       (str ,, "（貴族が冒険に出た理由 ["
                                               (@*human-noble :abstract) "]："
                                               (@*human-noble :detail) "）")
                   @*nightmare-freedom (str ,, "（自由になれた理由 ["
                                               (@*nightmare-freedom :abstract) "]："
                                               (@*nightmare-freedom :detail) "）"))]
      {:abstract (born :abstract) :detail detail}))

  (defn- born-detail []
    (when-let [born @*cur-born]
      [:div.nes-container.is-dark.is-rounded {:style {:width (vw 90)}}
       [:h3 (born :abstract)]
       [:h4 (born :detail)]
       (reset! *nightmare-origin nil)
       (reset! *nightmare-freedom nil)
       (reset! *lycant-head nil)
       [human-opts born]
       [nightmare-opts born]
       [lycant-opts born]
       [:hr]
       [list/choice
        :header [:h3 "これでいいですか？"]
        :choices [{:text "はい"
                   :on-click
                   #(do
                      (swap! cur-char (fn [c] (assoc-in c [:options :nature :born] (born-result born))))
                      (swap! cur-char (fn [c] (assoc-in c [:personality :species :origin] @*nightmare-origin)))
                      (swap! cur-char (fn [c] (assoc-in c [:personality :species :head] @*lycant-head)))
                      (container/switch-panels :cc-nature-born :cc-nature-infancy :left))}
                  {:text     "いいえ"
                   :on-click #(panel/close-modal :born-detail)}]
        :direction :horizontal]]))

  (defn- born-list []
    (let [cur-species (get-in @cur-char [:personality :species :name])]
      [:div.nes-container.is-dark.is-rounded.experience-list
       {:style {:width (vw 50) :height (vw 40)}}
       [:div.overflow-auto.w-100.h-100
        [list/choice
         :choices (into []
                        (for [b (@*born-data (keyword cur-species))] ^{:key (b :abstract)}
                          {:text (b :abstract) :on-click (fn []
                                                           (reset! *cur-born b)
                                                           (panel/show-modal :born-detail))}))]]]))

  (defn- random-born []
    (let [cur-species (get-in @cur-char [:personality :species :name])
          borns       (@*born-data (keyword cur-species))]
      (borns (rand-int (count borns)))))

  (defn born-main []
    (<load-born)
    [:div#cc-nature-born.w-100.h-100.p-1 {:style {:position "relative"}}
     [:div.grid-container.w-100.h-100
      ;; Header
      [:button.nes-btn.aux-left {:on-click #(container/switch-panels :cc-nature-born :cc-nature-job :right)} [:h3 "◁"]]
      [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "生まれを決定してください"]]
      ;; Body
      [:div.nes-container.is-dark.is-rounded.w-100.h-100.selector
       [list/choice
        :choices [{:text "ランダム" :on-click (fn []
                                                (reset! *cur-born (random-born))
                                                (panel/show-modal :born-detail))}
                  {:text "自分で選ぶ" :on-click #(panel/show-modal :born-experience-list)}]]]]
     [panel/modal :born-experience-list [(vw 15) (vw 35)]
      [born-list]]
     [panel/modal :born-detail [(vw 10) (vw 5)]
      [born-detail]]
     ]))

;;; Job ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(do

  (defonce *cur-job (reagent/atom nil))
  (defonce *cur-magical-ability (reagent/atom nil))

  (defn- aptitude-test [species job]
    (let [aptitudes (:aptitudes (first (filter #(= (% :name) species) const/species-list)))]
      (some? (some #{(:name job)} aptitudes))))

  (defn- job-icon [job]
    (let [has-aptitude? (aptitude-test
                         (get-in @cur-char [:personality :species :name])
                         job)]
      [:button.nes-btn {:class    (when-not has-aptitude? "is-disabled")
                        :style    {:height           (vw 10)   :width  (vw 10)
                                   :background-color "#bbbbbb" :margin (vw 1) :color "#222222"}
                        :on-click (fn []
                                    (when has-aptitude?
                                      (swap! cur-char (fn [c] (assoc-in c
                                                                        [:personality :job]
                                                                        (job :name))))
                                      (reset! *cur-job job)))}
       [:h3 (job :name)]]))

  (defn- magical-fighter-opts []
    [:div.nes-container.is-dark.is-rounded
     [list/choice
      :header [:h3 "魔法系技能を選択してください"]
      :choices-size :h4
      :choices (vec (for [magical-ability ["ソーサラー" "コンジャラー" "プリースト"]]
                      {:text     magical-ability
                       :on-click #(do (reset! *cur-magical-ability magical-ability)
                                      (swap! cur-char (fn [c] (assoc-in c
                                                                        [:abilities]
                                                                        (conj (@*cur-job :abilities) {:name magical-ability :level 2}))))
                                      (container/switch-panels :cc-nature-job :cc-nature-born :left))}))]])

  (defn job-main []
    [:div#cc-nature-job.w-100.h-100.p-1
     [:div.grid-container.w-100.h-100
      ;; Header
      [:button.nes-btn.aux-left {:on-click #(container/switch-panels :cc-nature-job :cc-nature-species :right)} [:h3 "◁"]]
      [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "職業を選んでください"]]
      ;; Icons
      (into [:div.d-flex.flex-row.flex-wrap.align-items-center.justify-content-center.w-100.h-100.icons]
            (for [job const/jobs] ^{:key (job :name)} [job-icon job]))
      ;; Detail
      [:div.nes-container.is-dark.is-rounded.w-100.h-100.description
       (when-let [job @*cur-job]
         (let [{:keys [technique physique mind]}
               (const/base-params
                (get-in @cur-char [:personality :species :name])
                (job :name))]
           [:div
            [:h3 (str (job :name) "（技：" technique " 体：" physique " 心：" mind "）")]
            [:h4 (:description job)]]))]
      ;; Button
      [:div.nes-btn.is-primary.aux-right
       {:style {:height (vw 7)}
        :on-click
        #(do
           (let [base (const/base-params
                       (get-in @cur-char [:personality :species :name])
                       (@*cur-job :name))]
             (swap! cur-char (fn [c] (assoc-in c [:parameters :base] base))))
           (swap! cur-char (fn [c] (assoc-in c [:status :exp] (-> @*cur-job :exp))))
           (if-not (re-find #"魔法.士" (@*cur-job :name))
             (do (swap! cur-char (fn [c] (assoc-in c [:abilities] (@*cur-job :abilities))))
                 (container/switch-panels :cc-nature-job :cc-nature-born :left))
             (panel/show-modal :job-magical-fighter-opts)))}
       [:h3 "決定"]]
      [panel/modal :job-magical-fighter-opts [(vw 20) (vw 20)]
       [magical-fighter-opts]]]]))

;;; Species ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(do

  (def cols-count 3)
  (def idx->pos-class ["panel-left" "panel-center" "panel-right"])
  (defonce *head-idx (reagent/atom 0))

  (defn- shift-head-idx [cur diff]
    (let [len      (count const/species-list)
          next-raw (+ cur diff)]
      (cond
        (< next-raw 0)    (* (Math/floor (/ (dec len) cols-count)) cols-count)
        (>= next-raw len) 0
        :else             next-raw)))

  (defn- species-panel [idx species]
    ^{:key idx}
    [:button.nes-container.is-dark.is-rounded.w-100.h-100
     {:class    (idx->pos-class idx)
      :on-click #(do (swap! cur-char (fn [c] (assoc-in c [:personality :species :name] (species :name))))
                     ;; At this point nightmare's language is empty because origin is not determined yet
                     (swap! cur-char (fn [c] (assoc-in c [:languages] (species :languages))))
                     (container/switch-panels :cc-nature-species :cc-nature-job :left))}
     [:div.h-25 [:h4 (species :name)]]
     [:hr]
     [:div.h-25 [:h4 (species :feature)]]
     [:hr]
     [:div.h-25 [:h4 (species :description)]]])

  (defn species-main []
    [:div#cc-nature-species.w-100.h-100.p-1
     [:div.grid-container.w-100.h-100
      ;; Header
      [:button.nes-btn.aux-left {:on-click #(container/switch-panels :cc-nature-species :home :down)} [:h3 "△"]]
      [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "種族を選んでください"]]
      [:button.nes-btn.aux-right
       {:style    {:color            "#ffffff"
                   :background-color (case (get-in @cur-char [:personality :gender])
                                       "male"   "#1133ee"
                                       "female" "#ff33dd"
                                       "#993399")}
        :on-click #(let [next (case (get-in @cur-char [:personality :gender])
                                "male"        "female"
                                "female"      "lgbttiqq2sa"
                                "male")]
                     (swap! cur-char (fn [c] (assoc-in c [:personality :gender] next))))}
       [:h3 (case (get-in @cur-char [:personality :gender])
              "male"   "♂"
              "female" "♀"
              "◎")]]
      ;; Arrows
      [:div.d-flex.flex-row.align-items-center.justify-content-center.w-100.h-100.button-left
       [:button.nes-container.is-dark.is-rounded.text-center
        {:on-click (fn [] (swap! *head-idx #(shift-head-idx % (- cols-count))))} [:h3 "◀"]]]
      [:div.d-flex.flex-row.align-items-center.justify-content-center.w-100.h-100.button-right
       [:button.nes-container.is-dark.is-rounded.text-center
        {:on-click (fn [] (swap! *head-idx #(shift-head-idx % cols-count)))} [:h3 "▶"]]]
      ;; Panels
      (map-indexed #(species-panel %1 %2) (take cols-count (subvec const/species-list @*head-idx)))
      ]]))
