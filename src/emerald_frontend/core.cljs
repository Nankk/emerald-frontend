(ns emerald-frontend.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as rf]
   [emerald-frontend.events :as events]
   [emerald-frontend.views :as views]
   [emerald-frontend.config :as config]
   [emerald-frontend.style.core :as style.core]
   [emerald-frontend.util.js :as util.js]))

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
        w     (-> (util.js/get-el "app") .-clientWidth)
        ;; h     (-> (util.js/get-el "app") .-clientHeight)
        ;; rw    (/ (* h (ratio 0)) (ratio 1))
        rh    (/ (* w (ratio 1)) (ratio 0))]
    (rf/dispatch-sync [::events/set-panel-width w #_(min w rw)])
    ;; LATER: Don't know how to change font size relatively both to vw and to vh
    (rf/dispatch-sync [::events/set-panel-height rh #_(min h rh)])))

(defn init []
  (rf/dispatch-sync [::events/initialize-db])
  (-> js/window (.addEventListener "resize" resize-screen))
  (dev-setup)
  (mount-root)
  (resize-screen))
