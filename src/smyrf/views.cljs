(ns smyrf.views
  (:require
   [re-frame.core :as rf]
   [smyrf.subs :as subs]
   [smyrf.events :as events]
   ))

(defn view-node [node]
  [:div
   [:input {:type "text"
            :placeholder "type here"
            :on-change #(events/text-change % (:id node))}]
   [:div
    [:text "Begin: "]
    [:input {:type "date"}]
    [:text "End: "]
    [:input {:type "date"}]]
   [:p [:text (:text node)]]])

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
     [:p [:text @text]]]))
