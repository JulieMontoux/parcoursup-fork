#!/bin/bash

echo "Démarrage du serveur de base de données..."

/etc/init.d/oracle-xe-18c start

echo "En attente de la fin des opérations de démarrage de la base de données..."

while ! lsnrctl services | grep 'xepdb1' &> /dev/null
do
    echo "La base de données n'est pas encore prête..."
    sleep 1
done

sleep 2

echo "Base de données prête."

bacasable_user_exists=$(sqlplus -S sys/admin123@XEPDB1 as sysdba @/db-setup/check-if-bacasable-user-exists.sql)

if [ $bacasable_user_exists == 1 ]; then
    echo "L'utilisateur 'bacasable' existe déjà."
else
    echo "Création de l'utilisateur 'bacasable' et du schéma qui lui est associé."
    sqlplus -S sys/admin123@XEPDB1 as sysdba @/db-setup/create-bacasable-user-schema-tables.sql
fi


tests_user_exists=$(sqlplus -S sys/admin123@XEPDB1 as sysdba @/db-setup/check-if-tests-user-exists.sql)

if [ $tests_user_exists == 1 ]; then
    echo "L'utilisateur 'tests' existe déjà."
else
    echo "Création de l'utilisateur 'tests' et du schéma qui lui est associé."
    sqlplus -S sys/admin123@XEPDB1 as sysdba @/db-setup/create-tests-user-schema-tables.sql
fi

sqlplus -S sys/admin123@XEPDB1 as sysdba @/db-setup/open-port-8080.sql

tail -f $ORACLE_BASE/diag/rdbms/*/*/trace/alert*.log

