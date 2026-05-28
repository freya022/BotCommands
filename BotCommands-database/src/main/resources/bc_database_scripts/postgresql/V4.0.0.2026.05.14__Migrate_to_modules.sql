-- This script moves the tables from the 3.X 'bc' schema to the schema of the respective modules

-- Prevent old BC versions from interacting again
UPDATE bc.bc_version
SET version = '4.0.0-alpha.1'
WHERE one_row = TRUE;

------------------ Application commands ------------------

CREATE SCHEMA bc_commands_app;

SET search_path TO bc_commands_app;

CREATE TABLE schema_version
(
    one_row bool DEFAULT TRUE,
    version text NOT NULL,

    PRIMARY KEY (one_row),
    CHECK (one_row)
);

INSERT INTO schema_version
VALUES (TRUE, '4.0.0-alpha.1');

ALTER TABLE bc.application_commands_cache
    SET SCHEMA bc_commands_app;

RESET search_path;

------------------ Components ------------------

CREATE SCHEMA bc_components;

SET search_path TO bc_components;

CREATE TABLE schema_version
(
    one_row bool DEFAULT TRUE,
    version text NOT NULL,

    PRIMARY KEY (one_row),
    CHECK (one_row)
);

INSERT INTO schema_version
VALUES (TRUE, '4.0.0-alpha.1');

ALTER TABLE bc.bc_component
    SET SCHEMA bc_components;

ALTER TABLE bc.bc_component_component_group
    SET SCHEMA bc_components;

ALTER TABLE bc.bc_component_constraints
    SET SCHEMA bc_components;

ALTER TABLE bc.bc_ephemeral_handler
    SET SCHEMA bc_components;

ALTER TABLE bc.bc_ephemeral_timeout
    SET SCHEMA bc_components;

ALTER TABLE bc.bc_persistent_handler
    SET SCHEMA bc_components;

ALTER TABLE bc.bc_persistent_timeout
    SET SCHEMA bc_components;

RESET search_path;
