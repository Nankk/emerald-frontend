(ns emerald-frontend.db
  (:require [emerald-frontend.config :as config]))

(def default-db
  (if config/debug?
    {:cur-character  {:name    ""
                      :age     10
                      :gender  "male"
                      :species "人間"
                      :job     "戦士"
                      :nature  {:born        ""
                                :infancy     {:event     {:name        ""
                                                          :type        ""
                                                          :description ""}
                                              :happening {:name        ""
                                                          :description ""}}
                                :childhood   {:event     {:name        ""
                                                          :type        ""
                                                          :description ""}
                                              :happening {:name        ""
                                                          :description ""}}
                                :adolescence {:event   {:name        ""
                                                        :type        ""
                                                        :description ""}
                                              :purpose {:name        ""
                                                        :description ""}}}}}
    {}))
