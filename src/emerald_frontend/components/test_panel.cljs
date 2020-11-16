(ns emerald-frontend.components.test-panel
  (:require [reagent.core :as r]))

(def ^:private panel-style
  {:background-color "#2266ee"})

;; Does this work on current version of reagent?
(defn test-panel []
  (let [count! (r/atom 0)] ; Closure example
    (fn [& {:keys [style children]}]
      (println "children:" (into [:div] children))
      [:div.panel {:style (merge panel-style style)
                   :on-mouse-over #(swap! count! inc)}
       (str @count!)
       (into [:div] children)])))
