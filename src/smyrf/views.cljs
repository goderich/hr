(ns smyrf.views
  (:require
   [re-frame.core :as rf]
   [smyrf.subs :as subs]
   [smyrf.events :as events]
   ))

(defn input-block [id]
  (let [node (rf/subscribe [::subs/node id])]
    [:div
     [:input {:type "text"
              :placeholder "hai"
              :on-change #(rf/dispatch [::events/input (-> % .-target .-value) id])}]
     [:div [:text (:text @node)]]]))

(defn main-panel []
  (let [name (rf/subscribe [::subs/name])
        text (rf/subscribe [::subs/text])]
    [:div
     [:h1
      "仁侍" @name "! " "ClojureScript! Simple!"]
     [input-block 0]
     [:div [:text @text]]
     ]))
