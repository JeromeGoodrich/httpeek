(ns httpeek.migrations
  (require [migratus.core :as m]
           [environ.core :refer [env]]))

(def config {:store :database
             :migration-dir "migrations"
             :db {:classname (env :db-classname)
                  :subprotocol (env :db-subprotocol)
                  :subname (env :db-subname)
                  :user (env :db-user)
                  :password (env :db-password)}})

(defn migrate []
  (m/migrate config))

(defn rollback []
  (m/rollback config))
