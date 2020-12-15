(ns emerald-frontend.db
  (:require [emerald-frontend.config :as config]))

(def default-db
  (if config/debug?
    {:cur-character {:name      {:first ""
                                 :middle ""
                                 :last ""}
                     :age       10
                     :gender    "male"
                     :species   "人間"
                     :job       "戦士"
                     :languages {}
                     :abilities {}
                     :skills    []
                     :nature    {:born        ""
                                 :infancy     {:event     {:title       ""
                                                           :type        ""
                                                           :description ""}
                                               :happening {:title       ""
                                                           :description ""}}
                                 :childhood   {:event     {:title       ""
                                                           :type        ""
                                                           :description ""}
                                               :happening {:title       ""
                                                           :description ""}}
                                 :adolescence {:event            {:title       ""
                                                                  :description ""}
                                               :departure-reason {:title ""}}}}}
    {}))
