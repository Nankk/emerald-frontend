(ns emerald-frontend.components.button
  (:require
   [emerald-frontend.util.css :refer [vw]]
   [reagent.core :as reagent]
   ))

(defn checkbox
  "Returns checkbox reagent component.

  Options:
  | on-click      | Callback function when clicked.         |
  |               | Current checked state (before changing) |
  |               | is passed as an argument                |
  | checked       | Initial state, true or false            |
  | scale         | Scaling factor for the box              |
  | scale-between | Scaling factor for the margin between   |
  |               | box and content                         |
  "
  ;;; Overloads
  ([] (checkbox nil nil))
  ([options-or-content]
   (if (map? options-or-content)
     (checkbox options-or-content nil)
     (checkbox nil options-or-content)))
  ;;; Main
  ([options content]
   (let [opts-default    {:scale 1.0
                          :scale-between 1.0}
         opts-merged     (merge opts-default options)
         {:keys [on-click
                 checked
                 scale
                 scale-between]} opts-merged
         width           (* 1.7 scale)
         checked-state   (reagent/atom (or checked false))]
     (fn []
       [:a.hstack-vcenter {:on-click #(do
                                        (when on-click (on-click @checked-state))
                                        (swap! checked-state not))}
        [:div.position-relative {:style {:margin-right (vw (* 0.5 scale-between scale))}}
         ;; Border
         [:div.nes-container.is-dark
          {:style {:border-width (vw 0.2)
                   :box-shadow   (let [d (vw 0.3) c1 "#000" c2 "#555"]
                                   (str (str d " " d " 0px 0px " c1 ",")
                                        (str d " " d " 0px 0px " c2 " inset")))
                   :margin       0
                   :padding      0
                   :width        (vw width)
                   :height       (vw width)}}]
         ;; Check mark
         [:div.position-absolute
          {:style {:font-size (vw (* width 1.5))
                   :left      (vw (* scale -0.0))
                   :top       (vw (* scale -1.2))}}
          (when @checked-state "✔")]
         ;; Cursor area
         [:div.position-absolute.font-size-6
          {:style    {:background-color "rgba(0,0,0,0)"
                      :top              0
                      :left             0
                      :width            (vw 1.7)
                      :height           (vw 1.7)}}]]
        ;; User specified content
        [:div content]]))))
