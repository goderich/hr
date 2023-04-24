(ns smyrf.events
  (:require
   [re-frame.core :as re-frame]
   [smyrf.db :as db]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::input
 (fn [db [_ text id]]
   (assoc-in db [:nodes id :text] text)))

(defn text-change [text id]
  (re-frame/dispatch [::input (-> text .-target .-value) id]))

(re-frame/reg-event-db
 ::add
 (fn [db _]
   (let [id (count (:nodes db))
         node {:text "placeholder" :id id}
         nodes (conj (:nodes db) node)]
     (assoc db :nodes nodes))))
