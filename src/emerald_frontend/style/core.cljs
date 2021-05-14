(ns emerald-frontend.style.core
  (:require
   [emerald-frontend.style.global :as global]
   [emerald-frontend.style.character-status :as status]
   [emerald-frontend.style.main-panel :as main-panel]
   [emerald-frontend.style.cc-from-nature :as cc-from-nature]
   [emerald-frontend.style.session.player-home :as player-home]
   [emerald-frontend.style.session.growth :as growth]
   [emerald-frontend.style.session.shop :as shop]
   ))

(defn- appendln [& ss]
  (str (apply str ss) "\n\n"))

(defn summarize []
  (-> ""
      (appendln (global/css))
      (appendln (main-panel/css))
      (appendln (cc-from-nature/css))
      (appendln (status/css))
      (appendln (player-home/css))
      (appendln (growth/css))
      (appendln (shop/css))
      ))
