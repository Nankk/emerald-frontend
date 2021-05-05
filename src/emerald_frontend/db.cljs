(ns emerald-frontend.db
  (:require [emerald-frontend.config :as config]))

(def default-db
  {:cur-panel   :home
   :font-family "PixelMplus10"
   :player      {:panel-mode nil ; [:exploration :shop :battle]
                 }
   :master      {}})
