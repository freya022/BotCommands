------------ Base framework

drop table if exists bc_version cascade;
drop table if exists bc_statement_result cascade;

create table bc_version
(
    one_row bool primary key default true check (one_row),
    version text not null
);

insert into bc_version
values (true, '3.0.0'); -- Change in Database.kt too

create table bc_statement_result
(
    id         serial   not null primary key,
    query      text     not null,
    success    smallint not null,
    time_nanos bigint   not null
);

------------ Components

drop table if exists bc_component, bc_component_constraints,
    bc_ephemeral_handler, bc_persistent_handler,
    bc_ephemeral_timeout, bc_persistent_timeout,
    bc_component_component_group cascade;

create table bc_component
(
    component_id   serial   not null primary key,
    component_type smallint not null check (component_type between 0 and 2), -- Can also be a group ! (0)
    lifetime_type  smallint not null check (lifetime_type between 0 and 1),
    one_use        bool     not null
);

create table bc_component_constraints
(
    component_id int      not null primary key references bc_component on delete cascade,
    users        bigint[] not null,
    roles        bigint[] not null,
    permissions  bigint   not null
);

-- Component types

create table bc_ephemeral_handler
(
    component_id int not null primary key references bc_component on delete cascade,
    handler_id   int not null
);

create table bc_persistent_handler
(
    component_id int    not null primary key references bc_component on delete cascade,
    handler_name text   not null,
    user_data    text[] not null
);

-- Component timeouts

create table bc_ephemeral_timeout
(
    component_id         int                      not null primary key references bc_component on delete cascade,
    expiration_timestamp timestamp with time zone not null,
    handler_id           int                      null
);

create table bc_persistent_timeout
(
    component_id         int                      not null primary key references bc_component on delete cascade,
    expiration_timestamp timestamp with time zone not null,
    handler_name         text                     null,
    user_data            text[]                   not null
);

-- Group, bc_component can already be a group
-- Associative table
create table bc_component_component_group
(
    group_id     int not null references bc_component on delete cascade,
    component_id int not null references bc_component on delete cascade,

    primary key (group_id, component_id)
);