(ns emerald-frontend.util.http
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [clojure.walk :as walk]
            [camel-snake-kebab.core :refer [->kebab-case-keyword ->snake_case]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [emerald-frontend.const :as const]
            [emerald-frontend.macros.async :refer [defan]]))

(defan <get-data [url-from-root]
  (println "<get-data")
  (println (str const/server-url url-from-root))
  (try
    (let [url (str const/server-url url-from-root)
          data (<! (http/get url {:with-credentials? false
                                  :keywordize-keys false}))]
      data
      ;; (->> data
      ;;      (transform-keys #(->kebab-case-keyword % :separator \_) ,,))
      )
    (catch js/Error e
      e)))

(defan <post-data [url-from-root data]
  (let [url (str const/server-url url-from-root)
        res (<! (http/post url {:json-params data
                                :with-credentials? false
                                :keywordize-keys false}))]
    res))

(defan <delete [url-from-root & data]
  (let [url (str const/server-url url-from-root)
        res (<! (http/delete url {:json-params data
                                  :with-credentials? false
                                  :keywordize-keys false}))]
    res))
