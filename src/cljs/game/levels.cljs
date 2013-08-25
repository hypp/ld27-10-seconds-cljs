(ns ten-seconds.levels
	(:require
;	 [ten-seconds.audio :as audio]
			[ten-seconds.utils :as utils]
	)

)

(def max-x-pos 750) ; Should be less than canvas width
(def min-x-pos 50)
(def diff-x-pos (- max-x-pos min-x-pos))
(def max-y-pos 450) ; Should be less than canvas height
(def min-y-pos 50)
(def diff-y-pos (- max-y-pos min-y-pos))
(def min-width 20)
(def max-width 100)
(def diff-width (- max-width min-width))
(def min-height 20)
(def max-height 100)
(def diff-height (- max-height min-height))

(defn generate-random-rect []
	(let [top (+ min-y-pos (rand-int diff-y-pos))
	 	  left (+ min-x-pos (rand-int diff-x-pos))
	 	  width (+ min-width (rand-int diff-width))
	 	  height (+ min-height (rand-int diff-height))
	 	  color (utils/hsv-to-rgb (* (rand) 360) 0.5 0.95)]
		{:type "rect" :top top :left left :width width :height height :color color :alpha 0.8} 
	)
)

(defn generate-random-circle []
	(let [y (+ min-y-pos (rand-int diff-y-pos))
	 	  x (+ min-x-pos (rand-int diff-x-pos))
	 	  radius (+ min-width (rand-int diff-width))
	 	  color (utils/hsv-to-rgb (* (rand) 360) 0.5 0.95)]
		{:type "circle" :x x :y y :radius radius :color color :alpha 0.8} 	 	  
	)
)

(defn generate-random-object []
	(let [type (rand-int 2)]
		(cond
			(= 0 type) (generate-random-rect)
			(= 1 type) (generate-random-circle)
		)
	)
)

(defn generate-random-object-list [min-objs max-objs]
	(let [diff (- max-objs min-objs)
		  rand-objs (rand-int diff)
		  num-objs (+ min-objs rand-objs)] 
		(map #(generate-random-object) (range num-objs))
	)
)

(defn replace-object []
	(let [obj (generate-random-object)]
		(assoc obj :changed true)
	)
)

(defmulti change-object-position :type)
(defmethod change-object-position "circle" [obj]
;	(.log js/console "change-object-position circle")
	(let [y (+ min-y-pos (rand-int diff-y-pos))
	 	  x (+ min-x-pos (rand-int diff-x-pos))]
	 	 (assoc obj :x x :y y :changed true)
	)
)
(defmethod change-object-position "rect" [obj]
;	(.log js/console "change-object-position rect")
	(let [top (+ min-y-pos (rand-int diff-y-pos))
	 	  left (+ min-x-pos (rand-int diff-x-pos))]
	 	 (assoc obj :x left :y top :changed true)
	)
)
(defmethod change-object-position :default [obj]
	(.log js/console "Failed position" obj)
)

	
(defmulti change-object-size :type)
(defmethod change-object-size "circle" [obj]
;	(.log js/console "change-object-size circle")
	(let [radius (+ min-width (rand-int diff-width))]
		(assoc obj :radius radius :changed true) 	 	  
	)
)
(defmethod change-object-size "rect" [obj]
;	(.log js/console "change-object-size rect")
	(let [width (+ min-width (rand-int diff-width))
	 	  height (+ min-height (rand-int diff-height))]
	 	  (assoc obj :width width :height height :changed true) 
	)
)
(defmethod change-object-size :default [obj]
	(.log js/console "Failed size" obj)
)

(defn change-object-color [obj]
;	(.log js/console "change-object-color")
	(let [color (utils/hsv-to-rgb (* (rand) 360) 0.5 0.95)]
		(assoc obj :color color :changed true)
	)
)

(defn change-object [obj]
	(let [psc (rand-int 3)]
;		(.log js/console "change-object")
		(cond
			(= 0 psc) (change-object-position obj)
			(= 1 psc) (change-object-size obj)
			(= 2 psc) (change-object-color obj)
		)
	)
)

(defn leave-replace-change [obj]
	(let [lrc (rand-int 10)]
		(cond
			(= 0 lrc) (replace-object)
			(= 1 lrc) (change-object obj)
			:else obj ; leave it as it is
		)
	)
)

(defn mutate-object-list [org-obj-list]
	"Randomly replaces or changes objects in the list. The first object is always changed"
	(cons (replace-object) (map leave-replace-change (rest org-obj-list)))
;	(cons (replace-object) (rest org-obj-list))
)

(def initial-difficulty 2.0)
(def difficulty (atom initial-difficulty))

(defmulti on-animate :state)
(defmethod on-animate "first" [level]
	; Todo make this harder and harder...
	(let [num-objs (int @difficulty)
		  org-obj-list (generate-random-object-list num-objs num-objs)
		  changed-obj-list (mutate-object-list org-obj-list)
		  original (cons (:display-counter level) org-obj-list)
		  changed (cons (:display-counter level) changed-obj-list)]
;		(.log js/console "level1 animate first")
		(swap! difficulty #(+ 0.5 %))
		(assoc level :state "memorize" :object-list original :original original :changed changed :time (.getTime (new js/Date))) 
	)
)
	
(defmethod on-animate "memorize" [level]
	(let [now (.getTime (new js/Date))
		start (:time level)
		diff (- now start)
		seconds (int (/ diff 1000))
		display (- 10 seconds)
		obj-list (:object-list level)
		new-obj-list (cons (assoc (first obj-list) :msg (str display " seconds.")) (rest obj-list))]
;		(.log js/console "level1 animate memorize" diff display)
		(if (>= 0 display) 
			(assoc level :object-list '() :state "black" :time (.getTime (new js/Date)))
			(assoc level :object-list new-obj-list)
		)
	)	
)
	
(defmethod on-animate "black" [level]
	(let [now (.getTime (new js/Date))
		start (:time level)
		diff (- now start)
		seconds (int (/ diff 1000))
		display (- 2 seconds)
		obj-list (:object-list level)]
;		(.log js/console "level1 animate memorize" diff display)
		(if (>= 0 display) 
			(assoc level :object-list (:changed level) :state "solve" :time (.getTime (new js/Date)))
			(assoc level :done false)
		)
	)	
)
	
(defmethod on-animate "solve" [level]
	(let [now (.getTime (new js/Date))
		start (:time level)
		diff (- now start)
		seconds (int (/ diff 1000))
		display (- 10 seconds)
		obj-list (:object-list level)
		changed (filter #(contains? % :changed) obj-list)
		new-obj-list (cons (assoc (first obj-list) :msg (str display " seconds.")) (rest obj-list))]
;		(.log js/console "level1 animate solve" diff display)
		(if (or (>= 0 display) (= 0 (count changed))) 
			(assoc level :state "check" :object-list new-obj-list)
			(assoc level :object-list new-obj-list)
		)
	)	
)

(defmethod on-animate "show" [level]
	(let [now (.getTime (new js/Date))
		start (:time level)
		diff (- now start)
		seconds (int (/ diff 1000))
		display (- 2 seconds)
		obj-list (:object-list level)
		new-obj-list (cons (assoc (first obj-list) :msg (str "10 seconds. Game Over. You missed some changes. ")) (rest obj-list))]
;		(.log js/console "level1 animate show" diff display)
		(if (>= 0 display) 
			(do
				(swap! difficulty #(+ 0 initial-difficulty))
				(assoc level :game-over true)
			)
			(assoc level :object-list new-obj-list)
		)
	)	
)

(defmethod on-animate "check" [level]
	(let [obj-list (:object-list level)
		changed (filter #(contains? % :changed) obj-list)]
		(if (not= 0 (count changed))
			(assoc level :state "show" :object-list (map #(assoc % :outline true) obj-list) :time (.getTime (new js/Date)))
			(assoc level :done true)
		)
	)
)
	
(def level1 
{
	:next-level "level1"
	:done false
	:state "first"
	
	:on-animate (fn [level]
;		(.log js/console "level1 animate")
		(on-animate level)
	)

	:on-render (fn [level]  (.log js/console "level1 render"))
	
 	:on-click (fn [level clicks] 
		(.log js/console "level1 on-click")
		(if (= (:state level) "solve")
			(let [obj-list (:object-list level)
				 hit-list (mapcat (fn [click] (utils/hit-test (:x click) (:y click) obj-list)) clicks)
				 changed (filter #(contains? % :changed) hit-list)
				 unchanged (filter (complement #(contains? % :changed)) hit-list)
				 new-obj-list (utils/difference obj-list hit-list)
				 state (if (< 0 (count unchanged)) "check" (:state level)) 
				]
;				(if (< 0 (count changed))
;					(.log js/console "changed hit" game-over)
;				)			
;				(if (< 0 (count unchanged))
;					(.log js/console "unchanged hit" game-over)
;				)			
				(assoc level :object-list new-obj-list :state state)
			)
			(assoc level :state (:state level))
		)
	)

	:display-counter {:type "text" :msg "10 seconds." :font "20px Arial" :x 10 :y 20 :color [210, 200, 210] :alpha 1.0}
	
;	:original '(
;		{:type "text" :msg "10 seconds." :font "20px Arial" :x 10 :y 20 :color [210, 200, 210] :alpha 1.0}
;		{:type "circle" :x 200 :y 200 :radius 5 :color [123, 123, 123] :alpha 1.0} 
;		{:type "rect" :top 400 :left 400 :width 50 :height 50 :color [240, 12, 43] :alpha 1.0} 
;	)
;	:changed '(
;		{:type "text" :msg "10 seconds." :font "20px Arial" :x 10 :y 20 :color [210, 200, 210] :alpha 1.0}
;		{:type "circle" :x 200 :y 200 :radius 5 :color [123, 123, 123] :alpha 1.0}, 
;		{:type "rect" :top 300 :left 300 :width 50 :height 50 :color [240, 12, 43] :alpha 1.0 :changed true} 
;	)
}
)

(def intro
{
	:next-level "level1"
	:done false
	
	:on-animate (fn [level] 
;		(.log js/console "intro animate") 
		(assoc level :done false :first false)
	)
	:on-render (fn [level]  (.log js/console "intro render"))
	
	:on-click (fn [level clicks] (.log js/console "intro on-click") (assoc level :done true)) 
	
	:object-list '(
		{:type "text" :msg "10 seconds." :font "20px Arial" :x 10 :y 20 :color [210, 200, 210] :alpha 1.0}
		{:type "text" :msg "This is a memory game." :font "20px Arial" :x 60 :y 60 :color [210, 200, 210] :alpha 1.0}
		{:type "text" :msg "You have 10 seconds to memorize all objects on screen." :font "20px Arial" :x 110 :y 110 :color [210, 200, 210] :alpha 1.0}
		{:type "text" :msg "You will then see a new screen." :font "20px Arial" :x 160 :y 160 :color [210, 200, 210] :alpha 1.0}
		{:type "text" :msg "Remove all objects that are new or have changed." :font "20px Arial" :x 210 :y 210 :color [210, 200, 210] :alpha 1.0}
		{:type "text" :msg "Click to start." :font "20px Arial" :x 310 :y 310 :color [210, 200, 210] :alpha 1.0}
	)
}
)

(def game-over
{
	:next-level "intro"
	:done false
	
	:on-animate (fn [level] 
;		(.log js/console "game-over animate") 
		(assoc level :done false :first false)
	)
	:on-render (fn [level]  (.log js/console "game-over render"))
	
	:on-click (fn [level clicks] (.log js/console "game-over on-click") (assoc level :done true)) 
	
	:object-list '(
		{:type "text" :msg "10 seconds." :font "20px Arial" :x 10 :y 20 :color [210, 200, 210] :alpha 1.0}
		{:type "text" :msg "You failed." :font "20px Arial" :x 60 :y 60 :color [210, 200, 210] :alpha 1.0}
		{:type "text" :msg "Next time, try harder!" :font "20px Arial" :x 110 :y 110 :color [210, 200, 210] :alpha 1.0}
		{:type "text" :msg "Click to restart." :font "20px Arial" :x 310 :y 310 :color [210, 200, 210] :alpha 1.0}
	)
}
)

