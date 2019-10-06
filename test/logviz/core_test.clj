(ns logviz.core-test
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
         (sut/append [["key" 4]] "key" "value")
         [["key" 5]]
         ))
    ))

(deftest add-to-collection-with-one-element-new-bucket
  (testing "Add to collection with one element and create a new bucket"
    (is (=
         (sut/append [["key" 2]] "new-key" "value")
         [["key" 2] ["new-key" 1]]
         ))
    ))

(deftest graph
  (testing "Add to collection with one element and create a new bucket"
    (is (=
         (sut/graph [["_" 1] ["_" 2] ["_" 1] ["_" 3]])
         ["   x"
          " x x"
          "xxxx"]
         ))
    ))


(->> (sut/graph [["_" 1] ["_" 2] ["_" 1] ["_" 3]])
     (map println)
     )

