;;; Under construction...
;;; Outgoing panel is immediately hidden at switching timing instead of after the animation.
(ns emerald-frontend.components.transient-container
  (:require [reagent.core :as reagent]))

(defonce ^:private panel-states! (reagent/atom {})
  ;; Example
  #_{:panel-key {:container container-uuid
                 :visibility "visible | hidden"
                 :transient-class "slide-up"}})

(defn switch-panels
  "Switch panels from 'from-key' to 'to-key'.
  from-key = :panel-key
  to-key = :panel-key
  transition = {:fade | :slide-{up | down | left | right} | :push-{up | down | left | right}}"
  [from-key to-key transition]
  {:pre [(= (get-in @panel-states! [from-key :container])
            (get-in @panel-states! [to-key :container]))]}
  (println from-key to-key transition)
  ;; Transition
  (swap! panel-states! assoc-in [from-key :transient-class] (str (name transition) "-out"))
  (swap! panel-states! assoc-in [to-key :transient-class] (str (name transition) "-in"))
  ;; Visibility
  (swap! panel-states! assoc-in [from-key :visibility] "hidden")
  (swap! panel-states! assoc-in [to-key :visibility] "visible")

  (println @panel-states!))

(defn- add-panel-states [panels container-id]
  (let [panel-states (into {} (for [panel-key (keys panels)]
                                [panel-key {:container container-id
                                            :visibility "hidden"
                                            :transient-class :slide-up}]))]
    (swap! panel-states! merge (assoc-in panel-states [(first (keys panels)) :visibility] "visible"))))

(defn- transient-panel [panel]
  (let [state (@panel-states! (panel 0))]
    (when state
      ;; Each panel
      (println "state:" state)
      [:div {:class (state :transient-class)
             :style {:visibility (state :visibility)
                     :position "absolute"
                     :top "0px"
                     :left "0px"}}
       (panel 1)])))

(defn transient-container [panels]
  (reagent/create-class
   {:component-did-mount
    (fn []
      (add-panel-states panels (str (random-uuid))))

    :reagent-render
    (fn [] (into
            [:div {:style {:position "relative"}}] ; <- Container
            (for [panel panels]
              (transient-panel panel))))}))
