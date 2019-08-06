(ns nursery-schedule.core-test
  (:require [clojure.test :refer :all]
            [nursery-schedule.core :refer :all]
            [nursery-schedule.sample-data :refer :all]
            [nursery-schedule.rules-core :refer :all]
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

(deftest test-size-functions
  (testing "Make sure size functions do what we expect"
    (is (= ((at-least 1) 0) :undersubscribed))
    (is (= ((at-least 1) 1) :satisfied))
    (is (= ((at-least 1) 2) :satisfied))
    (is (= ((at-most 1) 0) :satisfied))
    (is (= ((at-most 1) 1) :satisfied))
    (is (= ((at-most 1) 2) :oversubscribed))
    (is (= ((exactly 1) 0) :undersubscribed))
    (is (= ((exactly 1) 1) :satisfied))
    (is (= ((exactly 1) 2) :oversubscribed))
    (is (= ((in-range 1 2) 0) :undersubscribed))
    (is (= ((in-range 1 2) 1) :satisfied))
    (is (= ((in-range 1 2) 2) :satisfied))
    (is (= ((in-range 1 2) 3) :oversubscribed))
    ))

(def volunteer-1 {:gender :female :breeze-id 1 :roles [:babies :sunday-school]})
(def volunteer-2 {:gender :female :breeze-id 2 :roles [:babies :pre-k :toddlers :sunday-school]})
(def volunteer-3 {:gender :male :breeze-id 3 :roles [:babies :pre-k :toddlers]})
(def volunteer-4 {:gender :female :breeze-id 4 :roles [:babies :pre-k :toddlers]})
(def volunteer-5 {:gender :female :breeze-id 5 :roles [:supervisor]})

(def hello [ volunteer-1 volunteer-2 volunteer-3 volunteer-4 volunteer-5 ])

(defn trio-maker [role counter]
  [ (size-rule (role-serving-check role) counter)
   (partial assign-role role)
   (role-available-check role) ])

(let [
      sunday-school (trio-maker :sunday-school (at-least 2))
      toddlers (trio-maker :toddlers (at-least 2))
      supervisors (trio-maker :supervisor (exactly 1)) 
      seed-with-supervisors [{:assigned-roles [:supervisor]}]
      seed-with-toddlers [{:assigned-roles [:toddlers]}]
      ]
  (deftest test-is-unsatisfied?
    (testing "This tests the is-unsatisfied predicate, to ensure that we can find rules that still need to be addressed"
      (is (= true (is-unsatisfied? [] toddlers)))
      (is (= false (is-unsatisfied? seed-with-supervisors supervisors)))
      (is (= true (is-unsatisfied? seed-with-toddlers toddlers)))
      ))
  (deftest test-strictest-unsatisfied-rule
    (testing "This tests that we can find the strictest rule that needs updating, to ensure we schedule scare resources first."
      (is (= supervisors (strictest-unsatisfied-rule [toddlers supervisors] [] hello)))
      (is (= toddlers (strictest-unsatisfied-rule [toddlers supervisors] seed-with-supervisors hello)))
      ))
  (deftest test-scheduling
    (testing "Not quite sure what this will do..."
      (let [worker (fn [rules seed] (sort-by :breeze-id (rule-iteration rules seed hello)))
            affirm-role (fn [rule record] ((rule 1) record))
            ]
        (is (= (worker [supervisors] []) [
                                          (affirm-role supervisors volunteer-5)
                                          ]))
        (is (= (worker [supervisors sunday-school] []) [
                                                        (affirm-role sunday-school volunteer-1)
                                                        (affirm-role sunday-school volunteer-2)
                                                        (affirm-role supervisors volunteer-5)
                                                        ]))
        (is (= (worker [supervisors sunday-school toddlers] []) [
                                                        (affirm-role sunday-school volunteer-1)
                                                        (affirm-role sunday-school volunteer-2)
                                                        (affirm-role toddlers volunteer-3)
                                                        (affirm-role toddlers volunteer-4)
                                                        (affirm-role supervisors volunteer-5)
                                                        ]))
        )))
  )

