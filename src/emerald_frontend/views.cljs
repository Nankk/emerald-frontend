(ns emerald-frontend.views
  (:require [emerald-frontend.components.container :as container]
            [emerald-frontend.panels.home :as home]
            [emerald-frontend.panels.cc-from-nature :as nature]
            [emerald-frontend.subs :as subs]
            [re-frame.core :as rf]))

(defn- panels []
  [container/transient-container
   {:home                 [home/main]
    :cc-nature-species    [nature/species-main]
    :cc-nature-job        [nature/job-main]
    :cc-nature-born       [nature/born-main]
    :cc-nature-infancy    [nature/infancy-main]
    :cc-nature-happening1 [nature/happening1-main]
    :cc-nature-childhood  [nature/childhood-main]
    :cc-nature-happening2 [nature/happening2-main]
    }])

(defn main-panel []
  [:div.d-flex.flex-row.align-items-center.justify-content-center.w-100.h-100
   [:div#main-panel {:style
                     {:width  (str @(rf/subscribe [::subs/panel-width]) "px")
                      :height (str @(rf/subscribe [::subs/panel-height]) "px")}}
    ;; [panels]
    [nature/born-main]
    ]])
