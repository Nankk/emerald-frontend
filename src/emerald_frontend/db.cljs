(ns emerald-frontend.db
  (:require [emerald-frontend.config :as config]))

(def default-db
  {:cur-panel :home
   :player    {:panel-mode nil ; [:exploration :shop :battle]
               }
   :master    {}})
