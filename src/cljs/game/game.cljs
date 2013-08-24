(ns ten-seconds.game)

(def canvas-width 800)
(def canvas-height 500)

(defn initial-state []
  {}
)

(defn render []
  (let [canvas (.getElementById js/document "surface")]
    (let [context (.getContext canvas "2d")]
      (set! (.-fillStyle context) (str "rgba(" 0 "," 0 "," 0 "," 0.1 ")"))
      (.fillRect context 0 0 canvas-width canvas-height)
;;      (render-fireworks context @fireworks)
    )
  )
)

(def request-animation-frame
  (or (.-mozRequestAnimationFrame js/window)
      (.-requestAnimationFrame js/window)
      (.-webkitRequestAnimationFrame js/window)
      #(js/timeout 10 (%)) ; Syntactic sugar for a fn
  )
)
       
(defn animate [state]
  (request-animation-frame #(animate state))
  (render)
)

(defn init []
  (.write js/document "Ludum Dare 27 entry called 10 seconds by Mathias Olsson")
  (.write js/document "<div><canvas id='surface'/></div>")
  (let [canvas (.getElementById js/document "surface")]
    (set! (.-width canvas) canvas-width)
    (set! (.-height canvas) canvas-height)
  )
  (animate initial-state)
)
  
(set! (.-onload js/window) init)
