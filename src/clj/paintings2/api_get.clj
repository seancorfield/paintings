(ns paintings2.api-get
  (:require [clj-http.client :as client]))


(defn read-numbers
  "Reads the ids of the paintings"
  []
  (let [ids (->> (client/get "https://www.rijksmuseum.nl/api/nl/collection?key=14OGzuak&format=json&type=schilderij&toppieces=True" {:as :json})
                 :body
                 :artObjects
                 (map :id))
        ids (reduce (fn [results id] (conj results (.substring id 3))) [] ids)]
    ids))


(defn read-data-painting
  "Reads the title, description, date , collection, colors and url of a image"
  [id]
  (let [art-objects (-> (str "https://www.rijksmuseum.nl/api/nl/collection/" id "?key=14OGzuak&format=json&type=schilderij&toppieces=True")
                        (client/get {:as :json})
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
  (let [art-objects (-> (str "https://www.rijksmuseum.nl/api/nl/collection/" id "/tiles?key=14OGzuak&format=json")
                        (client/get {:as :json})
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
    (pmap merge @paint-thread @image-thread)))

