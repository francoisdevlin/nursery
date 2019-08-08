(ns nursery-schedule.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :as hiccup]
            [clojure.edn :as edn]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [nursery-schedule.web-utils :refer :all]
            [nursery-schedule.people :as people]
            [nursery-schedule.event-template :as event-template]
            )
  )

(defn application-layout
  [& content]
  (hiccup/html 
    [:html 
     [:body 
      [:div {:style "text-align:right;"}
       [:table [:tr (map-kids :td 
                              (link-pair "Home" "/") 
                              (link-pair "People" "/people")
                              (link-pair "Event Template" "/event-template")
                              )]]]
      [:div content]
      ]]
    ))

(defroutes app-routes
  (GET "/" [] (application-layout [:h1 "Hello Bacon Pizza World"]))
  (GET "/people" [] (apply application-layout (people/render-all people/all-people)))
  (GET "/people/:id" [id] (apply application-layout (people/render (people/all-people id))))
  (GET "/event-template" [] (apply application-layout (event-template/render-all event-template/all-event-templates)))
  (GET "/event-template/:id" [id] (apply application-layout (event-template/render (event-template/all-event-templates id))))
  (route/not-found (application-layout [:h1 "Looks like we couldn't find it..."] [:img {:src "https://http.cat/404"}]))
  )

(def app
  (wrap-defaults app-routes site-defaults))
