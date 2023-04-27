(ns smyrf.views
  (:require
   [re-frame.core :as rf]
   [smyrf.subs :as subs]
   [smyrf.events :as events]
   [smyrf.calc :as calc]
   [tick.core :as t]
   ))

(defn- date->str [date]
  (let [y (str (- (t/year date) 1911) "年")]
    (str y (.monthValue date) "月")))

(defn view-payments [ms]
  (into [:div]
        (for [m ms]
          [:div
           (str (-> m :month date->str) "："
                "勞退為" (:pension m) "元，"
                "健保為" (:health m) "元"
                (:health-comment m) "，"
                (:days m) "天校方勞保支出為"
                (:labor m) "元" (:labor-comment m) "。")])))

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
     [view-payments (:insurance node)]]))

(defn view-nodes [nodes]
  (let [views (map view-node nodes)]
    `[:div ~@views]))

(defn view-total [m]
  [:div
   (str
    "總額試算：勞退為" (:pension m) "元，"
    "健保為" (:health m) "元，"
    "勞保為" (:labor m) "元。")])

(defn main-panel []
  (let [nodes (rf/subscribe [::subs/nodes])]
    [:div
     [:h1 "Simple 仁侍!"]
     [view-nodes @nodes]
     [:button {:on-click #(rf/dispatch [::events/add])} "+"]
     [view-total (calc/total @nodes)]]))
