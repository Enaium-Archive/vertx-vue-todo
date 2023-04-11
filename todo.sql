create table t_list
(
    id            uuid     not null
        primary key,
    user_id       uuid     not null,
    title         text     not null,
    created_time  datetime not null,
    modified_time datetime not null
);

create table t_task
(
    id            uuid                 not null
        primary key,
    user_id       uuid                 not null,
    list_id       uuid                 not null,
    content       text                 not null,
    important     tinyint(1) default 0 not null,
    finished      tinyint(1) default 0 not null,
    created_time  datetime             not null,
    modified_time datetime             not null
);

create table t_user
(
    id            uuid        not null
        primary key,
    username      varchar(18) not null,
    password      varchar(18) not null,
    created_time  datetime    not null,
    modified_time datetime    not null,
    constraint t_user_pk
        unique (username)
);

