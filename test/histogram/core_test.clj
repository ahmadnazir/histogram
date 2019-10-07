(ns histogram.core-test
  (:require [histogram.core :as sut]
            [clojure.test :refer :all]
            [histogram.color :as color]
            ))

(deftest append-to-empty-collection
  (testing "Add to empty collection"
    (is (=
         (sut/append [] "key" "value")
         [["key" 1]]
         ))
    ))

(deftest append-to-collection-with-one-element
  (testing "Add to collection with one element"
    (is (=
         (sut/append [["key" 4]] "key" "value")
         [["key" 5]]
         ))
    ))

(deftest append-to-collection-with-holes
  (testing "Add to collection with holes"
    (is (=
         (sut/append [[0 4]] 2 "value" :with-holes)
         [[0 4] [1 0] [2 1]]
         ))
    ))

(deftest append-to-collection-with-one-element-new-bucket
  (testing "Add to collection with one element and create a new bucket"
    (is (=
         (sut/append [["key" 2]] "new-key" "value")
         [["key" 2] ["new-key" 1]]
         ))
    ))

(deftest graph
  (testing "Add to collection with one element and create a new bucket"
    (is (=
         (sut/graph (fn [] "x") [["_" 1] ["_" 2] ["_" 1] ["_" 3]])
         ["   x"
          " x x"
          "xxxx"]
         ))
    ))

;; (sut/graph identity [["_" 1] ["_" 2] ["_" 1] ["_" 3]])
