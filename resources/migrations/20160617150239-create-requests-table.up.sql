CREATE TABLE requests(
  id        serial PRIMARY KEY,
  body      jsonb,
  bin_id    UUID  REFERENCES bins
);
