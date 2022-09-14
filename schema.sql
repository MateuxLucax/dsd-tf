PRAGMA foreign_keys = ON;
-- ^ tem que ser habilitado *a cada conexão* com o banco

drop table if exists users;
create table users (
        id          integer primary key autoincrement,
        username    text    unique not null,
        avatar_path text    null,
        created_at  timestamp default current_timestamp not null,
        updated_at  timestamp null
);

drop table if exists friends;
create table friends (
        friend1_id integer not null references users(id),
        friend2_id integer not null references users(id),
        primary key (friend1_id, friend2_id)
);

drop table if exists friend_request_status;
create table friend_request_status (
        id integer primary key,
        description text
);
insert into friend_request_status values (1, "Waiting"), (2, "Accepted"), (3, "Rejected");

drop table if exists friend_requests;
create table friend_requests (
        sender_id   integer references users(id),
        receiver_id integer references users(id),
        status      integer references friend_request_status(id),
        primary key (sender_id, receiver_id)
);
create index idx_friend_requests_receiver on friend_requests(receiver_id, sender_id);
