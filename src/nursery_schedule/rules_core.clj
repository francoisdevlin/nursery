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

(defn is-unsatisfied?  [seed-volunteers [rule _ _]]
    (= :undersubscribed (rule seed-volunteers)))

(defn rule-candidates [volunteer-list [_ _ helper-pred]]
  (filter helper-pred volunteer-list))

(defn strictest-unsatisfied-rule
  [rules seed-volunteers volunteer-list]
  (let [unsatisfied-rules (filter (partial is-unsatisfied? seed-volunteers) rules)]
    (apply min-key (comp count (partial rule-candidates volunteer-list)) unsatisfied-rules)))

(defn rule-iteration
  [rules seed-volunteers volunteer-list]
  (let [rule-info (strictest-unsatisfied-rule rules seed-volunteers volunteer-list)
        [rule mutator helper-pred] rule-info
        proposed-volunteer (mutator (rand-nth (rule-candidates volunteer-list rule-info)))
        next-iteration-volunteers (conj seed-volunteers proposed-volunteer)
        next-status (if (< 0 (count (filter (partial is-unsatisfied? next-iteration-volunteers) rules)))
                      :undersubscribed
                      :satisfied)
        next-ids (set (map :breeze-id next-iteration-volunteers))
        next-iteration-pool (remove (comp next-ids :breeze-id) volunteer-list)
        ]
    (cond 
      (= next-status :satisfied) next-iteration-volunteers
      (= next-status :undersubscribed) (rule-iteration rules next-iteration-volunteers next-iteration-pool)
      (= next-status :oversubscribed) (rule-iteration rules seed-volunteers volunteer-list)
      )))

