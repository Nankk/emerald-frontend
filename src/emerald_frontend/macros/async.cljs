(ns emerald-frontend.macros.async
  (:require-macros [emerald-frontend.macros.async :as async])
  (:require [cljs.core.async :as async :refer [go chan <! >!]]))
