CREATE TABLE requests(
  id        serial PRIMARY KEY,
  body      jsonb NOT NULL,
  bin_id    UUID NOT NULL REFERENCES bins,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);
