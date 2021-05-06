(ns emerald-frontend.util.js
  (:require [emerald-frontend.subs :as subs]
            [emerald-frontend.util.css :as css]
            [re-frame.core :as rf]
            ))

(defn by-id [id]
  (-> js/document (.getElementById id)))

(defn log [& args]
  (doseq [arg args]
    (.log js/console arg)))

(defn vw
  "Returns relative width considering current container size."
  [fraction]
  (let [w @(rf/subscribe [::subs/panel-width])
        h @(rf/subscribe [::subs/panel-height])]
    (* fraction (min w (/ (* h (css/aspect-ratio 0)) (css/aspect-ratio 1))))))
