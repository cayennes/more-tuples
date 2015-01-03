(defproject more-tuples "0.1.0-SNAPSHOT"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [om "0.6.3"]
                 [devcards "0.1.2-SNAPSHOT"]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [com.cemerick/clojurescript.test "0.3.1"]
            [lein-figwheel "0.1.5-SNAPSHOT"]]

  ; so that clojurescript compilation doesn't die when run in circle
  :jvm-opts ["-Xmx2g"]

  :cljsbuild {
    :test-commands {"unit" ["node" :node-runner
                            "testable.js"]}
    :builds [{:id "dev"
              :source-paths ["src"]
              :compiler {:output-to "dev.js"
                         :output-dir "out"
                         :optimizations :none
                         :source-map true}}
             {:id "release"
              :source-paths ["src"]
              :compiler {:output-to "more_tuples.js"
                         :optimizations :advanced
                         :pretty-print false
                         :preamble ["react/react.min.js"]
                         :externs ["react/externs/react.js"]}}
             {:id "test"
              :source-paths ["src" "test"]
              :compiler {:target :nodejs
                         :output-to "testable.js"
                         :optimizations :advanced
                         :hashbang false
                         :preamble ["react/react.min.js"]
                         :externs ["react/externs/react.js"]}}
             {:id "devcards"
              :source-paths ["src" "src-devcards"]
              :compiler {:output-to "dev-resources/public/js/compiled/devcards.js"
                         :output-dir "dev-resources/public/js/compiled/out"
                         :optimizations :none}}]})
