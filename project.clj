(defproject httpeek "0.1.0-SNAPSHOT"
            :url "http://example.com/FIXME"
            :license {:name "Eclipse Public License"
                      :url "http://www.eclipse.org/legal/epl-v10.html"}
            :dependencies [[org.clojure/clojure "1.7.0-RC2"]
                           [ring/ring-mock "0.3.0"]
                           [ring "1.5.0"]
                           [compojure "1.5.0"]
                           [org.clojure/java.jdbc "0.6.1"]
                           [org.slf4j/slf4j-nop "1.7.3"]
                           [org.postgresql/postgresql "9.4-1203-jdbc41"]
                           [hiccup "1.0.5"]
                           [migratus "0.8.25"]
                           [camel-snake-kebab "0.4.0"]
                           [environ "1.0.3"]
                           [speclj "3.3.1"]
                           [clojurewerkz/quartzite "2.0.0"]
                           [cheshire "5.6.2"]
                           [com.novemberain/validateur "2.5.0"]
                           [org.clojure/data.xml "0.0.8"]
                           [ring/ring-json "0.4.0"]]
            :min-lein-version "2.0.0"
            :profiles {:test {:dependencies [[speclj "3.3.1"]]}}
            :plugins [[speclj "3.3.1"]
                      [lein-environ "1.0.3"]
                      [lein-ring "0.9.7"]]
            :test-paths ["spec"]
            :jvm-opts ["-Duser.timezone=UTC"]
            :ring {:handler httpeek.handler/app* :auto-reload? true}
            :aliases {"migrate" ["run" "-m" "httpeek.migrations/migrate"]
                      "rollback" ["run" "-m" "httpeek.migrations/rollback"]
                      "delete-expired" ["run" "-m" "httpeek.main/-main"]})
