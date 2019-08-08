(ns nursery-schedule.event-template
  (:require [nursery-schedule.web-utils :refer :all]
            [nursery-schedule.core :refer :all]
            [nursery-schedule.sample-data :refer :all]
            [clojure.string :as s]
            )
  (:import (java.text SimpleDateFormat))
  )

(def all-event-templates {
                          "1" {:id 1 :name "Nursery" :roles [:supervisor :babies :toddlers :pre-k {:name :sunday-school :effective-on #inst "2019-09-15"}]}
                          })

(def yyyy-mm-dd-formatter (SimpleDateFormat. "yyyy-MM-dd"))

(def id-lookup ["Id" :id])
(def name-lookup-link ["Name" (partial link-for-entity "/event-template/" :name :id)])
(def name-lookup ["Name" :name])
(def roles-lookup ["Roles" (comp (partial s/join " ") :roles)])

(defn render-all
  [event-templates]
  [
   [:h1 "Event Management"]
   (render-sequence-table 
     (vals event-templates)
     id-lookup
     name-lookup-link 
     roles-lookup
     )
   ])

(defn render
  [event-template]
  [
   [:h1 (str "Viewing info for event " (:name event-template))]
   (render-entity-table 
     event-template
     id-lookup
     name-lookup
     roles-lookup
     )
   ])

