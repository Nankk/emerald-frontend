(ns emerald-frontend.cache.data
  (:require [emerald-frontend.util.async :refer [go? <?]]
            [emerald-frontend.subs :as subs]
            [emerald-frontend.events :as events]
            [re-frame.core :as rf]
            [emerald-frontend.util.http :as http]
            [taoensso.timbre :as timbre :refer [debug spy]]
            [clojure.string :as str]))

;; TODO 全データ持ってくるようにする
;;      cljのマクロでstaticに埋め込んでもいいけどそれだとfrontendのコードを
;;      公開できない感じになるのでダメ。

(defonce items (atom nil))
(defonce equipments (atom nil))

;; TODO 以下の<get-群どうにかdoseqできないものか…cljsだとevalできないのでつらあじ

(defn <get-items []
  (go?
   (let [{:keys [status body]} (<? (http/<get-data "/items/items"))]
     (if (= status 200)
       (reset! items body)
       (rf/dispatch-sync [::events/set-initialization-message "Failed to get items from server."])))))

(defn <get-equipments []
  (go?
   (let [{:keys [status body]} (<? (http/<get-data "/items/equipments"))]
     (if (= status 200)
       (reset! equipments body)
       (rf/dispatch-sync [::events/set-initialization-message "Failed to get equipments from server."])))))

(defn <get-all-data-from-server []
  (go?
   (try
     (<? (<get-items))
     (<? (<get-equipments))
     (when (str/blank? @(rf/subscribe [::subs/initialization-message]))
       (rf/dispatch-sync [::events/move-panel-to :home])))))
