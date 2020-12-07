(ns emerald-frontend.panels.cc-from-nature
  (:require [cljs.core.async :refer [go <!]]
            [emerald-frontend.components.container :as container]
            [emerald-frontend.components.list :as list]
            [emerald-frontend.const :as const]
            [emerald-frontend.events :as events]
            [emerald-frontend.subs :as subs]
            [emerald-frontend.macros.async :refer [defan defan-]]
            [emerald-frontend.util.http :as http]
            [re-frame.core :as rf]
            [reagent.core :as reagent]
            [emerald-frontend.components.panel :as panel]))

;;; Happenings ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(do

  (defonce *cur-happening1  (reagent/atom nil))
  (defonce *cur-happening2  (reagent/atom nil))
  (defonce *sub-item        (reagent/atom nil))
  (defonce *happening-data (reagent/atom {}))

  (defan- <load-happening []
    (println "<load-happening")
    (let [res (<! (http/<get-data "/cbb/happening"))]
      (reset! *happening-data (res :body))
      true))

  (defn- happening-additional [happening]
    (let [key (case (happening :title)
                "何者かにさらわれた"   :誰によって
                "投獄されたことがある" :投獄の理由
                "家族を失った"         :失った家族
                nil)]
      (if key
        (do
          (reset! *sub-item ((first (happening key)) :title))
          [list/choice
           :header [:h3 (str (name key) "：")]
           :direction :horizontal
           :choices (into [] (for [item (happening key)]
                               {:text     (item :title)
                                :on-click #(reset! *sub-item (item :title))}))])
        [:div])))

  (defn- happening1-result [happening]
    {:title (str (happening :title) (when @*sub-item (str "（" @*sub-item "）")))})

  (defn- happening1-detail [*target]
    (when-let [happening @*target]
      [:div.nes-container.is-dark.is-rounded {:style {:width "70vw"}}
       [:h3 (happening :title)]
       [:h4 (happening :description)]
       (reset! *sub-item nil)
       [happening-additional happening]
       [:hr]
       [list/choice
        :header [:h3 "これでいいですか？"]
        :choices [{:text "はい" :on-click #(do
                                             (rf/dispatch-sync [::events/set-character-param [:nature :infancy :happening]
                                                                (happening1-result happening)])
                                             (container/switch-panels :cc-nature-happening1 :cc-nature-childhood :left))}
                  {:text "いいえ" :on-click #(panel/close-modal :happening1-detail)}]
        :direction :horizontal]]))

  (defn- fortune-list [*target]
    [:div.nes-container.is-dark.is-rounded.experience-list {:style {:width "90vw" :height "30vw"}}
     [:div.overflow-auto.w-100.h-100
      [list/choice
       :choices (into []
                      (for [fortune (@*happening-data :幸運・幸福)]
                        ^{:key (fortune :title)}
                        {:text (fortune :title) :on-click (fn []
                                                            (reset! *target fortune)
                                                            (panel/show-modal :happening1-detail))}))]]])

  (defn- tragedy-list [*target]
    [:div.nes-container.is-dark.is-rounded.experience-list {:style {:width "90vw" :height "30vw"}}
     [:div.overflow-auto.w-100.h-100
      [list/choice
       :choices (into []
                      (for [tragedy (@*happening-data :事故や悲劇)]
                        ^{:key (tragedy :title)}
                        {:text (tragedy :title) :on-click (fn []
                                                            (reset! *target tragedy)
                                                            (panel/show-modal :happening1-detail))}))]]])

  (defn- love-list [*target]
    [:div.nes-container.is-dark.is-rounded.experience-list {:style {:width "90vw" :height "30vw"}}
     [:div.overflow-auto.w-100.h-100
      [list/choice
       :choices (into []
                      (for [love (@*happening-data :恋愛)]
                        ^{:key (love :title)}
                        {:text (love :title) :on-click (fn []
                                                         (reset! *target love)
                                                         (panel/show-modal :happening1-detail))}))]]])

  (defn- secret-list [*target]
    [:div.nes-container.is-dark.is-rounded.experience-list {:style {:width "90vw" :height "30vw"}}
     [:div.overflow-auto.w-100.h-100
      [list/choice
       :choices (into []
                      (for [secret (@*happening-data :秘密の背景)]
                        ^{:key (secret :title)}
                        {:text (secret :title) :on-click (fn []
                                                           (reset! *target secret)
                                                           (panel/show-modal :happening1-detail))}))]]])

  (defn- random-happening1 []
    (let [happenings (concat (@*happening-data :幸運・幸福)
                             (@*happening-data :事故や悲劇)
                             (repeat (count (@*happening-data :幸運・幸福)) {:title "特になし"}))]
      (nth happenings (rand-int (count happenings)))))

  (defn- happening1-type []
    [:div.nes-container.is-dark.is-rounded
     [list/choice
      :choices [{:text "幸運・幸福" :on-click #(panel/show-modal :happening1-fortune-list)}
                {:text "特になし" :on-click #(do
                                               (reset! *cur-happening1 {:title "特になし"})
                                               (panel/show-modal :happening1-detail))}
                {:text "事故や悲劇" :on-click #(panel/show-modal :happening1-tragedy-list)}]]])

  (defn- happening2-type []
    [:div.nes-container.is-dark.is-rounded
     [list/choice
      :choices [{:text "幸運・幸福" :on-click #(panel/show-modal :happening2-fortune-list)}
                {:text "事故や悲劇" :on-click #(panel/show-modal :happening2-tragedy-list)}
                {:text "恋愛" :on-click #(panel/show-modal :happening2-love-list)}
                {:text "秘密の背景" :on-click #(panel/show-modal :happening2-tragedy-list)}
                ]]])

  (defn happening1-main []
    (<load-happening)
    [:div#cc-nature-happening1.w-100.h-100.p-1 {:style {:position "relative"}}
     [:div.grid-container.w-100.h-100
      ;; Header
      [:button.nes-btn.aux-left {:on-click #(container/switch-panels :cc-nature-happening1 :cc-nature-infancy :right)} [:h3 "◁"]]
      [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "幼少期に起きたハプニングを決定してください"]]
      ;; Body
      [:div.nes-container.is-dark.is-rounded.w-100.h-100.selector
       [list/choice
        :choices [{:text "ランダム" :on-click (fn []
                                                (reset! *cur-happening1 (random-happening1))
                                                (panel/show-modal :happening1-detail))}
                  {:text "自分で選ぶ" :on-click #(panel/show-modal :happening1-type)}]]]]
     [panel/modal :happening1-type ["18vw" "30vw"] [happening2-type]]
     [panel/modal :happening1-fortune-list ["15vw" "5vw"] [fortune-list *cur-happening1]]
     [panel/modal :happening1-tragedy-list ["15vw" "5vw"] [tragedy-list *cur-happening1]]
     [panel/modal :happening1-detail ["20vw" "15vw"] [happening1-detail *cur-happening1]]
     ])

  (defn- random-happening2 []
    )

  (defn- happening2-detail []
    )

  (defn happening2-main []
    (<load-happening)
    [:div#cc-nature-happening2.w-100.h-100.p-1 {:style {:position "relative"}}
     [:div.grid-container.w-100.h-100
      ;; Header
      [:button.nes-btn.aux-left {:on-click #(container/switch-panels :cc-nature-happening2 :cc-nature-infancy :right)} [:h3 "◁"]]
      [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "少年期に起きたハプニングを決定してください"]]
      ;; Body
      [:div.nes-container.is-dark.is-rounded.w-100.h-100.selector
       [list/choice
        :choices [{:text "ランダム" :on-click (fn []
                                                (reset! *cur-happening2 (random-happening2))
                                                (panel/show-modal :happening2-detail))}
                  {:text "自分で選ぶ" :on-click #(panel/show-modal :happening2-type)}]]]]
     [panel/modal :happening2-type ["18vw" "30vw"] [happening2-type]]
     [panel/modal :happening2-fortune-list ["15vw" "5vw"] [fortune-list]]
     [panel/modal :happening2-tragedy-list ["15vw" "5vw"] [tragedy-list]]
     [panel/modal :happening2-love-list ["15vw" "5vw"] [love-list]]
     [panel/modal :happening2-secret-list ["15vw" "5vw"] [secret-list]]
     [panel/modal :happening2-detail ["20vw" "15vw"] [happening2-detail]]
     ])
  )

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
      [:div.nes-container.is-dark.is-rounded {:style {:width "90vw"}}
       [:h3 (childhood :title)]
       [:h4 (childhood :description)]
       (reset! *language nil)
       (when-let [languages (childhood :言語)]
         (reset! *language (first (childhood :言語)))
         [language languages])
       [:hr]
       [list/choice
        :header [:h3 "これでいいですか？"]
        :choices [{:text "はい" :on-click #(do
                                             (rf/dispatch-sync
                                              [::events/set-character-param [:nature :childhood :event]
                                               {:title       (childhood :title)
                                                :description (childhood :description)}])
                                             (when @*language
                                               (rf/dispatch-sync [::events/set-character-param [:languages]
                                                                  [@*language]])))}
                  {:text "いいえ" :on-click #(panel/close-modal :childhood-detail)}]
        :direction :horizontal]]))

  (defn- childhood-list []
    [:div.nes-container.is-dark.is-rounded.experience-list
     {:style {:width "50vw" :height "40vw"}}
     [:div.overflow-auto.w-100.h-100
      [list/choice
       :choices (into []
                      (for [childhood (@*childhood-data
                                       (keyword
                                        (const/job->category
                                         @(rf/subscribe [::subs/cur-character-param [:job]]))))]
                        ^{:key (childhood :title)}
                        {:text (childhood :title) :on-click (fn []
                                                              (reset! *cur-childhood childhood)
                                                              (panel/show-modal :childhood-detail))}))]]])

  (defn- random-childhood []
    (let [childhoods (@*childhood-data
                      (keyword
                       (const/job->category
                        @(rf/subscribe [::subs/cur-character-param [:job]]))))]
      (childhoods (rand-int (count childhoods)))))

  (defn childhood-main []
    (<load-childhood)
    [:div#cc-nature-childhood.w-100.h-100.p-1 {:style {:position "relative"}}
     [:div.grid-container.w-100.h-100
      ;; Header
      [:button.nes-btn.aux-left {:on-click #(container/switch-panels :cc-nature-childhood :cc-nature-job :right)} [:h3 "◁"]]
      [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "少年期の経験を決定してください"]]
      ;; Body
      [:div.nes-container.is-dark.is-rounded.w-100.h-100.selector
       [list/choice
        :choices [{:text "ランダム" :on-click (fn []
                                                (reset! *cur-childhood (random-childhood))
                                                (panel/show-modal :childhood-detail))}
                  {:text "自分で選ぶ" :on-click #(panel/show-modal :childhood-experience-list)}]]]]
     [panel/modal :childhood-experience-list ["15vw" "35vw"] [childhood-list]]
     [panel/modal :childhood-detail ["10vw" "5vw"] [childhood-detail]]
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
    (rf/dispatch-sync [::events/set-character-param [:nature :infancy :event]
                       {:title       (infancy :title)
                        :description (infancy :description)}])
    (rf/dispatch-sync [::events/set-character-param [:parameters :initial :dex] (infancy :A)])
    (rf/dispatch-sync [::events/set-character-param [:parameters :initial :agi] (infancy :B)])
    (rf/dispatch-sync [::events/set-character-param [:parameters :initial :str] (infancy :C)])
    (rf/dispatch-sync [::events/set-character-param [:parameters :initial :con] (infancy :D)])
    (rf/dispatch-sync [::events/set-character-param [:parameters :initial :int] (infancy :E)])
    (rf/dispatch-sync [::events/set-character-param [:parameters :initial :pow] (infancy :F)]))

  (defn- infancy-detail []
    (when-let [infancy @*cur-infancy]
      [:div.nes-container.is-dark.is-rounded {:style {:width "70vw"}}
       [:h3 (str (infancy :title) "（" (infancy :type) "）")]
       [:h4 (infancy :description)]
       (when (infancy :avoid) [:h4 (str "（非推奨：" (infancy :avoid) "）")])
       [:hr]
       [:h3 "能力値"]
       [:h3 (str "器" (infancy :A) " 敏" (infancy :B) " 筋" (infancy :C) " 生" (infancy :D) " 知" (infancy :E) " 精" (infancy :F))]
       [:hr]
       [list/choice
        :header [:h3 "これでいいですか？"]
        :choices [{:text "はい" :on-click #(do
                                             (set-infancy-to-cur-character infancy)
                                             (container/switch-panels :cc-nature-infancy :cc-nature-happening1 :left))}
                  {:text "いいえ" :on-click #(panel/close-modal :infancy-detail)}]
        :direction :horizontal]]))

  (defn- infancy-list []
    [:div.nes-container.is-dark.is-rounded.experience-list
     {:style {:width "50vw" :height "40vw"}}
     [:div.overflow-auto.w-100.h-100
      [list/choice
       :choices (into []
                      (for [infancy (@*infancy-data
                                     (keyword
                                      (str @(rf/subscribe [::subs/cur-character-param [:species]])
                                           "-"
                                           (const/job->category
                                            @(rf/subscribe [::subs/cur-character-param [:job]])))))]
                        ^{:key (infancy :title)}
                        {:text (infancy :title) :on-click (fn []
                                                            (reset! *cur-infancy infancy)
                                                            (panel/show-modal :infancy-detail))}))]]])

  (defn- random-infancy []
    (let [infancies (@*infancy-data
                     (keyword
                      (str @(rf/subscribe [::subs/cur-character-param [:species]])
                           "-"
                           (const/job->category
                            @(rf/subscribe [::subs/cur-character-param [:job]])))))]
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
      [panel/modal :infancy-experience-list ["15vw" "35vw"]
       [infancy-list]]]
     [panel/modal :infancy-detail ["20vw" "15vw"]
      [infancy-detail]]
     ]))

;;; Born ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(do

  (defonce *human-noble       (reagent/atom ""))
  (defonce *nightmare-origin  (reagent/atom ""))
  (defonce *nightmare-freedom (reagent/atom ""))
  (defonce *lycant-head       (reagent/atom ""))
  (defonce *cur-born          (reagent/atom nil))
  (defonce *born-data         (reagent/atom {}))

  (defan- <load-born []
    (println "<load-born")
    (let [res (<! (http/<get-data "/cbb/born"))]
      (reset! *born-data (res :body))
      (println (res :body))
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
                          {:text     (freedom :title)
                           :on-click #(reset! *human-noble freedom)}))]
     [:h4 ((first (filter #(= (% :title) (@*human-noble :title)) reasons)) :description)]])

  (defn- human-opts [born]
    (when (and (= @(rf/subscribe [::subs/cur-character-param [:species]]) "人間")
               (= (born :title) "貴族(都市)"))
      [:div
       ;; TODO :人間-貴族 を変更（both FE & BE）
       (when-let [reasons (born :人間-貴族)]
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
                          {:text     (freedom :title)
                           :on-click #(reset! *nightmare-freedom freedom)}))]
     [:h4 ((first (filter #(= (% :title) (@*nightmare-freedom :title)) freedoms)) :description)]])

  (defn- nightmare-opts [born]
    (when (= @(rf/subscribe [::subs/cur-character-param [:species]]) "ナイトメア")
      [:div
       (let [origins (set (map #(nth % 1) (filter #(re-find #"origin-" (name (% 0))) born)))]
         (reset! *nightmare-origin (first origins))
         [nightmare-origin origins])
       (when-let [freedoms (born :自由になれた理由)]
         (reset! *nightmare-freedom (first freedoms))
         [nightmare-freedom freedoms])]))

  (defn- lycant-opts [born]
    (when (= @(rf/subscribe [::subs/cur-character-param [:species]]) "リカント")
      (reset! *lycant-head (first (map :title (born :頭部))))
      [:div
       [:hr]
       [list/choice
        :header [:h3 "頭部の形状"]
        :direction :horizontal
        :choices-size :h4
        :choices (into [] (for [shape (map :title (born :頭部))]
                            {:text     shape
                             :on-click #(reset! *lycant-head shape)}))]]))

  ;; Main

  (defn- born-result [born]
    (let [title       (cond-> (born :title)
                        @*nightmare-origin (str ,, "（出自：" @*nightmare-origin "）")
                        @*lycant-head      (str ,, "（頭部の形状：" @*lycant-head "）"))
          description (cond-> (born :description)
                        @*human-noble       (str ,, "（貴族が冒険に出た理由 ["
                                                    (@*human-noble :title) "]："
                                                    (@*human-noble :description) "）")
                        @*nightmare-freedom (str ,, "（自由になれた理由 ["
                                                    (@*nightmare-freedom :title) "]："
                                                    (@*nightmare-freedom :description) "）"))]
      {:title title :description description}))

  (defn- born-detail []
    (when-let [born @*cur-born]
      [:div.nes-container.is-dark.is-rounded {:style {:width "90vw"}}
       [:h3 (born :title)]
       [:h4 (born :description)]
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
                      (rf/dispatch-sync
                       [::events/set-character-param [:nature :born] (born-result born)])
                      (container/switch-panels :cc-nature-born :cc-nature-infancy :left))}
                  {:text     "いいえ"
                   :on-click #(panel/close-modal :born-detail)}]
        :direction :horizontal]]))

  (defn- born-list []
    (let [cur-species @(rf/subscribe [::subs/cur-character-param [:species]])]
      [:div.nes-container.is-dark.is-rounded.experience-list
       {:style {:width "50vw" :height "40vw"}}
       [:div.overflow-auto.w-100.h-100
        [list/choice
         :choices (into []
                        (for [b (@*born-data (keyword cur-species))] ^{:key (b :title)}
                          {:text (b :title) :on-click (fn []
                                                        (reset! *cur-born b)
                                                        (panel/show-modal :born-detail))}))]]]))

  (defn- random-born []
    (let [cur-species @(rf/subscribe [::subs/cur-character-param [:species]])
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
     [panel/modal :born-experience-list ["15vw" "35vw"]
      [born-list]]
     [panel/modal :born-detail ["10vw" "5vw"]
      [born-detail]]
     ]))

;;; Job ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(do

  (defonce *cur-job (reagent/atom nil))

  (defn- aptitude-test [species job]
    (let [species-list const/species-list
          aptitudes    (get-in species-list [species :aptitude])]
      (some? (some #{job} aptitudes))))

  (defn- job-icon [job]
    (let [has-aptitude? (aptitude-test (@(rf/subscribe [::subs/cur-character]) :species) (job 0))]
      [:button.nes-btn {:class (when-not has-aptitude? "is-disabled")
                        :style {:height "10vw" :width "10vw" :background-color "#bbbbbb" :margin "1vw" :color "#222222"}
                        :on-click (fn []
                                    (when has-aptitude?
                                      (rf/dispatch-sync [::events/set-character-param [:job] (job 0)])
                                      (reset! *cur-job job)))}
       [:h3 (job 0)]]))

  (defn job-main []
    [:div#cc-nature-job.w-100.h-100.p-1
     [:div.grid-container.w-100.h-100
      ;; Header
      [:button.nes-btn.aux-left {:on-click #(container/switch-panels :cc-nature-job :cc-nature-species :right)} [:h3 "◁"]]
      [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "職業を選んでください"]]
      ;; Icons
      (into [:div.d-flex.flex-row.flex-wrap.align-items-center.justify-content-center.w-100.h-100.icons]
            (for [job const/jobs] ^{:key (job 0)} [job-icon job]))
      ;; Description
      [:div.nes-container.is-dark.is-rounded.w-100.h-100.description
       (when-let [cj @*cur-job]
         [:div
          [:h3 (cj 0)]
          [:h4 (get-in cj [1 :description])]])]
      ;; Button
      [:div.nes-btn.is-primary.aux-right {:style {:height "7vw"}
                                          :on-click #(container/switch-panels :cc-nature-job :cc-nature-born :left)}
       [:h3 "決定"]]
      ]]))

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
     {:class (idx->pos-class idx)
      :on-click (fn []
                  (rf/dispatch-sync [::events/set-character-param [:species] (species 0)])
                  (container/switch-panels :cc-nature-species :cc-nature-job :left))}
     [:div.h-25 [:h4 (species 0)]]
     [:hr]
     [:div.h-25 [:h4 ((species 1) :feature)]]
     [:hr]
     [:div.h-25 [:h4 ((species 1) :description)]]])

  (defn species-main []
    [:div#cc-nature-species.w-100.h-100.p-1
     [:div.grid-container.w-100.h-100
      ;; Header
      [:button.nes-btn.aux-left {:on-click #(container/switch-panels :cc-nature-species :home :down)} [:h3 "△"]]
      [:div.nes-container.is-dark.is-rounded.w-100.text-center.title [:h3 "種族を選んでください"]]
      [:button.nes-btn.is-primary.aux-right {:on-click #(container/switch-panels :cc-from-nature :home :down)} [:h3 "♂"]]
      ;; Arrows
      [:div.d-flex.flex-row.align-items-center.justify-content-center.w-100.h-100.button-left
       [:button.nes-container.is-dark.is-rounded.text-center
        {:on-click (fn [] (swap! *head-idx #(shift-head-idx % (- cols-count))))} [:h3 "◀"]]]
      [:div.d-flex.flex-row.align-items-center.justify-content-center.w-100.h-100.button-right
       [:button.nes-container.is-dark.is-rounded.text-center
        {:on-click (fn [] (swap! *head-idx #(shift-head-idx % cols-count)))} [:h3 "▶"]]]
      ;; Panels
      (map-indexed #(species-panel %1 %2) (take cols-count (subvec (vec const/species-list) @*head-idx)))
      ]]))
