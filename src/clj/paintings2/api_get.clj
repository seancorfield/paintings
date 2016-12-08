(ns paintings2.api-get
  (:require [clj-http.client :as client]
            [environ.core :refer [env]]))


(defn read-numbers
  "Reads the ids of the paintings"
  []
  (let [data (-> ( str "https://www.rijksmuseum.nl/api/nl/collection")
                 (client/get {:as :json :query-params {:key (env :key) :format "json" :type "schilderij" :toppieces "True"}})
                 :body
                 :artObjects
                 )
        map-id (map :id data)
        ids (reduce (fn [results id] (conj results (.substring id 3))) [] map-id)]
    ids ))


(defn read-data-painting
  "Reads the title, description, date , collection, colors and url of a image"
  [id]
  (let [art-objects (-> (str "https://www.rijksmuseum.nl/api/nl/collection/" id )
                        (client/get {:as :json :query-params {:key (env :key) :format "json" :type "schilderij" :toppieces "True"}})
                        :body
                        :artObject)
        name        (-> art-objects
                        :principalMakers
                        first
                        :name)
        description (:description art-objects)
        date (get-in art-objects [:dating :year])
        collectie (first (:objectCollection art-objects))
        colors (:colors art-objects)]
    {:id id :name name :description description :date date :collectie collectie :colors colors}))

(defn read-image-url
  "Reads the image-url"
  [id]
  (let [art-objects (-> (str "https://www.rijksmuseum.nl/api/nl/collection/" id "/tiles")
                        (client/get {:as :json :query-params {:format "json"  :key (env :key)}})
                        :body
                        :levels
                        )

        url (filter #(= (:name %) "z4") art-objects)
        tiles (:tiles (first url))
        image (get-in tiles [0 :url] )
     ]
    {:id id :tiles image }))

(defn do-both-in-parallel [ids]
  (let [paint-thread (future (pmap read-data-painting ids))
        image-thread (future (pmap read-image-url ids))]
    (map merge @paint-thread @image-thread)))
