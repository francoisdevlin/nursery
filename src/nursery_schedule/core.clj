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
   :birthday (if (= "" birthday) #inst "1970-01-01" (.parse (new SimpleDateFormat "dd/MM/yyyy") birthday))
   :roles (if (#{"Supervisor"} role) [:supervisor] [:babies :toddlers :pre-k :sunday-school])
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

(defn role-serving-check [role]
  (comp (partial some #{role}) :assigned-roles))

(defn role-available-check [role]
  (comp (partial some #{role}) :roles))

(defn assign-role
  [role volunteer]
  (assoc volunteer :assigned-roles #{role}))
