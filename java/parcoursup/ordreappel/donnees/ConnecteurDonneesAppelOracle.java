
/* Copyright 2018 © Ministère de l'Enseignement Supérieur, de la Recherche et de
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
package parcoursup.ordreappel.donnees;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.joining;
import parcoursup.donnees.ConnecteurOracle;
import parcoursup.ordreappel.algo.AlgoOrdreAppelEntree;
import parcoursup.ordreappel.algo.AlgoOrdreAppelSortie;
import parcoursup.ordreappel.algo.CandidatClasse;
import parcoursup.ordreappel.algo.GroupeClassement;
import parcoursup.ordreappel.algo.OrdreAppel;
import parcoursup.ordreappel.algo.VoeuClasse;

public class ConnecteurDonneesAppelOracle implements ConnecteurDonneesAppel, java.lang.AutoCloseable {

     
    public ConnecteurDonneesAppelOracle(
            String url,
            String user,
            String password,
            boolean useTNSNames) throws SQLException {
        co = new ConnecteurOracle(url, user, password, false);
    }

    public ConnecteurDonneesAppelOracle(Connection connection) {
        co = new ConnecteurOracle(connection);
    }

    private final ConnecteurOracle co;

    private Connection conn() {
        return co.connection();
    }


    /* chargement des classements depuis la base de données */
    @Override
    public AlgoOrdreAppelEntree recupererDonneesOrdreAppel() throws SQLException {
        return recupererDonnees(null);
    }

    @Override
    public GroupeClassement recupererDonneesOrdreAppelGroupe(int GroupeUniqueCGPCOD) throws SQLException {
        AlgoOrdreAppelEntree entree = recupererDonnees(GroupeUniqueCGPCOD);
        for (GroupeClassement groupe : entree.groupesClassements) {
            if (groupe.C_GP_COD == GroupeUniqueCGPCOD) {
                return groupe;
            }
        }
        throw new RuntimeException("Pas de groupe avec le C_GP_COD " + GroupeUniqueCGPCOD);
    }

    /* l'argument peut être null, dans ce cas toutes les données d'appel sont récupérées */
    private AlgoOrdreAppelEntree recupererDonnees(Integer GroupeUniqueCGPCOD) throws SQLException {
        Map<Integer, GroupeClassement> groupesClassements
                = new HashMap<>();

        try (Statement stmt = conn().createStatement()) {

            /* récupère la liste des groupes et les taux minimum de boursiers
            et de candidats du secteur depuis la base de données */
            LOGGER.info("Récupération des groupes");
            stmt.setFetchSize(1_000_000);

            String sql
                    = "SELECT DISTINCT "
                    //groupe de classement
                    + "rg.C_GP_COD,"
                    //flag taux de boursier 0/null = non 1=oui
                    + "NVL(r.A_RC_FLG_TAU_BRS,0),"
                    //taux min de boursier
                    + "NVL(r.A_RC_TAU_BRS_REC,0),"
                    //flag taux max de hors-secteur 0/null = non 1=oui
                    + "NVL(A_RC_FLG_TAU_NON_RES,0),"
                    //taux max de hors-secteur
                    + "NVL(r.A_RC_TAU_NON_RES_REC, 100)"
                    + " FROM a_rec_grp rg,"
                    + " a_rec r,"
                    + " g_tri_ins ti,"
                    + " g_for fr,"
                    + " g_fil fl,"
                    + " c_grp g "
                    + " WHERE rg.g_ti_cod=r.g_ti_cod "
                    + " AND   rg.g_ta_cod=r.g_ta_cod "
                    + " AND   rg.g_ti_cod=ti.g_ti_cod "
                    + " AND   ti.g_fr_cod_ins=fr.g_fr_cod "
                    + " AND   ti.g_fl_cod_ins=fl.g_fl_cod "
                    + " AND   rg.c_gp_cod=g.c_gp_cod "
                    + " AND   g.c_gp_eta_cla=2 " //classement finalisé
                    + " AND   ti.g_ti_flg_par_eff > 0 "//paramétrage effectué
                    + " AND   ti.g_ti_eta_cla=2 " //classement finalisé
                    + " AND   NVL(g.c_gp_flg_pas_cla, 0)!=1";

            if (GroupeUniqueCGPCOD != null) {
                sql += " AND rg.C_GP_COD=" + GroupeUniqueCGPCOD;
            }

            LOGGER.info(sql);

            try (ResultSet result = stmt.executeQuery(sql)) {

                while (result.next()) {

                    int C_GP_COD = result.getInt(1);

                    if (groupesClassements.containsKey(C_GP_COD)) {
                        throw new RuntimeException("Groupe dupliqué " + C_GP_COD);
                    }

                    /* un ou deux taux selon le type de formation */
                    boolean tauxBoursierDisponible = result.getBoolean(2);
                    boolean tauxResDisponible = result.getBoolean(4);
                    int tauxMinBoursier = tauxBoursierDisponible ? result.getInt(3) : 0;
                    int tauxMaxNonResident = tauxResDisponible ? result.getInt(5) : 100;
                    int tauxMinResident = 100 - tauxMaxNonResident;

                    if (tauxMinBoursier < 0
                            || tauxMinBoursier > 100
                            || tauxMinResident < 0
                            || tauxMinResident > 100) {
                        throw new RuntimeException("Taux incohérents");
                    }

                    groupesClassements.put(C_GP_COD,
                            new GroupeClassement(
                                    C_GP_COD,
                                    tauxMinBoursier,
                                    tauxMinResident
                            ));

                }
            }
        }
        try (Statement stmt = conn().createStatement()) {

            Set<Integer> groupesManquants = new HashSet<>();

            /* récupère la liste des candidats depuis la base de données */
            LOGGER.info("Récupération des voeux");
            stmt.setFetchSize(1_000_000);
            String sql = "SELECT "
                    //id du groupe de classement
                    + "cg.C_GP_COD, "
                    //id du candidat
                    + "c.G_CN_COD, "
                    //le rang peut-être nul pour les formations qui ne classent pas
                    + "NVL(C_CG_RAN,0), "
                    //le candidat a-t'il déclaré être boursier? non 0 lycée 1 dusup 2
                    + "g_cn_brs, "
                    //cette déclaration a-t'elle été confirmée
                    //via les remontées de base SIECLE (1)
                    //ou directement par le chef d'établissement (2)
                    + "g_cn_flg_brs_cer,"
                    //le candidat est-il du secteur ou assimilé dans ce groupe?
                    + "I_IS_FLC_SEC "
                    + " FROM c_can_grp cg, c_grp gp, c_jur_adm ja, g_can c, i_ins i"
                    //groupe de classement
                    + " WHERE cg.c_gp_cod=gp.c_gp_cod"
                    //jury admission
                    + " AND   gp.c_ja_cod=ja.c_ja_cod"
                    //candidat classé 5 non-classé 4
                    + " AND   i_ip_cod=5"
                    //id candidat
                    + " AND   cg.g_cn_cod=c.g_cn_cod"
                    //dossier non-annulé (décès...)
                    + " AND   c.g_ic_cod >= 0"
                    //id candidat
                    + " AND   cg.g_cn_cod=i.g_cn_cod"
                    //formation inscription
                    + " AND   ja.g_ti_cod=i.g_ti_cod"
                    //seulement les formations qui classent
                    + " AND   NVL(gp.c_gp_flg_pas_cla, 0)!=1 ";

            if (GroupeUniqueCGPCOD != null) {
                sql += " AND cg.C_GP_COD=" + GroupeUniqueCGPCOD;
            }

            LOGGER.info(sql);

            try (ResultSet result = stmt.executeQuery(sql)) {

                /* Remarque: le rangAppel est à null / 0 pour celles des formations
            non-sélectives qui ne réalisent pas de classement. */
                while (result.next()) {

                    int C_GP_COD = result.getInt(1);
                    int G_CN_COD = result.getInt(2);
                    int rang = result.getInt(3);

                    boolean estDeclareBoursierLycee = (result.getInt(4) == 1);
                    int confirmationBoursier = result.getInt(5);
                    boolean estConsidereBoursier
                            = estDeclareBoursierLycee
                            && (confirmationBoursier == 1 || confirmationBoursier == 2);
                    boolean estConsidereDuSecteur = result.getBoolean(6);

                    if (!groupesClassements.containsKey(C_GP_COD)) {
                        //peut arriver si les classements ne sont pas encore remontés
                        groupesManquants.add(C_GP_COD);
                        continue;
                    }

                    GroupeClassement ga = groupesClassements.get(C_GP_COD);
                    ga.ajouterVoeu(
                            new VoeuClasse(
                                    G_CN_COD,
                                    rang,
                                    estConsidereBoursier,
                                    estConsidereDuSecteur)
                    );
                }

                result.close();
                stmt.close();

                if (!groupesManquants.isEmpty()) {
                    LOGGER.log(Level.SEVERE, "{0} groupes manquants.", groupesManquants.size());
                    String list = groupesManquants.stream()
                            .map(String::valueOf)
                            .collect(joining(",", "(", ")"));
                    LOGGER.severe(list);
                    //throw new RuntimeException(groupesManquants.size() + " groupes manquants.");
                }

                AlgoOrdreAppelEntree entree = new AlgoOrdreAppelEntree();
                entree.groupesClassements.addAll(groupesClassements.values());
                return entree;

            } catch (SQLException ex) {
                throw new RuntimeException("Erreur de chargement des données", ex);
            }

        }

    }
    
    /* exportation des classements vers la base de données */
    private void exporterOrdresAppel(Map<Integer, OrdreAppel> ordresAppel) throws SQLException {

        /*
        conn().setAutoCommit(true);

         pour optimiser le temps d'exportation,
            les ordres d'appel sont stockées dans une table temporaire J_ORD_APPEL_TMP
            avant la mise a jour de la table C_CAN_GRP 
        try {
            conn().createStatement().execute("DROP TABLE J_ORD_APPEL_TMP");
        } catch (SQLException e) {
            // peut arriver si la table n'existait pas 
        }

        conn().createStatement().execute(
                "CREATE GLOBAL TEMPORARY TABLE J_ORD_APPEL_TMP ("
                + "C_GP_COD NUMBER(6,0),"
                + "G_CN_COD NUMBER(8,0),"
                + "C_CG_ORD_APP NUMBER(6,0),"
                + "PRIMARY KEY (C_GP_COD,G_CN_COD)"
                + ") ON COMMIT PRESERVE ROWS"
        );
        */

        conn().setAutoCommit(false);

        try (PreparedStatement ps
                = conn().prepareStatement(
                        "INSERT INTO J_ORD_APPEL_TMP (C_GP_COD,G_CN_COD,C_CG_ORD_APP) VALUES (?,?,?)")) {
            int count = 0;
            for (Entry<Integer, OrdreAppel> paire
                    : ordresAppel.entrySet()) {

                Integer C_GP_COD = paire.getKey();
                OrdreAppel ordre = paire.getValue();

                for (CandidatClasse candidat : ordre.candidats) {
                    ps.setInt(1, C_GP_COD);
                    ps.setInt(2, candidat.G_CN_COD);
                    ps.setInt(3, candidat.rangAppel);
                    ps.addBatch();
                    if (++count % 500_000 == 0) {
                        LOGGER.log(Level.INFO, "Exportation des ordres d'appel des voeux {0} a {1}", new Object[]{count - 499_999, count});
                        ps.executeBatch();
                        ps.clearBatch();
                        LOGGER.info("Fait");
                    }
                }
            }
            ps.executeBatch();
        }
        //conn().commit();
        //conn().commit();

        LOGGER.info("Mise à jour de la table C_CAN_GRP");
        conn().createStatement().execute("UPDATE "
                + "(SELECT  a.C_CG_ORD_APP cible, b.C_CG_ORD_APP source FROM C_CAN_GRP a,"
                + "J_ORD_APPEL_TMP b WHERE a.G_CN_COD=b.G_CN_COD AND a.C_GP_COD=b.C_GP_COD)"
                + "SET cible=source");
    }

    @Override
    public void exporterDonneesOrdresAppel(AlgoOrdreAppelSortie donnees) {

        try {

            LOGGER.log(Level.INFO, "D\u00e9but de l''exportation des ordres d''appel de {0} groupes", donnees.ordresAppel.size());

            exporterOrdresAppel(donnees.ordresAppel);

            LOGGER.info("Fin de l'exportation");

        } catch (SQLException ex) {
            LOGGER.severe("Erreur d'exportation des données");
            throw new RuntimeException("Erreur d'exportation des données", ex);
        }
    }

    /* vérifie si un candidat est boursier */
    @Override
    public boolean estBoursier(int G_CN_COD) throws Exception {

        Boolean estConsidereBoursier = null;

        try (Statement stmt = conn().createStatement()) {

            String sql = "SELECT "
                    //le candidat a-t'il déclaré être boursier? non 0 lycée 1 dusup 2
                    + "g_cn_brs, "
                    //cette déclaration a-t'elle été confirmée 
                    //via les remontées de base SIECLE (1)
                    //ou directement par le chef d'établissement (2)
                    + "g_cn_flg_brs_cer,"
                    //le candidat est-il du secteur sur les candidats
                    //passés par ce groupe
                    + " FROM g_can c"
                    //groupe de classement
                    + " WHERE c.g_cn_cod=" + G_CN_COD;

            try (ResultSet result = stmt.executeQuery(sql)) {

                /* Remarque: le rangAppel est à null / 0 pour celles des formations 
                non-sélectives qui ne réalisent pas de classement. */
                while (result.next()) {

                    boolean estDeclareBoursierLycee = (result.getInt(1) == 1);
                    int confirmationBoursier = result.getInt(2);
                    estConsidereBoursier = estDeclareBoursierLycee
                            && (confirmationBoursier == 1 || confirmationBoursier == 2);
                }
            }
        }

        if (estConsidereBoursier == null) {
            throw new RuntimeException("Impossible de récupérer les informations du candidat");
        }

        return estConsidereBoursier;
    }

    /* vérifie si un candidat est considéré du secteur dans un groupe */
    @Override
    public boolean estDuSecteur(int G_CN_COD, int C_GP_COD) throws Exception {

        Boolean estConsidereDuSecteur = null;

        try (Statement stmt = conn().createStatement()) {

            String sql = "SELECT "
                    //le candidat est-il du secteur sur les candidats
                    //passés par ce groupe
                    + "I_IS_FLC_SEC "
                    + " FROM a_rec_grp ar, i_ins i"
                    //groupe de classement
                    + " WHERE ar.c_gp_cod=" + C_GP_COD
                    + " AND   ar.g_ti_cod=i.g_ti_cod"
                    + " AND   i.g_cn_cod=" + G_CN_COD;

            try (ResultSet result = stmt.executeQuery(sql)) {

                /* Remarque: le rangAppel est à null / 0 pour celles des formations 
                non-sélectives qui ne réalisent pas de classement. */
                while (result.next()) {
                    estConsidereDuSecteur = result.getBoolean(1);
                    break;
                }
            }
        }

        if (estConsidereDuSecteur == null) {
            throw new RuntimeException("Impossible de récupérer les informations du candidat");
        }

        return estConsidereDuSecteur;
    }

    
    @Override
    public void close() throws Exception {
        co.close();
    }
  
    private static final Logger LOGGER = Logger.getLogger(ConnecteurDonneesAppelOracle.class.getSimpleName());

}
