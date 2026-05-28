------------------------------------------------------ 8th migration script for BotCommands ------------------------------------------------------
---------------------------------- Make sure to run the previous scripts (chronological order) before this one -----------------------------------

SET SCHEMA 'bc';

UPDATE bc_version
SET version = '3.0.0-beta.1'
WHERE one_row = true;

ALTER TABLE bc_persistent_handler
    ALTER COLUMN user_data SET DATA TYPE bytea array USING cast(user_data as bytea array);

ALTER TABLE bc_persistent_timeout
    ALTER COLUMN user_data SET DATA TYPE bytea array USING cast(user_data as bytea array);
