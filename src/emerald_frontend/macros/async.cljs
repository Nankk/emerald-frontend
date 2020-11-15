(ns emerald-frontend.macros.async
  (:require-macros [macros.async :as async])
  (:require [cljs.core.async :as async :refer [go chan <! >!]]))
