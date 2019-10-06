(ns logviz.core
  (:require [clojure.string :as s]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [logviz.util :as util]

            [logviz.color :as color])
  (:gen-class))

(defn bucket
  "Create time bucket. If time span is not specified, then 1 second is used as default."
  ([]
   (bucket 1000))  ;; Default time bucket interval is a second
  ([interval]
   (let [now (tc/to-long (t/now))]
     (quot now interval)
     )))

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
  (loop [coll []
         lines (line-seq (java.io.BufferedReader. *in*))
         ]
    (let [line (first lines)
          interval 1000
          current-bucket (bucket interval)
          coll (append coll current-bucket line :with-holes)
          ]
      (util/print! (graph (fn [] (color/nth-color 0)) coll)) ;; (map println ...) doesn't work - This needs to be evaluated eagerly
      (recur coll (rest lines))))
  )
