(ns nursery-schedule.people
  (:require [nursery-schedule.web-utils :refer :all]
            [nursery-schedule.core :refer :all]
            [nursery-schedule.sample-data :refer :all]
            [clojure.string :as s]
            )
  (:import (java.text SimpleDateFormat))
  )

(def all-people
  (index-by :breeze-id
            (map load-record sample-data)))

(def yyyy-mm-dd-formatter (SimpleDateFormat. "yyyy-MM-dd"))

(def id-lookup ["Id" :breeze-id])
(def name-lookup-link ["Name" (partial link-for-entity "/people/" :full-name :breeze-id)])
(def name-lookup ["Name" :full-name])
(def birthday-lookup ["Birthday" #(.format yyyy-mm-dd-formatter (:birthday %))])
(def frequency-lookup ["Frequency" #(str (:frequency %) "/week")])
(def roles-lookup ["Roles" (comp (partial s/join " ") :roles)])

(defn render-all
  [people]
  [
   [:h1 "People Management"]
   (render-sequence-table 
     (vals people)
     id-lookup
     name-lookup-link 
     birthday-lookup
     frequency-lookup
     roles-lookup
     )
   ])

(defn render
  [person]
  [
   [:h1 (str "Viewing info for " (:full-name person))]
   (render-entity-table 
     person
     id-lookup
     name-lookup
     birthday-lookup
     frequency-lookup
     roles-lookup
     )
   ])
