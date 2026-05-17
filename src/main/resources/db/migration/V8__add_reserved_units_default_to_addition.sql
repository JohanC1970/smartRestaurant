-- Add default value for reserved_units column in addition table
ALTER TABLE addition ALTER COLUMN reserved_units SET DEFAULT 0;

-- Update existing records to have 0 reserved_units if they are null
UPDATE addition SET reserved_units = 0 WHERE reserved_units IS NULL;
