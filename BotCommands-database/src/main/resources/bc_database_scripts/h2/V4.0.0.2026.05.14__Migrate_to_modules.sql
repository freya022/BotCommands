-- This script recreates all tables in the new schemas and copies the data
-- As H2 does not support changing the schema of a table
-- Nothing is deleted

-- Prevent old BC versions from interacting again
UPDATE bc.bc_version
SET version = '4.0.0-alpha.1'
WHERE one_row = TRUE;

------------------ Application commands ------------------

CREATE SCHEMA bc_commands_app;

SET SCHEMA bc_commands_app;

CREATE TABLE schema_version
(
    one_row bool DEFAULT TRUE,
    version text NOT NULL,

    PRIMARY KEY (one_row),
    CHECK (one_row)
);

INSERT INTO schema_version
VALUES (TRUE, '4.0.0-alpha.1');

CREATE TABLE application_commands_cache
(
    application_id bigint NOT NULL,
    guild_id       bigint NULL,
    data           text   NOT NULL,
    metadata       text   NOT NULL,

    UNIQUE NULLS NOT DISTINCT (application_id, guild_id)
);

INSERT INTO application_commands_cache
SELECT *
FROM bc.application_commands_cache;

------------------ Components ------------------

CREATE SCHEMA bc_components;

SET SCHEMA bc_components;

CREATE TABLE schema_version
(
    one_row bool DEFAULT TRUE,
    version text NOT NULL,

    PRIMARY KEY (one_row),
    CHECK (one_row)
);

INSERT INTO schema_version
VALUES (TRUE, '4.0.0-alpha.1');

CREATE TABLE bc_component
(
    component_id                     serial                   NOT NULL,
    component_type                   smallint                 NOT NULL, -- Can also be a group ! (0)
    lifetime_type                    smallint                 NOT NULL,
    one_use                          bool                     NOT NULL,
    rate_limit_group                 text                     NULL,
    filters                          text array               NOT NULL,
    expires_at                       timestamp with time zone NULL,
    reset_timeout_on_use_duration_ms int                      NULL,
    rate_limit_discriminator         text                     NULL,

    PRIMARY KEY (component_id),

    CHECK (component_type BETWEEN 0 AND 2),
    CHECK (lifetime_type BETWEEN 0 AND 1)
);

INSERT INTO bc_component
SELECT *
FROM bc.bc_component;

CREATE TABLE bc_component_constraints
(
    component_id int          NOT NULL,
    users        bigint array NOT NULL,
    roles        bigint array NOT NULL,
    permissions  bigint       NOT NULL,

    PRIMARY KEY (component_id),
    FOREIGN KEY (component_id) REFERENCES bc_component ON DELETE CASCADE
);

INSERT INTO bc_component_constraints
SELECT *
FROM bc.bc_component_constraints;

-- Component types

CREATE TABLE bc_ephemeral_handler
(
    component_id int NOT NULL,
    handler_id   int NOT NULL,

    PRIMARY KEY (component_id),
    FOREIGN KEY (component_id) REFERENCES bc_component ON DELETE CASCADE
);

INSERT INTO bc_ephemeral_handler
SELECT *
FROM bc.bc_ephemeral_handler;

CREATE TABLE bc_persistent_handler
(
    component_id int         NOT NULL,
    handler_name text        NOT NULL,
    user_data    bytea array NOT NULL,

    PRIMARY KEY (component_id),
    FOREIGN KEY (component_id) REFERENCES bc_component ON DELETE CASCADE
);

INSERT INTO bc_persistent_handler
SELECT *
FROM bc.bc_persistent_handler;

-- Component timeouts

CREATE TABLE bc_ephemeral_timeout
(
    component_id int NOT NULL,
    handler_id   int NOT NULL,

    PRIMARY KEY (component_id),
    FOREIGN KEY (component_id) REFERENCES bc_component ON DELETE CASCADE
);

INSERT INTO bc_ephemeral_timeout
SELECT *
FROM bc.bc_ephemeral_timeout;

CREATE TABLE bc_persistent_timeout
(
    component_id int         NOT NULL,
    handler_name text        NOT NULL,
    user_data    bytea array NOT NULL,

    PRIMARY KEY (component_id),
    FOREIGN KEY (component_id) REFERENCES bc_component ON DELETE CASCADE
);

INSERT INTO bc_persistent_timeout
SELECT *
FROM bc.bc_persistent_timeout;

-- Group, bc_component can already be a group
-- Associative table
CREATE TABLE bc_component_component_group
(
    group_id     int NOT NULL,
    component_id int NOT NULL,

    FOREIGN KEY (group_id) REFERENCES bc_component ON DELETE CASCADE,
    FOREIGN KEY (component_id) REFERENCES bc_component ON DELETE CASCADE,
    PRIMARY KEY (group_id, component_id)
);

INSERT INTO bc_component_component_group
SELECT *
FROM bc.bc_component_component_group;
