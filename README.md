# httpeek

httpeek is a request bin app. It provides you with a unique URL you can make requests to
and then allows you to see those requests in a friendly UI and return customizable responses.

## Installation

Clone the repo
`git clone http://github.com/Jgoodrich07/httpeek`

You will need to have Leinegen installed.

###Setting up the database

just run this `bin/create_db.sh`
Once your databases are set-up you can  migrate them using the following commands

For the test db: `lein with-profile test migrate`
For the dev db: `lein migrate`

In order to rollback the migrations it's the same command but `rollback` instead of migrate

#Running HTTPeek

To run the server: `lein ring server` Server is configured to auto-reload
To run the specs: `lein with-profile test spec -a`

