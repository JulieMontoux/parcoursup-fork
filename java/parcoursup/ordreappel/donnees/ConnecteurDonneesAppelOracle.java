
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
import static parcoursup.donnees.SQLStringsConstants.SELECT;
import parcoursup.exceptions.AccesDonneesException;
import parcoursup.exceptions.VerificationException;
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
            String password) throws AccesDonneesException {
        co = new ConnecteurOracle(url, user, password);
    }

    public ConnecteurDonneesAppelOracle(Connection connection) throws AccesDonneesException {
        co = new ConnecteurOracle(connection);
    }

    private final ConnecteurOracle co;

    private Connection conn() {
        return co.connection();
    }


    /* chargement des classements depuis la base de données */
    @Override
    public AlgoOrdreAppelEntree recupererDonneesOrdreAppel() throws AccesDonneesException {
        return recupererDonnees(null);
    }

    @Override
    public GroupeClassement recupererDonneesOrdreAppelGroupe(int groupeUniqueCGPCOD) throws AccesDonneesException {
        AlgoOrdreAppelEntree entree;
        entree = recupererDonnees(groupeUniqueCGPCOD);
        for (GroupeClassement groupe : entree.groupesClassements) {
            if (groupe.cGpCod == groupeUniqueCGPCOD) {
                return groupe;
            }
        }
        throw new AccesDonneesException("Pas de groupe avec le C_GP_COD " + groupeUniqueCGPCOD);
    }

    /* l'argument peut être null, dans ce cas toutes les données d'appel sont récupérées */
    private AlgoOrdreAppelEntree recupererDonnees(Integer groupeUniqueCGPCOD) throws AccesDonneesException {
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
                    + " AND   NVL(g.c_gp_flg_pas_cla, 0)!=1";

            if (groupeUniqueCGPCOD != null) {
                sql += " AND rg.C_GP_COD=" + groupeUniqueCGPCOD;
            }

            LOGGER.info(sql);

            try (ResultSet result = stmt.executeQuery(sql)) {

                while (result.next()) {

                    int cGpCod = result.getInt(1);

                    if (groupesClassements.containsKey(cGpCod)) {
                        throw new VerificationException(
                                "Groupe dupliqué " + cGpCod + " lors"
                                        + "de l'énumération des résultats de la requete"
                                        + "\r\n" + sql);
                    }

                    /* un ou deux taux selon le type de formation */
                    boolean tauxBoursierDisponible = result.getBoolean(2);
                    boolean tauxResDisponible = result.getBoolean(4);
                    int tauxMinBoursier = tauxBoursierDisponible ? result.getInt(3) : 0;
                    int tauxMaxNonResident = tauxResDisponible ? result.getInt(5) : 100;
                    int tauxMinResident = 100 - tauxMaxNonResident;

                    groupesClassements.put(cGpCod,
                            new GroupeClassement(
                                    cGpCod,
                                    tauxMinBoursier,
                                    tauxMinResident
                            ));

                }
            }
        } catch (SQLException ex) {
            throw new AccesDonneesException("Erreur SQL lors de la récupération des données d'appel", ex);
        } catch (VerificationException ex) {
            throw new AccesDonneesException("Problème d'intégrité des données d'appel", ex);
        }

        try (Statement stmt = conn().createStatement()) {

            Set<Integer> groupesManquants = new HashSet<>();

            /* récupère la liste des candidats depuis la base de données */
            LOGGER.info("Récupération des voeux");
            stmt.setFetchSize(1_000_000);
            String sql = SELECT
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

            if (groupeUniqueCGPCOD != null) {
                sql += " AND cg.C_GP_COD=" + groupeUniqueCGPCOD;
            }

            LOGGER.info(sql);

            try (ResultSet result = stmt.executeQuery(sql)) {

                /* Remarque: le rangAppel est à null / 0 pour celles des formations
            non-sélectives qui ne réalisent pas de classement. */
                while (result.next()) {

                    int cGpCod = result.getInt(1);
                    int gCnCod = result.getInt(2);
                    int rang = result.getInt(3);

                    boolean estDeclareBoursierLycee = (result.getInt(4) == 1);
                    int confirmationBoursier = result.getInt(5);
                    boolean estConsidereBoursier
                            = estDeclareBoursierLycee
                            && (confirmationBoursier == 1 || confirmationBoursier == 2);
                    boolean estConsidereDuSecteur = result.getBoolean(6);

                    if (!groupesClassements.containsKey(cGpCod)) {
                        //peut arriver si les classements ne sont pas encore remontés
                        groupesManquants.add(cGpCod);
                        continue;
                    }

                    GroupeClassement ga = groupesClassements.get(cGpCod);
                    ga.ajouterVoeu(
                            new VoeuClasse(
                                    gCnCod,
                                    rang,
                                    estConsidereBoursier,
                                    estConsidereDuSecteur)
                    );
                }

                if (!groupesManquants.isEmpty()) {
                    LOGGER.log(Level.SEVERE, "{0} groupes manquants.", groupesManquants.size());
                    String list = groupesManquants.stream()
                            .map(String::valueOf)
                            .collect(joining(",", "(", ")"));
                    LOGGER.severe(list);
                }

                AlgoOrdreAppelEntree entree = new AlgoOrdreAppelEntree();
                entree.groupesClassements.addAll(groupesClassements.values());
                return entree;

            }

        } catch (SQLException ex) {
            throw new AccesDonneesException("Erreur SQL lors de la récupération des données d'appel", ex);
        }

    }

    /* exportation des classements vers la base de données */
    private void exporterOrdresAppel(Map<Integer, OrdreAppel> ordresAppel) throws SQLException {

        conn().setAutoCommit(false);

        try (PreparedStatement ps
                = conn().prepareStatement(
                        "INSERT INTO J_ORD_APPEL_TMP (C_GP_COD,G_CN_COD,C_CG_ORD_APP) VALUES (?,?,?)")) {
            int count = 0;
            for (Entry<Integer, OrdreAppel> paire
                    : ordresAppel.entrySet()) {

                Integer cGpCod = paire.getKey();
                OrdreAppel ordre = paire.getValue();

                for (CandidatClasse candidat : ordre.candidats) {
                    ps.setInt(1, cGpCod);
                    ps.setInt(2, candidat.gCnCod);
                    ps.setInt(3, candidat.rangAppel);
                    ps.addBatch();
                    if (++count % 500_000 == 0) {
                        LOGGER.log(Level.INFO, "Exportation des ordres d appel des voeux {0} a {1}", new Object[]{count - 499_999, count});
                        ps.executeBatch();
                        ps.clearBatch();
                        LOGGER.info("Fait");
                    }
                }
            }
            ps.executeBatch();
        }

        LOGGER.info("Mise à jour de la table C_CAN_GRP");
        try (Statement ps = conn().createStatement()) {
            ps.execute("UPDATE "
                    + "(SELECT  a.C_CG_ORD_APP cible, b.C_CG_ORD_APP source FROM C_CAN_GRP a,"
                    + "J_ORD_APPEL_TMP b WHERE a.G_CN_COD=b.G_CN_COD AND a.C_GP_COD=b.C_GP_COD)"
                    + "SET cible=source");
        }
    }

    @Override
    public void exporterDonneesOrdresAppel(AlgoOrdreAppelSortie donnees) throws AccesDonneesException {

        try {

            LOGGER.log(Level.INFO, "D\u00e9but de l''exportation des ordres d''appel de {0} groupes", donnees.ordresAppel.size());

            exporterOrdresAppel(donnees.ordresAppel);

            LOGGER.info("Fin de l'exportation");

        } catch (SQLException ex) {
            LOGGER.severe("Erreur d'exportation des données");
            throw new AccesDonneesException("Erreur SQL lors de l'exportation des données", ex);
        }
    }

    /* vérifie si un candidat est boursier */
    @Override
    public boolean estBoursier(int gCnCod) throws AccesDonneesException {

        Boolean estConsidereBoursier = null;

        try (PreparedStatement stmt = conn().prepareStatement(
                SELECT
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
                + " WHERE c.g_cn_cod=?")) {

            stmt.setInt(1, gCnCod);
            try (ResultSet result = stmt.executeQuery()) {

                /* Remarque: le rangAppel est à null / 0 pour celles des formations 
                non-sélectives qui ne réalisent pas de classement. */
                while (result.next()) {

                    boolean estDeclareBoursierLycee = (result.getInt(1) == 1);
                    int confirmationBoursier = result.getInt(2);
                    estConsidereBoursier = estDeclareBoursierLycee
                            && (confirmationBoursier == 1 || confirmationBoursier == 2);
                }
            }
        } catch (SQLException ex) {
            throw new AccesDonneesException("Erreur acces données", ex);
        }

        if (estConsidereBoursier == null) {
            throw new AccesDonneesException("Impossible de récupérer les informations du candidat");
        }

        return estConsidereBoursier;
    }

    /* vérifie si un candidat est considéré du secteur dans un groupe */
    @Override
    public boolean estDuSecteur(int gCnCod, int cGpCod) throws AccesDonneesException {

        Boolean estConsidereDuSecteur = null;

        try (Statement stmt = conn().createStatement()) {

            String sql = SELECT
                    //le candidat est-il du secteur sur les candidats
                    //passés par ce groupe
                    + "I_IS_FLC_SEC "
                    + " FROM a_rec_grp ar, i_ins i"
                    //groupe de classement
                    + " WHERE ar.c_gp_cod=" + cGpCod
                    + " AND   ar.g_ti_cod=i.g_ti_cod"
                    + " AND   i.g_cn_cod=" + gCnCod;

            try (ResultSet result = stmt.executeQuery(sql)) {

                /* Remarque: le rangAppel est à null / 0 pour celles des formations 
                non-sélectives qui ne réalisent pas de classement. */
                while (result.next()) {
                    estConsidereDuSecteur = result.getBoolean(1);
                    break;
                }
            }
        } catch (SQLException ex) {
            throw new AccesDonneesException("Erreur SQL lors de l'accès aux données", ex);
        }

        if (estConsidereDuSecteur == null) {
            throw new AccesDonneesException("Impossible de récupérer les informations du candidat");
        }

        return estConsidereDuSecteur;
    }

    @Override
    public void close() throws SQLException {
        co.close();
    }

    private static final Logger LOGGER = Logger.getLogger(ConnecteurDonneesAppelOracle.class.getSimpleName());

}
