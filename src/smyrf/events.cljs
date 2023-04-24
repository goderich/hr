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
