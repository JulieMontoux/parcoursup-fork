
CREATE USER tests IDENTIFIED BY parcoursup123;

GRANT CONNECT TO tests;

GRANT CREATE SESSION TO tests;

GRANT CREATE TABLE TO tests;

ALTER USER tests QUOTA UNLIMITED ON USERS;

CONNECT tests/parcoursup123@localhost:1521/XEPDB1;

@/db-setup/create-tables.sql

quit;
/
