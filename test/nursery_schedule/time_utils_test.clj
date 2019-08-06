(ns nursery-schedule.time-utils-test
  (:require [clojure.test :refer :all]
            [nursery-schedule.core :refer :all]
            [nursery-schedule.time-utils :refer :all]
            ))

(deftest test-weeks-since
  (testing "See how long ago it was since service, in weeks"
    (is (= (weeks-since #inst "2019-01-01" #inst "2019-01-01") 0.0))
    (is (= (weeks-since #inst "2019-01-01" #inst "2019-01-08") 1.0))
    ))

(deftest test-is-minor-at-serving-time
  (testing "Let's check the minor logic..."
    (is (= (is-minor-at-serving-time #inst "2019-01-01" #inst "2019-01-01") true))
    (is (= (is-minor-at-serving-time #inst "2000-01-01" #inst "2019-01-01") false))
    ))

(deftest test-last-served-date
  (testing "See when this person last served"
    (is (= (last-served-date {:last-served-on #inst "2019-01-01"}) #inst "2019-01-01"))
    (is (= (last-served-date {}) #inst "1970-01-01"))
    ))

(deftest test-normalized-service-period
  (testing "Determine the normalized service interval"
    (let [jan-08-case (partial normalized-service-period #inst "2019-01-08")
          jan-15-case (partial normalized-service-period #inst "2019-01-15")]
      (is (= (jan-08-case {}) 0.0))
      (is (= (jan-08-case {:last-served-on #inst "2019-01-01" :frequency 1.0}) 1.0))
      (is (= (jan-15-case {:last-served-on #inst "2019-01-01" :frequency 1.0}) 2.0))
      (is (= (jan-15-case {:last-served-on #inst "2019-01-01" :frequency 0.5}) 1.0))
    )))

(deftest test-update-normalized-service-record
  (testing "Update last served"
    (let [jan-08-case (partial update-normalized-service-record #inst "2019-01-08")]
      (is (= (jan-08-case {}) {:normalized-service-period 0.0})) 
      )))

(deftest test-normalized-tree-index
  (testing "Test the index?"
    (let [entry (fn [id weight] {:id id :normalized-service-period weight})
          e1 (entry 1 0.5)
          e2 (entry 2 1.0)
          ]
    (is (= (normalized-tree-index []) (sorted-map)))
    (is (= (normalized-tree-index [e1 e2]) (sorted-map 0.5 e1 1.5 e2)))
    (is (= (biggest-key (normalized-tree-index [e1 e2]))) 1.5)
    )))

(deftest test-first-item-greater-than
  (testing "Test looking up an item, used for the random selection"
    (let [entry (fn [id weight] {:id id :normalized-service-period weight})
          e1 (entry 1 0.5)
          e2 (entry 2 1.0)
          e3 (entry 3 2.0)
          tree (normalized-tree-index [e1 e2 e3])]
      (is (= (first-item-greater-than 0.0 tree) e1))
      (is (= (first-item-greater-than 0.5 tree) e1))
      (is (= (first-item-greater-than 0.51 tree) e2))
      (is (= (first-item-greater-than 1.5 tree) e2))
      (is (= (first-item-greater-than 1.51 tree) e3))
      (is (= (first-item-greater-than 3.5 tree) e3))
    )))

