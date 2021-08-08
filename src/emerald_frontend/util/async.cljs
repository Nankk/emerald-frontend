(ns emerald-frontend.util.async
  (:require-macros emerald-frontend.util.async))

(defn throw-err [e]
  (when (instance? js/Error e) (throw e))
  e)
