(ns emerald-frontend.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as rf]
   [emerald-frontend.events :as events]
   [emerald-frontend.views :as views]
   [emerald-frontend.config :as config]
   [emerald-frontend.style.core :as style.core]
   [emerald-frontend.util.js :refer [by-id]]))

(defn- compile-garden []
  (println "Compiling garden...")
  (let [css-text (style.core/summarize)
        css-elem (. js/document getElementById "garden")]
    (set! (. css-elem -textContent) css-text)))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (compile-garden)
  (rdom/render [views/main-panel]
               (.getElementById js/document "app")))

(defn- resize-screen []
  (let [ratio [16 9]
        w     (int (-> (by-id "app") .-clientWidth))
        h     (int (-> (by-id "app") .-clientHeight))
        rh    (int (/ (* w (ratio 1)) (ratio 0)))
        rw    (int (/ (* h (ratio 0)) (ratio 1)))]
    (println "w h rw rh" w h rw rh)
    (rf/dispatch-sync [::events/set-panel-width (min w rw)])
    (rf/dispatch-sync [::events/set-panel-height (min h rh)])))

(defn init []
  (rf/dispatch-sync [::events/initialize-db])
  (-> js/window (.addEventListener "resize" resize-screen))
  (dev-setup)
  (mount-root)
  (resize-screen))
