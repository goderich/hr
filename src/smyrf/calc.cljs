(ns smyrf.calc
  (:require
   [tick.core :as t]
   [smyrf.data :as data]))

(def ^:private labor-cutoff-date
  "The cutoff point between new and old tables
  for calculating labor insurance."
  (t/date "2023-01-01"))

(def ^:private health-cutoff-date
  "The cutoff point between new and old tables
  for calculating health insurance."
  (t/date "2023-01-01"))

(defn- salary-bracket [salary table]
  (or (->> table
           (drop-while #(> salary (:income %)))
           (first))
      (last table)))

(defn- clamp
  "Lower the number if it's outside the bracket.
  Insurance rates go up to 30 days per month.
  31 days counts as 30."
  [n]
  (if (= n 31) 30 n))

(defn- inclusive-date-range
  "BEGIN and END are assumed to be strings."
  [begin end]
  (let [begin (t/date begin)
        end (t/>> (t/date end) (t/new-period 1 :days))]
    (t/range begin end)))

(comment
  (let [ds (inclusive-date-range (t/date "2022-12-25") (t/date "2023-01-08"))]
    (split-with #(< % labor-cutoff-date) ds)))

(defn- payments
  [salary begin end]
  (let [dates (inclusive-date-range begin end)
        freqs (frequencies (map t/year-month dates))
        labor-bracket (salary-bracket salary data/labor112)
        health-bracket (salary-bracket salary data/health112)
        pension (js/Math.round (* (:income labor-bracket) 0.06))]
    {:labor-bracket (:income labor-bracket)
     :health-bracket (:income health-bracket)
     :payments
     (into []
           (for [[month days] freqs]
             {:pension pension
              :labor (get-in labor-bracket [:insurance (clamp days) :organization])
              :health (:insurance health-bracket)
              :days days
              :month month}))}))

(defn insurance
  "Takes SALARY as an integer, and BEGIN and END as strings."
  [{:keys [salary begin end]}]
  (payments salary begin end))

(defn total [nodes]
  (let [sum-fn
        (fn [ms]
          {:pension (reduce + (map :pension ms))
           :health (reduce + (map :health ms))
           :labor (reduce + (map :labor ms))})]
    (->> nodes
         (map (comp sum-fn :payments :insurance))
         (sum-fn))))

(comment
  (->> (payments 42000 "2023-01-01" "2023-03-17")
       :payments
       total))
