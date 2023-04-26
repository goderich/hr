(ns smyrf.views
  (:require
   [re-frame.core :as rf]
   [smyrf.subs :as subs]
   [smyrf.events :as events]
   [smyrf.calc :as calc]
   ))

(defn view-node [node]
  (let [event-fn
        (fn [type value]
          (events/value-change {:type type :value value :node node}))]
    [:div
     "月薪："
     [:input {:type "text"
              :placeholder "請輸入月薪"
              :on-change #(event-fn :text %)}]
     [:div
      "開始："
      [:input {:type "date"
               :on-change #(event-fn :begin %)}]
      "  結束："
      [:input {:type "date"
               :on-change #(event-fn :end %)}]]
     [:div (-> node :insurance :text)]]))

(defn view-nodes [nodes]
  (let [views (map view-node nodes)]
    `[:div ~@views]))

(defn main-panel []
  (let [nodes (rf/subscribe [::subs/nodes])]
    [:div
     [:h1 "仁侍! ClojureScript! Simple!"]
     [view-nodes @nodes]
     [:button {:on-click #(rf/dispatch [::events/add])} "+"]
     [calc/total @nodes]
     ]))
