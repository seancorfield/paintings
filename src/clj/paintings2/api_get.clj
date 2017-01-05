(ns paintings2.api-get
  (:require [clj-http.client :as client]
            [environ.core :refer [env]]
            [clojure.spec :as s]))


;spec files to do some input validation

(s/def ::dating  (s/keys :req-un [::year]))



(s/def :basic/artObject
  (s/keys :req-un [::id ::principalMakers ::title]))

(s/def :detail/artObject
  (s/merge :basic/artObject (s/keys :req-un [::description ::dating ::collection ::colors])))

(s/def ::artObject
  (s/or :basic :basic/artObject :detail :detail/artObject))


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

(defn get-art-object
  "Given a response, returns the artObject element."
  [response]
  (-> response :body :artObject))

(defn read-data-front-page
  "Reads the title, description, date , collection, colors and url of a image"
  [art-object]
  (let [name (-> art-object
                 :principalMakers
                 first
                 :name)
        id (:objectNumber art-object)
        title (:title art-object)]
    {:id id :name name :title title}))

(defn read-data-detail-page
  "Reads the title, description, date , collection, colors and url of a image"
  [art-object]
  (let [basic-info (read-data-front-page art-object)
        description (:description art-object)
        date (get-in art-object [:dating :year])
        collectie (first (:objectCollection art-object))
        colors (:colors art-object)]
    (assoc basic-info :description description :date date :collectie collectie :colors colors)))

(defn read-image-data [id] (client/get
                             (str "https://www.rijksmuseum.nl/api/nl/collection/" id "/tiles")
                             {:as :json, :query-params {:format "json", :key (env :key)}}))
(defn read-image-url
  "Reads the image-url"
  [response]
  (let [art-objects (-> response
                        :body
                        :levels)
        url (filter #(= (:name %) "z4") art-objects)
        tiles (:tiles (first url))
        image (get-in tiles [0 :url])]

    {:tiles image}))

(defn fetch-paintings-and-images-front-page
  [ids]
  (let [paintings (pmap (comp read-data-front-page get-art-object read-json-data) ids)
        images (pmap (comp read-image-url read-image-data) ids)]
    (mapv merge paintings images)))

(defn fetch-paintings-and-images-detail-page
  [ids]
  (let [paintings (pmap (comp read-data-detail-page get-art-object read-json-data) ids)
        images (pmap (comp read-image-url read-image-data) ids)]
    (mapv merge paintings images)))
