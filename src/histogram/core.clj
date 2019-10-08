(ns histogram.core
  (:require [clojure.string :as s]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [clj-time.format :as tf]
            [histogram.util :as util]
            [histogram.color :as color])
  (:gen-class))

(def bucket-size 1000)

(defn bucket
  "Create time bucket. If time in ms (since epoch) is not specified, current time is used."
  ([] ;; only for backwards compatibility
   (let [now (tc/to-long (t/now))]
     (bucket now)
     ))
  ([time-ms]
   (quot time-ms bucket-size)
   ))

(defn quantify
  [line]
  (cond line 1 :else 0))

(defn append
  "Add value to the time bucket. If :fill is also specified, then add empty lines for every absent bucket."
  ([coll bucket line]
   (let [[k v] (last coll)]
     (cond (empty? coll) [ [bucket (quantify line)] ]
           (= k bucket)  (conj (vec (drop-last coll)) [bucket (inc v)])
           :else         (conj coll [bucket (quantify line)])
           )))
  ([coll bucket line _]
   (cond (empty? coll) (append coll bucket line) ;; TODO: this won't be needed
                                                 ;; once we initialize all
                                                 ;; values with empty buckets
                                                 ;; matching the first bucket
                                                 ;; that any value has seen
         :else         (let [[previous-bucket _] (last coll)]
                         (loop [b previous-bucket
                                c coll
                                ]
                           (cond
                                 (> b bucket) (throw (Exception. "Out of order time events are not supported yet"))
                                 (= b bucket) (append c bucket line)
                                 :else (let [b (+ b 1)]
                                         (recur b (append c b nil)))
                                 ))))
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

;; Get the built in date formatters
;;
;; (tf/show-formatters)
(def date-formats
  {
   :nginx "d/MMM/y':'H:m:s" ;; 01/Jan/2000:23:59:59
   :nginx-time "H:m:s"      ;; 23:59:59
   })

(defn date-formatter
  "Try to parse the string using the available formatters"
  [date-formats string]
  (let [pairs (seq date-formats)]
    (loop [[p & ps] pairs]
      (let [[name format] p
            formatter (tf/formatter format)
            selected (try
                       (tf/parse formatter string)
                       formatter
                       (catch Exception e
                         (println "No match for " name ":: " (.getMessage e)) ;; TODO: use logger
                         ))]
        (cond selected selected
              ps (recur ps)
              :else nil)
        ))))

(defn parse-line
  "Sanity"
  [line]
  (let [[t v & vs] (s/split line #"\s+")
        t (s/replace t #"[\[\]]" "") ;; remove square brackets
        ]
    [t v]
    ))

(defn numeric?
  "Does the string only contain numbers"
  [s]
  (not (re-find #"[^\d]" s))
  )

(defn create-date-parser
  "Create a date parse by inspecting the string"
  [string]
  (cond (empty? string) nil
        (numeric? string) (fn [t] (Long/valueOf t))
        :else             (let [formatter (date-formatter date-formats string)]
                            (cond formatter (fn [t] (tc/to-long (tf/parse formatter t)))
                                  :else     (println "No date parser found"))  ;; TODO: use logger
                            )))

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
         parser nil
         ]
    (let [line (first lines)
          [time value] (parse-line line)
          parser (cond parser parser :else (create-date-parser time))]
      (cond parser (let [t (parser time)
                         b (bucket t)
                         coll (m value)
                         m (assoc m value (append coll b value :fill
                                                  ))
                         gs (map-indexed (fn [i [k v]] (graph (fn [] (color/nth-color i)) v)) (seq m))
                         ]
                     (loop [xs gs]
                       (cond (empty? xs) (println "")
                             :else (do (util/print! (first xs))
                                       (recur (rest xs))
                                       )))
                     (recur m (rest lines) parser))

            :else (recur m (rest lines) nil)
            )
      )))
