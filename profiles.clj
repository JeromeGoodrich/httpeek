{:dev {:env {:database-url "postgres://httpeek:password@localhost:5432/httpeek"}
             :jvm-opts ["-Dlogfile.path=development"]}
 :test {:env {:database-url "postgres://httpeek:password@localhost:5432/httpeek_spec"}
              :jvm-opts ["-Dlogfile.path=test"]}
 :heroku {:env {:database-url #=(eval (System/getenv "DATABASE_URL"))}}
                :jvm-opts ["-Dlogfile.path=heroku"]}

