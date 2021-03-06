(ns ten-seconds.audio 
   (:require [cljs.core.async :as async])
   (:require-macros [cljs.core.async.macros :as async-m])
)

(def loaded-audio (atom ()))

(def audio-context (if window/webkitAudioContext
                     (new window/webkitAudioContext)
                     window/AudioContext
                     )
)



(defn play-sound [audio-buffer loop]
  (let [source (.createBufferSource audio-context)]
  	(.connect source (.-destination audio-context))
  	(set! (.-buffer source) audio-buffer)
  	(set! (.-loop source) loop)
  	; start does not work in Safari. Use old notOn instead
;  	(.start source 0)
  	(.noteOn source 0)
  )
)

(defn play-by-name [name]
  (.log js/console "play-by-name")
  (doseq [audio @loaded-audio]
     (.log js/console (:name audio))
     (play-sound (:buffer audio) true)
  ) 
)

(defn load-audio [url name]
  (let [context audio-context
  		request (new js/XMLHttpRequest)
  		loaded-chan (async/chan)
  		decoded-chan (async/chan)
  		]
  		(.open request "GET" url true)
  		(set! (.-responseType request) "arraybuffer")
  		(set! (.-onload request) (fn [] (.log js/console "Loaded cb") (async/put! loaded-chan (.-response request))))
 		(.send request)
  		(.log js/console "Start")
  		
  		(async-m/go 
  			(let [response (async/<! loaded-chan)]
  				(.log js/console "Loaded chan")
  				(.decodeAudioData context response (fn [buffer](.log js/console "Decoded") (async/put! decoded-chan buffer)) #(.log js/console "Decode failed"))
  			)
  		)
  		
  		(async-m/go
  			(let [buffer (async/<! decoded-chan)];
  				(.log js/console "Decoded chan")
  				(swap! loaded-audio (fn [la] (cons {:buffer buffer :name name} la)))
  				; TODO I really don't want to play it here...
  				(play-by-name name)
  			)
  		)
		  			  		
  		(.log js/console "Done")
	)
)

