(ns emerald-frontend.panels.session.util
  (:require
   [emerald-frontend.util.http :as http]
   [emerald-frontend.events :as events]
   [emerald-frontend.subs :as subs]
   [emerald-frontend.util.async :refer [go? <?]]
   [re-frame.core :as rf]
   ))

(defn <update-char [char idx & [options]]
  (go?
   (let [pre-char               @(rf/subscribe [::subs/cur-char])
         opts-default           {:wait?        true
                                 :wait-message "通信中…"}
         {:keys [wait?
                 wait-message]} (merge opts-default options)]
     (try
       (when wait? (rf/dispatch-sync [::events/show-loading wait-message]))
       (rf/dispatch-sync [::events/set-cur-char
                          (:body (<? (http/<put-data (str "/characters/" idx) char)))])
       (catch js/Error e
         (try
           (println "Update-char failed. Rolling back...")
           (rf/dispatch-sync [::events/set-cur-char pre-char])
           (<? (http/<put-data (str "/characters/" idx) char))
           (catch js/Error e
             (println "Oops failed again."))))
       (finally
         (when wait? (rf/dispatch-sync [::events/hide-loading]))))))
  true)
