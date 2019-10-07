(ns histogram.util)

(defn print!
  "Eagerly print all the values in a collection"
  [collection]
  (loop [rows collection]
    (cond (empty? rows) (println "")
          :else (do (println (first rows))
                    (recur (rest rows))
                    )))
  )

(defn tee!
  "Print and return"
  [col]
  (do
    (print! col)
    col
    )
  )
