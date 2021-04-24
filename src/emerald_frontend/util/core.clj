(ns emerald-frontend.util.core)

(defmacro update-in!
  "Atomic version of `update-in`."
  [m-atom ks f]
  `(let [m# ~m-atom]
     (swap! m# #(update-in % ~ks ~f))))
