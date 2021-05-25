
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
package fr.parcoursup.algos.ordreappel.donnees;

import fr.parcoursup.algos.exceptions.AccesDonneesException;
import fr.parcoursup.algos.exceptions.AccesDonneesExceptionMessage;
import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;
import fr.parcoursup.algos.ordreappel.algo.*;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fr.parcoursup.algos.donnees.ConnecteurSQL.*;
import static fr.parcoursup.algos.donnees.SQLStringsConstants.*;
import static java.util.stream.Collectors.joining;

public class ConnecteurDonneesAppelSQL implements ConnecteurDonneesAppel {

    public ConnecteurDonneesAppelSQL(Connection connection) throws AccesDonneesException {
        if (connection == null) {
            throw new AccesDonneesException(AccesDonneesExceptionMessage.CONNECTEUR_SQL_CONNEXION_NULL);
        }
        this.connection = connection;
    }

    /* connection non null */
    protected final Connection connection;

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
        throw new AccesDonneesException(AccesDonneesExceptionMessage.CONNECTEUR_DONNEES_APPEL_SQL_GROUPE_MANQUANT, groupeUniqueCGPCOD);
    }

    /* recupere les groupes de classements */
    private Map<Integer, GroupeClassement> recupererGroupes(Integer groupeUniqueCGPCOD) throws AccesDonneesException {
        Map<Integer, GroupeClassement> groupesClassements
                = new HashMap<>();

        try (Statement stmt = connection.createStatement()) {

            /* récupère la liste des groupes et les taux minimum de boursiers
            et de candidats du secteur depuis la base de données */
            LOGGER.info("Récupération des groupes");
            stmt.setFetchSize(1_000_000);

            String sql
                    = SELECT + "DISTINCT "
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
                    + FROM
                    + RECRUTEMENTS_GROUPES_TABLE +  " rg,"
                    + RECRUTEMENTS_FORMATIONS_TABLE +  " r,"
                    + FOR_INSCRIPTIONS_TABLE + " ti,"
                    + FOR_TYPE_TABLE +  " fr,"
                    + FIL_TYPE_TABLE +  " fl,"
                    + C_GRP + " g "
                    + WHERE +  " rg.g_ti_cod=r.g_ti_cod "
                    + AND + " rg.g_ta_cod=r.g_ta_cod "
                    + AND + " rg.g_ti_cod=ti.g_ti_cod "
                    + AND + " ti.g_fr_cod_ins=fr.g_fr_cod "
                    + AND + " ti.g_fl_cod_ins=fl.g_fl_cod "
                    + AND + " rg.c_gp_cod=g.c_gp_cod "
                    + AND + " g.c_gp_eta_cla=2 " //classement finalisé
                    + AND + " ti.g_ti_flg_par_eff > 0 "//paramétrage effectué
                    + AND + " NVL(g.c_gp_flg_pas_cla, 0)!=1";

            if (groupeUniqueCGPCOD != null) {
                sql += AND + " rg.C_GP_COD=" + groupeUniqueCGPCOD;
            }

            LOGGER.info(sql);

            try (ResultSet result = stmt.executeQuery(sql)) {

                while (result.next()) {

                    int cGpCod = result.getInt(1);

                    if (groupesClassements.containsKey(cGpCod)) {
                        throw new VerificationException(VerificationExceptionMessage.CONNECTEUR_DONNEES_APPEL_SQL_GROUPE_DUPLIQUE, cGpCod, sql);
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
            throw new AccesDonneesException(AccesDonneesExceptionMessage.CONNECTEUR_DONNEES_APPEL_SQL_ERREUR_SQL_RECUPERATION, ex);
        } catch (VerificationException ex) {
            throw new AccesDonneesException(AccesDonneesExceptionMessage.CONNECTEUR_DONNEES_APPEL_SQL_INTEGRITE, ex);
        }
        return groupesClassements;

    }

    /* recupere les voeux et les stocke dans les groupes de classements */
    private Map<Integer, GroupeClassement> recupererGroupesEtVoeux(
            Integer groupeUniqueCGPCOD
    ) throws AccesDonneesException {

        Map<Integer, GroupeClassement> groupesClassements = recupererGroupes(groupeUniqueCGPCOD);
        try (Statement stmt = connection.createStatement()) {

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
                    +  FROM
                    + CLASSEMENTS_TABLE +  " cg," +
                    C_GRP + " gp," +
                    C_JUR_ADM + " ja," +
                    CANDIDATS_TABLE + " c," +
                    INSCRIPTIONS_TABLE + " i"
                    //groupe de classement
                    + WHERE + " cg.c_gp_cod=gp.c_gp_cod"
                    //jury admission
                    + AND + " gp.c_ja_cod=ja.c_ja_cod"
                    //candidat classé 5 non-classé 4
                    + AND + " i_ip_cod=5"
                    //id candidat
                    + AND + " cg.g_cn_cod=c.g_cn_cod"
                    //dossier non-annulé (décès...)
                    + AND + " c.g_ic_cod >= 0"
                    //id candidat
                    + AND + " cg.g_cn_cod=i.g_cn_cod"
                    //formation inscription
                    + AND + " ja.g_ti_cod=i.g_ti_cod"
                    //seulement les formations qui classent
                    + AND + " NVL(gp.c_gp_flg_pas_cla, 0)!=1 ";

            if (groupeUniqueCGPCOD != null) {
                sql += AND + " cg.C_GP_COD=" + groupeUniqueCGPCOD;
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
            }
            return groupesClassements;
        } catch (SQLException | VerificationException ex) {
            throw new AccesDonneesException(AccesDonneesExceptionMessage.CONNECTEUR_DONNEES_APPEL_SQL_ERREUR_SQL_RECUPERATION, ex);
        }
    }

    /* l'argument peut être null, dans ce cas toutes les données d'appel sont récupérées */
    private AlgoOrdreAppelEntree recupererDonnees(Integer groupeUniqueCGPCOD) throws AccesDonneesException {
        Map<Integer, GroupeClassement> groupesClassements = recupererGroupesEtVoeux(groupeUniqueCGPCOD);
        AlgoOrdreAppelEntree entree = new AlgoOrdreAppelEntree();
        entree.groupesClassements.addAll(groupesClassements.values());
        return entree;
    }

    /* exportation des classements vers la base de données */
    private void exporterOrdresAppel(Map<Integer, OrdreAppel> ordresAppel) throws SQLException {

        connection.setAutoCommit(false);

        try (PreparedStatement ps
                = connection.prepareStatement(
                        "INSERT INTO " + J_ORD_APPEL_TMP + " (C_GP_COD,G_CN_COD,C_CG_ORD_APP) VALUES (?,?,?)")) {
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
        try (Statement ps = connection.createStatement()) {
            ps.execute(
                    UPDATE + CLASSEMENTS_TABLE +  " a " +
                            "set a.C_CG_ORD_APP ="
                    + " (SELECT b.C_CG_ORD_APP from " + J_ORD_APPEL_TMP + " b "
                    + WHERE + "a.G_CN_COD=b.G_CN_COD AND a.C_GP_COD=b.C_GP_COD)");
            connection.commit();
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
            throw new AccesDonneesException(AccesDonneesExceptionMessage.CONNECTEUR_DONNEES_APPEL_SQL_EXPORTATION, ex);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ConnecteurDonneesAppelSQL.class.getSimpleName());

}

