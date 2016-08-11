# httpeek

httpeek is a request bin app. It provides you with a unique URL you can make requests to
and then allows you to see those requests in a friendly UI and return customizable responses.

## Installation

Clone the repo:


`git clone http://github.com/Jgoodrich07/httpeek`


(You will need to have Leinegen installed)

###Setting up the database

run `bin/create_db.sh`. The script will create the databases and run the necessary migrations

If for some reason you need to rollback the migrations, use `lein rollback`.

###Running HTTPeek

- To run the server: `lein ring server` Server is configured to auto-reload
- To run the specs: `lein with-profile test spec -a`
- To run the the job cleans up expired bins: `lein delete-expired`
