;;; Under construction...
;;; Outgoing panel is immediately collapse at switching timing instead of after the animation.
(ns emerald-frontend.components.container
  (:require [reagent.core :as reagent]))

(defonce ^:private panel-states! (reagent/atom {})
  ;; Example
  #_{:panel-key {:container container-uuid
                 :visibility "visible | collapse"
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
  (swap! panel-states! assoc-in [from-key :visibility] "collapse")
  (swap! panel-states! assoc-in [to-key :visibility] "visible")

  (println @panel-states!))

(defn- add-panel-states [panels container-id]
  (let [panel-states (into {} (for [panel-key (keys panels)]
                                [panel-key {:container container-id
                                            :visibility "collapse"
                                            :transient-class :slide-up}]))]
    (swap! panel-states! merge (assoc-in panel-states [(first (keys panels)) :visibility] "visible"))))

(defn- transient-panel [panel-kv]
  (let [state (@panel-states! (panel-kv 0))]
    (when state
      ;; Each panel-kv
      (println "state:" state)
      [:div.w-100.h-100 {:class (state :transient-class)
                         :style {:visibility (state :visibility)
                                 :position "absolute"
                                 :top "0px"
                                 :left "0px"}}
       (panel-kv 1)])))

(defn transient-container
  "Container to manage multiple panels and their transition.
  Arguments: {'panel1-key 'panel1-component ...}"
  [panels]
  (reagent/create-class
   {:component-did-mount
    (fn []
      (add-panel-states panels (str (random-uuid))))

    :reagent-render
    (fn [] (into
            [:div.w-100.h-100 {:style {:position "relative"}}] ; <- Container
            (for [panel panels]
              (transient-panel panel))))}))
