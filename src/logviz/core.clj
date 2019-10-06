(ns logviz.core
  (:require [clojure.string :as s]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [logviz.util :as util]
            )
    (:gen-class))

(defn bucket
  "Create time bucket"
  ([]
   (bucket 1))
  ([width]
   (let [now (tc/to-long (t/now))
         width (* 1000 width) ;; Minimum size of the bucket is a second
         ]
     (quot now width)
     )))

(defn append
  "Add value to the bucket"
  [coll bucket line]
  (let [[k v] (last coll)]
    (cond (empty? coll) [ [bucket 1] ]
          (= k bucket)  (conj (vec (drop-last coll)) [bucket (inc v)])
          :else         (conj coll [bucket 1])
          )))

;; graph
;; ----

(defn pivot
  "Pivot a matrix"
  [col]
  (apply map list col))

(defn flip
  "Flip a matrix"
  [col]
  (map reverse col))

(defn graph
  "Graph! Take the values and create a histogram. [[k v]] -> v -> number of chars -> pivot -> graph"
  [col]
  (let [values (map second col)
        height (apply max values)
        ]
    (->> values
         (map (fn [x] (concat (repeat x "#") (repeat (- height x) " ")))) ;; use chars for the histogram
         flip                                                ;; mirror / flip i.e. 180 degrees
         pivot                                             ;; pivot / rotate 90 degrees
         (map (fn [c] (str (apply str c))))                           ;; string representation of a graph
         ))
  )


;; TODO
;;
;; Sporadic:
;; - find the min dist?
;;
;; Events really close together:
;; - based on the distribution, group the keys and graph
;;
;; Adjust the height:
;; - If there are too many items in a bucket, then they need to be normalized
;;
;; Different types of items:
;; - Right now I am only counting the items - not counting the different types of items


(defn -main [& args]
  (loop [m [] lines (line-seq (java.io.BufferedReader. *in*))]
    (let [line (first lines)
          time (t/time-now)
          m (append m (bucket) line)
          ]
      ;; The graph m has some lazy component that I can't figure out - need to
      ;; explicity get all the rows by calling `first`. Simply doing a (map
      ;; println (graph m)) won't print anything
      (util/print! (graph m))
      (recur m (rest lines))))
  )
