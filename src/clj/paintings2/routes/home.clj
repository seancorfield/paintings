(ns paintings2.routes.home
  (:require [paintings2.layout :as layout]
            [compojure.core :refer [defroutes GET  ]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [paintings2.api-get :as api]
            [compojure.route :refer [resources]]))

(defn home-page []
  (layout/render
    "home.html" {:paintings (api/do-both-in-parallel(api/read-numbers))  }))

(defroutes home-routes
  (GET "/" [] (home-page))
  (resources "/")  )



