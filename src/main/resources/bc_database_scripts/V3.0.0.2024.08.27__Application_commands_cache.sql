------------------------------------------------------ 6th migration script for BotCommands ------------------------------------------------------
---------------------------------- Make sure to run the previous scripts (chronological order) before this one -----------------------------------

SET SCHEMA 'bc';

UPDATE bc_version
SET version = '3.0.0-alpha.19'
WHERE one_row = true;

CREATE TABLE application_commands_cache
(
    application_id BIGINT NOT NULL,
    guild_id       BIGINT NULL,
    data           TEXT   NOT NULL,
    metadata       TEXT   NOT NULL,

    UNIQUE (application_id, guild_id)
);
