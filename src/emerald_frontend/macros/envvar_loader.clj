(ns emerald-frontend.macros.envvar-loader
  (:require [environ.core :refer [env]]))

(defmacro inline-envvar [key]
  (get env key))
