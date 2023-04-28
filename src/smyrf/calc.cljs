(ns smyrf.calc
  (:require
   [tick.core :as t]
   [smyrf.data :as data]))

;; Define data aliases at the top
;; so they only have to be changed here.
(def ^:private data-health-old data/health111)
(def ^:private data-health-new data/health112)
(def ^:private data-labor-old data/labor111)
(def ^:private data-labor-new data/labor112)
;; Cutoff points between new and old data.
(def ^:private cutoff-date-labor (t/date "2023-01-01"))
(def ^:private cutoff-date-health (t/date "2023-01-01"))
;; Comments to put on old data.
(def ^:private comment-labor-old "（111年勞保級距）")
(def ^:private comment-health-old "（111年健保級距）")

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

(defn- date-health-ins
  ([insurance dates] (date-health-ins insurance dates nil))
  ([insurance dates comment]
   (let [helper
         (fn [m]
           [m {:health insurance, :health-comment comment}])]
     (->> (map t/year-month dates)
          (into #{})
          (map helper)
          (into {})))))

(defn- health-payments [salary dates]
  (let [[old-dates new-dates] (split-with #(< % cutoff-date-health) dates)
        old-ins (:insurance (salary-bracket salary data-health-old))
        new-ins (:insurance (salary-bracket salary data-health-new))]
    (merge
     (date-health-ins old-ins old-dates comment-health-old)
     (date-health-ins new-ins new-dates))))

(defn- labor-helper-transform
  [bracket comment [month days]]
  (let [pension (js/Math.round (* (:income bracket) 0.06))]
    [month
     {:days days
      :labor (get-in bracket [:insurance (clamp days) :organization])
      :pension pension
      :labor-comment comment}]))

(defn- labor-helper
  ([bracket dates] (labor-helper bracket dates nil))
  ([bracket dates comment]
   (->> (map t/year-month dates)
        (frequencies)
        (map (partial labor-helper-transform bracket comment))
        (into {}))))

(defn- labor-payments [salary dates]
  (let [[old-dates new-dates] (split-with #(< % cutoff-date-labor) dates)
        old-bracket (salary-bracket salary data-labor-old)
        new-bracket (salary-bracket salary data-labor-new)]
    (merge
     (labor-helper old-bracket old-dates comment-labor-old)
     (labor-helper new-bracket new-dates))))

(defn insurance
  "Takes SALARY as an integer, and BEGIN and END as strings."
  [{:keys [salary begin end]}]
  (let [dates (inclusive-date-range begin end)
        health (health-payments salary dates)
        labor (labor-payments salary dates)
        months (sort (keys health))]
    (for [month months]
      (merge (labor month) (health month) {:month month}))))

(defn total [nodes]
  (let [sum-fn
        (fn [ms]
          {:pension (reduce + (map :pension ms))
           :health (reduce + (map :health ms))
           :labor (reduce + (map :labor ms))})]
    (->> nodes
         (map (comp sum-fn :insurance))
         (sum-fn))))

(comment
  (->> (insurance {:salary 42000 :begin "2023-01-01" :end "2023-03-17"})
       ))
