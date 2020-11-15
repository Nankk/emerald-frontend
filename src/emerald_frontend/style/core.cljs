(ns emerald-frontend.style.core
  (:require
   [emerald-frontend.style.global :as global]
   [emerald-frontend.style.main-panel :as main-panel]))

(defn- appendln [& ss]
  (str (apply str ss) "\n\n"))

(defn summarize []
  (-> ""
      (appendln (global/css))
      (appendln (main-panel/css))
      ))
