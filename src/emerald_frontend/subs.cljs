(ns emerald-frontend.subs
  (:require
   [re-frame.core :as rf]
   ))

(def subscriptions
  {::panel-width    [:panel-width]
   ::panel-height   [:panel-height]
   ::loading?       [:loading?]
   ::chars          [:chars]
   ::cur-panel      [:cur-panel]
   ::cur-char       [:cur-char]
   ::cur-char-idx   [:cur-char-idx]
   })

(doseq [[sub-key item] subscriptions]
  (if (coll? item)
    (rf/reg-sub sub-key
      (fn [db _] (get-in db (vec item))))
    (rf/reg-sub sub-key item)))
