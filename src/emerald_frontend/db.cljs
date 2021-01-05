(ns emerald-frontend.db
  (:require [emerald-frontend.config :as config]))

;; TODO Some data have to be set due to the current specification of transient-container.
;;      It tries to show all the UIs at once when the page is loaded.
(def default-db
  {:cur-character
   {:personality {:name    {:first "" :middle "" :last ""}
                  :age     20
                  :job     "操霊術師"
                  :gender  "male"
                  :species {:name "人間"}}
    :equipments  {}
    :accessories {:head       []
                  :foot       []
                  :neck       []
                  :face       []
                  :back       []
                  :waist      []
                  :others     []
                  :left-hand  []
                  :ear        []
                  :right-hand []}
    :abilities   [{:name "スカウト" :level 1}]
    :skills      {:general []}
    :status      {:exp 0 :honor 0}
    :options     {:nature {:born        {:name   "芸術の民"
                                         :detail "芸術に影響を受けやすい環境にあった。古来の舞踊を伝承する民族かもしれないし、スラムの壁に描かれた絵に天啓を見たのかもしれない。"}
                           :infancy     {:event     {:name   "神殿で過ごす"
                                                     :detail "神の教えを学び、数多くの奉仕に努めた。そのことで、数多くの知識や技術を身につけることができた。"
                                                     :type   "汎用魔法使い"}
                                         :happening {:name "身分に関係なく、立派な仕事に就けた"}}
                           :childhood   {:event     {:name   "フィールドワーク過多"
                                                     :detail "きみの師匠、あるいはきみの学んだ環境では、野外や遺跡に出向き、調査することを特に重視していた。きみはそちらにも才能があった。"}
                                         :happening {:name "一定期間の記憶がない"}}
                           :adolescence {:event            {:name   "鍛錬と根性"
                                                            :detail "気を失うほどの鍛錬の果てに手に入れた技は、多くの人を救うだろう。こつこつと、地道に、魔法をかける対象の数を2つ、3つと増やしていった。"}
                                         :departure-reason {:name "友達に勧められて"}}}}
    :items       []
    :languages   [{:name "エルフ語" :oral true :literal true}]
    :parameters  {:base    {:physique 1 :technique 1 :mind 1}
                  :initial {:dex 1 :agi 1 :str 1 :con 1 :int 1 :pow 1}
                  :growth  {:dex 1 :agi 1 :str 1 :con 1 :int 1 :pow 1}}}})
