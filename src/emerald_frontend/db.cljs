(ns emerald-frontend.db)

(def default-db
  {:cur-character {}
   #_{:name    'str
      :age     'int
      :gender  'k
      :species 'k
      :type    'k
      :nature  {:born        'str
                :infancy     {:event     'str
                              :happening 'str}
                :childhood   {:event     'str
                              :happening 'str}
                :adolescence {:event            'str
                              :departure-reason 'str}}}})
