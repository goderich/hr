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
  [begin end]
  (let [begin (t/date begin)
        end (t/>> (t/date end) (t/new-period 1 :days))]
    (t/range begin end)))

(defn- last-day-of-month?
  [date]
  (when date
    (-> date t/year-month t/end t/date t/dec (= date))))

(defn- health-ins-months
  "Health insurance only counts for the full month,
  so if the final month is not complete, remove it."
  [dates]
  (let [all-months (distinct (map t/year-month dates))
        full-last-month? (last-day-of-month? (last dates))]
    (if full-last-month?
      all-months
      (butlast all-months))))

(defn- date-health-ins
  ([insurance dates] (date-health-ins insurance dates nil))
  ([insurance dates comment]
   (let [helper
         (fn [m]
           [m {:health insurance, :health-comment comment}])]
     (->> (health-ins-months dates)
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
  [{:keys [salary begin end]}]
  (let [dates (inclusive-date-range begin end)
        health (health-payments salary dates)
        labor (labor-payments salary dates)
        ;; Health insurance is zero for the final month if it is incomplete,
        ;; so we use the dates on labor insurance.
        months (sort (keys labor))]
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
  (->> (insurance {:salary 42000 :begin "2023-01-01" :end "2023-03-17"}))
  (last-day-of-month? (t/date "2023-02-28")) ;; => true
  (last-day-of-month? (t/date "2024-02-28")) ;; => false
  (last-day-of-month? (t/date "2023-08-31")) ;; => true

  )
