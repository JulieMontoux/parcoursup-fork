CREATE USER bacasable IDENTIFIED BY parcoursup123;

GRANT CONNECT TO bacasable;

GRANT CREATE SESSION TO bacasable;

GRANT CREATE TABLE TO bacasable;

ALTER USER bacasable QUOTA UNLIMITED ON USERS;

CONNECT bacasable/parcoursup123@localhost:1521/XEPDB1;

@/db-setup/create-schema.sql

quit;
/
