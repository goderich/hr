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

(defn- int->chinese-num [n]
  (let [sinicize {1 "一", 2 "二", 3 "三", 4 "四", 5 "五",
                  6 "六", 7 "七", 8 "八", 9 "九"}
        dec (let [q (quot n 10)]
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
              [:div
               [:div (str (-> m :month date->str) "：")]
               [:div (str "勞退為 " (:pension m) " 元")]
               (if (:health m)
                 [:div (str "健保為 " (:health m) " 元"
                            (:health-comment m))]
                 [:div "無健保支出"])
               [:div (str (:days m) "天校方勞保支出為 "
                          (:labor m) " 元" (:labor-comment m))]
               [:p]])))))

(defn datepicker-inner [node]
  (let [id (:id node)
        begin-atom (reagent/atom nil)
        end-atom (reagent/atom (when-let [end (-> node :input :end)]
                                 (new js/Date end)))
        options {:placeholderText "請選擇日期"
                 :dateFormat "yyyy年MM月dd日"
                 :todayButton "今日"
                 :calendarStartDay 1
                 :startDate @begin-atom
                 :endDate @end-atom}]
    (reagent/create-class
     {:display-name "date picker"

      :reagent-render
      (fn []
        [:div
         [:div "開始："
          [:> DatePicker
           (merge options
                  {:selected @begin-atom
                   :onChange (fn [new-date]
                               (reset! begin-atom new-date)
                               (rf/dispatch [::events/input new-date id :begin]))
                   :selectsStart true})]]
         [:div "結束："
          [:> DatePicker
           (merge options
                  {:selected @end-atom
                   :onChange (fn [new-date]
                               (reset! end-atom new-date)
                               (rf/dispatch [::events/input new-date id :end]))
                   :selectsEnd true
                   :minDate @begin-atom})]]])})))

(defn view-node [node]
  [:div
   [:div (int->chinese-num (inc (:id node)))]
   "月薪："
   [:input.input
    {:type "text"
     :placeholder "請輸入月薪"
     :on-change #(events/salary-text-change {:value % :node node})}]
   [datepicker-inner node]
   [view-payments (:insurance node)]])

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
        egg? (rf/subscribe [::subs/egg?])
        nodes (rf/subscribe [::subs/nodes])]
    [:div
     [:h1 "Simple 仁侍!"]
     (when @egg?
       [:img {:src "resources/public/egg.jpg"}])
     [:div.header "112年11月更新"]
     [view-nodes @nodes]
     [:hr.hline]
     [view-total (calc/total @nodes)]
     [:div.meta-container
      [:div.container
       [:button.add {:on-click #(rf/dispatch [::events/add])} "+"]]
      [:div.container
       [:button.add {:on-click #(rf/dispatch [::events/remove])} "-"]]
      [:div.container
       [:input {:type "checkbox"
                :checked @details?
                :on-change #(rf/dispatch [::events/check-details])}]
       "顯示細算資料？"]]
     [:p]
     (if (= (get-in @nodes [2 :input :text]) "520")
       [:p.shell
        [:button.egg
         {:on-click #(rf/dispatch [::events/egg])}
         (if @egg? "看上面~" "Made by 郭育賢")]]
       [:p.footer
        "Made by 郭育賢"])]))
