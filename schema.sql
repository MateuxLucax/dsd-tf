drop table if exists users;

create table users (
        id          integer primary key autoincrement,
        username    text    unique not null,
        password    text    not null,
        avatar_path text    null,
        created_at  timestamp default current_timestamp not null,
        updated_at  timestamp null,

        check (updated_at is null or updated_at > created_at)
);

drop table if exists friends;

create table friends (
        friend1_id integer not null,
        friend2_id integer not null,
        created_at timestamp default current_timestamp not null,

        primary key (friend1_id, friend2_id),

        foreign key (friend1_id) references users(id)
            on delete cascade
            on update cascade,
        foreign key (friend2_id) references users(id)
            on delete cascade
            on update cascade
);

drop table if exists friend_request_status;

drop table if exists friend_requests;

-- a friend request is pending by existence
-- once the request is accepted, the users become friends and the request is deleted
-- or once the request is rejected, the request is just deleted without the users becoming friends

create table friend_requests (
        sender_id   integer,
        receiver_id integer,
        created_at  timestamp default current_timestamp not null,
        updated_at  timestamp null,  -- when it was accepted/rejected

        check (updated_at is null or updated_at > created_at),

        primary key (sender_id, receiver_id),

        foreign key (sender_id) references users(id)
            on delete cascade
            on update cascade,
        foreign key (receiver_id) references users(id)
            on delete cascade
            on update cascade,
);

create index idx_friend_requests_receiver on friend_requests(receiver_id, sender_id);
