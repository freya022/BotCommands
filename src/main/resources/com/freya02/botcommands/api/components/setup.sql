create sequence if not exists group_seq as bigint;

create table if not exists ComponentData
(
    componentId         text not null primary key check (length(componentId) = 64),
    type                int  not null check ( type >= 0 and type <= 3 ),
    groupId             bigint,
    oneUse              bool not null,
    constraints         text not null,
    expirationTimestamp bigint
);

create table if not exists LambdaComponentData
(
    componentId text      not null references ComponentData on delete cascade,
    handlerId   bigserial not null
);

create table if not exists PersistentComponentData
(
    componentId text not null references ComponentData on delete cascade,
    handlerName text not null,
    args        text not null
);

delete
from componentdata
    USING lambdacomponentdata
WHERE type in (1, 3);