(ns smyrf.views
  (:require
   [re-frame.core :as rf]
   [smyrf.subs :as subs]
   [smyrf.events :as events]
   ))

(defn view-node [node]
  (let [event-fn
        (fn [type value]
          (events/value-change {:type type :value value :node node}))]
    [:div
     [:input {:type "text"
              :placeholder "請輸入月薪"
              :on-change #(event-fn :text %)}]
     [:div
      "Begin: "
      [:input {:type "date"
               :on-change #(event-fn :begin %)}]
      "End: "
      [:input {:type "date"
               :on-change #(event-fn :end %)}]]
     [:div (-> node :insurance)]]))

(defn view-nodes []
  (let [nodes (rf/subscribe [::subs/nodes])
        views (map view-node @nodes)]
    `[:div ~@views]))

(defn main-panel []
  (let [text (rf/subscribe [::subs/text])]
    [:div
     [:h1 "仁侍! ClojureScript! Simple!"]
     [view-nodes]
     [:button {:on-click #(rf/dispatch [::events/add])} "+"]
     [:p @text]]))
