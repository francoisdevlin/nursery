(ns nursery-schedule.time-restrictions
  )

(defprotocol Restrictable
  (is-active? [this target-date] "Determine if the restriction is active at a given time"))

(defrecord BlackoutDates
  [dates]
  Restrictable
  (is-active? [this target-date] (not (nil? (dates target-date)))))

(defrecord InvertRestriction
  [child]
  Restrictable
  (is-active? [this target-date] (not (is-active? child target-date))))

(defrecord RangeRestriction
  [start end]
  Restrictable
  (is-active? [this target-date]
    (and (if (nil? start) true (apply <= (map #(.getTime %) [start target-date])))
         (if (nil? end) true (apply <= (map #(.getTime %) [target-date end])))
         )
  ))

