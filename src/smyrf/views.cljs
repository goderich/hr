(ns smyrf.views
  (:require
   [re-frame.core :as rf]
   [smyrf.subs :as subs]
   [smyrf.events :as events]
   [smyrf.calc :as calc]
   [tick.core :as t]
   [reagent.core :as reagent]
   ["react-datepicker$default" :as DatePicker]
   ))

(defn- date->str [date]
  (let [y (str (- (t/year date) 1911) "年")]
    (str y (.monthValue date) "月")))

(def ^:private sinicize
  {1 "一"
   2 "二"
   3 "三"
   4 "四"
   5 "五"
   6 "六"
   7 "七"
   8 "八"
   9 "九"})

(defn- int->chinese-num [n]
  (let [dec (let [q (quot n 10)]
              (cond
                (> q 1) (str (sinicize q) "十")
                (= q 1) "十"))
        un (sinicize (rem n 10))]
    (str dec un "、")))

(defn view-payments [ms]
  (let [details? (rf/subscribe [::subs/details?])]
    (when @details?
      (into [:div.details]
            (for [m ms]
              [:p
               [:div (str (-> m :month date->str) "：")]
               [:div (str "勞退為" (:pension m) "元")]
               [:div (str "健保為" (:health m) "元"
                          (:health-comment m))]
               [:div (str (:days m) "天校方勞保支出為"
                          (:labor m) "元" (:labor-comment m))]])))))

(defn datepicker-inner [node]
  (let [id (:id node)
        begin-atom (reagent/atom nil)
        end-atom (reagent/atom (when-let [end (-> node :input :end)]
                                 (new js/Date end)))]
    (reagent/create-class
     {:display-name "date picker"

      :component-did-mount
      (fn []
        (println "date picker did mount"))

      :reagent-render
      (fn []
        [:div
         [:span "開始："]
         [:> DatePicker
          {:selected @begin-atom
           :onChange (fn [new-date]
                       (println id " begin changed to: " (str (t/date new-date)))
                       (reset! begin-atom new-date)
                       (rf/dispatch [::events/input (str (t/date new-date)) id :begin]))
           :selectsStart true
           :placeholderText "請選擇日期"
           :dateFormat "yyyy 年 MM 月 dd 日"
           :todayButton "今日"
           :calendarStartDay 1
           :startDate @begin-atom
           :endDate @end-atom}]
         [:span "結束："]
         [:> DatePicker
          {:selected @end-atom
           :onChange (fn [new-date]
                       (println id " end changed to: " (str (t/date new-date)))
                       (reset! end-atom new-date)
                       (rf/dispatch [::events/input (str (t/date new-date)) id :end]))
           :selectsEnd true
           :placeholderText "請選擇日期"
           :dateFormat "yyyy 年 MM 月 dd 日"
           :todayButton "今日"
           :calendarStartDay 1
           :startDate @begin-atom
           :endDate @end-atom
           :minDate @begin-atom}]])})))

(defn view-node [node]
  (let [event-fn
        (fn [type value]
          (events/value-change {:type type :value value :node node}))]
    [:div
     [:div (int->chinese-num (inc (:id node)))]
     "月薪："
     [:input.input
      {:type "text"
       :placeholder "請輸入月薪"
       :on-change #(event-fn :text %)}]
     [datepicker-inner node]
     [view-payments (:insurance node)]]))

(defn view-nodes [nodes]
  (let [views (map view-node nodes)]
    `[:div ~@(interpose [:hr.hline] views)]))

(defn view-total [m]
  [:p.total
   [:div (str "總額試算：")]
   [:div (str "勞退為 " (:pension m) " 元")]
   [:div (str "健保為 " (:health m) " 元")]
   [:div (str "勞保為 " (:labor m) " 元")]])

(defn main-panel []
  (let [details? (rf/subscribe [::subs/details?])
        nodes (rf/subscribe [::subs/nodes])]
    [:div
     [:h1 "Simple 仁侍!"]
     [view-nodes @nodes]
     [:hr.hline]
     [view-total (calc/total @nodes)]
     [:div.meta-container
      [:div.container
       [:button.add {:on-click #(rf/dispatch [::events/add])} "+"]]
      [:div.container
       [:input {:type "checkbox"
                :checked @details?
                :on-change #(rf/dispatch [::events/check-details])}]
       "顯示細算資料？"]]
     [:p]
     [:p.footer
      "Made by 郭育賢"]]))
