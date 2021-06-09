(ns emerald-frontend.const
  (:require [emerald-frontend.config :as config]
            [emerald-frontend.macros.envvar-loader :refer [inline-envvar]]))

(def server-address-env (inline-envvar :emerald-server-host))

;; If environment variable `EMERALD_SERVER_HOST` is set when compilation,
;; its value (e.g. "https://foo.com/path/to/api", without closing slash) is embedded
;; into resulting 'app.js' and used as api root.
;; If not, "http://localhost:3000" is used.
(def server-url
  (or js/window.SERVER_URL server-address-env "http://localhost:3000"))

(def upload-image-size-limit 200000)

(def species-list
  [{:name        "人間"
    :feature     "運命変転：運命をコロコロする"
    :description "一般的な種族で数が多くて云々"
    :languages   {"交易共通語" {:oral true :literal true}
                  "地方語"     {:oral true :literal true}}
    :aptitudes   ["戦士" "剣士" "拳闘士" "密偵" "魔術師" "操霊術師" "神官" "魔法戦士" "魔法剣士"]}
   {:name        "エルフ"
    :feature     "親水性：水によく溶ける"
    :description "人混みで鋭利な耳が突き刺さる死亡事故が絶えない"
    :languages   {"交易共通語" {:oral true :literal true}
                  "エルフ語"   {:oral true :literal true}}
    :aptitudes   ["剣士" "拳闘士" "密偵" "魔術師" "操霊術師" "神官" "魔法剣士"]}
   {:name        "ドワーフ"
    :feature     "耐火：安心のタイガー社製"
    :description "ロリとヒゲしかいない特異な種族。それ以外の容姿に生まれると殺されてしまうのだ"
    :languages   {"交易共通語" {:oral true :literal true}
                  "ドワーフ語" {:oral true :literal true}}
    :aptitudes   ["戦士" "拳闘士" "神官" "魔法戦士"]}
   {:name        "タビット"
    :feature     "跳躍：跳躍ロールに100%成功する"
    :description "月で餅を突いて一生を終えるため地球上には存在しない"
    :languages   {"交易共通語" {:oral true :literal true}
                  "神紀文明語" {:oral false :literal true}}
    :aptitudes   ["魔術師" "操霊術師"]}
   {:name        "リカント"
    :feature     "変身：柴犬になれる"
    :description "月夜の晩によく遠吠えをする。ご近所トラブルに発展することもしばしば"
    :languages   {"交易共通語" {:oral true :literal true}
                  "リカント語" {:oral true :literal true}}
    :aptitudes   ["戦士" "剣士" "拳闘士" "密偵"]}
   {:name        "ナイトメア"
    :feature     "穢れ：中二心をくすぐれる"
    :description "社会不適合者をカッコよく言い換えたもの。本人は満足そう"
    :languages   {}
    :aptitudes   ["戦士" "拳闘士" "密偵" "魔術師" "操霊術師" "神官" "魔法戦士"]}])

(def jobs
  [{:name        "戦士"
    :description "いわゆるタンク。PTに1人は入れとかないと壊滅するんジャー"
    :abilities   {"ファイター" 2}
    :exp         {:total 3000 :used 2000}}
   {:name        "剣士"
    :description "紙装甲である代わりにガンガンクリティカル出すんジャー"
    :abilities   {"フェンサー" 2}
    :exp         {:total 3000 :used 1500}}
   {:name        "魔法戦士"
    :description "剣と魔法のよくばりセット"
    :abilities   {"ファイター" 1}
    :exp         {:total 3500 :used 3500}}
   {:name        "魔法剣士"
    :description "「まほうけん」が使える。わけではない。魔法と剣が使える"
    :abilities   {"フェンサー" 1}
    :exp         {:total 3500 :used 3000}}
   {:name        "拳闘士"
    :description "男と生まれたからには、誰でも一生の内一度は夢見る「地上最強の男」。グラップラーとは、「地上最強の男」を目指す格闘士のことである（例のBGM）"
    :abilities   {"グラップラー" 2}
    :exp         {:total 3000 :used 2000}}
   {:name        "密偵"
    :description "スパイ業を営む。国家の陰謀の成否は彼に掛かっている"
    :abilities   {"スカウト"   2
                  "フェンサー" 1}
    :exp         {:total 3000 :used 2000}}
   {:name        "魔術師"
    :description "黒魔道士のこと"
    :abilities   {"ソーサラー" 2}
    :exp         {:total 3000 :used 2000}}
   {:name        "操霊術師"
    :description "公式イラストのおねえさんがエロい"
    :abilities   {"コンジャラー" 2}
    :exp         {:total 3000 :used 2000}}
   {:name        "神官"
    :description "白魔道士のこと"
    :abilities   {"プリースト" 2}
    :exp         {:total 3000 :used 2000}}])

(def job->category
  {"戦士"     "戦士系"
   "剣士"     "戦士系"
   "魔法戦士" "魔法戦士系"
   "魔法剣士" "魔法戦士系"
   "拳闘士"   "戦士系"
   "密偵"     "密偵系"
   "魔術師"   "遠隔支援系"
   "操霊術師" "遠隔支援系"
   "神官"     "遠隔支援系"})

(def job-species-base-map
  {"戦士"     {"人間"           {:technique 7 :physique 9 :mind 5}
               "ドワーフ"       {:technique 4 :physique 11 :mind 5}
               "ナイトメア"     {:technique 7 :physique 15 :mind 8}
               "リカント"       {:technique 10 :physique 9 :mind 6}
               "ルーンフォーク" {:technique 9 :physique 12 :mind 5}
               "リルドラケン"   {:technique 5 :physique 14 :mind 6}
               "ティエンス"     {:technique 8 :physique 12 :mind 8}}
   "剣士"     {"人間"           {:technique 9 :physique 6 :mind 6}
               "エルフ"         {:technique 12 :physique 5 :mind 9}
               "リカント"       {:technique 12 :physique 6 :mind 7}
               "ルーンフォーク" {:technique 11 :physique 9 :mind 6}
               "グラスランナー" {:technique 14 :physique 1 :mind 10}}
   "拳闘士"   {"人間"           {:technique 8 :physique 8 :mind 5 }
               "エルフ"         {:technique 11 :physique 4 :mind 11 }
               "ドワーフ"       {:technique 5 :physique 10 :mind 5 }
               "ナイトメア"     {:technique 7 :physique 15 :mind 8 }
               "リカント"       {:technique 11 :physique 7 :mind 7 }
               "ルーンフォーク" {:technique 9 :physique 12 :mind 5 }
               "リルドラケン"   {:technique 6 :physique 13 :mind 6 }
               "グラスランナー" {:technique 14 :physique 2 :mind 9 }}
   "密偵"     {"人間"           {:technique 10 :physique 7 :mind 4 }
               "エルフ"         {:technique 12 :physique 4 :mind 10 }
               "ナイトメア"     {:technique 11 :physique 13 :mind 6 }
               "リカント"       {:technique 13 :physique 5 :mind 7 }
               "ルーンフォーク" {:technique 12 :physique 6 :mind 8 }
               "グラスランナー" {:technique 13 :physique 0 :mind 12 }
               "レプラカーン"   {:technique 14 :physique 4 :mind 5 }}
   "魔術師"   {"人間"           {:technique 6 :physique 5 :mind 10 }
               "エルフ"         {:technique 10 :physique 3 :mind 13 }
               "タビット"       {:technique 5 :physique 7 :mind 10 }
               "ナイトメア"     {:technique 5 :physique 13 :mind 12 }
               "ルーンフォーク" {:technique 9 :physique 8 :mind 9 }
               "メリア"         {:technique 8 :physique 6 :mind 15 }
               "レプラカーン"   {:technique 11 :physique 3 :mind 9 }}
   "操霊術師" {"人間"           {:technique 7 :physique 4 :mind 10 }
               "エルフ"         {:technique 9 :physique 4 :mind 13 }
               "タビット"       {:technique 6 :physique 6 :mind 10 }
               "ナイトメア"     {:technique 6 :physique 11 :mind 13 }
               "ルーンフォーク" {:technique 7 :physique 9 :mind 10 }
               "メリア"         {:technique 7 :physique 6 :mind 16 }
               "レプラカーン"   {:technique 11 :physique 3 :mind 9 }}
   "神官"     {"人間"         {:technique 4 :physique 8 :mind 9 }
               "エルフ"       {:technique 9 :physique 5 :mind 12 }
               "ドワーフ"     {:technique 4 :physique 7 :mind 9 }
               "ナイトメア"   {:technique 6 :physique 14 :mind 10 }
               "メリア"       {:technique 8 :physique 8 :mind 13 }
               "レプラカーン" {:technique 10 :physique 5 :mind 8 }}
   "魔法戦士" {"人間"           {:technique 7 :physique 9 :mind 5 }
               "ドワーフ"       {:technique 4 :physique 11 :mind 5 }
               "ナイトメア"     {:technique 7 :physique 15 :mind 8 }
               "ルーンフォーク" {:technique 9 :physique 12 :mind 5 }
               "リルドラケン"   {:technique 5 :physique 14 :mind 6 }
               "ティエンス"     {:technique 8 :physique 12 :mind 8 }}
   "魔法剣士" {"人間"           {:technique 9 :physique 6 :mind 6 }
               "エルフ"         {:technique 12 :physique 5 :mind 9 }
               "ルーンフォーク" {:technique 11 :physique 9 :mind 6 }
               "メリア"         {:technique 10 :physique 8 :mind 11 }
               "レプラカーン"   {:technique 13 :physique 5 :mind 5 }}})

(defn base-params [species job]
  (get-in job-species-base-map [(name job) (name species)]))

(defn language->str [{:keys [name oral literal]}]
  (let [or?   (or oral literal)
        both? (and oral literal)]
    (str name
         (when or?
           (str "（" (when oral "会話") (when both? "、") (when literal "読文") "）")))))

(def abilities
  [{:name "ファイター" :category #{:warrior :cqc} :table :a}
   {:name "グラップラー" :category #{:warrior :cqc} :table :a}
   {:name "フェンサー" :category #{:warrior :cqc} :table :b}
   {:name "シューター" :category #{:warrior} :table :b}
   {:name "ソーサラー" :category #{:magician} :table :a}
   {:name "コンジャラー" :category #{:magician} :table :a}
   {:name "プリースト" :category #{:magician} :table :a}
   {:name "フェアリーテイマー" :category #{:magician} :table :a}
   {:name "マギテック" :category #{:magician} :table :a}
   {:name "デーモンルーラー" :category #{:magician} :table :a}
   {:name "ドルイド" :category #{:magician} :table :a}
   {:name "スカウト" :category #{:other} :table :b}
   {:name "レンジャー" :category #{:other} :table :b}
   {:name "セージ" :category #{:other} :table :b}
   {:name "エンハンサー" :category #{:other} :table :b}
   {:name "バード" :category #{:other} :table :b}
   {:name "ライダー" :category #{:other} :table :b}
   {:name "アルケミスト" :category #{:other} :table :b}])
