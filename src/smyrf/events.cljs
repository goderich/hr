(ns smyrf.events
  (:require
   [re-frame.core :as re-frame]
   [smyrf.db :as db]
   [smyrf.calc :as calc]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(defn insurance-dispatch [db id]
  (let [{:keys [text begin end] :as input} (get-in db [:nodes id :input])]
    (if (every? seq [text begin end])
      (->> input
           :text
           (js/parseInt)
           (assoc input :salary)
           (calc/insurance)
           (assoc-in db [:nodes id :insurance]))
      db)))

(re-frame/reg-event-db
 ::input
 (fn [db [_ text id key]]
   (-> db
       (assoc-in [:nodes id :input key] text)
       (insurance-dispatch id))))

(defmulti value-change :type)

(defmethod value-change :text [{:keys [value node]}]
  (let [str (-> value .-target .-value)]
    (when (>= (count str) 5)
      (re-frame/dispatch [::input str (:id node) :text]))))

(defmethod value-change :begin [{:keys [value node]}]
  (let [str (-> value .-target .-value)]
    (re-frame/dispatch [::input str (:id node) :begin])))

(defmethod value-change :end [{:keys [value node]}]
  (let [str (-> value .-target .-value)]
    (re-frame/dispatch [::input str (:id node) :end])))

(re-frame/reg-event-db
 ::add
 (fn [db _]
   (let [end (-> db :nodes last :input :end)
         node {:id (count (:nodes db)), :input {:end end}}
         nodes (conj (:nodes db) node)]
     (assoc db :nodes nodes))))

(re-frame/reg-event-db
 ::check-details
 (fn [db _]
   (update db :details? not)))
