(ns emerald-frontend.components.panel
  (:require [clojure.walk :as walk]
            [emerald-frontend.macros.util :as util]
            [emerald-frontend.util.css :refer [px->vw vw]]
            [emerald-frontend.util.js :as util.js]
            [reagent.core :as reagent]))

;;; Test panel ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private panel-style
  {:background-color "#2266ee"})

(defn test-panel []
  (let [count! (reagent/atom 0)] ; Closure example
    (fn [& {:keys [style children]}]
      (println "children:" (into [:div] children))
      [:div.panel {:style (merge panel-style style)
                   :on-mouse-over #(swap! count! inc)}
       (str @count!)
       (into [:div] children)])))

;;; Modal ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private modal-zidx-base 101)

(defonce ^:private next-zidx
  (let [*count (atom modal-zidx-base)]
    (fn []
      (swap! *count inc)
      @*count)))

(defonce ^:private *modal-state (reagent/atom {})
  #_{'modal-key {:display 'str}})

(defn close-modal [key]
  (swap! *modal-state assoc-in [key :display] "none"))

(defn close-all-modals []
  (swap! *modal-state
         (fn [state]
           (walk/postwalk #(if (and (map? %) (% :display))
                             (assoc % :display "none")
                             %) state))))

(defn show-modal [key]
  (swap! *modal-state assoc-in [key :display] "block")
  (println "showed, map is" @*modal-state))

(defn- backdropper [modal-key z-idx rigid?]
  (let [display (get-in @*modal-state [modal-key :display])]
    (println "backdropper-display" display)
    (if (= display "none")
      [:div {:style {:display "none"}}]
      [:div {:on-click #(when-not rigid? (close-modal modal-key))
             :style {:z-index z-idx
                     :position "absolute"
                     :top (vw -100)
                     :left (vw -100)
                     :width (vw 300)
                     :height (vw 300)
                     :background-color "#000000"
                     :opacity "0.3"}}])))

(defn- modal-body [key [top left] z-idx body]
  (let [display (get-in @*modal-state [key :display])]
    (if (= display "none")
      [:div {:style {:display "none"}}]
      [:div {:style {:z-index z-idx
                     :position "absolute"
                     :top top
                     :left left}}
       body])))

(defn modal
  "Make sure to set {:position \"relative\"} to the container of this dialog.
  `pos` is a vector of [top left]."
  ([key pos body] (modal key pos nil body))
  ([key pos options body]
   (let [{:keys [backdropper-rigid?]} options]
     (swap! *modal-state assoc key {:display "none"})
     [:div
      [backdropper key (next-zidx) backdropper-rigid?]
      [modal-body key pos (next-zidx) body]])))

;;; Tab ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def tabs
  "Options:
  | header    | Header text or component                                                |
  | items     | Defined as a vector of maps.                                            |
  |           | Each map must contain keys [:key :value]. :keys are arbitrary values.   |
  |           | e.g. [{:key :item1 :value ITEM1} {:key :item2 :value ITEM2} ...]        |
  | on-change | Callback function when selected tab is changed.                         |
  |           | Args are previous :key and new :key.                                    |
  | init-key  | First selected key.                                                     |
  "
  (let [x-pos        (atom 0)
        tab-id       (str (random-uuid))
        tabs-vw      (reagent/atom nil)
        cur-item-key (reagent/atom nil)]
    (fn [& [options]]
      (reagent/create-class
       {:component-did-mount
        (fn []
          (let [whole-width  (.-offsetWidth (util.js/by-id (str "whole" tab-id)))
                other-widths (reduce + (for [n     ["header" "left" "right"]
                                             :when (util.js/by-id (str n tab-id))]
                                         (.-offsetWidth (util.js/by-id (str n tab-id)))))
                init-key     (or (options :init-key)
                                 (:key (first (options :items))))]
            (reset! tabs-vw (px->vw (- whole-width other-widths)))
            (reset! cur-item-key init-key)))
        :reagent-render
        (fn []
          (let [opts-default {:header nil
                              :items  []}
                {:keys [header items on-change]}
                (merge opts-default options)]
            [:div.hstack-vcenter.w-100 {:id (str "whole" tab-id)}
             (when header
               [:div.text-nowrap.text-center.p-3 {:id (str "header" tab-id)} header])
             [:div.hstack-justify-vcenter.w-100
              [:div.pr-3 {:id       (str "left" tab-id)
                          :on-click (fn []
                                      (swap! x-pos #(max 0 (- % (util.js/vw 15))))
                                      (.scrollTo (util.js/by-id tab-id) @x-pos 0))} "◀"]
              (when-let [width @tabs-vw]
                (into
                 [:div.hstack-vcenter.flex-nowrap.overflow-hidden.w-100
                  {:id    tab-id
                   :style {:width width}}]
                 (for [{:keys [key value]} items]
                   [:div.text-nowrap.mr-3
                    {:class    (when (= @cur-item-key key) "base-container")
                     :on-click #(do
                                  (when on-change (on-change @cur-item-key key))
                                  (reset! cur-item-key key))}
                    value])))
              [:div.p-3
               {:id (str "right" tab-id)
                :on-click
                (fn []
                  (swap! x-pos #(min (-> (util.js/by-id tab-id) .-scrollWidth) (+ % (util.js/vw 15))))
                  (.scrollTo (util.js/by-id tab-id) @x-pos 0))} "▶"]]]))}))))

;;; Accordion ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn accordion
  "Create accordion panel.
  **Note** Make sure call this as a function inside a hiccup square bracket
           when component has reagent atoms inside."
  [& [options]]
  (let [closed? (reagent/atom true)]
    (fn []
      (let [id             (str (random-uuid))
            content-height (reagent/atom nil)
            opts-default   {:header  nil
                            :content nil}
            {:keys [header content]} ; Content固定化されてます？
            (merge opts-default options)]
        (reagent/create-class
         {:component-did-mount
          (fn []
            (reset! content-height (px->vw (- (.-offsetHeight (util.js/by-id id))))))
          :component-did-update
          (fn []
            (reset! content-height (px->vw (- (.-offsetHeight (util.js/by-id id))))))
          :reagent-render
          (fn []
            [:div.overflow-hidden
             ;; Header
             [:a.nes-container.is-dark.hstack-vcenter-justify.m-0.p-0.pr-5
              {:on-click #(swap! closed? not)}
              header
              [:div.ease-out-300.font-size-1 {:style {:transform (when @closed? "rotate(180deg)")}}
               "▲"]]
             ;; Content
             [:div.ease-out-300
              {:on-click #(reset! content-height (px->vw (- (* 1.2 (.-offsetHeight (util.js/by-id id))))))
               :style    {:margin-top (if @content-height
                                        (if @closed? @content-height 0)
                                        (vw -200))}}
              [:div {:id id}
               content]]])})))))
