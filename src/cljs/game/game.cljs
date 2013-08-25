(ns ten-seconds.game
	(:require [ten-seconds.audio :as audio]
			[ten-seconds.canvas :as canvas]
			[ten-seconds.utils :as utils]
			[ten-seconds.levels :as levels]
			[clojure.string :as string]
			[clojure.set :as set])
)


(def canvas-width 800)
(def canvas-height 500)

(def clicks (atom []))


(defn initial-state []
  {
  :level levels/intro
  }
)


(defn render-level [context level] 
	(let [obj-list (:object-list level)]
		(doseq [obj (reverse obj-list)]
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
		(= level "intro") levels/intro
		(= level "level1") levels/level1
		:else levels/game-over
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
  
  (audio/load-audio "audio/ludumdare10seconds.ogg" "music")
  (animate (initial-state))
)
  
(set! (.-onload js/window) init)
