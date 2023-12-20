create table account
(
    created_at   datetime                              default CURRENT_TIMESTAMP,
    phone_number char(15)                              null,
    employee_id  char(12)                              null,
    id           binary(16)                            not null primary key,
    email        varchar(255)                          not null unique,
    password     varchar(255)                          not null,
    username     varchar(255)                          not null unique,
    role         enum ('LIBRARIAN', 'BORROWER')        not null default 'BORROWER',
    status       enum ('ACTIVE', 'BANNED', 'INACTIVE') not null default 'ACTIVE'
);

create table category
(
    id   bigint AUTO_INCREMENT primary key,
    name varchar(50) null
);

create table book
(
    category_id bigint      not null,
    id          bigint AUTO_INCREMENT primary key,
    authors     json        not null,
    description text        not null,
    title       varchar(50) not null,
    created_at  datetime default now(),
    updated_at  datetime default now() on update CURRENT_TIMESTAMP,
    constraint fk_book_category
        foreign key (category_id) references category (id) on update cascade on delete cascade
);


create table publisher
(
    id   bigint AUTO_INCREMENT primary key,
    name varchar(100) not null unique
);

create table book_copy
(
    version      int                                    default 1,
    year_publish date   not null,
    id           bigint AUTO_INCREMENT primary key,
    publisher_id bigint not null,
    quantity     int                                    default 1,
    book_data bigint not null,
    constraint fk_bookcopy_publisher
        foreign key (publisher_id) references publisher (id) on delete cascade on update cascade,
    constraint fk_bookcopy_book
        foreign key (book_data) references book (id) on delete cascade,
    constraint unique_version_bookdata unique (version, book_data)
);


create table checkout
(
    is_deleted  bit      default 0,
    book_id     bigint       not null,
    end_time    datetime default CURRENT_TIMESTAMP,
    pickup_at   datetime     null,
    start_time  datetime default CURRENT_TIMESTAMP,
    borrower_id binary(16)   not null,
    id          varchar(255) not null
        primary key,
    constraint fk_checkout_book
        foreign key (book_id) references book_copy (id),
    constraint fk_checkout_borrower
        foreign key (borrower_id) references account (id) on delete cascade
);

create table reservation
(
    is_deleted  bit      default 0,
    book_id     bigint       not null,
    end_time    datetime default CURRENT_TIMESTAMP,
    pickup_at   datetime     null,
    start_time  datetime default CURRENT_TIMESTAMP,
    borrower_id binary(16)   not null,
    id          varchar(255) not null primary key,
    constraint fk_reservation_borrower
        foreign key (borrower_id) references account (id)
            on delete cascade,
    constraint fk_reservation_book
        foreign key (book_id) references book_copy (id)
            on delete no action
            on update cascade
);

