(ns emerald-frontend.style.session.shop
  (:require [garden.core :as g]
            [emerald-frontend.style.colors :as c]
            [emerald-frontend.util.css :refer [vw]]))

(defn css []
  (g/css
   [:#player-shop
    [:.grid-container
     {:display               "grid"
      :grid-template-columns "1fr 1fr"
      :grid-template-rows    "1fr 1fr"
      :gap                   (str (vw 1) " " (vw 1))
      :grid-template-areas   (pr-str "screen showcase"
                                     "cart showcase")}]
    [:.screen {:grid-area "screen"}]
    [:.cart {:grid-area "cart"}]
    [:.showcase {:grid-area "showcase"}]]
   [:#player-shop-showcase
    [:.grid-container
     {:display               "grid"
      :grid-template-columns "1fr"
      :grid-template-rows    "1fr 7fr 2fr"
      :gap                   (str (vw 1) " " (vw 1))
      :grid-template-areas   (pr-str "tab"
                                     "list"
                                     "description")}]
    [:.tab {:grid-area "tab"}]
    [:.list {:grid-area "list"}]
    [:.description {:grid-area "description"}]]
   [:#player-shop-cart
    [:.grid-container
     {:display               "grid"
      :grid-template-columns "5fr 3fr"
      :grid-template-rows    (str (vw 3) " 1fr")
      :gap                   (str (vw 1) " " (vw 1))
      :grid-template-areas   (pr-str "title title"
                                     "items cashier")}]
    [:.title {:grid-area "title"}]
    [:.items {:grid-area "items"}]
    [:.cashier {:grid-area "cashier"}]
    ]))
