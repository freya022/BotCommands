SET search_path TO bc_commands_app_cache;

-- region Setup
CREATE TABLE schema_version
(
    one_row bool DEFAULT TRUE,
    version text NOT NULL,

    PRIMARY KEY (one_row),
    CHECK (one_row)
);

INSERT INTO schema_version
VALUES (TRUE, '4.0.0-alpha.1');
-- endregion

-- region Application commands cache
CREATE TABLE application_commands_cache
(
    application_id bigint NOT NULL,
    guild_id       bigint NULL,
    data           text   NOT NULL,
    metadata       text   NOT NULL,

    UNIQUE NULLS NOT DISTINCT (application_id, guild_id)
);
-- endregion

RESET search_path;
