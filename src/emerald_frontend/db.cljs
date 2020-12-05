(ns emerald-frontend.db)

(def default-db
  {:cur-character {} #_ {:name    'str
                         :age     'int
                         :gender  'k
                         :species 'str
                         :job     'str
                         :nature  {:born        'str
                                   :infancy     {:event     {:name        'str
                                                             :type        'str
                                                             :description 'str}
                                                 :happening {:name        'str
                                                             :description 'str}}
                                   :childhood   {:event     {:name        'str
                                                             :type        'str
                                                             :description 'str}
                                                 :happening {:name        'str
                                                             :description 'str}}
                                   :adolescence {:event   {:name        'str
                                                           :type        'str
                                                           :description 'str}
                                                 :purpose {:name        'str
                                                           :description 'str}}}}})
