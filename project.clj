(defproject httpeek "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
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
                 [migratus "0.8.25"]]
  :profiles {:dev {:dependencies [[speclj "3.3.1"]]}}
  :plugins [[speclj "3.3.1"]
            [lein-ring "0.9.7"]
            [migratus-lein "0.3.7"]]
  :test-paths ["spec"]
  :ring {:handler httpeek.handler/app*}
  :migratus {:store :database
           :migration-dir "migrations"
           :db {:classname "com.postgresql.jdbc.Driver"
                :subprotocol "postgresql"
                :subname "//localhost:5432/httpeek"
                :user "httpeek"
                :password ""}})
