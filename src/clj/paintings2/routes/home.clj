(ns paintings2.routes.home
  (:require [paintings2.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [paintings2.api-get :as api]
            [compojure.route :refer [resources]]
            [environ.core :refer [env]]
            [paintings2.api-get :as api]
            [clj-http.client :as client]))

(defn home-page []
  (let [url "https://www.rijksmuseum.nl/api/nl/collection"
        options {:as :json :query-params {:key (env :key) :format "json" :type "schilderij" :toppieces "True"}}]
        (layout/render
          "home.html" {:paintings (-> (client/get url options)
                                      api/read-numbers
                                      api/do-both-in-parallel)})))

(defroutes home-routes
  (GET "/" [] (home-page))
  (resources "/")  )

