(ns histogram.core
  (:require [clojure.string :as s]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [histogram.util :as util]
            [histogram.color :as color])
  (:gen-class))

(defn bucket
  "Create time bucket. If time in ms (since epoch) is not specified, current time is used. Time span should be in milliseconds."
  ([interval] ;; only for backwards compatibility
   (let [now (tc/to-long (t/now))]
     (quot now interval)
     ))
  ([interval time-ms]
   (quot time-ms interval)))

(defn quantify
  [line]
  (cond line 1 :else 0))

(defn append
  "Add value to the bucket. If :with-holes is also specified, then add empty lines for every absent bucket."
  ([coll bucket line]
   (let [[k v] (last coll)]
     (cond (empty? coll) [ [bucket (quantify line)] ]
           (= k bucket)  (conj (vec (drop-last coll)) [bucket (inc v)])
           :else         (conj coll [bucket (quantify line)])
           )))
  ([coll bucket line _]
   (cond (empty? coll) (append coll bucket line)
         :else         (let [[previous-bucket _] (last coll)
                             count               (- bucket previous-bucket)]
                         (loop [b previous-bucket
                                c coll
                                ]
                           (cond (= b bucket) (append c bucket line)
                                 :else (recur (inc b) (append c (inc b) nil))
                                 )
                           )
                         ))
   ))

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
  [char-fn coll ]
  (let [values (map second coll)
        height (apply max values)
        ]
    (->> values
         (map (fn [v] (concat (repeat v (char-fn)) (repeat (- height v) " "))))
         flip                                                                   ;; mirror / flip i.e. 180 degrees
         pivot                                                                  ;; pivot / rotate 90 degrees
         (map (fn [c] (str (apply str c))))                                     ;; string representation of a graph
         )
    )
  )


;; TODO
;;
;; Adjust the height:
;; - If there are too many items in a bucket, then they need to be normalized
;;
;; Different types of items:
;; - Right now I am only counting the items - not counting the different types of items

(defn -main [& args]
  (loop [m {}
         lines (line-seq (java.io.BufferedReader. *in*))
         ]
    (let [line (first lines)
          [t value & values] (s/split line #" ") ;; time and value ;; todo check if line exists
          time-ms t                              ;; convert t to time-ms e.g '07/Oct/2019:18:10:14'
          b (bucket 1000)                        ;; Pass time-ms
          coll (m value)
          m (assoc m value (append coll b value :with-holes))
          gs (map-indexed (fn [i [k v]] (graph (fn [] (color/nth-color i)) v)) (seq m))
      ]
      (loop [xs gs]
        (cond (empty? xs) (println "")
              :else (do (util/print! (first xs))
                        (recur (rest xs))
                        )))
      (recur m (rest lines))
      )))
