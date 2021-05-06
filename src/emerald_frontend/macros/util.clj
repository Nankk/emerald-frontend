(ns emerald-frontend.macros.util)

(defmacro when-let*
  "Multiple binding version of when-let.
  Do following processes only when all bindings are true."
  [bindings & body]
  `(let ~bindings
     (if (and ~@(take-nth 2 bindings))
       (do ~@body))))
