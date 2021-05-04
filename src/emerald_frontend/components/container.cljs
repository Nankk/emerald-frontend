;;; Under construction...
;;; Outgoing panel is immediately none at switching timing instead of after the animation.
(ns emerald-frontend.components.container
  (:require [reagent.core :as reagent]))

(defonce ^:private panel-states! (reagent/atom {})
  ;; Example
  #_{:panel-key {:container container-uuid
                 :display "block | none"
                 :class "slide-up"}})

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
  (swap! panel-states! assoc-in [from-key :class] (str (name transition) "-out"))
  (swap! panel-states! assoc-in [to-key :class] (str (name transition) "-in")))

(defn- add-panel-states [init-key panels container-id]
  (let [panel-states (into {} (for [key (keys panels)]
                                [key {:container container-id
                                      :class     (if (= key init-key)
                                                   "slide-up"
                                                   "d-none")}]))]
    (reset! panel-states! panel-states)))

(defn- transient-panel [[key component]]
  (let [state (@panel-states! key)]
    (when state
      ;; Each panel-kv
      [:div.w-100.h-100 {:class (state :class)
                         :style {:position "absolute"
                                 :top "0px"
                                 :left "0px"}}
       component])))

(defn transient-container
  "Container to manage multiple panels and their transition.
  init-key 'keyword-of-init-panel
  panels   {'panel1-key 'panel1-component ...}"
  [init-key panels]
  (reagent/create-class
   {:component-did-mount
    (fn []
      (add-panel-states init-key panels (str (random-uuid))))

    :reagent-render
    (fn [] (into
            [:div.w-100.h-100 {:style {:position "relative"}}] ; <- Container
            (for [panel panels]
              (transient-panel panel))))}))
