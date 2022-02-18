drop table if exists componentdata, lambdacomponentdata, persistentcomponentdata cascade;
drop sequence if exists group_seq cascade;

create table if not exists Version
(
    oneRow  bool primary key default true check (oneRow),
    version text not null
);

insert into Version
values (true, '2');