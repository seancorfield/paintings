(ns paintings2.api-get
  (:require [clj-http.client :as client]
            [environ.core :refer [env]]
            [clojure.spec :as s]
            [com.gfredericks.test.chuck :as chuck]
            [com.gfredericks.test.chuck.generators :as gen']
            [clojure.spec.gen :as gen]
            [clojure.spec.test :as stest]))

;spec files to do some input validation

(s/def ::objectNumber (s/and string? #(re-matches #"[A-Z]{2}-[A-Z]-\d{1,4}" %)))
(s/def ::name string?)
(s/def ::title string?)
(s/def ::description string?)
(s/def ::year (s/int-in 1900 2018))
(s/def ::objectCollection string?)
(s/def ::colors (s/coll-of string?))

(s/def ::dating  (s/keys :req-un [::year]))
(s/def ::principalMakers (s/coll-of (s/keys :req-un [::name])))
(s/def :basic/artObject
  (s/keys :req-un [::objectNumber ::principalMakers ::title]))
(s/def :detail/artObject
  (s/merge :basic/artObject (s/keys :req-un [::description ::dating ::objectCollection ::colors])))
(s/def ::artObject
  (s/or :basic :basic/artObject :detail :detail/artObject))
(s/def :artObject/body
 (s/keys :req-un [::artObject]))
(s/def :artObject/response
 (s/keys :req-un [:artObject/body]))

; spec to test some functions

(s/fdef get-objectNumbers
 :args (s/cat :response :artObject/response)
 :ret (s/coll-of ::objectNumber))

; some custom generators
(s/def ::objectNumber
 (s/with-gen ::objectNumber
  (fn [] (gen'/string-from-regex #"([A-Z]{2}-[A-Z]-\d{1,4})"))))

; the test functions

(stest/summarize-results (stest/check 'paintings2.api-get/get-objectNumbers))

;  the core functions

(defn get-objectNumbers
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

(defn get-data-front-page
  "Reads the title, description, date , collection, colors and url of a image"
  [art-object]
  (let [name (-> art-object
                 :principalMakers
                 first
                 :name)
        id (:objectNumber art-object)
        title (:title art-object)]
    {:id id :name name :title title}))

(defn get-data-detail-page
  "Reads the title, description, date , collection, colors and url of a image"
  [art-object]
  (let [basic-info (get-data-front-page art-object)
        description (:description art-object)
        date (get-in art-object [:dating :year])
        collectie (first (:objectCollection art-object))
        colors (:colors art-object)]
    (assoc basic-info :description description :date date :collectie collectie :colors colors)))

(defn read-image-data [id] (client/get
                             (str "https://www.rijksmuseum.nl/api/nl/collection/" id "/tiles")
                             {:as :json, :query-params {:format "json", :key (env :key)}}))
(defn get-image-url
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
  (let [paintings (pmap (comp get-data-front-page get-art-object read-json-data) ids)
        images (pmap (comp get-image-url read-image-data) ids)]
    (mapv merge paintings images)))

(defn fetch-paintings-and-images-detail-page
  [ids]
  (let [paintings (pmap (comp get-data-detail-page get-art-object read-json-data) ids)
        images (pmap (comp get-image-url read-image-data) ids)]
    (mapv merge paintings images)))
