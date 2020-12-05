(ns emerald-frontend.const
  (:require [emerald-frontend.config :as config]))

(def server-url
  (if config/debug?
    "http://localhost:3000"
    "https://hogefuga.net"))

;; (def species-str->key
;;   {"人間"       :human
;;    "エルフ"     :elf
;;    "タビット"   :tabbit
;;    "ドワーフ"   :dwarf
;;    "リカント"   :lycant
;;    "ナイトメア" :nightmare})

(def species-list
  {"人間"       {:feature     "運命変転：運命をコロコロする"
                 :description "一般的な種族で数が多くて云々"
                 :aptitude    ["戦士" "剣士" "拳闘士" "密偵" "魔術師" "操霊術師" "神官" "魔法戦士" "魔法剣士"]}
   "エルフ"     {:feature     "親水性：水によく溶ける"
                 :description "人混みで鋭利な耳が突き刺さる死亡事故が絶えない"
                 :aptitude    ["剣士" "拳闘士" "密偵" "魔術師" "操霊術師" "神官" "魔法剣士"]}
   "タビット"   {:feature     "跳躍：跳躍ロールに100%成功する"
                 :description "月で餅を突いて一生を終えるため地球上には存在しない"
                 :aptitude    ["魔術師" "操霊術師"]}
   "ドワーフ"   {:feature     "耐火：安心のタイガー社製"
                 :description "ロリとヒゲしかいない特異な種族。それ以外の容姿に生まれると殺されてしまうのだ"
                 :aptitude    ["戦士" "拳闘士" "神官" "魔法戦士"]}
   "リカント"   {:feature     "変身：柴犬になれる"
                 :description "月夜の晩によく遠吠えをする。ご近所トラブルに発展することもしばしば"
                 :aptitude    ["戦士" "剣士" "拳闘士" "密偵"]}
   "ナイトメア" {:feature     "穢れ：中二心をくすぐれる"
                 :description "社会不適合者をカッコよく言い換えたもの。本人は満足そう"
                 :aptitude    ["戦士" "拳闘士" "密偵" "魔術師" "操霊術師" "神官" "魔法戦士"]}})

;; (def job-str->key
;;   {"戦士"     :fighter
;;    "剣士"     :knight
;;    "魔法戦士" :magical-fighter
;;    "魔法剣士" :magical-knight
;;    "拳闘士"   :grappler
;;    "密偵"     :spy
;;    "魔術師"   :sorcerer
;;    "操霊術士" :conjurer
;;    "神官"     :priest})

(def jobs
  (array-map
   "戦士" {:description "いわゆるタンク。PTに1人は入れとかないと壊滅するんジャー"}
   "剣士" {:description "紙装甲である代わりにガンガンクリティカル出すんジャー"}
   "魔法戦士" {:description "MP持ちのタンク。すなわち自己修復機能付きの壁"}
   "魔法剣士" {:description "「まほうけん」が使える。わけではない。魔法と剣が使える"}
   "拳闘士" {:description "男と生まれたからには、誰でも一生の内一度は夢見る「地上最強の男」。グラップラーとは、「地上最強の男」を目指す格闘士のことである（例のBGM）"}
   "密偵" {:description "スパイ業を営む。国家の陰謀の成否は彼に掛かっている"}
   "魔術師" {:description "黒魔道士のこと"}
   "操霊術師" {:description "公式イラストのおねえさんがエロい"}
   "神官" {:description "白魔道士のこと"}))
