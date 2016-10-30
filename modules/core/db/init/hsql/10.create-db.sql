-- begin NE_CUSTOMER
create table NE_CUSTOMER (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    ADDRESS_CITY varchar(255),
    ADDRESS_COUNTRY varchar(255),
    --
    NAME varchar(255),
    STATUS integer,
    USER_ID varchar(36),
    --
    primary key (ID)
)^
-- end NE_CUSTOMER
-- begin NE_ORDER
create table NE_ORDER (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255),
    NUM integer,
    CUSTOMER_ID varchar(36),
    --
    primary key (ID)
)^
-- end NE_ORDER
-- begin NE_TEST
create table NE_TEST (
    ID varchar(36) not null,
    VERSION integer not null,
    CREATE_TS timestamp,
    CREATED_BY varchar(50),
    UPDATE_TS timestamp,
    UPDATED_BY varchar(50),
    DELETE_TS timestamp,
    DELETED_BY varchar(50),
    --
    NAME varchar(255),
    --
    primary key (ID)
)^
-- end NE_TEST
