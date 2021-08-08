(ns emerald-frontend.db
  (:require [emerald-frontend.config :as config]))

(def default-db
  {:cur-panel              :initialization
   :font-family            "PixelMplus10"
   :player                 {:panel-mode nil ; [:exploration :shop :battle]
                            }
   :master                 {}})
