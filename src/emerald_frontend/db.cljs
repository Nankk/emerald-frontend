(ns emerald-frontend.db
  (:require [emerald-frontend.config :as config]))

;; TODO Some data have to be set due to the current specification of transient-container.
;;      It tries to show all the UIs at once when the page is loaded.
(def default-db
  {:cur-character {:age        20
                   :name       {:first "" :middle "" :last ""}
                   :species    "エルフ"
                   :nature     {:born        {:title       "芸術の民"
                                              :description "芸術に影響を受けやすい環境にあった。古来の舞踊を伝承する民族かもしれないし、スラムの壁に描かれた絵に天啓を見たのかもしれない。"}
                                :infancy     {:event     {:title       "神殿で過ごす"
                                                          :description "神の教えを学び、数多くの奉仕に努めた。そのことで、数多くの知識や技術を身につけることができた。"
                                                          :type        "汎用魔法使い"}
                                              :happening {:title "身分に関係なく、立派な仕事に就けた"}}
                                :childhood   {:event     {:title       "フィールドワーク過多"
                                                          :description "きみの師匠、あるいはきみの学んだ環境では、野外や遺跡に出向き、調査することを特に重視していた。きみはそちらにも才能があった。"}
                                              :happening {:title "一定期間の記憶がない"}}
                                :adolescence {:event            {:title       "鍛錬と根性"
                                                                 :description "気を失うほどの鍛錬の果てに手に入れた技は、多くの人を救うだろう。こつこつと、地道に、魔法をかける対象の数を2つ、3つと増やしていった。"}
                                              :departure-reason {:title "友達に勧められて"}}}
                   :abilities  {"スカウト" 1}
                   :skills     ["魔法拡大/数"]
                   :gender     "male"
                   :languages  {}
                   :parameters {:base {:technique 7 :physique 4 :mind 10}
                                :initial {:dex 9 :agi 5 :str 2 :con 5 :int 10 :pow 7}}
                   :job        "操霊術師"
                   :exp        2000}})
