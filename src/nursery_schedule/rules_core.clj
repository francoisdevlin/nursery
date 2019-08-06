(ns nursery-schedule.rules-core)

(defn every-pred?  [& predicates]
  (fn [x] (every? (fn [pred] (pred x)) predicates)))

(defn not-pred?  [pred]
  (fn [x] (not (pred x))))

(defn size-engine
  [record-predicate size-handler proposed-volunteers]
  (let [volunteer-count (count (filter record-predicate proposed-volunteers))]
    (size-handler volunteer-count)))

(defn size-rule
  [record-pred size-pred]
  (partial size-engine record-pred size-pred))

(defn at-least
  [expected-size]
  (fn [actual-size] 
    (if (< actual-size expected-size)
      :undersubscribed :satisfied)))

(defn at-most
  [expected-size]
  (fn [actual-size] 
    (if (<= actual-size expected-size)
      :satisfied :oversubscribed)))

(defn in-range
  [min-size max-size]
  (fn [size]
    (cond (< size min-size) :undersubscribed
          (> size max-size) :oversubscribed
          true :satisfied
          )))

(defn exactly
  [expected-size]
  (fn [size]
    (cond (< size expected-size) :undersubscribed
          (> size expected-size) :oversubscribed
          true :satisfied
          )))

