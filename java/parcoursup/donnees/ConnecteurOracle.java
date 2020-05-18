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
import oracle.jdbc.OracleDriver;
import oracle.jdbc.pool.OracleDataSource;
import parcoursup.exceptions.AccesDonneesException;

public class ConnecteurOracle implements java.lang.AutoCloseable {

    public static final int SPARSE_DATA_TEST_MODE = 0;
    
    public static final String BULLETINS_TABLE = "I_BUL_SCO";
    public static final String FORMATIONS_TABLE = "SP_G_TRI_AFF";
    public static final String FOR_INSCRIPTIONS_TABLE = "G_TRI_INS";
    public static final String RECRUTEMENTS_TABLE = "A_REC_GRP";
    public static final String ADMISSION_TABLE = "A_ADM";
    public static final String INSCRIPTIONS_TABLE = "I_INS";
    public static final String SITUATION_VOEU_TABLE = "A_SIT_VOE";
    public static final String FOR_TYPE_TABLE = "G_FOR" ;
    public static final String FILIERES_TABLE = "G_FIL" ;
    public static final String NOTES_BAC_TABLE = "I_CAN_EPR_BAC" ;
    public static final String MATIERES_BAC_TABLE = "I_EPR_BAC" ;
    public static final String CANDIDATS_TABLE = "G_CAN" ;
    public static final String MATIERES_TABLE = "I_MAT" ;
    public static final String CLASSEMENTS_TABLE = "C_CAN_GRP";
    public static final String TYPES_MACRO_TABLE = "G_PRO_NEW";
    public static final String ETABLISSEMENTS_TABLE = "G_ETA";

    /* connexion à la base de données */
    private final Connection conn;

    /* spécifie si la connexion doit être close en même temps que l'objet */
    private final boolean cleanupOnClose;

    public ConnecteurOracle(Connection connection) throws AccesDonneesException {
        if (connection == null) {
            throw new AccesDonneesException("Impossible de créer un ConnecteurOracle à partir d'une connexion ull");
        }
        cleanupOnClose = false;
        this.conn = connection;
    }

    public ConnecteurOracle(String url, String user, String password) throws AccesDonneesException {
        cleanupOnClose = true;

        try {
            if (url == null || url.isEmpty()) {
                OracleDriver ora = new OracleDriver();
                conn = ora.defaultConnection();
            } else {
                OracleDataSource ods = new OracleDataSource();
                ods.setURL(url);
                ods.setUser(user);
                ods.setPassword(password);
                conn = ods.getConnection();
            }
        } catch (SQLException ex) {
            throw new AccesDonneesException("Echec de création du connecteur", ex);
        }
    }

    public Connection connection() {
        return conn;
    }

    @Override
    public void close() throws SQLException {
        if (cleanupOnClose && conn != null) {
            conn.close();
        }
    }

}
