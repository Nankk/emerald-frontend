(ns emerald-frontend.util.http
  (:require [camel-snake-kebab.core :refer [->kebab-case-keyword ->snake_case]]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [clojure.walk :as walk]
            [emerald-frontend.const :as const]
            [emerald-frontend.util.async :refer [<? go?]]
            [taoensso.timbre :refer [debug info spy]]))

(defn <get-data [url-from-root]
  (debug "<get-data" (str const/server-url url-from-root))
  (go?
   (try
     (let [url (str const/server-url url-from-root)
           data (<? (http/get url {:with-credentials? false
                                   :keywordize-keys false}))]
       data
       ;; TODO 何のコメントこれ？↓
       ;; (->> data
       ;;      (transform-keys #(->kebab-case-keyword % :separator \_) ,,))
       ))))

(defn <post-data [url-from-root data]
  (go?
   (let [url (str const/server-url url-from-root)
         res (<? (http/post url {:json-params data
                                 :with-credentials? false
                                 :keywordize-keys false}))]
     res)))

(defn <put-data [url-from-root data]
  (go?
   (let [url (str const/server-url url-from-root)
         res (<? (http/put url {:json-params data
                                :with-credentials? false
                                :keywordize-keys false}))]
     res)))

(defn <delete [url-from-root & data]
  (go?
   (let [url (str const/server-url url-from-root)
         res (<? (http/delete url {:json-params data
                                   :with-credentials? false
                                   :keywordize-keys false}))]
     res)))
