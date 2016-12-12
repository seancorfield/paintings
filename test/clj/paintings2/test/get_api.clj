(ns paintings2.test.get_api
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [paintings2.api-get :as api]))

(deftest test-app
  (testing "get numbers"
    (is (= ["1234" "abcd"] (api/read-numbers {:body {:artObjects [{:objectNumber "1234"} {:objectNumber "abcd"}]}}))))

  (testing "get data "
    (is (= {:id 1 :name "Roelof" :description "rubbish" :date "12-02-1980" :collectie nil :colors "yellow"} (api/read-data-painting {:body {:artObject {:objectNumber 1 :principalMakers [{:name "Roelof"}] :description "rubbish" :dating {:year "12-02-1980"} :collectie nil :colors "yellow"}}}))))

  (testing "get image "
    (is (= {:tiles "http://example.com"} (api/read-image-url {:body {:levels [{:name "z4", :tiles [{:url "http://example.com"}]}]}})))
    )

  )

  (run-all-tests)





