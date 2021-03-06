(ns nursery-schedule.time-restrictions-test
  (:require [clojure.test :refer :all]
            [nursery-schedule.time-restrictions :refer :all]
            )
  (:import [nursery_schedule.time_restrictions BlackoutDates InvertRestriction RangeRestriction])
  )

(deftest test-blackout-engine
  (testing ""
    (let [blackout (BlackoutDates. #{1 2 3})
          invert-blackout (InvertRestriction. blackout)
          nil-range-blackout (RangeRestriction. nil nil)
          less-than-range-blackout (RangeRestriction. nil #inst "2019-01-01")
          greater-than-range-blackout (RangeRestriction. #inst "2019-01-01" nil)
          closed-range-blackout (RangeRestriction. #inst "2019-01-01" #inst "2019-01-07")
          ]
      (is (= true (is-active? blackout 2)))
      (is (= false (is-active? blackout 4)))
      (is (= false (is-active? invert-blackout 2)))
      (is (= true (is-active? invert-blackout 4)))
      (is (= true (is-active? nil-range-blackout 4)))
      (is (= true (is-active? less-than-range-blackout #inst "2018-12-31")))
      (is (= true (is-active? less-than-range-blackout #inst "2019-01-01")))
      (is (= false (is-active? less-than-range-blackout #inst "2019-01-02")))
      (is (= false (is-active? greater-than-range-blackout #inst "2018-12-31")))
      (is (= true (is-active? greater-than-range-blackout #inst "2019-01-01")))
      (is (= true (is-active? greater-than-range-blackout #inst "2019-01-02")))
      (is (= false (is-active? closed-range-blackout #inst "2018-12-31")))
      (is (= true (is-active? closed-range-blackout #inst "2019-01-01")))
      (is (= true (is-active? closed-range-blackout #inst "2019-01-02")))
      (is (= true (is-active? closed-range-blackout #inst "2019-01-07")))
      (is (= false (is-active? closed-range-blackout #inst "2019-01-08")))
      )))

(deftest test-printing-items
  (testing ""
    (let [blackout (BlackoutDates. #{#inst "2019-01-01" #inst "2018-01-01"})
          invert-blackout (InvertRestriction. blackout)
          nil-range-blackout (RangeRestriction. nil nil)
          less-than-range-blackout (RangeRestriction. nil #inst "2019-01-01")
          greater-than-range-blackout (RangeRestriction. #inst "2019-01-01" nil)
          closed-range-blackout (RangeRestriction. #inst "2019-01-01" #inst "2019-01-07")
          ]
      (is (= "on dates 2018-01-01, 2019-01-01" (print-restriction blackout)))
      (is (= "except on dates 2018-01-01, 2019-01-01" (print-restriction invert-blackout)))
      (is (= "" (print-restriction nil-range-blackout)))
      (is (= "Through 2019-01-01" (print-restriction less-than-range-blackout)))
      (is (= "Starting on 2019-01-01" (print-restriction greater-than-range-blackout)))
      (is (= "Starting on 2019-01-01 Through 2019-01-07" (print-restriction closed-range-blackout)))
      )))
