(defproject ld27-10-seconds-cljs "0.1.0-SNAPSHOT"
  :description "Ludum Dare 27 entry 10 seconds"
  :url "http://example.com/FIXME"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.txt"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  
  :plugins [
  			;; lein-cljsbuild plugin to build a CLJS project
  			[lein-cljsbuild "0.3.2"]
			]
            
  ;; cljsbuild options configuration
  :cljsbuild {:builds
              {
               :dev
               {;; clojurescript source code path
                :source-paths ["src/brepl" "src/cljs"]

                ;; Google Closure Compiler options
                :compiler {;; the name of emitted JS script file
                           :output-to "resources/public/js/10_seconds.js"

                           :optimizations :simple

                           ;; prettyfying emitted JS
                           :pretty-print true}
                }
            }
        }  
  )
