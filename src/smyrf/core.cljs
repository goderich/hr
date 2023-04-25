(ns smyrf.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [smyrf.events :as events]
   [smyrf.views :as views]
   ))


(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (mount-root))
