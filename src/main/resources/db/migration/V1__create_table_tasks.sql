create table account
(
    created_time datetime                              default CURRENT_TIMESTAMP,
    phone_number char(15)                              null,
    employee_id  char(12)                              null,
    id           binary(16)                            not null primary key,
    email        varchar(255)                          not null unique,
    password     varchar(255)                          not null,
    username     varchar(255)                          not null unique,
    role         enum ('LIBRARIAN', 'BORROWER')        not null default 'BORROWER',
    status       enum ('ACTIVE', 'BANNED', 'INACTIVE') not null default 'ACTIVE'
);

create table book_copy_seq
(
    next_val bigint null
);

create table book_seq
(
    next_val bigint null
);

create table category
(
    id   bigint not null primary key,
    name varchar(50) null
);

create table book
(
    category_id bigint       not null,
    id          bigint       not null primary key,
    authors     json         null,
    description text         null,
    title       varchar(100) null,
    versions    json         null,
    constraint fk_book_category
        foreign key (category_id) references category (id)
);

create table category_seq
(
    next_val bigint null
);

create table publisher
(
    id   bigint       not null primary key,
    name varchar(100) not null unique
);

create table book_copy
(
    status       enum('AVAILABLE', 'RESERVED', 'LOST') default 'AVAILABLE',
    version      int     default 1,
    year_publish date    not null,
    id           bigint  not null primary key,
    publisher_id bigint  not null,
    constraint fk_bookcopy_publisher
        foreign key (publisher_id) references publisher (id)
);

create table checkout
(
    is_deleted  bit         default 0,
    book_id     bigint       not null,
    end_time    datetime  default CURRENT_TIMESTAMP,
    pickup_at   datetime  null,
    start_time  datetime  default CURRENT_TIMESTAMP,
    borrower_id binary(16)   not null,
    id          varchar(255) not null
        primary key,
    constraint fk_checkout_book
        foreign key (book_id) references book_copy (id),
    constraint fk_checkout_borrower
        foreign key (borrower_id) references account (id)
);

create table publisher_seq
(
    next_val bigint null
);

create table reservation
(
    is_deleted  bit          default 0,
    book_id     bigint       not null,
    end_time    datetime  default CURRENT_TIMESTAMP,
    pickup_at   datetime  null,
    start_time  datetime  default CURRENT_TIMESTAMP,
    borrower_id binary(16)   not null,
    id          varchar(255) not null primary key,
    constraint fk_reservation_borrower
        foreign key (borrower_id) references account (id),
    constraint fk_reservation_book
        foreign key (book_id) references book_copy (id)
);

