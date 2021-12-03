create table if not exists Version
(
    oneRow  bool primary key default true check (oneRow),
    version text not null
);