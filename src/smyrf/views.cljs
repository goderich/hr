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

(defn view-payments [m]
  (let [header
        (str "勞保級距為" (:labor-bracket m) "元，"
             "健保級距為" (:health-bracket m) "元。")

        body
        (for [p (:payments m)]
          [:div
           (str (-> p :month date->str) "："
                "勞退為" (:pension p) "元，"
                "健保為" (:health p) "元，"
                (:days p) "天校方勞保支出為" (:labor p) "元。")])]

    (into [:div] (cons [:div header] body))))

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
     [:h1 "仁侍! ClojureScript! Simple!"]
     [view-nodes @nodes]
     [:button {:on-click #(rf/dispatch [::events/add])} "+"]
     [view-total (calc/total @nodes)]]))
