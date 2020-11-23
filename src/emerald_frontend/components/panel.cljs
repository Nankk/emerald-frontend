(ns emerald-frontend.components.panel
  (:require [reagent.core :as reagent]))

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

(defn show-modal [key]
  (swap! *modal-state assoc-in [key :display] "block")
  (println "showed, map is" @*modal-state))

(defn- backdropper [modal-key z-idx]
  (let [display (get-in @*modal-state [modal-key :display])]
    (println "backdropper-display" display)
    [:div {:on-click #(close-modal modal-key)
           :style {:display display
                   :z-index z-idx
                   :position "absolute"
                   :top "0px"
                   :left "0px"
                   :width "100vw"
                   :height "100vh"
                   :background-color "#000000"
                   :opacity "0.3"}}]))

(defn- modal-body [key [top left] z-idx body]
  (let [display (get-in @*modal-state [key :display])]
    [:div {:style {:display display
                   :z-index z-idx
                   :position "absolute"
                   :top top
                   :left left}}
     (into [:div] body)]))

(defn modal [key pos & body]
  (swap! *modal-state assoc key {:display "none"})
  [:div
   [backdropper key (next-zidx)]
   [modal-body key pos (next-zidx) body]])
