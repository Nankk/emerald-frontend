(ns emerald-frontend.util.js)

(defn by-id [id]
  (-> js/document (.getElementById id)))
