(ns nursery-schedule.time-restrictions
  (:require [clojure.string :as s])
  (:import (java.text SimpleDateFormat))
  )

(def yyyy-mm-dd-formatter (SimpleDateFormat. "yyyy-MM-dd"))

(defprotocol Restrictable
  (is-active? [this target-date] "Determine if the restriction is active at a given time"))

(defprotocol Printable
  (print-restriction [this] "Prints a human readable description of the restriction"))

(defrecord BlackoutDates
  [dates]
  Restrictable
  (is-active? [this target-date] (not (nil? (dates target-date))))
  Printable
  (print-restriction [this] (str "on dates " (s/join ", " (map #(.format yyyy-mm-dd-formatter %) (sort dates)))))
  )

(defrecord InvertRestriction
  [child]
  Restrictable
  (is-active? [this target-date] (not (is-active? child target-date)))
  Printable
  (print-restriction [this] (str "except " (print-restriction child)))
  )

(defrecord RangeRestriction
  [start end]
  Restrictable
  (is-active? [this target-date]
    (and (if (nil? start) true (apply <= (map #(.getTime %) [start target-date])))
         (if (nil? end) true (apply <= (map #(.getTime %) [target-date end])))
         ))
  Printable
  (print-restriction [this] 
    (str 
      (if (not (nil? start)) (str "Starting on " (.format yyyy-mm-dd-formatter start)))
      (if (some nil? [start end]) "" " ")
      (if (not (nil? end)) (str "Through " (.format yyyy-mm-dd-formatter end)))
      ))
  )

