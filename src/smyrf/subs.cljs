(ns smyrf.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::text
 (fn [db]
   (:text db)))

(re-frame/reg-sub
 ::nodes
 (fn [db _]
   (get db :nodes)))
