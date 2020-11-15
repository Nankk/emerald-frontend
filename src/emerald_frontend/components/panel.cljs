(ns emerald-frontend.components.panel
  (:require [reagent.core :as r]))

(def panel-style
  {:background-color "#2266ee"})

;; Does this work on current version of reagent?
(defn panel []
  (let [count! (r/atom 0)] ; Closure example
    (fn [& {:keys [style children]}]
      (println "children:" (into [:div] children))
      [:div.panel {:style (merge panel-style style)
                   :on-mouse-over #(swap! count! inc)}
       (str @count!)
       (into [:div] children)])))
