{:dev      {:env {:db "postgres://httpeek:password@localhost:5432/httpeek"}}
 :test     {:env {:db "postgres://httpeek:password@localhost:5432/httpeek_spec"}}
 :heroku   {:env {:db #=(eval (System/getenv "DATABASE_URL"))}}}
