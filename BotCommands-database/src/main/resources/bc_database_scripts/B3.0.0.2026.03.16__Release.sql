SET search_path TO bc;

-- region Setup
CREATE TABLE bc_version
(
    one_row bool DEFAULT TRUE,
    version text NOT NULL,

    PRIMARY KEY (one_row),
    CHECK (one_row)
);

INSERT INTO bc_version
VALUES (TRUE, '3.0.0');
-- endregion

-- region Components
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

CREATE TABLE bc_component_constraints
(
    component_id int          NOT NULL,
    users        bigint array NOT NULL,
    roles        bigint array NOT NULL,
    permissions  bigint       NOT NULL,

    PRIMARY KEY (component_id),
    FOREIGN KEY (component_id) REFERENCES bc_component ON DELETE CASCADE
);

-- Component types

CREATE TABLE bc_ephemeral_handler
(
    component_id int NOT NULL,
    handler_id   int NOT NULL,

    PRIMARY KEY (component_id),
    FOREIGN KEY (component_id) REFERENCES bc_component ON DELETE CASCADE
);

CREATE TABLE bc_persistent_handler
(
    component_id int         NOT NULL,
    handler_name text        NOT NULL,
    user_data    bytea array NOT NULL,

    PRIMARY KEY (component_id),
    FOREIGN KEY (component_id) REFERENCES bc_component ON DELETE CASCADE
);

-- Component timeouts

CREATE TABLE bc_ephemeral_timeout
(
    component_id int NOT NULL,
    handler_id   int NOT NULL,

    PRIMARY KEY (component_id),
    FOREIGN KEY (component_id) REFERENCES bc_component ON DELETE CASCADE
);

CREATE TABLE bc_persistent_timeout
(
    component_id int         NOT NULL,
    handler_name text        NOT NULL,
    user_data    bytea array NOT NULL,

    PRIMARY KEY (component_id),
    FOREIGN KEY (component_id) REFERENCES bc_component ON DELETE CASCADE
);

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
