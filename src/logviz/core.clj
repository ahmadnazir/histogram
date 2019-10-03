(ns logviz.core
  (:require [clojure.string :as s]
            [clj-time.core :as t]
            )
    (:gen-class))


(defn bucket
  "Create time bucket"
  []
  (let [now (t/time-now)
        sec (t/second now)
        min (t/second now)
        hr (t/hour now)
        ]
    [hr min sec]
    ))

(defn append
  "Add value to the bucket"
  [coll bucket line]
  (let [[k v] (last coll)]
    (cond (empty? coll) (vector [bucket 1])
          (= k bucket)  (conj (drop-last coll) [bucket (inc v)])
          :else         (conj coll [bucket 1])
          )))

(defn -main [& args]
  (loop [m [] lines (line-seq (java.io.BufferedReader. *in*))]
    (let [line (first lines)
          time (t/time-now)
          m (append m (bucket) line)
          ]
      (println m)
      (recur m (rest lines))))
  )
