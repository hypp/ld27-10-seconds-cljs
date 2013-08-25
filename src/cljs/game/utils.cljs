(ns ten-seconds.utils)


(defn point-in-circle? [x y circle]
	"Check if a point is within a circle"
	(let [x1 (:x circle)
		  y1 (:y circle)
		  dx (Math/abs (- x x1))
		  dy (Math/abs (- y y1))
		  radius (:radius circle)
		  hit (cond 
		  	(> dx radius) false 
		  	(> dy radius) false
		  	(<= (+ dx dy) radius) true
		  	(<= (+ (* dx dx) (* dy dy)) (* radius radius)) true
		  	:else false
		  )]
;		(if hit
;			(.log js/console "hit")
;			(.log js/console "not hit")
;		)
		hit	  
	)
)

(defn point-in-rect? [x y rect]
	"Check if a point is within a rectangle"
	(let [x1 (:left rect)
		  x2 (+ x1 (:width rect))
		  y1 (:top rect)
		  y2 (+ y1 (:height rect))
		  hit (and
		(>= x x1) 
		(<= x x2)
		(>= y y1)
		(<= y y2))]
;	(if hit
;			(.log js/console "hit")
;			(.log js/console "not hit")
;	)
	hit
  )
)

(defn is-obj-hit? [x y obj]
	"Check if an object is hit"
	(cond
		(= "circle" (:type obj)) (point-in-circle? x y obj)
		(= "rect" (:type obj)) (point-in-rect? x y obj)
	)
)

(defn hit-test [x y obj-list]
	"Returns a list of objects that where hit"
	(filter #(is-obj-hit? x y %) obj-list)
)

(defn find-value [value list]
	(if (seq list)
		(if (= value (first list))
			true
			(recur value (rest list))
		)
		false
	)
)

(defn difference [list-a list-b]
	(remove #(find-value % list-b) list-a)
)

(defn hsv-to-rgb [h s v]
  (map #(int (* 255 %)) ; Syntactic sugar for fn [x] (* 255 x)
	  (let [u (int (/ h 60.0))
	        f (- (/ h 60.0) u)
	        p (* v (- 1 s))
	        q (* v (- 1 (* s f)))
	        t (* v (- 1 (* s (- 1 f))))]
	        (condp = u
	           0 (list v t q)
	           1 (list q v p) 
	           2 (list p v t)
	           3 (list p q v) 
	           4 (list t p v) 
	           5 (list v p q)
	        )
        )
    )
)


