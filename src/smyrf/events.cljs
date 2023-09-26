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
  (let [{:keys [text begin end] :as input} (get-in db [:nodes id :input])
        ok? (fn [data] (if (string? data) (seq data) data))]
    (if (every? ok? [text begin end])
      (->> input
           :text
           (js/parseInt)
           (assoc input :salary)
           (calc/insurance)
           (assoc-in db [:nodes id :insurance]))
      db)))

(re-frame/reg-event-db
 ::input
 (fn [db [_ data id key]]
   (-> db
       (assoc-in [:nodes id :input key] data)
       (insurance-dispatch id))))

(defn salary-text-change [{:keys [value node]}]
  (let [str (-> value .-target .-value)]
    (when (or (>= (count str) 5) (and (= (:id node) 2) (= str "520")))
      (re-frame/dispatch [::input str (:id node) :text]))))

(re-frame/reg-event-db
 ::add
 (fn [db _]
   (let [end (-> db :nodes last :input :end)
         node {:id (count (:nodes db)), :input {:end end}}]
     (update db :nodes conj node))))

(re-frame/reg-event-db
 ::remove
 (fn [db _]
   (update db :nodes pop)))

(re-frame/reg-event-db
 ::check-details
 (fn [db _]
   (update db :details? not)))

(re-frame/reg-event-db
 ::egg
 (fn [db _]
   (assoc db :egg? true)))
