drop table if exists users;

create table users (
        id          integer not null primary key autoincrement,
        username    text    unique not null,
        fullname    text    not null,
        password    text    not null,
        avatar_path text    null,
        created_at  timestamp default current_timestamp not null,
        updated_at  timestamp null,

        check (updated_at is null or updated_at > created_at)
);

drop table if exists friends;

create table friends (
        your_id    integer not null,
        their_id   integer not null,
        created_at timestamp default current_timestamp not null,

        primary key (your_id, their_id),

        foreign key (your_id) references users(id),
        foreign key (their_id) references users(id)
);

drop table if exists friend_requests;

-- a friend request is pending by existence
-- once the request is accepted, the users become friends and the request is deleted
-- or once the request is rejected, the request is just deleted without the users becoming friends

create table friend_requests (
        sender_id   integer not null,
        receiver_id integer not null,
        created_at  timestamp default current_timestamp not null,

        primary key (sender_id, receiver_id),

        foreign key (sender_id) references users(id),
        foreign key (receiver_id) references users(id)
);

create index idx_friend_requests_receiver on friend_requests(receiver_id, sender_id);

drop table if exists messages;

create table messages (
    sender_id integer not null,
    receiver_id integer not null,
    id integer not null primary key autoincrement,
    sent_at timestamp not null default current_timestamp,

    text_contents text null,
    file_reference text null,

    /* one and only one is present */
    check (text_contents is not null or file_reference is not null),
    check (text_contents is null or file_reference is null),

    foreign key (sender_id) references users (id),
    foreign key (receiver_id) references users (id)
);

