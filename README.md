# httpeek

httpeek is a request bin app. It provides you with a unique URL you can make requests to
and then allows you to see those requests in a friendly UI and return customizable responses.

## Installation

Clone the repo
`git clone http://github.com/Jgoodrich07/httpeek`

You will need to have Leinegen installed.

###Setting up the database

you will need to create a database to use with the app.


The profiles.clj file dictates the configuration map for a Postgres database using the
jdbc driver. the `env` key is what the environ library uses to store configuration data
specific to different profiles. In order to use the app your database will have to match
the configuration below.


```clojure

{:dev {:env {:db-classname "com.postgresql.jdbc.Driver"
            :db-subprotocol "postgresql"
            :db-subname "//localhost:5432/httpeek"
            :db-password ""
            :db-user "httpeek"}}
:test {:env {:db-classname "com.postgresql.jdbc.Driver"
             :db-subprotocol "postgresql"
             :db-subname "//localhost:5432/httpeek_spec"
             :db-password ""
             :db-user "httpeek_spec"}}}
```

###Migrating the database

Once your databases are set-up you will need to migrate them using the following commands

For the test db: `lein with-profile test migrate`
For the dev db: `lein migrate`

In order to rollback the migrations it's the same command but `rollback` instead of migrate

#Running HTTPeek

To run the server: `lein ring server` Server is configured to auto-reload
To run the specs: `lein with-profile test spec -a`

