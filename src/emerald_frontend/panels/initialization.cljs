(ns emerald-frontend.panels.initialization
  (:require [emerald-frontend.events :as events]
            [emerald-frontend.subs :as subs]
            [re-frame.core :as rf]))

(defn main []
  (let [msg @(rf/subscribe [::subs/initialization-message])]
    [:div.vstack-hcenter-vcenter
     (or msg "")]))
