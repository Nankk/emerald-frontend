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
  (let [url (str const/server-url url-from-root)
        data (<! (http/get url {:with-credentials? false
                                :keywordize-keys false}))]
    data
    ;; (->> data
    ;;      (transform-keys #(->kebab-case-keyword % :separator \_) ,,))
    ))

(defan <post-character [character-formatted]
  (let [url (str const/server-url "/characters/")
        res (<! (http/post url {:json-params character-formatted
                                :with-credentials? false
                                :keywordize-keys false}))]
    res))
