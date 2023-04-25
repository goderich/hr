(ns smyrf.calc
  (:require [tick.core :as t]))

(defn insurance [{:keys [text begin end]}]
  (str text "_" (t/year-month (t/date begin)) "_" end))
