#!/usr/bin/env bash

set -e

setup_db() {
  local db_name="$1"
  local db_profile_name="$2"

  echo "Creating database user..."
  createuser --createdb admin || echo "Skipping..."

  echo "Creating ${db_profile_name} database..."
  createdb --username=admin ${db_name} || echo "Skipping..."

  psql ${db_name} -c 'CREATE EXTENSION "pgcrypto";' || echo "Skipping..."

  echo "Applying migrations to ${db_profile_name} database..."
  lein with-profile ${db_profile_name} migrate
}

setup_procedure() {
  echo "Setting up the project..."

  setup_db admin test
  setup_db admin dev

  echo "Done. You should now be able to run specs, start the server, etc."
}

setup_procedure
