drop table if exists bc_version, bc_component_data, bc_lambda_component_data, bc_persistent_component_data cascade;
drop sequence if exists bc_component_group_seq cascade;

create table bc_version
(
    one_row  bool primary key default true check (one_row),
    version text not null
);

insert into bc_version
values (true, '3.0.0'); -- Change in Database.kt too

create sequence if not exists bc_component_group_seq as bigint;

create table if not exists bc_component_data
(
    component_id         text not null primary key check (length(component_id) = 64),
    type                 int  not null check ( type >= 0 and type <= 3 ),
    group_id             bigint,
    one_use              bool not null,
    constraints          text not null,
    expiration_timestamp bigint
);

create table if not exists bc_lambda_component_data
(
    component_id text      not null references bc_component_data on delete cascade,
    handler_id   bigserial not null
);

create table if not exists bc_persistent_component_data
(
    component_id text not null references bc_component_data on delete cascade,
    handler_name  text not null,
    args         text not null
);