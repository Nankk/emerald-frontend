(ns emerald-frontend.util.async
  (:require [cljs.core.async :as async]))

(defmacro <?
  "Safe version of `clojure.core.async/<!` which catches and returns exception."
  [ch]
  `(throw-err (async/<! ~ch)))

(defmacro error-or [& body]
  `(try
     ~@body
     (catch js/Error e#
       e#)))

(defmacro go?
  "Safe version of `clojure.core.async/go` which catches exception and throw it."
  [& body]
  `(async/go (error-or ~@body)))
