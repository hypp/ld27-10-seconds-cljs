(ns ten-seconds.game
	(:require [ten-seconds.audio :as audio]
			[ten-seconds.canvas :as canvas]
			[ten-seconds.utils :as utils]
			[clojure.string :as string]
			[clojure.set :as set])
)


(def canvas-width 800)
(def canvas-height 500)

(def clicks (atom []))

(def level1 
{
	:next-level "game-over"
	:done false
	:state "first"
	
	:on-animate-first (fn [level]
;		(.log js/console "level1 animate first")
		(assoc level :state "memorize" :object-list (:original level) :time (.getTime (new js/Date))) 
	)
	
	:on-animate-memorize (fn [level]
		(let [now (.getTime (new js/Date))
			  start (:time level)
			  diff (- now start)
			  seconds (int (/ diff 1000))
			  display (- 10 seconds)
			  obj-list (:object-list level)
			  new-obj-list (cons (assoc (first obj-list) :msg (str display " seconds.")) (rest obj-list))]
;			(.log js/console "level1 animate memorize" diff display)
			(if (>= 0 display) 
				(assoc level :object-list '() :state "black" :time (.getTime (new js/Date)))
				(assoc level :object-list new-obj-list)
			)
		)	
	)
	
	:on-animate-black (fn [level]
		(let [now (.getTime (new js/Date))
			  start (:time level)
			  diff (- now start)
			  seconds (int (/ diff 1000))
			  display (- 2 seconds)
			  obj-list (:object-list level)]
;			(.log js/console "level1 animate memorize" diff display)
			(if (>= 0 display) 
				(assoc level :object-list (:changed level) :state "solve" :time (.getTime (new js/Date)))
				(assoc level :done false)
			)
		)	
	)
	
	:on-animate-solve (fn [level]
		(let [now (.getTime (new js/Date))
			  start (:time level)
			  diff (- now start)
			  seconds (int (/ diff 1000))
			  display (- 10 seconds)
			  obj-list (:object-list level)
			  new-obj-list (cons (assoc (first obj-list) :msg (str display " seconds.")) (rest obj-list))]
;			(.log js/console "level1 animate memorize" diff display)
			(if (>= 0 display) 
				(assoc level :done true)
				(assoc level :object-list new-obj-list)
			)
		)	
	)
	
	:on-animate (fn [level]
;		(.log js/console "level1 animate")
		(cond
			(= (:state level) "first") ((:on-animate-first level) level)
			(= (:state level) "memorize") ((:on-animate-memorize level) level)
			(= (:state level) "black") ((:on-animate-black level) level)
			(= (:state level) "solve") ((:on-animate-solve level) level)
		)
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
				 game-over (< 0 (count unchanged)) 
				]
				(if (< 0 (count changed))
					(.log js/console "changed hit" game-over)
				)			
				(if (< 0 (count unchanged))
					(.log js/console "unchanged hit" game-over)
				)			
				(assoc level :object-list new-obj-list :game-over game-over)
			)
			(assoc level :state (:state level))
		)
	)
	
	:original '(
		{:type "text" :msg "10 seconds." :font "20px Arial" :x 10 :y 20 :color [210, 200, 210] :alpha 1.0}
		{:type "circle" :x 200 :y 200 :radius 5 :color "123, 123, 123" :alpha 1.0} 
		{:type "rect" :top 400 :left 400 :width 50 :height 50 :color "240, 12, 43" :alpha 1.0} 
	)
	:changed '(
		{:type "text" :msg "10 seconds." :font "20px Arial" :x 10 :y 20 :color [210, 200, 210] :alpha 1.0}
		{:type "circle" :x 200 :y 200 :radius 5 :color "123, 123, 123" :alpha 1.0}, 
		{:type "rect" :top 300 :left 300 :width 50 :height 50 :color "240, 12, 43" :alpha 1.0 :changed true} 
	)
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

(defn initial-state []
  {
  :level intro
  }
)


(defn render-level [context level] 
	(let [obj-list (:object-list level)]
		(doseq [obj obj-list]
			(cond
				(= "circle" (:type obj)) (canvas/render-circle context obj)
				(= "rect" (:type obj)) (canvas/render-rect context obj)
				(= "text" (:type obj)) (canvas/render-text context obj)
			)
		)
	)
)

(defn convert-level [level]
	(cond
		(= level "intro") intro
		(= level "level1") level1
		:else game-over
	)
)

(defn next-state [state]
	(let [level (:level state)
		  on-animate (:on-animate level)
		  done (:done level)
		  game-over (and (contains? level :game-over) (:game-over level))]
		  (cond 
		  	(= true done) (assoc state :level (convert-level (:next-level level)))
		  	(= true game-over) (assoc state :level (convert-level "game-over"))
		  	:else (assoc state :level (on-animate level))
		  )
	)
)

(defn render [state]
  (let [canvas (.getElementById js/document "surface")
        context (.getContext canvas "2d")]
;      (set! (.-fillStyle context) (str "rgba(" 0 "," 0 "," 0 "," 0.1 ")"))
;      (.fillRect context 0 0 canvas-width canvas-height)
      (set! (.-fillStyle context) (str "rgba(" 0 "," 0 "," 0 "," 1.0 ")"))
      (.fillRect context 0 0 canvas-width canvas-height)
	  (render-level context (:level state))
  )
)

(def request-animation-frame
  (or (.-mozRequestAnimationFrame js/window)
      (.-requestAnimationFrame js/window)
      (.-webkitRequestAnimationFrame js/window)
      #(js/timeout 10 (%)) ; Syntactic sugar for a fn
  )
)

(defn process-clicks [state clicks]
	(if (seq clicks)
		(let [level (:level state)
			  on-click (:on-click level)
			  new-level (on-click level clicks)]
				(assoc state :level new-level)
		)
		state
	)
)

(defn clear_clicks [_]
  []
)

(defn animate [state]
	(let [new-state (process-clicks state @clicks)] 
		(swap! clicks clear_clicks)
  		(render new-state)
  		(request-animation-frame #(animate (next-state new-state)))
  	)
)

(defn click [event]
    (let [target (.-target event)
          x (or (.-offsetX event) (.-layerX event))
          y (or (.-offsetY event) (.-layerY event))]
;          (.log js/console "x:" x "y:" y "target:" target)
          (swap! clicks #(conj % {:x x :y y}))
    )
)

(defn init []
  (.write js/document "Ludum Dare 27 entry called 10 seconds by Mathias Olsson")
  (.write js/document "<div><canvas id='surface'/></div>")
  (let [canvas (.getElementById js/document "surface")]
    (set! (.-width canvas) canvas-width)
    (set! (.-height canvas) canvas-height)
    (.addEventListener canvas "click" #(click %) false)
  )
  
  (audio/load-audio "audio/villain.ogg" "music")
  (animate (initial-state))
)
  
(set! (.-onload js/window) init)
