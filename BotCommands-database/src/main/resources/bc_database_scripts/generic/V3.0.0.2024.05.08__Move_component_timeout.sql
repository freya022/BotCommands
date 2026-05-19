------------------------------------------------------ 4th migration script for BotCommands ------------------------------------------------------
---------------------------------- Make sure to run the previous scripts (chronological order) before this one -----------------------------------

SET SCHEMA 'bc';

UPDATE bc_version
SET version = '3.0.0-alpha.14'
WHERE one_row = true;

-- Add expiration column to base component
ALTER TABLE bc_component
    ADD COLUMN expires_at TIMESTAMP WITH TIME ZONE NULL;

-- Copy existing timestamps to new column
UPDATE bc_component c
SET expires_at = (SELECT expiration_timestamp FROM bc_persistent_timeout pt WHERE c.component_id = pt.component_id)
WHERE lifetime_type = 0;

UPDATE bc_component c
SET expires_at = (SELECT expiration_timestamp FROM bc_ephemeral_timeout et WHERE c.component_id = et.component_id)
WHERE lifetime_type = 1;

-- Drop old expiration columns
ALTER TABLE bc_persistent_timeout
    DROP COLUMN expiration_timestamp;
ALTER TABLE bc_ephemeral_timeout
    DROP COLUMN expiration_timestamp;

-- Previously, the timeout rows could have an expiration timestamp, and a null handler, or, both.
-- Now, the timeout rows only exist if an handler is entirely set

-- Remove rows with no handler
DELETE
FROM bc_ephemeral_timeout
WHERE handler_id IS NULL;

DELETE
FROM bc_persistent_timeout
WHERE handler_name IS NULL;

-- Tighten null constraints
ALTER TABLE bc_ephemeral_timeout
    ALTER COLUMN handler_id SET NOT NULL;
ALTER TABLE bc_persistent_timeout
    ALTER COLUMN handler_name SET NOT NULL;