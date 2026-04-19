-- Add price column to drink table if it doesn't exist
ALTER TABLE drink ADD COLUMN IF NOT EXISTS price DOUBLE PRECISION NOT NULL DEFAULT 0;
