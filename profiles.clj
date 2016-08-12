{:dev {:env {:database-url "postgres://httpeek:password@localhost:5432/httpeek"}}
 :test {:env {:database-url "postgres://httpeek:password@localhost:5432/httpeek_spec"}}
 :heroku {:env {:database-url #=(eval (System/getenv "DATABASE_URL"))}}}
