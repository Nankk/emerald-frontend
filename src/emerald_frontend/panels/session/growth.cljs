(ns emerald-frontend.panels.session.growth
  (:require
   [emerald-frontend.util.css :refer [vw]]
   [emerald-frontend.const :as const]
   [reagent.core :as reagent]
   ))

(def param-key->str
  {:dex "器用度"
   :agi "敏捷度"
   :str "筋力"
   :con "生命力"
   :int "知力"
   :pow "精神力"})

(defn- container [char idx]
  (when char
    [:div#growth
     (let [{{{:keys [first middle last]} :name}     :personality
            {:keys [level max-hp max-mp hp mp exp]} :status
            {:keys [bonus]}                         :parameters} char]
       [:div.nes-container.is-dark.is-rounded.grid-container {:style {:width (vw 90) :height (vw 50)}}
        ;;; Upper
        [:div.upper.h-100.font-size-1.d-flex.flex-row.justify-content-between
         [:div.h-100
          [:div.d-flex.flex-row.align-items-center
           [:img.profile-image
            {:src   (str const/server-url "/characters/" idx "/image")
             :style {:width (vw 12) :height (vw 12)}}]
           [:div
            [:div (str first " " middle " " last)] ; Name
            [:table.nes-table.is-dark.w-50
             [:tbody
              [:tr [:td "Lv"] [:td level]]
              [:tr [:td "HP"] [:td (str hp "/" max-hp)]]
              [:tr [:td "MP"] [:td (str mp "/" max-mp)]]]]]]]
         [:div.nes-container.is-dark.is-rounded.d-flex.flex-row {:style {:width (vw 45)}}
          [:div.border-right "能力値"]]]
        ;;; Lower
        [:div.lower.w-100.h-100 {:style {:font-size (vw 1.5)}}
         [:div.nes-container.is-dark.is-rounded.h-100
          [:table.nes-table.is-dark.w-100
           (into [:tbody] (keep-indexed (fn [i p]
                                          [:tr
                                           [:td (param-key->str p)]
                                           [:td.center (bonus p)]])
                                        [:dex :agi :str :con :int :pow]))]]]])]))

(defn main [char idx]
  [container char idx])
