(ns emerald-frontend.panels.session.items
  (:require [emerald-frontend.subs :as subs]
            [emerald-frontend.cache.data :as data]
            [emerald-frontend.util.css :refer [vw]]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

(defonce cur-item (reagent/atom nil))

(defn item-component [item]
  [:div.hstack-vcenter-justify.text-wrap.mb-3.p-2
   {:style {:width (vw 42)}
    :on-click #(do (reset! cur-item item) (println @cur-item))}
   [:div (item :name)]
   [:div (item :count)]])

(defn main []
  (let [char @(rf/subscribe [::subs/cur-char])]
    [:div#player-items
     [:div.grid-container.base-container.vw-90.vh-50.font-size-4
      ;; items
      [:div.items.h-100
       [:div.overflow-auto {:style {:width  (vw 87)
                                    :height (vw 33.5)}}
        (into [:div.hstack.flex-wrap.justify-content-between.p-3]
              (for [[_ item] (:items char)]
                (item-component item)))]]
      ;; description
      [:div.description
       [:hr]
       [:div (some-> @cur-item :description)]]
      ]]))
