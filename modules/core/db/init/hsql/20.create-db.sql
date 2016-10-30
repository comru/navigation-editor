-- begin NE_CUSTOMER
alter table NE_CUSTOMER add constraint FK_NE_CUSTOMER_USER foreign key (USER_ID) references SEC_USER(ID)^
create index IDX_NE_CUSTOMER_USER on NE_CUSTOMER (USER_ID)^
-- end NE_CUSTOMER
-- begin NE_ORDER
alter table NE_ORDER add constraint FK_NE_ORDER_CUSTOMER foreign key (CUSTOMER_ID) references NE_CUSTOMER(ID)^
create index IDX_NE_ORDER_CUSTOMER on NE_ORDER (CUSTOMER_ID)^
-- end NE_ORDER
