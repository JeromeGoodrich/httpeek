CREATE TABLE bins(
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  private             boolean NOT NULL,
  custom_response     jsonb,
  created_at          timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);
