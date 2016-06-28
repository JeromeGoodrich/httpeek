(ns httpeek.migrations
  (require [migratus.core :as m]
           [environ.core :refer [env]]
           [httpeek.db :as db]))

(def config {:store :database
             :migration-dir "migrations"
             :db db/db

(defn migrate []
  (m/migrate config))

(defn rollback []
  (m/rollback config))
