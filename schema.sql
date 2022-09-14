create table if not exists users (
        id          integer primary key autoincrement,
        username    text    unique not null,
        avatar_path text    null,
        created_at  timestamp default current_timestamp not null,
        updated_at  timestamp null
);


