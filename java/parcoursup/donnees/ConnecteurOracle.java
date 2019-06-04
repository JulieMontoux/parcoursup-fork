/* Copyright 2019 © Ministère de l'Enseignement Supérieur, de la Recherche et de l'Innovation,
    Hugo Gimbert (hugo.gimbert@enseignementsup.gouv.fr) 

    This file is part of Algorithmes-de-parcoursup.

    Algorithmes-de-parcoursup is free software: you can redistribute it and/or modify
    it under the terms of the Affero GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Algorithmes-de-parcoursup is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    Affero GNU General Public License for more details.

    You should have received a copy of the Affero GNU General Public License
    along with Algorithmes-de-parcoursup.  If not, see <http://www.gnu.org/licenses/>.

 */
package parcoursup.donnees;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.jdbc.OracleDriver;
import oracle.jdbc.pool.OracleDataSource;

public class ConnecteurOracle implements java.lang.AutoCloseable {

    /* connexion à la base de données */
    private final Connection conn;
    
    /* spécifie si la connexion doit être close en même temps que l'objet */    
    private final boolean cleanupOnClose;
    
    public ConnecteurOracle(Connection connection) {
        if(connection == null) {
            throw new RuntimeException("Impossible de créer un ConnecteurOracle à partir d'une connexion ull");
        }
        cleanupOnClose = false;
        this.conn = connection;
    }
    
    public ConnecteurOracle(String url, String user, String password, boolean useTNSNames) throws SQLException {
        cleanupOnClose = true;
        
        if (url == null || url.isEmpty()) {
            OracleDriver ora = new OracleDriver();
            conn = ora.defaultConnection();

        } else if (useTNSNames) {
            
            /* utilisé pour renseigner le chemin vers le fichier de config  tnsnames.ora
            "When using TNSNames with the JDBC Thin driver,
            you must set the oracle.net.tns_admin property
            to the directory that contains your tnsnames.ora file."        
             */
            String TNSAlias = url;
            String TNS_ADMIN = System.getenv("TNS_ADMIN");
            if (TNS_ADMIN == null) {
                TNS_ADMIN = "/Applications/instantclient_12_2/";
                //throw new RuntimeException("La variable d'environnement TNS_ADMIN n'est pas positionnée.");
            }
            LOGGER.log(Level.INFO, "Connexion \u00e0 la base Oracle en utilisant les param\u00e8tres de connexion du dossier TNS {0}", TNS_ADMIN);
            System.setProperty("oracle.net.tns_admin", TNS_ADMIN);

            OracleDataSource ods = new OracleDataSource();

            ods.setURL("jdbc:oracle:thin:@" + TNSAlias);
            ods.setUser(user);
            ods.setPassword(password);

            conn = ods.getConnection();

        } else {
            OracleDataSource ods = new OracleDataSource();
            ods.setURL(url);
            ods.setUser(user);
            ods.setPassword(password);
            conn = ods.getConnection();
        }
    }
    
    public Connection connection() {
        return conn;
    }
    
    @Override
    public void close() throws Exception {
        if (cleanupOnClose && conn != null) {
            conn.close();
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ConnecteurOracle.class.getSimpleName());

}
