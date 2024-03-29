(ns smyrf.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::details?
 (fn [db]
   (get db :details?)))

(re-frame/reg-sub
 ::egg?
 (fn [db]
   (get db :egg?)))

(re-frame/reg-sub
 ::nodes
 (fn [db]
   (get db :nodes)))
