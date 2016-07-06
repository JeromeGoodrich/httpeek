(defproject httpeek "0.1.0-SNAPSHOT"
            :url "http://example.com/FIXME"
            :license {:name "Eclipse Public License"
                      :url "http://www.eclipse.org/legal/epl-v10.html"}
            :dependencies [[org.clojure/clojure "1.7.0-RC2"]
                           [ring/ring-mock "0.3.0"]
                           [ring "1.5.0"]
                           [compojure "1.5.0"]
                           [org.clojure/java.jdbc "0.6.1"]
                           [org.postgresql/postgresql "9.4-1203-jdbc41"]
                           [hiccup "1.0.5"]
                           [migratus "0.8.25"]
                           [environ "1.0.3"]
                           [speclj "3.3.1"]
                           [cheshire "5.6.2"]
                           [ring/ring-json "0.4.0"]]
            :profiles {:test {:dependencies [[speclj "3.3.1"]]}}
            :plugins [[speclj "3.3.1"]
                      [lein-environ "1.0.3"]
                      [lein-ring "0.9.7"]]
            :test-paths ["spec"]
            :ring {:handler httpeek.handler/app* :auto-refresh? true}
            :aliases {"migrate" ["run" "-m" "httpeek.migrations/migrate"]
                      "rollback" ["run" "-m" "httpeek.migrations/rollback"]})
