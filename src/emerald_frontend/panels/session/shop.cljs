(ns emerald-frontend.panels.session.shop
  (:require [emerald-frontend.util.async :refer [go? <?]]
            [emerald-frontend.util.css :refer [vw]]
            [emerald-frontend.util.http :as http]
            [emerald-frontend.components.button :as button]
            [emerald-frontend.subs :as subs]
            [emerald-frontend.style.colors :as colors]
            [emerald-frontend.panels.session.util :refer [<update-char]]
            [re-frame.core :as rf]
            [reagent.core :as reagent]
            [emerald-frontend.components.panel :as panel]
            [clojure.string :as str]
            [clojure.walk :as walk]))

(defn screen []
  [:div.screen.w-100.h-100 {:style {:background-color "rgba(0, 0, 0, 0.5)"}}
   [:div.hstack-vcenter-hcenter.w-100.h-100.font-size-5
    [:div
     [:div.font-size-3 "よろずや（仮）"]
     [:img {:src "img/dev/sted.png"}]]]])

(defn checkout [{:as states :keys [cart-state all-items]} rest]
  (let [char (atom @(rf/subscribe [::subs/cur-char]))]
    (doseq [[uuid {:keys [name count pack-for quantity]}] @cart-state]
      (let [target-uuid        (or pack-for uuid)
            target-name        (atom nil)
            target-description (atom nil)
            _                  (walk/postwalk (fn [x]
                                                (when (and (map? x) (= (x :uuid) target-uuid))
                                                  (reset! target-name (x :name))
                                                  (reset! target-description (x :description)))
                                                x)
                                              @all-items)
            orig-count         (get-in @char [:items (keyword target-uuid) :count])
            incr               (* (or quantity 1) count)
            new-count          (+ (or orig-count 0) incr)
            new-item           {:uuid        target-uuid
                                :name        @target-name
                                :count       new-count
                                :description (or @target-description "")}]
        (swap! char #(assoc-in % [:items target-uuid] new-item))))
    (<update-char (assoc-in @char [:status :g] rest) (@char :idx))
    (reset! cart-state {})))

(defn cart [{:as states :keys [cart-state]}]
  [:div#player-shop-cart.cart.w-100.h-100.font-size-3
   [:div.grid-container.base-container.w-100.h-100
    ;; Title
    [:div.title
     [:div.hstack-justify-vcenter
      [:div "買い物かご"]
      [:div.nes-btn.is-smaller {:on-click #(reset! cart-state {})}
       "全部戻す"]]
     [:hr]]
    ;; Items
    [:div.items.vstack.overflow-auto.pr-3
     {:style {:height       (vw 16.3)
              :border-right (str (vw 0.3) " solid " (colors/base 10))}}
     (doall (for [[uuid item] @cart-state]
              ^{:key uuid}
              [:div.hstack-justify-vcenter.flex-nowrap
               [:div (item :name)]
               [:div.hstack-vcenter
                [:div.text-nowrap (str (item :price)  "G × " (item :count))]
                [:div.nes-btn.is-smaller
                 {:on-click (fn []
                              (let [count (get-in @cart-state [uuid :count])]
                                (if (<= count 1)
                                  (swap! cart-state #(dissoc % uuid))
                                  (swap! cart-state #(update-in % [uuid :count] dec)))))}
                 "-"]]]))]
    ;; Cashier
    (let [cur           (get-in @(rf/subscribe [::subs/cur-char]) [:status :g])
          subtotal      (reduce + (map #(* (% :price) (% :count)) (vals @cart-state)))
          rest          (- cur subtotal)
          can-checkout? (>= rest 0)]
      [:div.cashier.h-100
       [:div.vstack-justify.h-100
        [:div
         [:div.hstack-justify [:div "所持金"] [:div (str cur " G")]]
         [:div.hstack-justify.mb-2 [:div "商品合計"] [:div (str subtotal " G")]]
         [:hr]
         [:div.hstack-justify [:div "購入後"] [:div {:class (when-not can-checkout? "font-error")} (str rest " G")]]]
        [:button.nes-btn.is-small.is-primary
         {:class    (when-not can-checkout? "is-disabled")
          :on-click #(when can-checkout?
                       (checkout states rest))}
         "確定"]]])]])

(def shelves
  (array-map
   :weapons             "武器"
   :bullets             "矢弾"
   :shields             "盾"
   :armors              "鎧"
   :accessories         "装飾品"
   :ability-accessories "技能用装飾品"
   :general             "一般アイテム"
   :tools               "冒険道具類"
   :recovery            "回復用アイテム"
   :ability-items       "技能用アイテム"))

(defn tabs [{:keys [all-items cur-shelf ranks-displayed?]}]
  [:div.font-size-4.vw-35
   [panel/tabs {:header    "商品棚"
                :items     (vec (for [[k v] shelves] {:key k :value v}))
                :on-change (fn [prev new] (reset! cur-shelf new))
                :init-key  :weapons}]
   (when (:rank (first (@all-items @cur-shelf)))
     [:div.hstack-vcenter-justify.px-5
      [:div "ランク"]
      (doall (for [rank [:b :a :s :ss]] ^{:key rank}
               [button/checkbox
                {:on-click (fn [cur-checked?]
                             (swap! ranks-displayed? #(assoc % rank (not cur-checked?))))
                 :checked  (@ranks-displayed? rank)}
                (str/upper-case (name rank))]))])])

;; category rank

(def rank->colstr
  {"B" "#ee9900" "A" "#cc3333" "S" "#cccc00" "SS" "#ffffcc"})

(defn item-summary [{:as item :keys [rank name uuid price not-implemented]}
                    {:as states :keys [cur-item cart-state]}]
  [:div.py-2 {:style {:background-color (when (= (some-> @cur-item :uuid) uuid) "#306088")}
              :on-click #(do (reset! cur-item item))}
   [:a.mx-5.my-2.hstack-justify
    [:div.hstack-vcenter
     [:div.mr-3 {:style {:color (rank->colstr rank)}}
      rank]
     [:div {:style {:color (when not-implemented "#ff0000")}}
      name]]
    [:div.hstack-vcenter
     [:div.mr-4 (str price " G")]
     [:div.font-size-4
      {:on-click (fn []
                   (swap! cart-state
                          (fn [cart] (update cart uuid #(if %
                                                          (update % :count inc)
                                                          (assoc item :count 1))))))}
      [:i.fas.fa-shopping-cart]]]
    ]])

(def categories
  {:weapons ["ソード" "アックス" "スピア" "メイス" "スタッフ" "フレイル"
             "ウォーハンマー" "格闘" "ボウ" "クロスボウ" "ガン"]
   :armors ["非金属鎧" "金属鎧" "冒険者技能用アイテム"]
   :accessories ["頭" "顔" "耳" "首" "背中" "手" "腰" "足" "任意"]})

(defn grouped-items [{:as states :keys [all-items cur-shelf ranks-displayed?]}]
  (let [cur-items (@all-items @cur-shelf)
        grouped   (group-by #(% :category) cur-items)]
    (into [:div.m-2.font-size-3]
          (for [category (categories @cur-shelf)
                :let     [items (grouped category)]]
            [(panel/accordion
              {:header  [:div.hstack-vcenter
                         [:div.m-3.bg-secondary {:style {:width (vw 3) :height (vw 3)}}
                          "　"]
                         [:div category]]
               :content (into [:div]
                              (for [item  items
                                    :when (or (not (item :rank))
                                              (@ranks-displayed? (keyword (str/lower-case (item :rank)))))]
                                [item-summary item states]))})]))))

(defn plain-items [{:as states :keys [all-items cur-shelf ranks-displayed?]}]
  (into [:div.font-size-3]
        (for [item  (@all-items @cur-shelf)
              :when (or (not (item :rank))
                        (@ranks-displayed? (keyword (str/lower-case (item :rank)))))]
          [item-summary item states])))

(defn item-list [{:as states :keys [all-items cur-shelf ranks-displayed?]}]
  (when @cur-shelf
    [:div.list.h-100.overflow-auto
     (let [cur-items    (@all-items @cur-shelf)
           ranked?      (:rank (first cur-items))
           categorized? (:category (first cur-items))]
       (if categorized?
         (grouped-items states)
         (plain-items states)))]))

(defn description [{:as states :keys [cur-shelf cur-item]}]
  [:div.description.font-size-3
   [:hr]
   (when @cur-item
     [:div (@cur-item :description)])])

(defonce showcase-states {:cur-item         (reagent/atom nil)
                          :cur-shelf        (reagent/atom :weapons)
                          :ranks-displayed? (reagent/atom {:b true :a true :s true :ss true})})

(defn remove-not-for-sales [items]
  (into {} (for [[shelf-key products] items]
             [shelf-key (loop [queue    products
                               filtered []]
                          (if (empty? queue)
                            filtered
                            (let [product (first queue)]
                              (recur (rest queue) (if (product :not-for-sale?)
                                                    filtered
                                                    (conj filtered product))))))])))

(defn showcase [shop-states]
  (let [states (merge shop-states showcase-states)]
    [:div#player-shop-showcase.showcase.w-100.h-100
     [:div.base-container.grid-container.w-100.h-100
      [tabs states]
      [item-list states]
      [description states]]])
  )

(defonce shop-states {:all-items  (atom nil)
                      :cart-state (reagent/atom {})})

(defn main []
  (let [item-table      (reagent/atom nil)
        equipment-table (reagent/atom nil)]
    (go? (reset! item-table (:body (<? (http/<get-data "/items/items"))))
         (reset! equipment-table (:body (<? (http/<get-data "/items/equipments")))))
    (fn []
      (when (and @item-table @equipment-table)
        (when-not @(shop-states :all-items)
          (reset! (shop-states :all-items) (remove-not-for-sales (merge @item-table @equipment-table))))
        (let [states shop-states]
          [:div#player-shop
           [:div.grid-container.base-container.vw-95.vh-50
            [screen states]
            [cart states]
            [showcase states]]])))))
