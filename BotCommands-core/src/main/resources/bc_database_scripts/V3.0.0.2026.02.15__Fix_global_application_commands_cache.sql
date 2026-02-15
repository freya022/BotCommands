------------------------------------------------------ 9th migration script for BotCommands ------------------------------------------------------
---------------------------------- Make sure to run the previous scripts (chronological order) before this one -----------------------------------

SET SCHEMA 'bc';

UPDATE bc_version
SET version = '3.0.0-beta.9'
WHERE one_row = true;

ALTER TABLE application_commands_cache
    DROP CONSTRAINT application_commands_cache_application_id_guild_id_key;

TRUNCATE application_commands_cache;

ALTER TABLE application_commands_cache
    ADD UNIQUE NULLS NOT DISTINCT (application_id, guild_id);
