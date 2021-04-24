(ns emerald-frontend.util.js)

(defn by-id [id]
  (-> js/document (.getElementById id)))

(defn log [& args]
  (doseq [arg args]
    (.log js/console arg)))
