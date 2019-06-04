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
package parcoursup.propositions.donnees;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import parcoursup.donnees.ConnecteurOracle;
import parcoursup.propositions.affichages.AlgosAffichages;
import parcoursup.propositions.algo.AlgoPropositionsEntree;
import parcoursup.propositions.algo.AlgoPropositionsSortie;
import parcoursup.propositions.algo.GroupeAffectation;
import parcoursup.propositions.algo.GroupeAffectationUID;
import parcoursup.propositions.algo.GroupeInternat;
import parcoursup.propositions.algo.GroupeInternatUID;
import parcoursup.propositions.algo.Voeu;
import parcoursup.propositions.algo.VoeuUID;
import parcoursup.propositions.meilleursbacheliers.MeilleurBachelier;


/*
    Récupération et injection des données depuis et vers la base Oracle

    La base identifie:

    * chaque candidat par un G_CN_COD
    * chaque formation d'inscription par un G_TI_COD
    * chaque formation d'affectation par un G_TA_COD
    * chaque commission de classement pédagogique des voeuxEnAttente par un C_GP_COD
    * chaque commission de classement internat des voeuxEnAttente par un C_GI_COD

    Plus de détails dans doc/implementation.txt
 */
public class ConnecteurDonneesPropositionsOracle implements ConnecteurDonneesPropositions, java.lang.AutoCloseable {

    /* connexion a la base de données */
    private final ConnecteurOracle co;

    private Connection conn() {
        return co.connection();
    }

    @Override
    public void close() throws Exception {
        co.close();
    }

    public ConnecteurDonneesPropositionsOracle(
            String url,
            String user,
            String password,
            boolean useTNSNames) throws SQLException {
        co = new ConnecteurOracle(url, user, password, useTNSNames);
    }

    public ConnecteurDonneesPropositionsOracle(Connection conn) {
        co = new ConnecteurOracle(conn);
    }

    /* variable stockant les données d'entrée pendant la récupération */
    AlgoPropositionsEntree entree = null;

    @Override
    public AlgoPropositionsEntree recupererDonnees() throws SQLException {

        entree = new AlgoPropositionsEntree();

        LOGGER.info("Vérification de l'interruption du flux de données entrantes.");
        /* Si = 1 indique que le programme d'admission est en train de tourner
        pour faire des propositions. Si c'est le cas, tout est bloqué */
        try (ResultSet result
                = conn().createStatement().executeQuery(
                        "SELECT g_pr_val FROM g_par WHERE g_pr_cod="
                        + indexFlagInterruptionDonnéeesEntrantes)) {
            result.next();
            boolean estVerouille = result.getBoolean(1);
            if (!estVerouille) {
                throw new RuntimeException(
                        "Veuillez interrompre le flux de données entrantes "
                        + "et positionner le g_pr_cod="
                        + indexFlagInterruptionDonnéeesEntrantes + " à 1");
            }

        }

        LOGGER.info("Récupération du nombre de jours écoulés depuis l'ouverture de la campagne");
        GroupeInternat.nbJoursCampagne = nbJoursDepuis(indexDateDebutDeCampagne) + 1;
        LOGGER.log(Level.INFO, "{0} jours.", GroupeInternat.nbJoursCampagne);

        LOGGER.info("Récupération du nombre de jours de campagne à la date pivot");
        GroupeInternat.nbJoursCampagneDatePivotInternats
                = nbJoursEntre(indexDateDebutDeCampagne, indexDateOuvertureCompleteInternats);
        LOGGER.log(Level.INFO, "{0} jours.", GroupeInternat.nbJoursCampagneDatePivotInternats);

        LOGGER.info("Récupération des candidats ayant activé le répondeur automatique");
        recupererCandidatsAvecRepondeurAutomatique();

        LOGGER.info("Récupération des groupes d'affectation");
        recupererGroupesAffectation();

        LOGGER.info("Récupération des internats");
        recupererInternats();

        LOGGER.info("Récupération des propositions aux candidats ");
        recupererPropositionsActuelles();

        LOGGER.info("Récupération des informations relatives aux meilleurs bacheliers");
        recupererDonneesMeilleursBacheliers();

        LOGGER.info("Récupération des voeux en attente avec demande internat dans un internat à classement propre");
        recupererVoeuxAvecInternatsAClassementPropre();

        LOGGER.info("Récupération des voeux en attente sans internat, ou dans un internat sans classement propre");
        recupererVoeuxSansInternatAClassementPropre();

        if (!groupesManquants.isEmpty()) {
            LOGGER.log(Level.SEVERE, "{0} groupes manquants.", groupesManquants.size());
        }

        if (!internatsManquants.isEmpty()) {
            LOGGER.log(Level.SEVERE, "{0} internats manquants.", internatsManquants.size());
        }

        LOGGER.info("Fin de la récupération des données depuis la base Oracle");

        return entree;

    }

    /* exportation des résultats du calcul: propositions à faire */
    @Override
    public void exporterDonnees(AlgoPropositionsSortie sortie) throws SQLException {

        conn().setAutoCommit(false);

        /* Si il y a eu un problème lors de l'export, on le signale via ce flag */
        if (sortie.alerte) {
            conn().createStatement().executeQuery(
                    "UPDATE G_PAR SET G_PR_VAL=1 WHERE G_PR_COD = " + indexFlagAlerte
            );
            conn().commit();
        }

        LOGGER.info("Exportation des propositions d'admission");
        exporterNouvellesPropositionsAdmission(sortie);
        conn().commit();

        LOGGER.info("Exportation des démissions");
        exporterDemissionsAutomatiques(sortie);
        conn().commit();

        LOGGER.info("Exportation des prédictions des derniers appelés à la date pivot");
        exporterPredicteurRangDernierAppele(sortie.groupes);
        conn().commit();

        LOGGER.info("Exportation des affichages");
        exporterAffichages(sortie);
        conn().commit();
    }

    /* exportation des données affichées: rangs sur liste d'attente et rangs
    des dernier appelés. */
 /* variable stockant les rangs affichés dans les ordres d'appel.
    Il y a quelques rares cas d'erreurs de classement par les formations 
    (de l'ordre de la centaine par an). Dans ce cas un candidat peut être
    remonté dans l'ordre d'appel, ce qui crée deux candidats avec le
    même ordre d'appel, avec priorité au candidat
    remonté. Dans ce cas le champ C_CG_ORD_APP utilisé par l'algorithme
    est incrémenté pour tous les candidats suivants, afin de maintenir 
    la propriété d'unicité du candidat pour un ordre d'appel donnée. Le champ
    C_CG_ORD_APP_AFF reste, lui égal à sa valeur initiale, 
    sauf pour le canddiat qui a bénéficié de la remontée dans le classement. 
    C'est le champ C_CG_ORD_APP_AFF qui est affiché au candidat surle site de
    Parcoursup, et utilisé pour le mise à jour des affichages. */
    public Map<VoeuUID, Integer> ordresAppelAffiches = new HashMap<>();

    public void exporterAffichages(AlgoPropositionsSortie sortie) throws SQLException {

        LOGGER.info("Prise en compte des propositions du jour");
        Set<VoeuUID> voeuxAvecPropositionDansMemeFormation
                = recupererVoeuxAvecPropositionAnterieureDansMemeFormation();

        /* ajout de toutes les propositions du jour */
        Set<VoeuUID> propositionsDuJour = new HashSet<>();
        for (Voeu v : sortie.voeux) {
            if (v.estPropositionDuJour()) {
                propositionsDuJour.add(v.id);
            }
        }

        LOGGER.info("Mise à jour des rangs d'appels affichés");
        for (Voeu v : sortie.voeux) {
            Integer ordre = ordresAppelAffiches.get(v.id);
            if (ordre != null) {
                v.ordreAppel = ordre;
            }
        }

        LOGGER.info("Mise à jour des affichages candidats");
        AlgosAffichages.mettreAJourAffichages(sortie,
                voeuxAvecPropositionDansMemeFormation,
                propositionsDuJour
        );

        LOGGER.info("Exportation des rangs des barres des derniers appelés pour les voeux sans internat");
        exporterBarresAfficheesVoeuxSansInternat(sortie);
        conn().commit();

        LOGGER.info("Exportation des rangs des barres des derniers appelés  pour les voeux avec internat");
        exporterBarresAfficheesVoeuxAvecInternat(sortie);
        conn().commit();

        LOGGER.info("Exportation des rangs sur liste d'attente");
        exporterRangsSurListeAttente(sortie);
        conn().commit();
    }

    private void exporterNouvellesPropositionsAdmission(AlgoPropositionsSortie sortie) throws SQLException {
        LOGGER.info("Préparation de la table A_ADM_PROP avant export");
        conn().createStatement().executeQuery(
                "DELETE FROM A_ADM_PROP WHERE NB_JRS=" + GroupeInternat.nbJoursCampagne);

        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO A_ADM_PROP "
                + "(G_CN_COD,G_TA_COD,I_RH_COD,C_GP_COD,G_TI_COD,C_GI_COD,A_AM_FLG_MBC,NB_JRS)"
                + " VALUES (?,?,?,?,?,?,?," + GroupeInternat.nbJoursCampagne + ")")) {
            int count = 0;

            for (Voeu voe : sortie.voeux) {
                if (voe.estPropositionDuJour()) {

                    GroupeAffectationUID groupe = voe.groupe.id;
                    GroupeInternatUID internat = voe.internatID();

                    assert voe.id.G_TA_COD == groupe.G_TA_COD;

                    ps.setInt(1, voe.id.G_CN_COD);
                    ps.setInt(2, voe.id.G_TA_COD);
                    ps.setInt(3, voe.id.I_RH_COD ? 1 : 0);
                    ps.setInt(4, groupe.C_GP_COD);
                    ps.setInt(5, groupe.G_TI_COD);
                    ps.setInt(6, (internat == null) ? 0 : internat.C_GI_COD);
                    ps.setBoolean(7, voe.eligibleDispositifMB);

                    ps.addBatch();
                    if (++count % 100_000 == 0) {
                        LOGGER.log(Level.INFO, "Exportation des propositions {0} a {1}", new Object[]{count - 99_999, count});
                        ps.executeBatch();
                        ps.clearBatch();
                        //LOGGER.info("Fait");
                    }
                }
            }

            ps.executeBatch();
            LOGGER.log(Level.INFO, "{0} propositions exportées.", count);

        }

    }

    private void exporterDemissionsAutomatiques(AlgoPropositionsSortie sortie) throws SQLException {

        LOGGER.info("Préparation de la table A_ADM_DEM_RA avant export");
        conn().createStatement().executeQuery(
                "DELETE FROM A_ADM_DEM_RA WHERE NB_JRS=" + GroupeInternat.nbJoursCampagne);

        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO A_ADM_DEM_RA "
                + "(G_CN_COD,G_TA_COD,I_RH_COD,C_GP_COD,G_TI_COD,C_GI_COD,EST_DEM_PROP,NB_JRS)"
                + " VALUES (?,?,?,?,?,?,?," + GroupeInternat.nbJoursCampagne + ")")) {
            int count = 0;

            for (Voeu voe : sortie.voeux) {

                if (voe.estDemissionAutomatique()) {
                    GroupeAffectationUID groupe = voe.groupe.id;
                    GroupeInternatUID internat = voe.internatID();

                    assert voe.id.G_TA_COD == groupe.G_TA_COD;

                    ps.setInt(1, voe.id.G_CN_COD);
                    ps.setInt(2, voe.id.G_TA_COD);
                    ps.setInt(3, voe.id.I_RH_COD ? 1 : 0);
                    ps.setInt(4, groupe.C_GP_COD);
                    ps.setInt(5, groupe.G_TI_COD);
                    ps.setInt(6, (internat == null) ? 0 : internat.C_GI_COD);
                    ps.setBoolean(7, voe.estDemissionAutomatiqueProposition());

                    ps.addBatch();
                    if (++count % 100_000 == 0) {
                        LOGGER.log(Level.INFO, "Exportation des demissions {0} a {1}", new Object[]{count - 99_999, count});
                        ps.executeBatch();
                        ps.clearBatch();
                        //LOGGER.info("Fait");
                    }
                }
            }

            ps.executeBatch();
            LOGGER.log(Level.INFO, "{0} démissions automatiques exportées.", count);

        }
    }

    private void exporterPredicteurRangDernierAppele(Collection<GroupeAffectation> groupes) throws SQLException {

        LOGGER.info("Préparation de la table A_ADM_PRED_DER_APP avant export");
        conn().createStatement().executeQuery(
                "DELETE FROM A_ADM_PRED_DER_APP WHERE NB_JRS=" + GroupeInternat.nbJoursCampagne);

        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO A_ADM_PRED_DER_APP "
                + "(G_TA_COD,C_GP_COD,A_RG_RAN_DER,NB_JRS)"
                + " VALUES (?,?,?," + GroupeInternat.nbJoursCampagne + " )")) {

            for (GroupeAffectation g : groupes) {
                ps.setInt(1, g.id.G_TA_COD);
                ps.setInt(2, g.id.C_GP_COD);
                ps.setInt(3,
                        (g.estimationRangDernierAppeleADatePivot == Integer.MAX_VALUE)
                                ? -1
                                : g.estimationRangDernierAppeleADatePivot
                );
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void exporterBarresAfficheesVoeuxAvecInternat(AlgoPropositionsSortie sortie) throws SQLException {

        LOGGER.info("Récupération des rangs des dernier appelés au jour n-1");
        /* Dans chaque groupe internat, tous les candidats inclus strictement 
        dans le rectangle délimité par
        internat.nouvelleBarreAppel et 
        internat.nouvelleBarreInternat et 

        ont reçu une proposition ou bien ont renoncé à leur voeu avec internat.
        
        Cette information est transmise aux candidats, mais pas telle quelle:
        on s'assure que les informations affichées sont monotones,
        au sens où le rectangle du jour n+1 contient le rectangle du jour n,
        sinon cela serait source de confusion.
         */

 /* deux maps sont utilisées pour améliorer les performances 
        dans la requête suivante */
        Map<GroupeInternatUID, GroupeInternat> internats = new HashMap<>();
        for (GroupeInternat internat : sortie.internats) {
            internats.put(internat.id, internat);
        }

        /* on compare avec les valeurs au jour n-1 et on met à jour
                si la relation d'inclusion est vérifiée */
        try (Statement stmt = conn().createStatement()) {
            stmt.setFetchSize(100_000);

            try (ResultSet result = stmt.executeQuery(
                    "SELECT C_GI_COD,"
                    + "G_TA_COD,"
                    + "prop.G_TI_COD,"
                    + "C_GP_COD,"
                    + "MAX(A_RG_RAN_DER), "
                    + "MAX(A_RG_RAN_DER_INT),"
                    + "g_ti_cla_int_uni"
                    + " FROM A_REC_GRP_INT_PROP prop,G_TRI_INS ins"
                    + " WHERE (NB_JRS < " + GroupeInternat.nbJoursCampagne + ")"
                    + " AND C_GI_COD != 0"
                    + " AND ins.G_TI_COD=prop.G_TI_COD"
                    + " GROUP BY C_GI_COD,G_TA_COD,prop.G_TI_COD,C_GP_COD,g_ti_cla_int_uni")) {

                while (result.next()) {
                    int C_GI_COD = result.getInt(1);
                    int G_TA_COD = result.getInt(2);
                    int G_TI_COD = result.getInt(3);
                    int C_GP_COD = result.getInt(4);
                    int dernierAppeleRangAppe = result.getInt(5);
                    int dernierAppeleClassementInternat = result.getInt(6);
                    int typeInternat = result.getInt(7);

                    GroupeInternatUID internatUID = internatUID(typeInternat, C_GI_COD, G_TA_COD);
                    GroupeInternat internat = internats.get(internatUID);

                    if (internat == null) {
                        throw new RuntimeException("Disparition suspecte de l'internat " + internatUID);
                    }

                    GroupeAffectationUID groupeUID = new GroupeAffectationUID(C_GP_COD, G_TI_COD, G_TA_COD);
                    Integer nouvelleBarreAppel = internat.barresAppelAffichees.get(groupeUID);
                    Integer nouvelleBarreInternat = internat.barresInternatAffichees.get(groupeUID);

                    boolean inclusion
                            = (nouvelleBarreAppel != null)
                            && (nouvelleBarreInternat != null)
                            && nouvelleBarreInternat >= dernierAppeleClassementInternat
                            && nouvelleBarreAppel >= dernierAppeleRangAppe;

                    /* Si la relation d'inclusion n'est pas vérifiée, 
                            on ne modifie pas l'affichage aux candidats
                            pour ne pas créer d'incompréhension. 
                            Rq: le message affiché au candidat reste correct. */
                    if (!inclusion) {
                        internat.barresAppelAffichees.put(groupeUID, dernierAppeleRangAppe);
                        internat.barresInternatAffichees.put(groupeUID, dernierAppeleClassementInternat);
                    }

                }
            }
        }

        LOGGER.info("Préparation de la table A_REC_GRP_INT_PROP avant export");
        conn().createStatement().executeQuery(
                "DELETE FROM A_REC_GRP_INT_PROP WHERE C_GI_COD !=0 AND NB_JRS=" + GroupeInternat.nbJoursCampagne);

        LOGGER.info("Export dans la table A_REC_GRP_INT_PROP");
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO A_REC_GRP_INT_PROP "
                + "(C_GI_COD,G_TA_COD,G_TI_COD,C_GP_COD,A_RG_RAN_DER,A_RG_RAN_DER_INT,NB_JRS)"
                + " VALUES (?,?,?,?,?,?," + GroupeInternat.nbJoursCampagne + " )")) {

            for (GroupeInternat internat : sortie.internats) {
                for (GroupeAffectation g : internat.groupesConcernes) {
                    Integer barreAppelAffichee = internat.barresAppelAffichees.get(g.id);
                    Integer barreInternatAffichee = internat.barresInternatAffichees.get(g.id);
                    if (barreAppelAffichee != null && barreInternatAffichee != null) {
                        ps.setInt(1, internat.id.C_GI_COD);
                        ps.setInt(2, g.id.G_TA_COD);
                        ps.setInt(3, g.id.G_TI_COD);
                        ps.setInt(4, g.id.C_GP_COD);
                        ps.setInt(5, barreAppelAffichee);
                        ps.setInt(6, barreInternatAffichee);
                        ps.addBatch();
                    }
                }
            }
            ps.executeBatch();
        }
    }

    private void exporterBarresAfficheesVoeuxSansInternat(
            AlgoPropositionsSortie sortie
    ) throws SQLException {

        LOGGER.info("Récupération des rangs des dernier appelés au jour n-1");
        /* Dans chaque formation avec internat,
        on affiche le rang du dernier appelé */

        Map<GroupeAffectationUID, GroupeAffectation> groupes = new HashMap<>();
        for (GroupeAffectation g : sortie.groupes) {
            groupes.put(g.id, g);
        }

        /* on compare avec les valeurs au jour n-1 et on met à jour pour garantir la monotonie */
        try (Statement stmt = conn().createStatement()) {
            stmt.setFetchSize(100_000);

            try (ResultSet result = stmt.executeQuery(
                    "SELECT G_TA_COD,G_TI_COD,C_GP_COD,MAX(A_RG_RAN_DER)"
                    + " FROM A_REC_GRP_INT_PROP"
                    + " WHERE (NB_JRS < " + GroupeInternat.nbJoursCampagne + ")"
                    + " AND C_GI_COD=0"
                    + " GROUP BY C_GI_COD,G_TA_COD,G_TI_COD,C_GP_COD")) {
                while (result.next()) {
                    int G_TA_COD = result.getInt(1);
                    int G_TI_COD = result.getInt(2);
                    int C_GP_COD = result.getInt(3);
                    int dernierAppeleRangAppe = result.getInt(4);

                    GroupeAffectation groupe
                            = groupes.get(new GroupeAffectationUID(C_GP_COD, G_TI_COD, G_TA_COD));

                    if (groupe != null) {
                        groupe.rangDernierAppeleAffiche
                                = Math.max(dernierAppeleRangAppe, groupe.rangDernierAppeleAffiche);
                    }
                }
            }
        }

        LOGGER.info("Préparation de la table A_REC_GRP_INT_PROP avant export");
        conn().createStatement().executeQuery(
                "DELETE FROM A_REC_GRP_INT_PROP WHERE C_GI_COD=0"
                + " AND NB_JRS=" + GroupeInternat.nbJoursCampagne);

        LOGGER.info("Export dans la table A_REC_GRP_INT_PROP");
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO A_REC_GRP_INT_PROP "
                + "(C_GI_COD,G_TA_COD,G_TI_COD,C_GP_COD,A_RG_RAN_DER,A_RG_RAN_DER_INT,NB_JRS)"
                + " VALUES (0,?,?,?,?,0," + GroupeInternat.nbJoursCampagne + " )")) {

            for (GroupeAffectation g : sortie.groupes) {
                ps.setInt(1, g.id.G_TA_COD);
                ps.setInt(2, g.id.G_TI_COD);
                ps.setInt(3, g.id.C_GP_COD);
                ps.setInt(4, g.rangDernierAppeleAffiche);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void exporterRangsSurListeAttente(AlgoPropositionsSortie sortie) throws SQLException {

        LOGGER.info("Préparation de la table A_VOE_PROP avant export");
        conn().createStatement().executeQuery(
                "DELETE FROM A_VOE_PROP WHERE NB_JRS=" + GroupeInternat.nbJoursCampagne);

        LOGGER.log(Level.INFO, "Exportation des rangs sur liste d''attente de {0} voeux", sortie.voeux.size());

        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO A_VOE_PROP "
                + "(G_CN_COD,G_TA_COD,I_RH_COD,C_GP_COD,G_TI_COD,A_VE_RAN_LST_ATT,NB_JRS)"
                + " VALUES (?,?,?,?,?,?," + GroupeInternat.nbJoursCampagne + ")")) {
            int count = 0;

            for (Voeu voe : sortie.voeux) {

                /* Le rang sur liste d'attente ne concerne que les voeux
                en ettente  dans des formations sans internat à classement propre.
                 */
                if (voe.rangListeAttente == 0) {
                    continue;
                }

                GroupeAffectationUID groupe = voe.groupe.id;

                ps.setInt(1, voe.id.G_CN_COD);
                ps.setInt(2, voe.id.G_TA_COD);
                ps.setInt(3, voe.id.I_RH_COD ? 1 : 0);
                ps.setInt(4, groupe.C_GP_COD);
                ps.setInt(5, groupe.G_TI_COD);
                ps.setInt(6, voe.rangListeAttente);

                ps.addBatch();
                if (++count % 100_000 == 0) {
                    LOGGER.log(Level.INFO, "Exportation des rangs {0} a {1}",
                            new Object[]{count - 99_999, count});
                    ps.executeBatch();
                    ps.clearBatch();
                    //LOGGER.info("Fait");
                }
            }

            ps.executeBatch();
            LOGGER.log(Level.INFO, "{0} rangs sur liste d'attente exportés.", count);

        }

    }

    /* permet de comptabiliser les entree.internats manquants, avant le début de campagne */
    private final Set<GroupeInternatUID> internatsManquants
            = new HashSet<>();

    /* permet de comptabiliser les groupes manquants, avant le début de campagne */
    private final Set<GroupeAffectationUID> groupesManquants = new HashSet<>();

    private void recupererGroupesAffectation() throws SQLException {

        entree.groupesAffectations.clear();

        try (Statement stmt = conn().createStatement()) {
            stmt.setFetchSize(1_000_000);
            try (ResultSet result = stmt.executeQuery(
                    "SELECT C_GP_COD,"
                    + " G_TI_COD,"
                    + " G_TA_COD,"
                    + " A_RG_NBR_SOU capacite,"
                    + " NVL(a_rg_ran_lim,0),"
                    + "(SELECT MAX(C_CG_ORD_APP) FROM A_ADM adm, C_CAN_GRP cla "
                    + " WHERE "
                    + " adm.g_cn_cod=cla.g_cn_cod"
                    + " AND adm.g_ta_cod=a_rec_grp.g_ta_cod"
                    + " AND adm.g_ti_cod=a_rec_grp.g_ti_cod"
                    + " AND adm.c_gp_cod=a_rec_grp.c_gp_cod"
                    + " AND cla.c_gp_cod=a_rec_grp.c_gp_cod"
                    + " AND adm.a_ta_cod=1" //proc principale
                    + " AND cla.i_ip_cod=5" //classé
                    + " AND cla.c_cg_ord_app is not null)"
                    + " rangDernierAppele"
                    + " FROM a_rec_grp")) {

                while (result.next()) {

                    int C_GP_COD = result.getInt(1);
                    int G_TI_COD = result.getInt(2);
                    int G_TA_COD = result.getInt(3);
                    int nbRecrutementsSouhaite = result.getInt(4);
                    int rangLimite = result.getInt(5);/* peut être null, vaut 0 dans ce cas */
                    int rangDernierAppele = result.getInt(6);

                    GroupeAffectationUID id
                            = new GroupeAffectationUID(C_GP_COD, G_TI_COD, G_TA_COD);
                    if (entree.groupesAffectations.containsKey(id)) {
                        throw new RuntimeException("GroupeAffectation dupliqué");
                    }

                    entree.ajouter(
                            new GroupeAffectation(
                                    nbRecrutementsSouhaite,
                                    id,
                                    rangLimite,
                                    rangDernierAppele)
                    );

                }
            }
        }
    }

    private void recupererInternats() throws SQLException {
        entree.internats.clear();
        try (Statement stmt = conn().createStatement()) {
            stmt.setFetchSize(100_000);

            try (ResultSet result = stmt.executeQuery(
                    "SELECT C_GI_COD, NVL(G_TA_COD,0), A_RI_NBR_SOU "
                    + "FROM a_rec_grp_int")) {
                while (result.next()) {
                    int C_GI_COD = result.getInt(1);
                    int G_TA_COD = result.getInt(2);
                    int nbPlacesTotal = result.getInt(3);

                    GroupeInternatUID id
                            = new GroupeInternatUID(C_GI_COD, G_TA_COD);

                    if (entree.internats.containsKey(id)) {
                        throw new RuntimeException("Internat dupliqué");
                    }

                    entree.internats.put(id, new GroupeInternat(
                            id,
                            nbPlacesTotal
                    )
                    );

                }
            }
        }
    }

    /* Récupère un nombre de jours dans la table gpar */
    private int nbJoursDepuis(int index) throws SQLException {

        Integer nbJours = null;

        try (ResultSet result
                = conn().createStatement().executeQuery(
                        "SELECT TRUNC(SYSDATE) - TRUNC(TO_DATE(g_pr_val, 'DD/MM/YYYY:HH24MI'))"
                        + " FROM g_par WHERE g_pr_cod=" + index)) {
            while (result.next()) {
                nbJours = result.getInt(1);
            }
        }
        if (nbJours == null) {
            throw new RuntimeException("Date incohérente");
        }
        return nbJours;
    }

    /* Récupère un nombre de jours dans la table gpar */
    private int nbJoursEntre(int index1, int index2) throws SQLException {

        Integer nbJours = null;

        try (ResultSet result
                = conn().createStatement().executeQuery(
                        "SELECT TRUNC(TO_DATE(g_par2.g_pr_val, 'DD/MM/YYYY:HH24MI')) "
                        + "- TRUNC(TO_DATE(g_par1.g_pr_val, 'DD/MM/YYYY:HH24MI'))"
                        + " FROM g_par g_par1,g_par g_par2 WHERE g_par1.g_pr_cod=" + index1
                        + " and g_par2.g_pr_cod=" + index2)) {
            while (result.next()) {
                nbJours = result.getInt(1);
            }
        }
        if (nbJours == null) {
            throw new RuntimeException("Date incohérente");
        }
        return nbJours;
    }

    /* Récupère les voeuxEnAttente sur lesquels il ya une proposition d'admission qui
    bloque une place car elle est soit en attente de réponse du candidat soit 
    acceptée par le candidat. 
     */
    private void recupererPropositionsActuelles() throws SQLException {

        try (Statement stmt = conn().createStatement()) {
            stmt.setFetchSize(100_000);
            try (ResultSet result = stmt.executeQuery(
                    "SELECT "
                    + "adm.G_CN_COD,"//id candidat
                    + "adm.G_TI_COD, "//id formation affectation
                    + "adm.G_TA_COD, "//id formation affectation
                    + "adm.I_RH_COD, "//voeu avec internat?
                    + "adm.C_GP_COD, "
                    + "adm.C_GI_COD, "
                    + "adm.A_TA_COD, "
                    // A_TA_CODs
                    // 1 admission en PP 
                    // 2 apprentissage 
                    // 3 admission en PC 
                    // 5 admission en CAES 
                    // 10 inscription par l'établissement
                    + "ti.g_ti_cla_int_uni "//type d'internat (cf notes ci-dessous)
                    + " FROM A_ADM adm,"//table des admissions
                    + " A_SIT_VOE sv,"//statut du voeu
                    + " g_tri_ins ti"//données formations inscriptions                    
                    + " WHERE "
                    + " adm.a_sv_cod=sv.a_sv_cod"
                    + " AND sv.a_sv_flg_aff=1"
                    + " AND adm.g_ti_cod=ti.g_ti_cod"
                    + " AND adm.a_ta_cod != 2"
            )) {
                while (result.next()) {

                    int G_CN_COD = result.getInt(1);
                    int G_TI_COD = result.getInt(2);
                    int G_TA_COD = result.getInt(3);
                    boolean I_RH_COD = result.getBoolean(4);
                    int C_GP_COD = result.getInt(5);
                    int C_GI_COD = result.getInt(6);
                    int A_TA_COD = result.getInt(7);
                    boolean estAffectationPP = (A_TA_COD == 1);
                    int type_internat = result.getInt(8);

                    GroupeAffectationUID groupeId
                            = new GroupeAffectationUID(C_GP_COD, G_TI_COD, G_TA_COD);

                    GroupeAffectation groupe
                            = entree.groupesAffectations.get(groupeId);

                    if (groupe == null) {
                        // peut arriver si les classements ou données d'appel ne sont pas renseignées 
                        groupesManquants.add(groupeId);
                        continue;
                    }

                    GroupeInternatUID internatId
                            = internatUID(type_internat, C_GI_COD, G_TA_COD);

                    GroupeInternat internat = entree.internats.get(internatId);

                    if (internat == null) {
                        Voeu v = new Voeu(
                                G_CN_COD,
                                I_RH_COD,
                                groupe,
                                0,
                                0,
                                Voeu.StatutVoeu.affecteJoursPrecedents,
                                !estAffectationPP);
                        entree.voeux.add(v);
                    } else {
                        Voeu v = new Voeu(
                                G_CN_COD,
                                groupe,
                                0,
                                internat,
                                0,
                                0,
                                Voeu.StatutVoeu.affecteJoursPrecedents,
                                !estAffectationPP);
                        entree.voeux.add(v);
                    }

                }
            }
        }

    }

    /* Récupère les données liées aux meilleurs bacheliers */
    private void recupererDonneesMeilleursBacheliers() throws SQLException {

        LOGGER.info("Récupération du nombre de places MBC");
        try (Statement stmt = conn().createStatement()) {
            stmt.setFetchSize(100_000);
            try (ResultSet result = stmt.executeQuery(
                    "SELECT "
                    + " arec.G_TA_COD,"//id formation affectation
                    + " arec.A_RC_PLA_MBC "//nb places réservées MBC
                    + " FROM A_REC arec"//table des formations
                    + " WHERE "
                    + " NVL(A_RC_FLG_MBC,0) = 1"
            )) {

                while (result.next()) {
                    int G_TA_COD = result.getInt(1);
                    int nb_places = result.getInt(2);
                    entree.nbPlacesMeilleursBacheliers.put(G_TA_COD, nb_places);
                }
            }
        }

        LOGGER.info("Récupération des propositions MBC");
        try (Statement stmt = conn().createStatement()) {
            stmt.setFetchSize(100_000);
            try (ResultSet result = stmt.executeQuery(
                    "SELECT "
                    + " G_CN_COD,"
                    + " G_TA_COD,"//id formation affectation
                    + " I_RH_COD"
                    + " FROM A_ADM adm,A_SIT_VOE asv"
                    + " WHERE adm.a_sv_cod=asv.a_sv_cod"
                    + " AND asv.a_sv_flg_aff=1"
                    + " AND A_AM_FLG_MBC=1"
            )) {

                while (result.next()) {
                    int G_CN_COD = result.getInt(1);
                    int G_TA_COD = result.getInt(2);
                    boolean I_RH_COD = result.getBoolean(3);
                    entree.propositionsMeilleursBacheliers.add(
                            new VoeuUID(G_CN_COD, G_TA_COD, I_RH_COD));
                }
            }
        }

        LOGGER.info("Récupération des MBC et de leurs moyennes au Bac");
        try (Statement stmt = conn().createStatement()) {

            String sql = "SELECT "
                    + "can.G_CN_COD,"
                    + "I_CE_NOT " //moyenne au Bac
                    + " FROM G_CAN can, i_can_epr_bac bac"
                    //groupe de classement
                    + " WHERE can.G_CN_COD=bac.G_CN_COD"
                    + " AND   can.G_CN_FLG_MBC = 1"
                    + " AND   bac.I_EB_COD = 20";/* code de la moyenne générale */

            try (ResultSet result = stmt.executeQuery(sql)) {

                while (result.next()) {
                    int G_CN_COD = result.getInt(1);
                    double moyenne = result.getDouble(2);
                    MeilleurBachelier mb
                            = new MeilleurBachelier(
                                    G_CN_COD,
                                    moyenne
                            );
                    entree.meilleursBacheliers.add(mb);
                }
            }
        }

    }

    /* calcule l'identificateur internat en fonction des données, y compris le flag 
     type_internat  = g_ti_cla_int_uni en base */
    private GroupeInternatUID internatUID(int type_internat, int C_GI_COD, int G_TA_COD) {
        /* on distingue le cas des établissements avec internat unique propre à plusieurs
                    formations et celui des établissements avec des entree.internats propres à chaque formation.
                    Remarque: dans les deux cas les entree.internats peuvent être mixtes ou non, cf doc de ref.
         */
        boolean internatUnique = (type_internat == 1);
        return new GroupeInternatUID(C_GI_COD, internatUnique ? 0 : G_TA_COD);
    }

    private void recupererVoeuxAvecInternatsAClassementPropre() throws SQLException {

        int compteur = 0;

        try (Statement stmt = conn().createStatement()) {
            stmt.setFetchSize(100_000);
            String requete
                    = "SELECT "
                    + "v.g_cn_cod,"//id candidat
                    + "v.g_ta_cod,"//id affectation
                    + "ti.g_ti_cod,"//id inscription
                    + "cg.c_gp_cod,"//groupe de classement pédagogique
                    + "cg.c_cg_ord_app,"//ordre d'appel
                    + "cg.c_cg_ord_app_aff,"//ordre d'appel
                    + "cgi.c_gi_cod,"//id internat
                    + "cgi.c_ci_ran,"//rang de classement internat
                    + "ti.g_ti_cla_int_uni,"//type d'internat (cf notes ci-dessous)
                    + "NVL(v.a_ve_typ_maj,0),"//code modification (annulation démission, modification classements erronés)
                    + "NVL(v.a_ve_ord,0)"//rang du voeu dans le répondeur automatique (null di désactivé)
                    + " FROM "
                    + "g_can c,"//candidats
                    + "a_voe v,"//voeux
                    + "a_sit_voe sv,"//codes situations des voeuxEnAttente
                    + "a_rec_grp rg,"//groupes de classement pédagogique
                    + "c_can_grp cg,"//classements pédagogiques
                    + "a_rec_grp_int rgi,"//groupes de classement internats
                    + "c_can_grp_int cgi,"//classements internats
                    + "g_tri_ins ti"//données formations inscriptions
                    + " WHERE "
                    + " v.i_rh_cod=1"//voeux avec internat
                    + " AND sv.a_sv_flg_att=1"//voeu en attente
                    + " AND cg.i_ip_cod=5" //candidat classé
                    + " AND cgi.i_ip_cod=5" //candidat classé
                    + " AND c.g_ic_cod >= 0" //dossier non-annulé (décès...)
                    + " AND ti.g_ti_eta_cla=2" //classement terminé
                    + " AND c.g_cn_cod=v.g_cn_cod"
                    + " AND cg.c_cg_ord_app is not null"
                    + " AND v.a_sv_cod=sv.a_sv_cod"
                    + " AND v.g_cn_cod=cg.g_cn_cod"
                    + " AND cg.c_gp_cod=rg.c_gp_cod"
                    + " AND rg.g_ta_cod=v.g_ta_cod"
                    + " AND rg.g_ti_cod=ti.g_ti_cod"
                    + " AND v.g_cn_cod=cgi.g_cn_cod"
                    + " AND cgi.c_gi_cod=rgi.c_gi_cod"
                    + " AND "
                    + "( "
                    + "   (rgi.g_ta_cod is null AND rgi.g_ea_cod_ins=ti.g_ea_cod_ins AND rg.g_ti_cod=ti.g_ti_cod) " //internat par établissement -> un seul groupe de classement
                    + "   OR "
                    + "   (rgi.g_ta_cod is not null AND rgi.g_ta_cod=v.g_ta_cod AND rgi.g_ti_cod=ti.g_ti_cod)" //internat par formation
                    + " )"
                    + " AND ti.g_ti_cla_int_uni IN (0,1)" //restriction aux internats à classement propre
                    ;

            LOGGER.log(Level.INFO, "Execution de la requete {0}", requete);

            try (ResultSet result = stmt.executeQuery(requete)) {
                while (result.next()) {
                    int G_CN_COD = result.getInt(1);
                    int G_TA_COD = result.getInt(2);
                    int G_TI_COD = result.getInt(3);
                    int C_GP_COD = result.getInt(4);
                    int rangAppel = result.getInt(5);
                    int rangAppelAffiche = result.getInt(6);
                    int C_GI_COD = result.getInt(7);
                    int C_CI_RAN = result.getInt(8);
                    int type_internat = result.getInt(9);
                    /*
                    g_ti_cla_int_uni = 3 : internat obligatoire
                    g_ti_cla_int_uni = 2 : internat sans élection
                    g_ti_cla_int_uni = -1 : pas d'internat
                    g_ti_cla_int_uni = 0 : l'internat est par formation
                    g_ti_cla_int_uni = 1 :  l'internat est commun
                                            à plusieurs formations de l'établissement
                     */
                    int type_maj = result.getInt(10);
                    int rangRepondeurAutomatique = result.getInt(11);

                    boolean annulationDemission = (type_maj == 1);
                    boolean modificationClassement = (type_maj == 10 || type_maj == 20);

                    GroupeAffectationUID groupeId
                            = new GroupeAffectationUID(C_GP_COD, G_TI_COD, G_TA_COD);
                    GroupeAffectation groupe
                            = entree.groupesAffectations.get(groupeId);

                    if (groupe == null) {
                        /* peut arriver si les classements ou données d'appel ne sont pas renseignées */
                        groupesManquants.add(groupeId);
                        continue;
                    }

                    GroupeInternatUID internatId
                            = internatUID(type_internat, C_GI_COD, G_TA_COD);

                    GroupeInternat internat = entree.internats.get(internatId);
                    if (internat == null) {
                        internatsManquants.add(internatId);
                        continue;
                    }

                    Voeu v = new Voeu(
                            G_CN_COD,
                            groupe,
                            rangAppel,
                            internat,
                            C_CI_RAN,
                            rangRepondeurAutomatique,
                            Voeu.StatutVoeu.enAttenteDeProposition,
                            false
                    );

                    ordresAppelAffiches.put(v.id, rangAppelAffiche);

                    compteur++;
                    if (compteur % 100_000 == 0) {
                        LOGGER.log(Level.INFO, "{0} voeux récupérés", compteur);
                    }

                    if (annulationDemission) {
                        v.setAnnulationDemission();
                    }
                    if (modificationClassement) {
                        v.setCorrectionClassement();
                    }

                    entree.voeux.add(v);

                }
            }
        }
        LOGGER.log(Level.INFO, "{0} voeux en attente avec internat à classement propre", compteur);
    }

    private void recupererVoeuxSansInternatAClassementPropre() throws SQLException {
        int compteur = 0;
        try (Statement stmt = conn().createStatement()) {
            int fetch_size = 500_000;
            stmt.setFetchSize(fetch_size);
            String requete
                    = "SELECT "
                    + "v.g_cn_cod,"//id candidat
                    + "v.g_ta_cod,"//id affectation
                    + "v.i_rh_cod,"//demande internat (1) ou pas (0)
                    + "NVL(v.a_ve_ord,0),"//rang du voeu dans le répondeur automatique (null di désactivé)
                    + "ti.g_ti_cod,"//id inscription
                    + "cg.c_gp_cod,"//groupe de classement pédagogique
                    + "cg.c_cg_ord_app rang,"//ordre d'appel.
                    + "cg.c_cg_ord_app_aff rang_aff,"//ordre d'appel affiché.
                    + "NVL(v.a_ve_typ_maj,0)"//code modification (annulation démission, modification classements erronés)
                    + " FROM "
                    + "g_can c,"//candidats
                    + "a_voe v,"//voeux
                    + "a_sit_voe sv,"//codes situations des voeuxEnAttente
                    + "a_rec_grp rg,"//groupes de classement pédagogique
                    + "c_can_grp cg,"//classements pédagogiques
                    + "g_tri_ins ti"//données formations inscriptions
                    + " WHERE "
                    + " cg.i_ip_cod=5" //candidat classé
                    + " AND c.g_ic_cod >= 0" //dossier non-annulé (décès...)
                    + " AND ti.g_ti_eta_cla=2" //classement terminé
                    + " AND sv.a_sv_flg_att=1"//voeu en attente
                    + " AND cg.C_CG_ORD_APP is not null"
                    + " AND c.g_cn_cod=v.g_cn_cod"
                    + " AND v.a_sv_cod=sv.a_sv_cod"
                    + " AND v.g_cn_cod=cg.g_cn_cod"
                    + " AND cg.c_gp_cod=rg.c_gp_cod"
                    + " AND rg.g_ta_cod=v.g_ta_cod"
                    + " AND rg.g_ti_cod=ti.g_ti_cod"
                    //exclut les formations d'inscriptions avec internat à classement propre
                    + " AND (v.i_rh_cod =0 or ti.g_ti_cla_int_uni NOT IN (0,1)) " //+ " AND v.g_ta_cod= 8972"
                    ;

            LOGGER.log(Level.INFO, "Execution de la requete {0}", requete);

            try (ResultSet result = stmt.executeQuery(requete)) {
                while (result.next()) {
                    int G_CN_COD = result.getInt(1);
                    int G_TA_COD = result.getInt(2);
                    boolean avecInternat = result.getBoolean(3);
                    int rangRepondeurAutomatique = result.getInt(4);
                    int G_TI_COD = result.getInt(5);
                    int C_GP_COD = result.getInt(6);
                    int rangAppel = result.getInt(7);
                    int rangAppelAffiche = result.getInt(8);

                    int type_maj = result.getInt(9);
                    boolean annulationDemission = (type_maj == 1);
                    boolean modificationClassement = (type_maj == 10 || type_maj == 20);

                    GroupeAffectationUID groupeId
                            = new GroupeAffectationUID(C_GP_COD, G_TI_COD, G_TA_COD);
                    GroupeAffectation groupe
                            = entree.groupesAffectations.get(groupeId);

                    if (groupe == null) {
                        /* peut arriver si les classements ou données d'appel ne sont pas renseignées */
                        groupesManquants.add(groupeId);
                        continue;
                    }
                    Voeu voeu = new Voeu(
                            G_CN_COD,
                            avecInternat,
                            groupe,
                            rangAppel,
                            rangRepondeurAutomatique,
                            Voeu.StatutVoeu.enAttenteDeProposition,
                            false
                    );

                    ordresAppelAffiches.put(voeu.id, rangAppelAffiche);

                    compteur++;
                    if (compteur % fetch_size == 0) {
                        LOGGER.log(Level.INFO, "{0} voeux récupérés", compteur);
                    }

                    if (annulationDemission) {
                        voeu.setAnnulationDemission();
                    }

                    if (modificationClassement) {
                        voeu.setCorrectionClassement();
                    }

                    entree.voeux.add(voeu);

                }
            }
        }
        LOGGER.log(Level.INFO, "{0} voeux en attente sans internat a classement propre", compteur);
    }

    private void recupererCandidatsAvecRepondeurAutomatique() throws SQLException {

        entree.candidatsAvecRepondeurAutomatique.clear();

        try (Statement stmt = conn().createStatement()) {
            stmt.setFetchSize(100_000);
            try (ResultSet result = stmt.executeQuery(
                    "SELECT "
                    + " G_CN_COD"//id candidat
                    + " FROM G_CAN can"//table des candidats
                    + " WHERE "
                    + " NVL(can.g_cn_flg_ra,0) = 1"
            )) {

                while (result.next()) {
                    int G_CN_COD = result.getInt(1);
                    entree.candidatsAvecRepondeurAutomatique.add(G_CN_COD);
                }
            }
        }

    }

    /* récupère les voeux en attente pour les quels le candidat a déjà eu une proposition
    dans la même formation, typiquement dans les formations avec internat.
    Utilisé pour le calcul du rang sur liste d'attente.
     */
    private Set<VoeuUID> recupererVoeuxAvecPropositionAnterieureDansMemeFormation() throws SQLException {

        LOGGER.info("Récupération des voeux en attente dont les candidats "
                + "ont déjà eu une proposition dans la même formation");

        Set<VoeuUID> voeux = new HashSet<>();

        try (Statement stmt = conn().createStatement()) {
            stmt.setFetchSize(100_000);
            String sql = "SELECT "
                    + " v.G_CN_COD,v.G_TA_COD,v.I_RH_COD"
                    + " FROM "
                    + " A_VOE v,"//voeu
                    + " A_SIT_VOE sv,"//etat voeu 
                    + " A_ADM adm"//proposition d'admission
                    + " WHERE "
                    + " v.a_sv_cod = sv.a_sv_cod"
                    + " AND sv.a_sv_flg_att=1"
                    + " AND v.G_CN_COD=adm.G_CN_COD"
                    + " AND v.G_TA_COD=adm.G_TA_COD"
                    + " AND adm.A_TA_COD=1"; //admission en procédure normale

            LOGGER.info(sql);

            try (ResultSet result = stmt.executeQuery(sql)) {

                while (result.next()) {
                    int G_CN_COD = result.getInt(1);
                    int G_TA_COD = result.getInt(2);
                    boolean I_RH_COD = result.getBoolean(3);
                    voeux.add(new VoeuUID(G_CN_COD, G_TA_COD, I_RH_COD));
                }
            }
            LOGGER.info(voeux.size() + " voeux récupérés");
        }

        return voeux;
    }

    /* flag permettant de vérifier l'interruption des données entrantes pendant le calcul des propositions */
    private static final int indexFlagInterruptionDonnéeesEntrantes = 31;

    /* flag permettant de signaler un problème lors du calcul des propositions */
    private static final int indexFlagAlerte = 34;

    /* index de table stockant la date du début de campagen */
    private static final int indexDateDebutDeCampagne = 35;

    /* index de table stockant la date de l'ouverture complète des internats */
    private static final int indexDateOuvertureCompleteInternats = 334;

    private static final Logger LOGGER = Logger.getLogger(ConnecteurDonneesPropositionsOracle.class.getSimpleName());

}
