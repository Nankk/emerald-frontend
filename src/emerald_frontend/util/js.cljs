(ns emerald-frontend.util.js)

(defn get-el [id]
  (-> js/document (.getElementById id)))
