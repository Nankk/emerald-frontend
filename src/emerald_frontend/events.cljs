(ns emerald-frontend.events
  (:require
   [re-frame.core :as rf]
   [emerald-frontend.db :as db]
   ))

(def handlers
  {::initialize-db       (fn [_ [_ _]] db/default-db)
   ::set-panel-width     [:panel-width]
   ::set-panel-height    [:panel-height]
   ::set-character-param (fn [db [_ ks v]]
                           (assoc-in db (concat [:cur-character] ks) v))
   })

(doseq [[ev-key item] handlers]
  (if (coll? item)
    (rf/reg-event-db ev-key
      (fn [db [_ v]] (assoc-in db (vec item) v)))
    (rf/reg-event-db ev-key item)))

(comment
  (rf/dispatch-sync [::set-character-param [:species] "人間"])
  (rf/dispatch-sync [::set-character-param [:species] "ナイトメア"])
  (rf/dispatch-sync [::set-character-param [:species] "エルフ"])
  (rf/dispatch-sync [::set-character-param [:job] "戦士"])
  (rf/dispatch-sync [::set-character-param [:gender] "female"])
  (rf/dispatch-sync [::set-character-param [:nature :infancy :event :type] "バランス"])
  )
