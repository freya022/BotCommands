drop table if exists bc_version, bc_component_data, bc_lambda_component_data, bc_persistent_component_data, bc_statement_result cascade;
drop sequence if exists bc_component_group_seq cascade;

create table bc_version
(
    one_row bool primary key default true check (one_row),
    version text not null
);

insert into bc_version
values (true, '3.0.0'); -- Change in Database.kt too

create sequence bc_component_group_seq as bigint;

create table bc_component_data
(
    component_id         text     not null primary key check (length(component_id) = 64),
    type                 smallint not null check ( type >= 0 and type <= 3 ),
    group_id             bigint   not null default 0,
    one_use              bool     not null,
    constraints          text     not null,
    expiration_timestamp bigint
);

create table bc_lambda_component_data
(
    component_id text      not null references bc_component_data on delete cascade,
    handler_id   bigserial not null
);

create table bc_persistent_component_data
(
    component_id text not null references bc_component_data on delete cascade,
    handler_name text not null,
    args         text not null
);

create table bc_statement_result
(
    id         serial   not null primary key,
    query      text     not null,
    success    smallint not null,
    time_nanos bigint   not null
);

create table bc_data
(
    id                   text     not null primary key check (length(id) = 64),
    data                 text     not null,
    lifetime_type        smallint not null,        -- This is only a hint to do a startup cleanup, as to minimize scheduling needs
    expiration_timestamp timestamp with time zone, -- Don't ask me why it needs a timezone, but it works.
    timeout_handler_id   text     not null
);
