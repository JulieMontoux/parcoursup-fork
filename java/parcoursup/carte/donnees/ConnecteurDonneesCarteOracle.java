
/* Copyright 2019 © Ministère de l'Enseignement Supérieur, de la Recherche et de
l'Innovation,
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
package parcoursup.carte.donnees;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import parcoursup.carte.algo.Filiere;
import parcoursup.carte.algo.AlgoCarteEntree;
import parcoursup.carte.algo.AlgoCarteSortie;
import parcoursup.carte.algo.DomaineOnisep;
import parcoursup.carte.algo.Recommandation;
import parcoursup.exceptions.AccesDonneesException;
import parcoursup.donnees.ConnecteurOracle;
import static parcoursup.donnees.SQLStringsConstants.INSERT_INTO;
import static parcoursup.donnees.SQLStringsConstants.SELECT;
import static parcoursup.donnees.SQLStringsConstants.TRUNCATE_TABLE;
import static parcoursup.donnees.SQLStringsConstants.WHERE;
import parcoursup.exceptions.VerificationException;

public class ConnecteurDonneesCarteOracle implements ConnecteurDonneesCarte {

    public ConnecteurDonneesCarteOracle(
            String url,
            String user,
            String password) throws AccesDonneesException {
        co = new ConnecteurOracle(url, user, password);
    }

    public ConnecteurDonneesCarteOracle(Connection connection) throws AccesDonneesException {
        co = new ConnecteurOracle(connection);
    }

    private final ConnecteurOracle co;

    private Connection conn() {
        return co.connection();
    }

    /**
     * @return récupération des données de la carte
     * @throws AccesDonneesException
     */
    @Override
    public AlgoCarteEntree recupererDonneesCarte() throws AccesDonneesException {

        /* récupération de la liste des filières de Parcoursup */
        AlgoCarteEntree entree = new AlgoCarteEntree();

        try {
            recuperationDesFilieres(entree);

            recuperationVoeuxCommuns();

            recuperationDomainesOnisep(entree);

        } catch (SQLException ex) {
            throw new AccesDonneesException("Erreur SQL du chargement des données", ex);
        } catch (VerificationException ex) {
            throw new AccesDonneesException("Erreur d'integrité des données", ex);
        }

        return entree;
    }

    static final String TABLE_FILIERES_SIMILAIRE = "G_FIL_SIM";
    static final String TABLE_MOTS_CLES = "G_FIL_KEYS";
    static final String TABLE_STATS_BAC = "G_REC_BAC";
    static final String TABLE_STATS_TAUX_ACCES = "G_REC_TAU_ACC";

    /* export des données */
    @Override
    public void exporterDonneesCarte(AlgoCarteSortie sortie) throws AccesDonneesException {

        try {

            conn().setAutoCommit(false);

            /*
            g_fil_sim  :  table des filères similaires
             */
            LOGGER.info("Préparation de la table " + TABLE_FILIERES_SIMILAIRE + " avant export");
            try (Statement ps = conn().createStatement()) {
                ps.execute(TRUNCATE_TABLE + TABLE_FILIERES_SIMILAIRE);
            }

            LOGGER.log(Level.INFO, "Export de {0} recommandations dans la table " + TABLE_FILIERES_SIMILAIRE, sortie.recommandations.size());
            try (PreparedStatement ps = conn().prepareStatement(
                    INSERT_INTO + TABLE_FILIERES_SIMILAIRE
                    + " (G_FL_COD_ORI,G_FL_COD_SIM,G_FS_SCO,G_FS_PRO_SEM,G_FS_PCT_VOE_COM)"
                    + " VALUES (?,?,?,?,?)")) {

                for (Entry<Recommandation, Integer> entry : sortie.recommandations.entrySet()) {
                    Recommandation reco = entry.getKey();
                    Integer score = entry.getValue();
                    ps.setInt(1, reco.filiere1.cle);
                    ps.setInt(2, reco.filiere2.cle);
                    ps.setInt(3, score);
                    ps.setInt(4, reco.proximiteSemantique);
                    ps.setInt(5, reco.pctVoeuxCommuns);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn().commit();

            /*
            g_fil_keys: table liant une filière à une liste de mots clés rentrée sous la forme d'une chaine de caractères séparés par des espaces.
             */
            LOGGER.info("Préparation de la table " + TABLE_MOTS_CLES + " avant export");
            try (Statement ps = conn().createStatement()) {
                ps.execute(TRUNCATE_TABLE + TABLE_MOTS_CLES);
            }

            LOGGER.log(Level.INFO, "Export de {0} recommandations dans la table " + TABLE_MOTS_CLES, sortie.recommandations.size());
            try (PreparedStatement ps = conn().prepareStatement(
                    INSERT_INTO + TABLE_MOTS_CLES
                    + " (G_FL_COD,G_FL_KEYS)"
                    + " VALUES (?,?)")) {

                for (Filiere filiere : sortie.filieres) {
                    ps.setInt(1, filiere.cle);
                    ps.setString(2, filiere.getMotsClesRechercheCarte());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn().commit();

            LOGGER.info("Export des statistiques des admissions bacheliers l'an dernier");
            try (Statement ps = conn().createStatement()) {
                ps.execute(TRUNCATE_TABLE + TABLE_STATS_BAC);
            }

            try (Statement ps = conn().createStatement()) {
                ps.execute(
                        INSERT_INTO + TABLE_STATS_BAC
                        + " (g_ta_cod,G_RB_GEN,G_RB_TEC,G_RB_PRO,G_RB_TOT) "
                        + "(SELECT adm.g_ta_cod g_ta_cod,"
                        + "count(case when (tbac.I_TC_COD = '1') then 1 end) as G_RB_GEN,"
                        + "count(case when (tbac.I_TC_COD = '2') then 1 end) as G_RB_TEC,"
                        + "count(case when (tbac.I_TC_COD = '3') then 1 end) as G_RB_PRO,"
                        + "count(*) as G_RB_TOT "
                        + "FROM A_ADM@DBL_ARCH adm,SP_G_TRI_AFF@DBL_ARCH,G_CAN@DBL_ARCH can,I_CLA@DBL_ARCH tbac,A_SIT_VOE@DBL_ARCH asv, G_TRI_AFF aff"
                        + " WHERE  "
                        + "adm.G_CN_COD=can.G_CN_COD "
                        + "AND can.I_CL_COD_BAC=tbac.I_CL_COD "
                        + "AND adm.A_SV_COD=asv.A_SV_COD "
                        + "AND asv.A_SV_FLG_AFF=1  "
                        + "AND SP_G_TRI_AFF.g_ta_cod=adm.g_ta_cod "
                        + "AND adm.g_ta_cod=aff.g_ta_cod "
                        + "GROUP BY adm.g_ta_cod,adm.G_TI_COD,SP_G_TRI_AFF.G_FL_COD_AFF)");
            }
            conn().commit();

            LOGGER.info("Export des taux d'accès de l'an dernier");
            try (Statement ps = conn().createStatement()) {
                ps.execute(TRUNCATE_TABLE + TABLE_STATS_TAUX_ACCES);
            }

            try (Statement ps = conn().createStatement()) {
                ps.execute(INSERT_INTO + TABLE_STATS_TAUX_ACCES
                        + " (g_ta_cod,TAUX_ACCES) "
                        + "SELECT * FROM "
                        + " (WITH rang_der_appele AS  "
                        + "( SELECT adm.g_ta_cod g_ta_cod,  cla.C_GP_COD c_gp_cod,  MAX(cla.C_CG_ORD_APP) rang  "
                        + "FROM                "
                        + "A_ADM@DBL_ARCH adm,         "
                        + "C_CAN_GRP@DBL_ARCH cla,    "
                        + "I_INS@DBL_ARCH ins        "
                        + "WHERE               "
                        + "adm.G_CN_COD=cla.G_CN_COD    "
                        + "AND ins.G_CN_COD=cla.G_CN_COD     "
                        + "AND ins.G_TI_COD=adm.G_TI_COD     "
                        + "AND ins.I_IS_VAL=1               "
                        + "AND adm.A_TA_COD = 1           "
                        + "AND adm.C_GP_COD = cla.C_GP_COD    "
                        + "AND EXTRACT(MONTH FROM adm.A_AM_DAT) <= 08 "
                        + "AND cla.C_CG_ORD_APP is not null GROUP BY          "
                        + "adm.g_ta_cod,   cla.c_gp_cod), nb_can_au_dessous_barre  "
                        + "AS "
                        + "( SELECT           "
                        + "COuNT(DISTINCT voe.G_CN_COD) nb, voe.g_ta_cod      "
                        + "FROM        "
                        + "A_VOE@DBL_ARCH voe,   "
                        + "C_CAN_GRP@DBL_ARCH cla,   "
                        + "rang_der_appele,  "
                        + "C_GRP@DBL_ARCH cg  "
                        + "WHERE    "
                        + "voe.a_sv_cod > -90   "
                        + "AND voe.G_CN_COD=cla.G_CN_COD  "
                        + "AND cla.C_GP_COD=rang_der_appele.C_GP_COD   "
                        + "AND voe.g_ta_cod = rang_der_appele.g_ta_cod  "
                        + "AND cg.C_GP_COD=rang_der_appele.C_GP_COD   "
                        + "AND ((cla.C_CG_ORD_APP is not null  "
                        + "AND cla.C_CG_ORD_APP <= rang_der_appele.rang))  "
                        + "GROUP BY          "
                        + "voe.g_ta_cod), "
                        + "nb_candidats AS  "
                        + "(SELECT  "
                        + "voe.g_ta_cod, "
                        + "COUNT(DISTINCT G_CN_COD) nb     "
                        + "FROM A_VOE@DBL_ARCH voe     "
                        + "WHERE    voe.a_sv_cod > -90    "
                        + "GROUP BY voe.g_ta_cod)  "
                        + "SELECT     aff.g_ta_cod g_ta_cod,  "
                        + "ROUND(100 * nb_can_au_dessous_barre.nb / nb_candidats.nb)          "
                        + "taux_adm   "
                        + "FROM  SP_G_TRI_AFF@DBL_ARCH aff     "
                        + "LEFT JOIN nb_candidats     "
                        + "ON aff.g_ta_cod=nb_candidats.g_ta_cod     "
                        + "LEFT JOIN nb_can_au_dessous_barre    "
                        + "ON nb_candidats.g_ta_cod = nb_can_au_dessous_barre.g_ta_cod    "
                        + "WHERE NVL(nb_candidats.nb,0) > 0 "
                        + "AND (NVL(nb_can_au_dessous_barre.nb,0) > 0) ) "
                );
            }
            conn().commit();

        } catch (SQLException ex) {
            throw new AccesDonneesException("Echec de l'exportation des donnees de la carte", ex);
        }

    }

    void recuperationDesFilieres(AlgoCarteEntree entree) throws SQLException, VerificationException {
        try (Statement stmt = conn().createStatement()) {

            /* récupère la liste des candidats depuis la base de données */
            LOGGER.info("Récupération des filières");
            stmt.setFetchSize(1_000_000);
            String sql = SELECT
                    //id du groupe de classement
                    + "fil.G_FL_LIB, "
                    + "fil.G_FL_SIG, "
                    + "fil.G_FL_FLG_APP, "
                    + "fil.G_FL_COD_FI "
                    + " FROM G_FIL fil";

            LOGGER.info(sql);

            try (ResultSet result = stmt.executeQuery(sql)) {
                while (result.next()) {
                    String gFlLib = result.getString(1);
                    String gFlSig = result.getString(2);
                    boolean gFlFlgApp = result.getBoolean(3);
                    int gFlCodFil = result.getInt(4);

                    entree.filieres.add(Filiere.creerFiliere(gFlLib, gFlSig, gFlCodFil, gFlFlgApp));
                }
            }
        }
    }

    void recuperationVoeuxCommuns() throws SQLException, VerificationException {
        /* récupération des voeux communs à l'année n-1 */
        try (Statement stmt = conn().createStatement()) {

            /* récupère la liste des candidats ayant des voeux confirmés à l'année n-1 */
            LOGGER.info("Récupération des voeux communs pour chaque filiere");
            stmt.setFetchSize(1_000_000);
            String sql = ""
                    + "WITH fil_by_cand as"
                    + " (SELECT DISTINCT aff.G_FL_COD_AFF g_fl_cod,  voeu.G_CN_COD"
                    //id du groupe de classement
                    + " FROM A_VOE@dbl_arch voeu, "
                    + "I_INS@dbl_arch ins, "
                    + "A_REC_GRP@dbl_arch  arecgrp, "
                    + "SP_G_TRI_AFF@dbl_arch  aff"
                    + WHERE
                    + " voeu.g_ta_cod=aff.g_ta_cod"
                    + " AND voeu.g_ta_cod = arecgrp.g_ta_cod"
                    + " AND arecgrp.g_ti_cod = ins.g_ti_cod"
                    + " AND voeu.g_cn_cod = ins.g_cn_cod"
                    + " AND NVL(ins.i_is_val,0) = 1"
                    + " )"
                    + " "
                    + " SELECT fil1.g_fl_cod, fil2.g_fl_cod, COUNT(DISTINCT fil1.G_CN_COD)"
                    + " from fil_by_cand fil1, fil_by_cand fil2"
                    + " where fil1.g_cn_cod=fil2.g_cn_cod "
                    + " group by fil1.g_fl_cod, fil2.g_fl_cod";

            LOGGER.info(sql);

            try (ResultSet result = stmt.executeQuery(sql)) {
                while (result.next()) {
                    int gFlCod1 = result.getInt(1);
                    int gFlCod2 = result.getInt(2);
                    int nbCandidats = result.getInt(3);
                    if (nbCandidats > Recommandation.NB_CANDIDATS_COMMUNS_MIN) {
                        Filiere filiere1 = Filiere.getFiliere(gFlCod1);
                        Filiere filiere2 = Filiere.getFiliere(gFlCod2);
                        if (filiere1 != null && filiere2 != null) {
                            filiere1.ajouterFiliereAvecvoeuxCommuns(filiere2, nbCandidats);
                        }
                    }
                }
            }
        }
    }

    void recuperationDomainesOnisep(AlgoCarteEntree entree) throws AccesDonneesException, VerificationException, SQLException {
        /* récupération des domaines ONISEP */
        try (Statement stmt = conn().createStatement()) {

            /* récupère la liste des candidats ayant des voeux confirmés à l'année n-1 */
            LOGGER.info("Récupération des domaines Onisep");
            stmt.setFetchSize(1_000_000);
            String sql = "SELECT g_ko_cod,g_ko_lib,NVL(g_ko_cod_niv_sup," + DomaineOnisep.NO_DKEY_SUP + ")"
                    + " FROM g_key_oni";

            LOGGER.info(sql);
            try (ResultSet result = stmt.executeQuery(sql)) {
                while (result.next()) {

                    int gKoCod = result.getInt(1);
                    String gKoLib = result.getString(2);
                    int gKoCodNivSup = result.getInt(3);

                    DomaineOnisep domaine = DomaineOnisep.creerDomaineOnisep(gKoCod, gKoLib, gKoCodNivSup);
                    entree.domaines.add(domaine);
                }
            } catch (SQLException ex) {
                throw new AccesDonneesException("Erreur de chargement des données", ex);
            }
        }

        /* lien entre filières et domaine */
        try (Statement stmt = conn().createStatement()) {

            /* récupère la liste des candidats ayant des voeux confirmés à l'année n-1 */
            LOGGER.info("Récupération des domaines de base pour chaque filière");
            stmt.setFetchSize(1_000_000);
            String sql = "SELECT g_fil.G_FL_COD, g_ide_key_oni.G_KO_COD "
                    + " FROM g_fil,g_fil_id_oni,g_ide_key_oni"
                    + WHERE
                    //correspondance avec ou sans apprentissage
                    + " (g_fil.g_fl_cod=g_fil_id_oni.g_fl_cod OR g_fil.g_fl_cod_fi=g_fil_id_oni.g_fl_cod)"
                    + " AND g_fil_id_oni.g_id_oni = g_ide_key_oni.g_id_oni";

            LOGGER.info(sql);
            try (ResultSet result = stmt.executeQuery(sql)) {
                while (result.next()) {
                    int gFlCod = result.getInt(1);
                    int gKocod = result.getInt(2);
                    Filiere filiere = Filiere.getFiliere(gFlCod);
                    if (filiere != null) {
                        DomaineOnisep domaine = DomaineOnisep.getDomaine(gKocod);
                        if (domaine != null) {
                            filiere.ajouterDomaine(domaine);
                        }
                    }
                }
            }
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ConnecteurDonneesCarteOracle.class.getSimpleName());

}
