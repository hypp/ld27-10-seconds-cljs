(ns ten-seconds.canvas
	(:require 
			[clojure.string :as string])
)

(defn render-text [context msg]
      (set! (.-fillStyle context) (str "rgba(" (string/join "," (:color msg)) "," (:alpha msg) ")"))
	  (set! (.-font context) (:font msg))
	  (.fillText context (:msg msg) (:x msg) (:y msg))
)

(defn render-circle [context circle]
    (set! (.-fillStyle context) (str "rgba(" (string/join "," (:color circle)) "," (:alpha circle) ")"))
    (.beginPath context)
    (.arc context (:x circle) (:y circle) (:radius circle) 0 (* 2 Math/PI) true)
;    (.closePath context)
	(.fill context)

	(if (and (contains? circle :changed) (contains? circle :outline))
		(do
			(set! (.-lineWidth context) 5);
			(set! (.-strokeStyle context) "#ffffff");
	      	(.stroke context);
		)
	)
)

(defn render-rect [context rect]
    (set! (.-fillStyle context) (str "rgba(" (string/join "," (:color rect)) "," (:alpha rect) ")"))
    (.beginPath context)
    (.rect context (:left rect) (:top rect) (:width rect) (:height rect))
	(.fill context)
    
	(if (and (contains? rect :changed) (contains? rect :outline))
		(do
			(set! (.-lineWidth context) 5);
			(set! (.-strokeStyle context) "#ffffff");
	      	(.stroke context);
		)
	)

)
