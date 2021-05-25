SET PAGESIZE 0
SET HEADING OFF
select ltrim(count(*)) from dba_users where username='TESTS';
exit;
