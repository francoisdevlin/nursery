(ns nursery-schedule.web-utils
  (:require 
    [hiccup.core :as hiccup]
    ))

(defn map-kids
  [tag & kids]
  (map (fn[kid] [tag kid]) kids))

(defn link-pair
  [text href]
  [:a {:href href} text])

(defn link-for-entity
  [prefix text-fn href-fn entity]
  (link-pair (text-fn entity) (str prefix (href-fn entity))))

(defn render-sequence-table
  [data & pairs]
  (let [headings (map first pairs)
        row-data-fn (apply juxt (map second pairs))
        row-td-fn (partial apply map-kids :td)
        ]
  [:table
   [:tr (apply map-kids :th headings)]
   (apply map-kids :tr (map (comp row-td-fn row-data-fn) data))
   ]))

(defn render-entity-table
  [entity & pairs]
  [:table
   (map (fn [[heading data-fn]] 
          [:tr 
           [:td heading]
           [:td (data-fn entity)]
           ]) pairs)])
