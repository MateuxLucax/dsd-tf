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

-- read only
create table friend_request_status (
        id integer primary key,
        description text
);
insert into friend_request_status values (1, "Pending"), (2, "Accepted"), (3, "Rejected");

drop table if exists friend_requests;

-- user X can't send request to Y if there's already a pending request from Y to X
-- but that's application logic (just a reminder for later)

create table friend_requests (
        sender_id   integer,
        receiver_id integer,
        status      integer,
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

        foreign key (status) references friend_request_status(id)
            on delete no action
            on update no action
);

create index idx_friend_requests_receiver on friend_requests(receiver_id, sender_id);
