# httpeek

HTTPeek is an application created to help users capture and read HTTP requests in a user friendly way.
To get started, create a 'bin' below. This bin will provide you with a unique URL to make requests to. You can also
specify what type of HTTP response you want requests to that bin to return. Bins are ephemeral and only last a maximum
of 24 hours.

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
