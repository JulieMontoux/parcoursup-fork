--------------------------------------------------------
-- Ouverture du port 8080
-- Permettra par la suite aux clients de s'assurer que
-- la base de données est prête
-- (ex. : script wait-for-it.sh)
--------------------------------------------------------

begin
dbms_xdb_config.sethttpport(8080);
end;
/

exit;
