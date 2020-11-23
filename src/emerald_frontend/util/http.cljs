(ns emerald-frontend.util.http
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [clojure.walk :as walk]
            [emerald-frontend.const :as const]
            [emerald-frontend.macros.async :refer [defan]]))

(defan <get-data [url-from-root]
  (println "<get-data")
  (let [url (str const/server-url url-from-root)
        data (<! (http/get url {:with-credentials? false}))]
    data))
