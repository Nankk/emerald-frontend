(ns emerald-frontend.components.list)

;;; Choice ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn choice
  "Choice panel: Show options and let users select one.
  Arguments: {:header 'header-component
              :choices [{:text 'choice-text1
                         :on-click 'click-fn1}} ...]
              :choices-size ':h1-:h5
              :direction ':horizontal/:vertical"
  [& {:keys [header choices choices-size direction]}]
  [:div
   (when header [:div header])
   (let [group-name   (str (random-uuid))
         font-size 5]
     (into
      [:div]
      (for [i (range (count choices))]
        ^{:key (random-uuid)}
        (let [body [:label
                    [:input.nes-radio.is-dark
                     {:type            "radio"
                      :name            group-name
                      :default-checked (= i 0)
                      :on-click        ((choices i) :on-click)}]
                    [:span {:class (str "font-size-" (or choices-size font-size))
                            :style {:display "inline-block"}}
                     ((choices i) :text)]]]
          (if (and direction (= direction :horizontal))
            body
            [:div body])))))])
