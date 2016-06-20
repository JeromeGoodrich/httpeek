CREATE TABLE bins(
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  created_at    timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);
