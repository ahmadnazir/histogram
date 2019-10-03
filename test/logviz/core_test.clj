(ns logviz.test.core-test
  (:require [logviz.core :as sut]
            [clojure.test :refer :all]))

(deftest add-to-empty-collection
  (testing "Add to empty collection"
    (is (=
         (sut/append [] "key" "value")
         [["key" 1]]
         ))
    ))

(deftest add-to-collection-with-one-element
  (testing "Add to collection with one element"
    (is (=
         (sut/append [["key" 1]] "key" "value")
         [["key" 2]]
         ))
    ))

(deftest add-to-collection-with-one-element-new-bucket
  (testing "Add to collection with one element and create a new bucket"
    (is (=
         (sut/append [["key" 2]] "new-key" "value")
         [["key" 2] ["new-key" 1]]
         ))
    ))

