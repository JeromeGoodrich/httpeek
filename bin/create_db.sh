#!/usr/bin/env bash

set -e

setup_db() {
  local db_name="$1"
  local db_profile_name="$2"

  echo "Creating database user..."
  createdb || echo "Skipping..."
  createuser --createdb httpeek || echo "Skipping..."
  psql -c "ALTER USER httpeek PASSWORD 'password';"

  echo "Creating ${db_profile_name} database..."
  createdb --username=httpeek ${db_name} || echo "Skipping..."

  psql ${db_name} -c 'CREATE EXTENSION "pgcrypto";' || echo "Skipping..."

  echo "Applying migrations to ${db_profile_name} database..."
  lein with-profile ${db_profile_name} migrate
}

setup_procedure() {
  echo "Setting up the project..."

  setup_db httpeek_spec test
  setup_db httpeek dev

  echo "Done. You should now be able to run specs, start the server, etc."
}

setup_procedure
