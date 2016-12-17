(ns paintings2.api-get
  (:require [clj-http.client :as client]
            [environ.core :refer [env]]))


(defn read-numbers
  "Reads the ids of the paintings"
  [response]
  (->> (:body response)
       :artObjects
       (map :objectNumber)))


(defn read-json-data [id] (client/get
                            (str "https://www.rijksmuseum.nl/api/nl/collection/" id)
                            {:as :json,
                             :query-params
                                 {:key       (env :key),
                                  :format    "json",
                                  :type      "schilderij",
                                  :toppieces "True"}}))

(defn read-data-detail-page
  "Reads the title, description, date , collection, colors and url of a image"
  [response]
  (let [art-objects (-> response
                        :body
                        :artObject)
        name (-> art-objects
                 :principalMakers
                 first
                 :name)
        id (:objectNumber art-objects)
        description (:description art-objects)
        date (get-in art-objects [:dating :year])
        collectie (first (:objectCollection art-objects))
        colors (:colors art-objects)]
    {:id id :name name :description description :date date :collectie collectie :colors colors}))


(defn read-data-front-page
  "Reads the title, description, date , collection, colors and url of a image"
  [response]
  (let [art-objects (-> response
                        :body
                        :artObject)
        name (-> art-objects
                 :principalMakers
                 first
                 :name)
        id (:objectNumber art-objects)
        title (:title art-objects)]
    {:id id :name name :title title}))


(defn read-image-data [id] (client/get
                             (str "https://www.rijksmuseum.nl/api/nl/collection/" id "/tiles")
                             {:as :json, :query-params {:format "json", :key (env :key)}}))
(defn read-image-url
  "Reads the image-url"
  [response]
  (let [art-objects (-> response
                        :body
                        :levels
                        )

        url (filter #(= (:name %) "z4") art-objects)
        tiles (:tiles (first url))
        image (get-in tiles [0 :url])
        ]
    {:tiles image}))

(defn do-both-in-parallel-front
  [ids]
  (let [paint-thread (future (pmap #(read-data-front-page (read-json-data %)) ids))
        image-thread (future (pmap #(read-image-url (read-image-data %)) ids))]
    (map merge @paint-thread @image-thread)))


(defn do-both-in-parallel-detail
  [ids]
  (let [paint-thread (future (pmap #(read-data-detail-page (read-json-data %)) ids))
        image-thread (future (pmap #(read-image-url (read-image-data %)) ids))]
    (map merge @paint-thread @image-thread)))















