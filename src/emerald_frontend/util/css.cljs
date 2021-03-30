(ns emerald-frontend.util.css)

(def aspect-ratio [16 9])

(defn vw
  "Returns css text specifying the correct relative fraction considering current container size."
  [fraction]
  (str "calc(" fraction " * min(1vw, 1vh * " (aspect-ratio 0) " / " (aspect-ratio 1) "))"))
