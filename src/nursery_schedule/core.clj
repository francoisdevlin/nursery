(ns nursery-schedule.core
  (:require 
    [nursery-schedule.rules-core :refer :all]
    [nursery-schedule.time-utils :refer :all]
    )
  (:import (java.text SimpleDateFormat)))

(defn load-record
  [[breeze-id full-name birthday role frequency & other]]
  {
   :breeze-id breeze-id
   :full-name full-name
   :birthday (.parse (new SimpleDateFormat "dd/MM/yyyy") birthday)
   :role ({"Supervisor" :supervisor} role)
   :frequency (Float/parseFloat frequency)
   })

(defn is-minor-at-serving-time
  [birthday proposed-served-date]
  (> 
    (+ 4 (* 18 52)) ;18 * 52 for the weeks in 18 years + 4 more for lead years, that one extra day, etc.  It's okay if it's over by a week. 
    (weeks-since birthday proposed-served-date)))

(defn index-by
  "How is this not in core, seriously?"
  [index-fn coll]
  (into {} (map (juxt index-fn identity) coll)))

(defn schedule-event
  [event-date volunteer-list]
  (let [update-partial (partial update-normalized-service-record event-date)
        updated-records (map update-partial volunteer-list)
        id-indexed-volunteers (index-by :breeze-id updated-records)]
    ))

(defn grab-signups
  [event-date volunteer-list]
  [])

(defn grab-associated-volunteers
  [proposed-volunteer volunteer-index]
  [proposed-volunteer])

(defn is-satisfied
  [event-rules volunteer-list])

(defn particpant-count-status
  [size proposed-volunteers]
  (size-engine identity (exactly size) proposed-volunteers))

(defn role-serving-check [role]
  (comp (partial some #{role}) :assigned-roles))

(defn role-available-check [role]
  (comp (partial some #{role}) :roles))

(defn assign-role
  [role volunteer]
  (assoc volunteer :assigned-roles #{role}))

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

(def is-female? (comp #{:female} :gender))

(def active-supervisor (role-serving-check :supervisor))
(def active-babies (role-serving-check :babies))
(def active-toddlers (role-serving-check :toddlers))
(def active-pre-k (role-serving-check :pre-k))
(def active-sunday-school (role-serving-check :sunday-school))

(def >1 (at-least 1))
(def >2 (at-least 2))

(def supervisor-size (size-rule active-supervisor >1))
(def babies-size (size-rule active-babies >2))
(def babies-has-one-woman (size-rule (every-pred? active-babies is-female?) >1))
(def toddlers-size (size-rule active-toddlers >2))
(def toddlers-has-one-woman (size-rule (every-pred? active-toddlers is-female?) >1))
(def pre-k-size (size-rule active-pre-k >2))
(def pre-k-has-one-woman (size-rule (every-pred? active-pre-k is-female?) >1))
(def sunday-school-size (size-rule active-sunday-school >2))
(def sunday-school-has-one-woman (size-rule (every-pred? active-sunday-school is-female?) >1))


(defn assign-volunteers
  [event-date event-rules volunteer-list])

