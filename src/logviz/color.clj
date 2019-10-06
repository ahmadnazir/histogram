(ns logviz.color)

(defn color
  "Bash color cyan"
  [code s]
  (str "\033[" code "m" s "\033[49m")
  )

(defn nth-color
  "Indexed colors"
  [n]
  (-> (map list (range 0 10) [ 46 47 45 44 43])
      (nth n)
      second
      (color " ")
       )
  )


