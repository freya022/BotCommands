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
    bc_ephemeral_timeout, bc_persistent_timeout,
    bc_ephemeral_component, bc_ephemeral_component_timeout,
    bc_persistent_component, bc_persistent_component_timeout,
    bc_component_group, bc_component_component_group, bc_ephemeral_group_timeout, bc_persistent_group_timeout cascade;

create table bc_component
(
    component_id   text     not null primary key check (length(component_id) = 64),
    component_type smallint not null,
    lifetime_type  smallint not null check (lifetime_type = 0 or lifetime_type = 1),
    one_use        bool     not null
);

create table bc_component_constraints
(
    component_id text     not null primary key references bc_component on delete cascade,
    users        bigint[] not null,
    roles        bigint[] not null,
    permissions  bigint   not null
);

-- Component types

create table bc_ephemeral_component
(
    component_id text      not null primary key references bc_component on delete cascade,
    handler_id   bigserial not null -- Returned
);

create table bc_persistent_component
(
    component_id text   not null primary key references bc_component on delete cascade,
    handler_name text   not null,
    args         text[] not null
);

-- Component timeouts

create table bc_ephemeral_timeout
(
    component_id         text                     not null primary key references bc_ephemeral_component on delete cascade,
    expiration_timestamp timestamp with time zone not null,
    handler_id           serial                   not null -- Returned
);

create table bc_persistent_timeout
(
    component_id         text                     not null primary key references bc_persistent_component on delete cascade,
    expiration_timestamp timestamp with time zone not null,
    handler_name         text                     not null
);

create table bc_ephemeral_component_timeout
(
    component_id text not null references bc_ephemeral_component on delete cascade,
    timeout_id   text not null references bc_ephemeral_timeout on delete cascade
);

create table bc_persistent_component_timeout
(
    component_id text not null references bc_ephemeral_component on delete cascade,
    timeout_id   text not null references bc_persistent_timeout on delete cascade
);

-- Group
create table bc_component_group
(
    group_id serial not null primary key, -- Returned
    one_use  bool   not null
);

create table bc_component_component_group
(
    group_id     int  not null references bc_component_group,
    component_id text not null references bc_component,

    primary key (group_id, component_id)
);

create table bc_ephemeral_group_timeout
(
    group_id   int  not null references bc_component_group on delete cascade,
    timeout_id text not null references bc_ephemeral_timeout on delete cascade
);

create table bc_persistent_group_timeout
(
    group_id   int  not null references bc_component_group on delete cascade,
    timeout_id text not null references bc_persistent_timeout on delete cascade
);