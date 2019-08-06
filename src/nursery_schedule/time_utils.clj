(ns nursery-schedule.time-utils)

(defn seconds-since
  [last-served-date proposed-served-date]
  (let [milliseconds-since (- (.getTime proposed-served-date) (.getTime last-served-date))]
    (/ milliseconds-since 1000)))

(defn days-since
  [last-served-date proposed-served-date]
  (let [milliseconds-since (- (.getTime proposed-served-date) (.getTime last-served-date))]
    (/ milliseconds-since 1000 86400.0)))

(defn weeks-since
  [last-served-date proposed-served-date]
  (let [milliseconds-since (- (.getTime proposed-served-date) (.getTime last-served-date))]
    (/ milliseconds-since 1000 86400 7.0)))

(defn last-served-date
  [record]
  (:last-served-on record #inst "1970-01-01"))

(defn normalized-service-period
  [service-date record]
  (let [last-date (last-served-date record)
        service-frequency (:frequency record 0.0)]
    (* (weeks-since last-date service-date) service-frequency)))

(defn update-normalized-service-record
  [event-date record]
  (assoc record :normalized-service-period 
         (normalized-service-period event-date record)))

(defn normalized-tree-index
  [volunteers]
  (java.util.TreeMap. (into (sorted-map) 
        (map vector
             (reductions + (map :normalized-service-period volunteers))
             volunteers))))

(defn biggest-key
  [tree-map]
  (.getKey (.lastEntry tree-map)))

(defn first-item-greater-than
  [k tree-map]
  (.getValue (.ceilingEntry tree-map k)))

