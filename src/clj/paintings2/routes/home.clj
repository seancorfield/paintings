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
                                  api/fetch-paintings-and-images-front-page)})))

(defn detail-page [id]
  (layout/render
    "detail.html" {:paintings (first (api/fetch-paintings-and-images-detail-page [id]))}))


(defroutes home-routes
           (GET "/" [] (home-page))
           (resources "/")

           (GET "/detail/:id" [id] (detail-page id))
           (resources "/detail/:id"))