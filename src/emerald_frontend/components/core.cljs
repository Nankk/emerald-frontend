(ns emerald-frontend.components.core
  (:require
   [emerald-frontend.components.modal :as m]
   [emerald-frontend.components.test-panel :as tp]
   [emerald-frontend.components.transient-container :as tc]
   ))

;; Test panel (for test)
(def test-panel tp/test-panel)

;; Transient container
(def switch-panels tc/switch-panels)
(def transient-container tc/transient-container)
