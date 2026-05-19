------------------------------------------------------ 9th migration script for BotCommands ------------------------------------------------------
---------------------------------- Make sure to run the previous scripts (chronological order) before this one -----------------------------------

SET SCHEMA 'bc';

UPDATE bc_version
SET version = '3.0.0-beta.9'
WHERE one_row = true;

-- PostgreSQL
ALTER TABLE application_commands_cache
    DROP IF EXISTS application_commands_cache_application_id_guild_id_key;

-- H2
ALTER TABLE application_commands_cache
    DROP IF EXISTS CONSTRAINT_INDEX_A;

-- Delete global commands cache, as this is what could have been duplicated
DELETE FROM application_commands_cache WHERE guild_id IS NULL;

ALTER TABLE application_commands_cache
    ADD CONSTRAINT app_commands_cache_unique_key UNIQUE NULLS NOT DISTINCT (application_id, guild_id);
