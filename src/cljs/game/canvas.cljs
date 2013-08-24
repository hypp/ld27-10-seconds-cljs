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
    (set! (.-fillStyle context) (str "rgba(" (:color circle) "," (:alpha circle) ")"))
    (.beginPath context)
    (.arc context (:x circle) (:y circle) (:radius circle) 0 (* 2 Math/PI) true)
    (.closePath context)
	(.fill context)
)

(defn render-rect [context rect]
    (set! (.-fillStyle context) (str "rgba(" (:color rect) "," (:alpha rect) ")"))
    (.fillRect context (:left rect) (:top rect) (:width rect) (:height rect))
)
