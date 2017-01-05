(ns paintings2.routes.home
  (:require [paintings2.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [compojure.route :refer [resources]]
            [environ.core :refer [env]]
            [paintings2.api-get :as api]
            [clj-http.client :as client]
            [clojure.spec :as s]))

;;  the specs input validation file

(defn ->long [s] (try (Long/parseLong s) (catch Exception _ ::s/invalid)))

(s/def ::page (s/and (s/conformer ->long) (s/int-in 1 471)))

(s/def ::optional-page (s/nilable ::page))


;; the source files to make the routes

(defn home-page [page]
  (let [page-num (s/conform ::page page)
        url "https://www.rijksmuseum.nl/api/nl/collection"
        options {:as :json :query-params {:key (env :key) :format "json" :type "schilderij" :toppieces "True" :p page-num :ps 10}}]
    (if (s/invalid? page-num) 1 page-num)
    (layout/render
      "home.html" {:paintings (-> (client/get url options)
                                  (api/read-numbers)
                                  (api/fetch-paintings-and-images-front-page))})))

(defn detail-page [id]
  (layout/render
    "detail.html" {:paintings (first (api/fetch-paintings-and-images-detail-page [id]))}))


(defroutes home-routes
           (GET "/" [page] (home-page page))
           (resources "/")

           (GET "/detail/:id" [id] (detail-page id))
           (resources "/detail/:id"))
