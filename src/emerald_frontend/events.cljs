(ns emerald-frontend.events
  (:require
   [re-frame.core :as rf]
   [emerald-frontend.db :as db]
   ))

(def handlers
  {::initialize-db    (fn [_ [_ _]] db/default-db)
   ::set-panel-width  [:panel-width]
   ::set-panel-height [:panel-height]
   ::set-loading?     [:loading?]
   ::move-panel-to    [:cur-panel]
   ::set-cur-char     [:cur-char]
   ::clear-cur-char   (fn [db [_]] (assoc db :cur-char nil))
   ::set-cur-char-idx [:cur-char-idx]
   })

(doseq [[ev-key item] handlers]
  (if (coll? item)
    (rf/reg-event-db ev-key
      (fn [db [_ v]] (assoc-in db (vec item) v)))
    (rf/reg-event-db ev-key item)))
