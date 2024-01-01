set schema 'public';

create table tag
(
    name       text      not null,
    created_at timestamp not null default now(),
    content    text      not null,

    check (name ~ '^[\w-]+$') -- Dont allow spaces
);

insert into tag (name, content)
values ('zip-closed', 'Bukkit moment'),
       ('upsertCommand', 'updateCommands >>>');