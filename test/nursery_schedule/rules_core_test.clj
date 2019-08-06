(ns nursery-schedule.rules-core-test
  (:require [clojure.test :refer :all]
            [nursery-schedule.core :refer :all]
            [nursery-schedule.rules-core :refer :all]
            ))

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
