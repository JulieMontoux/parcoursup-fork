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
package fr.parcoursup.algos.propositions.donnees;

import fr.parcoursup.algos.donnees.ConnecteurSQL;
import fr.parcoursup.algos.exceptions.AccesDonneesException;
import fr.parcoursup.algos.exceptions.AccesDonneesExceptionMessage;
import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.propositions.affichages.AlgosAffichages;
import fr.parcoursup.algos.propositions.algo.*;

import java.sql.*;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static fr.parcoursup.algos.donnees.ConnecteurSQL.*;
import static fr.parcoursup.algos.donnees.SQLStringsConstants.*;


/*
    Récupération et injection des données depuis et vers la base de données

    La base identifie:

    * chaque candidat par un gCnCod
    * chaque formation d'inscription par un gTiCod
    * chaque formation d'affectation par un gTaCod
    * chaque commission de classement pédagogique des voeuxEnAttente par un cGpCod
    * chaque commission de classement internat des voeuxEnAttente par un cGiCod

    Plus de détails dans doc/implementation.txt
 */
public class ConnecteurDonneesPropositionsSQL implements ConnecteurDonneesPropositions {

    /* connexion a la base de données */
    private final Connection connection;

    public ConnecteurDonneesPropositionsSQL(
            Connection connection) {
        this.connection = connection;
        this.verifierInterruptionFluxDonneesEntrantes = true;
        this.recupererSeulementVoeuxEnAttente = true;
        this.sparseDataTestingMode = 0;
        this.simulationAvantDebutCampagne = false;
    }

    /* ce connecteur est utilisé par le simulateur */
    public ConnecteurDonneesPropositionsSQL(
            Connection connection,
            boolean verifierInterruptionFluxDonneesEntrantes,
            boolean recupererSeulementVoeuxEnAttente,
            int sparseDataTestingMode,
            boolean utiliserRangSiPasOrdreAppel
    ) {
        this.connection = connection;
        this.verifierInterruptionFluxDonneesEntrantes = verifierInterruptionFluxDonneesEntrantes;
        this.recupererSeulementVoeuxEnAttente = recupererSeulementVoeuxEnAttente;
        this.sparseDataTestingMode = sparseDataTestingMode;
        this.simulationAvantDebutCampagne = utiliserRangSiPasOrdreAppel;
    }

    final boolean verifierInterruptionFluxDonneesEntrantes;
    final boolean recupererSeulementVoeuxEnAttente;
    final int sparseDataTestingMode;//mode dans lequel seul une partie des données est résupérée
    final boolean simulationAvantDebutCampagne;

    private final static String NB_JRS_EQUALS = " NB_JRS=? ";

    /* variable stockant les données d'entrée pendant la récupération */
    AlgoPropositionsEntree entree = null;

    public void initialiserAlgoPropositionsEntree() throws AccesDonneesException, SQLException {
        LOGGER.info("Vérification de l'interruption du flux de données entrantes.");
            /* Si = 1 indique que le programme d'admission est en train de tourner
            pour faire des propositions. Si c'est le cas, tout est bloqué */
        try (PreparedStatement ps = connection.prepareStatement(
                SELECT + "g_pr_val" + FROM + G_PAR + WHERE + "g_pr_cod=?")) {
            ps.setInt(1, INDEX_FLAG_INTERRUP_DONNEES);
            try (ResultSet result = ps.executeQuery()) {
                result.next();
                boolean estVerouille = result.getBoolean(1);
                if (verifierInterruptionFluxDonneesEntrantes
                        && !estVerouille) {
                    throw new AccesDonneesException(AccesDonneesExceptionMessage.CONNECTEUR_DONNEES_PROPOSITIONS_SQL_MAUVAIS_G_PR_COD, INDEX_FLAG_INTERRUP_DONNEES);
                }
            }
        }

        LOGGER.info("Récupération du nombre de jours depuis l'ouverture de la campagne");
        int nbJoursCampagne = nbJoursDepuisDebutCampagne() + 1;
        LOGGER.log(Level.INFO, "{0} jours.", nbJoursCampagne);

        LOGGER.info("Récupération du nombre de jours de campagne à la date pivot");
        int nbJoursCampagneDatePivotInternats
                = nbJoursEntreDebutCampagneEtOuvertureInternats();
        LOGGER.log(Level.INFO, "{0} jours.", nbJoursCampagneDatePivotInternats);

        Parametres parametres = new Parametres(nbJoursCampagne, nbJoursCampagneDatePivotInternats);
        entree = new AlgoPropositionsEntree(parametres);

    }

    @Override
    public AlgoPropositionsEntree recupererDonnees() throws AccesDonneesException {

        try {

            initialiserAlgoPropositionsEntree();

            LOGGER.info("Récupération des candidats ayant activé le répondeur automatique");
            recupererCandidatsAvecRepondeurAutomatique();

            LOGGER.info("Récupération des groupes d'affectation");
            entree.groupesAffectations.clear();
            boolean retroCompatibilityMode = !verifierInterruptionFluxDonneesEntrantes;
            Map<GroupeAffectationUID, GroupeAffectation> groupes
                    = recupererGroupesAffectation(
                    entree.getParametres(),
                    retroCompatibilityMode);
            for (GroupeAffectation g : groupes.values()) {
                entree.ajouter(g);
            }

            LOGGER.info("Récupération des internats");
            entree.internats.clear();
            for (GroupeInternat internat : recupererInternats().values()) {
                entree.internatsIndex.indexer(internat.id);
                entree.ajouter(internat);
            }

            LOGGER.info("Récupération des voeux en attente avec demande internat dans un internat ayant son propre classement");
            recupererVoeuxAvecInternatsAClassementPropre(entree.internatsIndex, recupererSeulementVoeuxEnAttente);

            LOGGER.info("Récupération des voeux en attente sans internat, ou avec internat n'ayant pas son propre classemnt");
            recupererVoeuxSansInternatAClassementPropre(recupererSeulementVoeuxEnAttente);

            LOGGER.info("Récupération des propositions non refusées ");
            recupererPropositionsActuelles(entree.internatsIndex);

            if (!groupesManquants.isEmpty()) {
                LOGGER.log(Level.SEVERE, "{0} groupes manquants.", groupesManquants.size());
            }

            if (!internatsManquants.isEmpty()) {
                LOGGER.log(Level.SEVERE, "{0} internats manquants.", internatsManquants.size());
            }

        } catch (SQLException ex) {
            throw new AccesDonneesException(AccesDonneesExceptionMessage.CONNECTEUR_DONNEES_PROPOSITIONS_SQL_RECUPERATION, ex);
        } catch (VerificationException ex) {
            throw new AccesDonneesException(AccesDonneesExceptionMessage.CONNECTEUR_DONNEES_PROPOSITIONS_SQL_ENTREE, ex);
        }

        LOGGER.info("Fin de la récupération des données depuis la base de données");

        return entree;

    }

    /* exportation des résultats du calcul: propositions à faire */
    @Override
    public void exporterDonnees(AlgoPropositionsSortie sortie) throws VerificationException, AccesDonneesException {
        exporterDonnees(sortie, true);
    }

    public void exporterDonnees(
            AlgoPropositionsSortie sortie,
            boolean yComprisAffichages)
            throws VerificationException, AccesDonneesException {
        try {
            connection.setAutoCommit(false);

            /* Si il y a eu un problème lors de l'export, on le signale via ce flag */
            if (sortie.getAlerte()) {
                try (PreparedStatement ps = connection.prepareStatement(
                        UPDATE + G_PAR + SET + "G_PR_VAL=1" + WHERE + "G_PR_COD =?")) {
                    ps.setInt(1, INDEX_FLAG_ALERTE);
                    ps.execute();
                }
                connection.commit();
            }

            LOGGER.info("Exportation des propositions d'admission");
            exporterNouvellesPropositionsAdmission(sortie);
            connection.commit();

            LOGGER.info("Exportation des démissions");
            exporterDemissionsAutomatiques(sortie);
            connection.commit();

            LOGGER.info("Exportation des prédictions des derniers appelés à la date pivot");
            exporterPredicteurRangDernierAppele(sortie.groupes, sortie.parametres);
            connection.commit();

            if (yComprisAffichages) {
                LOGGER.info("Exportation des affichages");
                exporterAffichages(sortie);
                connection.commit();
            }

        } catch (SQLException ex) {
            throw new AccesDonneesException(AccesDonneesExceptionMessage.CONNECTEUR_DONNEES_PROPOSITIONS_SQL_EXPORT, ex);
        }
    }

    /* exportation des données affichées: rangs sur liste d'attente et rangs
    des dernier appelés. */
    public void exporterAffichages(AlgoPropositionsSortie sortie) throws SQLException, VerificationException {

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

        LOGGER.info("Mise à jour des affichages candidats");
        AlgosAffichages.mettreAJourAffichages(sortie,
                voeuxAvecPropositionDansMemeFormation,
                propositionsDuJour
        );

        LOGGER.info("Exportation des rangs des barres des derniers appelés pour les voeux sans internat");
        exporterBarresAfficheesVoeuxSansInternat(sortie);
        connection.commit();

        LOGGER.info("Exportation des rangs des barres des derniers appelés  pour les voeux avec internat");
        exporterBarresAfficheesVoeuxAvecInternat(sortie);
        connection.commit();

        LOGGER.info("Exportation des rangs sur liste d'attente");
        exporterRangsSurListeAttente(sortie);
        connection.commit();

        LOGGER.info("Fin de l'exportation des affichages");
    }

    private static void ajouterVoeu(
            PreparedStatement ps,
            Voeu voe,
            boolean ajouterCGiCod
    ) throws SQLException {
        GroupeAffectationUID groupe = voe.groupeUID;
        GroupeInternatUID internat = voe.internatUID;
        if(voe.id.gTaCod != groupe.gTaCod) {
            //non-atteignable
            throw new AssertionError("ajouterVoeu: les gtacod du voeu et du groupe doivent correspondre");
        }
        ps.setInt(1, voe.id.gCnCod);
        ps.setInt(2, voe.id.gTaCod);
        ps.setInt(3, voe.id.iRhCod ? 1 : 0);
        ps.setInt(4, groupe.cGpCod);
        ps.setInt(5, groupe.gTiCod);
        if (ajouterCGiCod) {
            ps.setInt(6, (internat == null) ? 0 : internat.cGiCod);
        }
    }

    public void exporterNouvellesPropositionsAdmission(
            AlgoPropositionsSortie sortie
    ) throws SQLException {
        LOGGER.info("Préparation de la table A_ADM_PROP avant export");
        try (PreparedStatement ps
                = connection.prepareStatement(
                        DELETE_FROM + ADMISSIONS_TABLE_SORTIE + WHERE + NB_JRS_EQUALS)) {
            ps.setInt(1, sortie.parametres.nbJoursCampagne);
            ps.execute();
        }

        try (PreparedStatement ps = connection.prepareStatement(
                INSERT_INTO + ADMISSIONS_TABLE_SORTIE
                + "(G_CN_COD,g_ta_cod,I_RH_COD,C_GP_COD,G_TI_COD,C_GI_COD,A_AM_FLG_MBC,NB_JRS)"
                + VALUES + "(?,?,?,?,?,?,0,?)")) {
            int count = 0;

            for (Voeu voe : sortie.voeux) {
                if (voe.estPropositionDuJour()) {
                    ajouterVoeu(ps, voe, true);
                    ps.setInt(7, sortie.parametres.nbJoursCampagne);
                    addToBatchAndExecuteIfCounter(ps, ++count, 100_000);
                }
            }

            ps.executeBatch();
            LOGGER.log(Level.INFO, "{0} propositions exportées.", count);

        }

    }

    private void exporterDemissionsAutomatiques(AlgoPropositionsSortie sortie) throws SQLException {

        LOGGER.info("Préparation de la table A_ADM_DEM_RA avant export");
        try (PreparedStatement ps
                = connection.prepareStatement(DELETE_FROM + A_ADM_DEM_RA + WHERE + NB_JRS_EQUALS)) {
            ps.setInt(1, sortie.parametres.nbJoursCampagne);
            ps.execute();
        }

        try (PreparedStatement ps = connection.prepareStatement(
                INSERT_INTO + A_ADM_DEM_RA
                + "(G_CN_COD,g_ta_cod,I_RH_COD,C_GP_COD,G_TI_COD,C_GI_COD,EST_DEM_PROP,NB_JRS)"
                + VALUES + "(?,?,?,?,?,?,?,?)")) {
            int count = 0;

            for (Voeu voe : sortie.voeux) {

                if (voe.estDemissionAutomatique()) {
                    ajouterVoeu(ps, voe, true);
                    ps.setBoolean(7, voe.estDemissionAutomatiqueProposition());
                    ps.setInt(8, sortie.parametres.nbJoursCampagne);
                    addToBatchAndExecuteIfCounter(ps, ++count, 100_000);
                }
            }

            ps.executeBatch();
            LOGGER.log(Level.INFO, "{0} démissions automatiques exportées.", count);

        }
    }

    private void exporterPredicteurRangDernierAppele(Collection<GroupeAffectation> groupes,
            Parametres parametres) throws SQLException {

        LOGGER.info("Préparation de la table A_ADM_PRED_DER_APP avant export");
        try (PreparedStatement ps
                = connection.prepareStatement(DELETE_FROM + A_ADM_PRED_DER_APP + WHERE + NB_JRS_EQUALS)) {
            ps.setInt(1, parametres.nbJoursCampagne);
            ps.execute();
        }

        LOGGER.info("Export de " + groupes.size() + " prédicteurs dans " + A_ADM_PRED_DER_APP);
        try (PreparedStatement ps = connection.prepareStatement(
                  INSERT_INTO + A_ADM_PRED_DER_APP
                + "(g_ta_cod,C_GP_COD,A_RG_RAN_DER,NB_JRS)"
                + VALUES + "(?,?,?,?)")) {

            for (GroupeAffectation g : groupes) {
                ps.setInt(1, g.id.gTaCod);
                ps.setInt(2, g.id.cGpCod);
                ps.setInt(3,
                        (g.estimationRangDernierAppeleADatePivot == Integer.MAX_VALUE)
                                ? -1
                                : g.estimationRangDernierAppeleADatePivot
                );
                ps.setInt(4, parametres.nbJoursCampagne);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void exporterBarresAfficheesVoeuxAvecInternat(AlgoPropositionsSortie sortie) throws SQLException, VerificationException {

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
        mettreAJourBarresInternatsEnAugmentation(sortie);

        LOGGER.info("Préparation de la table A_REC_GRP_INT_PROP avant export");
        try (PreparedStatement ps = connection.prepareStatement(
                DELETE_FROM + A_REC_GRP_INT_PROP + WHERE
                        + "C_GI_COD !=0 " + AND + " NB_JRS=?")) {
            ps.setInt(1, sortie.parametres.nbJoursCampagne);
            int deleted = ps.executeUpdate();
            LOGGER.info(deleted + " entrées ont été supprimées de " + A_REC_GRP_INT_PROP);
        }

        LOGGER.info("Export dans la table A_REC_GRP_INT_PROP");
        try (PreparedStatement ps = connection.prepareStatement(
                 INSERT_INTO + A_REC_GRP_INT_PROP
                + "(C_GI_COD,g_ta_cod,G_TI_COD,C_GP_COD,A_RG_RAN_DER,A_RG_RAN_DER_INT,NB_JRS)"
                + VALUES + "(?,?,?,?,?,?,?)")) {

            int nb = 0;
            for (GroupeInternat internat : sortie.internats) {
                for (GroupeAffectation g : internat.groupesConcernes()) {
                    Integer barreAppelAffichee = internat.barresAppelAffichees.get(g.id);
                    Integer barreInternatAffichee = internat.barresInternatAffichees.get(g.id);
                    if (barreAppelAffichee != null && barreInternatAffichee != null) {
                        ps.setInt(1, internat.id.cGiCod);
                        ps.setInt(2, g.id.gTaCod);
                        ps.setInt(3, g.id.gTiCod);
                        ps.setInt(4, g.id.cGpCod);
                        ps.setInt(5, barreAppelAffichee);
                        ps.setInt(6, barreInternatAffichee);
                        ps.setInt(7, sortie.parametres.nbJoursCampagne);
                        ps.addBatch();
                        nb++;
                    }
                }
            }
            ps.executeBatch();
            LOGGER.info(nb + " entrees ont été exportées dans la table " + A_REC_GRP_INT_PROP + " terminé.");
        }
    }

    private void mettreAJourBarresInternatsEnAugmentation(AlgoPropositionsSortie sortie) throws SQLException, VerificationException {
        /* une map est initialisée pour améliorer les performances 
        dans la requête suivante */
        final Map<GroupeInternatUID, GroupeInternat> internats = new HashMap<>();
        for (GroupeInternat internat : sortie.internats) {
            internats.put(internat.id, internat);
        }

        /* on compare avec les valeurs au jour n-1 et on met à jour
                si la relation d'inclusion est vérifiée */
        try (PreparedStatement stmt = connection.prepareStatement(
                SELECT + " C_GI_COD,"
                + "g_ta_cod,"
                + "prop.G_TI_COD,"
                + "C_GP_COD,"
                + "MAX(A_RG_RAN_DER), "
                + "MAX(A_RG_RAN_DER_INT)"
                + FROM + A_REC_GRP_INT_PROP + " prop"
                + WHERE + "(NB_JRS < ?)"
                + AND + "C_GI_COD != 0"
                + GROUP_BY + "C_GI_COD,g_ta_cod,prop.G_TI_COD,C_GP_COD"
        )) {
            stmt.setFetchSize(100_000);
            stmt.setInt(1, sortie.parametres.nbJoursCampagne);

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    int cGiCod = result.getInt(1);
                    int gTaCod = result.getInt(2);
                    int gTiCod = result.getInt(3);
                    int cGpCod = result.getInt(4);
                    int dernierAppeleRangAppe = result.getInt(5);
                    int dernierAppeleClassementInternat = result.getInt(6);

                    GroupeAffectationUID groupeUID = new GroupeAffectationUID(cGpCod, gTiCod, gTaCod);
                    GroupeInternatUID internatUID = sortie.indexInternats.getInternat(cGiCod, groupeUID);
                    GroupeInternat internat = internats.get(internatUID);

                    if (internat == null) {
                        sortie.setAvertissement();
                        LOGGER.log(Level.WARNING, "exporterBarresAfficheesVoeuxAvecInternat: disparition suspecte de l''internat {0}", internatUID);
                        continue;
                    }

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
        try (PreparedStatement stmt = connection.prepareStatement(
                 SELECT + "g_ta_cod,G_TI_COD,C_GP_COD,MAX(A_RG_RAN_DER)"
                + FROM  + A_REC_GRP_INT_PROP
                + WHERE + "(NB_JRS < ?)"
                + AND + "C_GI_COD "  + EQUALS_ZERO
                + GROUP_BY + "C_GI_COD,g_ta_cod,G_TI_COD,C_GP_COD")) {

            stmt.setInt(1, sortie.parametres.nbJoursCampagne);
            stmt.setFetchSize(100_000);

            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    int gTaCod = result.getInt(1);
                    int gTiCod = result.getInt(2);
                    int cGpCod = result.getInt(3);
                    int dernierAppeleRangAppel = result.getInt(4);

                    GroupeAffectation groupe
                            = groupes.get(new GroupeAffectationUID(cGpCod, gTiCod, gTaCod));

                    if (groupe != null) {
                        groupe.setRangDernierAppeleAffiche(
                                Math.max(dernierAppeleRangAppel, groupe.getRangDernierAppeleAffiche())
                        );
                    }
                }
            }
        }

        LOGGER.info("Préparation de la table " + A_REC_GRP_INT_PROP + " avant export");
        try (PreparedStatement ps = connection.prepareStatement(
                DELETE_FROM +  A_REC_GRP_INT_PROP + " " + WHERE + "C_GI_COD=0" + AND + "NB_JRS=?")) {
            ps.setInt(1, sortie.parametres.nbJoursCampagne);
            int deleted = ps.executeUpdate();
            LOGGER.info(deleted + " entrées ont été supprimées de " + A_REC_GRP_INT_PROP);
        }

        LOGGER.info("Export de "  + sortie.groupes.size() +" groupes dans la table " + A_REC_GRP_INT_PROP);
        try (PreparedStatement ps = connection.prepareStatement(
                INSERT_INTO +  A_REC_GRP_INT_PROP
                + "(C_GI_COD,g_ta_cod,G_TI_COD,C_GP_COD,A_RG_RAN_DER,A_RG_RAN_DER_INT,NB_JRS)"
                + VALUES + "(0,?,?,?,?,0,?)")) {
            for (GroupeAffectation g : sortie.groupes) {
                ps.setInt(1, g.id.gTaCod);
                ps.setInt(2, g.id.gTiCod);
                ps.setInt(3, g.id.cGpCod);
                ps.setInt(4, g.getRangDernierAppeleAffiche());
                ps.setInt(5, sortie.parametres.nbJoursCampagne);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void exporterRangsSurListeAttente(AlgoPropositionsSortie sortie) throws SQLException {

        LOGGER.info("Préparation de la table A_VOE_PROP avant export");
        try (PreparedStatement ps = connection.prepareStatement(DELETE_FROM + A_VOE_PROP + WHERE + "NB_JRS=?")) {
            ps.setInt(1, sortie.parametres.nbJoursCampagne);
            ps.execute();
        }

        LOGGER.log(Level.INFO, "Exportation des rangs sur liste d''attente " +
                "de {0} voeux dans la table " + A_VOE_PROP, sortie.voeux.size());

        try (PreparedStatement ps = connection.prepareStatement(
                INSERT_INTO + A_VOE_PROP
                + "(G_CN_COD,g_ta_cod,I_RH_COD,C_GP_COD,G_TI_COD,A_VE_RAN_LST_ATT,NB_JRS)"
                + VALUES + "(?,?,?,?,?,?,?)")) {
            int count = 0;

            for (Voeu voe : sortie.voeux) {

                /* Le rang sur liste d'attente ne concerne que les voeux
                en ettente  dans des formations sans internat à classement propre.
                 */
                if (voe.getRangListeAttente() > 0) {
                    ajouterVoeu(ps, voe, false);
                    ps.setInt(6, voe.getRangListeAttente());
                    ps.setInt(7, sortie.parametres.nbJoursCampagne);
                    addToBatchAndExecuteIfCounter(ps,++count, 100_000);
                }
            }

            ps.executeBatch();
            LOGGER.log(Level.INFO, "{0} rangs sur liste d attente exportés.", count);

        }

    }

    private void addToBatchAndExecuteIfCounter(PreparedStatement ps, int count, int bound) throws SQLException {
        ps.addBatch();
        if (++count % bound== 0) {
            LOGGER.log(Level.INFO, "Exportation des voeux {0} a {1}",
                    new Object[]{count - bound, count});
            ps.executeBatch();
            ps.clearBatch();
        }
    }

    /* permet de comptabiliser les entree.internats manquants, avant le début de campagne */
    private final Set<GroupeInternatUID> internatsManquants
            = new HashSet<>();

    /* permet de comptabiliser les groupes manquants, avant le début de campagne */
    private final Set<GroupeAffectationUID> groupesManquants = new HashSet<>();

    boolean checkColumnExists(String tableName, String colName) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            try (ResultSet result
                         = stmt.executeQuery(
                    SELECT + "* FROM user_tab_cols "
                            + WHERE + "upper(column_name) = '" + colName.toUpperCase() + "'"
                            + AND + "upper(table_name) = '" + tableName.toUpperCase() + "'")) {
                return result.next();
            }
            catch (org.h2.jdbc.JdbcSQLSyntaxErrorException ex) {
                if(ex.getMessage().contains("Table \"USER_TAB_COLS\" not found")) {
                    return true;
                }
                else {
                    throw ex;
                }
            }
        }
    }

    /*@
    Le flag retroCompatibitilite permet d'adapter la requete à 
    la non existence du champ a_rg_flg_adm_stop sur les anciennes bases (avant 2020).
    Ce flag n'est pas utilisé en prod.
     */
    public Map<GroupeAffectationUID, GroupeAffectation>
            recupererGroupesAffectation(
                    Parametres parametres,
                    boolean retroCompatibitilite)
            throws SQLException, VerificationException {

        Map<GroupeAffectationUID, GroupeAffectation> resultat = new HashMap<>();

        boolean colAdmStopExists = true;
        boolean colFinReservationInternatExists = true;

        //Ce mode permet d'exécuter l'algorithme d'appel  et le simulateur sur des bases archivées
        if (retroCompatibitilite) {
            colAdmStopExists = checkColumnExists(RECRUTEMENTS_GROUPES_TABLE, "a_rg_flg_adm_stop");
            colFinReservationInternatExists = checkColumnExists(RECRUTEMENTS_FORMATIONS_TABLE, "a_rc_flg_fin_res_pla");
        } 

        try (Statement stmt = connection.createStatement()) {
            stmt.setFetchSize(1_000_000);
            String sql = SELECT + "C_GP_COD,"
                    + " rec.G_TI_COD,"
                    + " rec.g_ta_cod,"
                    + (simulationAvantDebutCampagne
                    ? " NVL(A_RG_NBR_SOU,A_RG_PLA) capacite,"
                    : " A_RG_NBR_SOU capacite,")
                    + " NVL(a_rg_ran_lim,0),"
                    + "(SELECT MAX(NVL(C_CG_ORD_APP,-1))"
                    + FROM
                    + ADMISSIONS_TABLE + " adm,"
                    + CLASSEMENTS_TABLE + " cla "
                    + WHERE
                    + " adm.g_cn_cod=cla.g_cn_cod"
                    + AND + " adm.g_ta_cod=rec.g_ta_cod"
                    + (sparseDataTestingMode > 0 ? AND + " MOD(adm.g_ta_cod," + sparseDataTestingMode + ")" + EQUALS_ZERO : "")
                    + AND + " adm.g_ti_cod=rec.g_ti_cod"
                    + AND + " adm.c_gp_cod=rec.c_gp_cod"
                    + AND + " cla.c_gp_cod=rec.c_gp_cod"
                    + AND + " adm.a_ta_cod=1" //proc principale
                    + AND + " cla.i_ip_cod=5" //classé
                    + AND + " cla.c_cg_ord_app is not null"
                    + ")"
                    + " rangDernierAppele,"
                    + (colAdmStopExists ? "NVL(rec.a_rg_flg_adm_stop,0)," : "0,")
                    + (colFinReservationInternatExists ? "NVL(r.a_rc_flg_fin_res_pla,0)" : "0")
                    + FROM
                    + RECRUTEMENTS_GROUPES_TABLE + " rec, "
                    + RECRUTEMENTS_FORMATIONS_TABLE +  " r "
                    + WHERE + "rec.g_ta_cod=r.g_ta_cod";

            LOGGER.warning(sql);
            try (ResultSet result = stmt.executeQuery(sql)) {

                while (result.next()) {

                    int cGpCod = result.getInt(1);
                    int gTiCod = result.getInt(2);
                    int gTaCod = result.getInt(3);
                    int nbRecrutementsSouhaite = result.getInt(4);
                    int rangLimite = result.getInt(5);/* peut être null, vaut 0 dans ce cas */
                    int rangDernierAppele = result.getInt(6);
                    int blocageAdmissionGroupe = (colAdmStopExists ? result.getInt(7) : 0);
                    boolean finReservationPlacesInternat = (colFinReservationInternatExists && result.getInt(8) == 1);
                    if (blocageAdmissionGroupe != 0) {
                        nbRecrutementsSouhaite = 0;
                        rangLimite = 0;
                    }

                    GroupeAffectationUID id
                            = new GroupeAffectationUID(cGpCod, gTiCod, gTaCod);

                    GroupeAffectation g = new GroupeAffectation(
                            nbRecrutementsSouhaite,
                            id,
                            rangLimite,
                            rangDernierAppele,
                            parametres);
                    if(finReservationPlacesInternat) {
                        g.setFinDeReservationPlacesInternats();
                    }
                    resultat.put(id, g);

                }
            }
        }
        return resultat;
    }

    public Map<GroupeInternatUID, GroupeInternat> recupererInternats() throws SQLException, VerificationException {
        Map<GroupeInternatUID, GroupeInternat> resultat = new HashMap<>();
        try (Statement stmt = connection.createStatement()) {
            stmt.setFetchSize(100_000);

            try (ResultSet result = stmt.executeQuery(
                    SELECT + "C_GI_COD, NVL(g_ta_cod,0), NVL(g_ti_cod,0),A_RI_NBR_SOU "
                    + FROM + A_REC_GRP_INT)) {
                while (result.next()) {
                    int cGiCod = result.getInt(1);
                    int gTaCod = result.getInt(2);
                    int gTiCod = result.getInt(3);
                    int nbPlacesTotal = result.getInt(4);

                    GroupeInternatUID id
                            = new GroupeInternatUID(cGiCod, gTiCod, gTaCod);

                    resultat.put(id,
                            new GroupeInternat(
                                    id,
                                    nbPlacesTotal));
                }
            }
        }
        return resultat;
    }

    /* Récupère un nombre de jours dans la table gpar */
    private int nbJoursDepuisDebutCampagne() throws SQLException, AccesDonneesException {

        Integer nbJours = null;
        
       

        try (PreparedStatement ps = connection.prepareStatement(
                SELECT + "SYSDATE,TO_DATE(g_par.g_pr_val, 'DD/MM/YYYY:HH24MI')" +
                FROM + G_PAR + WHERE + "g_pr_cod=?")) {
            ps.setInt(1, ConnecteurDonneesPropositionsSQL.INDEX_DATE_DEBUT_CAMPAGNE);
            try (ResultSet result = ps.executeQuery()) {
                while (result.next()) {
                    java.sql.Timestamp maintenant = result.getTimestamp(1);
                    java.sql.Timestamp  dateDebutCampagne = result.getTimestamp(2);
                    if (maintenant == null || dateDebutCampagne == null) {
                        throw new AccesDonneesException(AccesDonneesExceptionMessage.CONNECTEUR_DONNEES_PROPOSITIONS_SQL_DATE);
                    }
                    nbJours = Math.toIntExact(Duration.between(
                            dateDebutCampagne.toLocalDateTime(),
                            maintenant.toLocalDateTime()
                            ).toDays());
                }
            }
        }
        if(nbJours == null) {
            throw new AccesDonneesException(AccesDonneesExceptionMessage.CONNECTEUR_DONNEES_PROPOSITIONS_SQL_DATE);
        } else {
            return nbJours;
        }
    }

    /* Récupère un nombre de jours dans la table gpar */
    private int nbJoursEntreDebutCampagneEtOuvertureInternats() throws SQLException, AccesDonneesException {

        Integer nbJours = null;

        try (PreparedStatement ps = connection.prepareStatement(
                SELECT + ""+
                        "TO_DATE(g_par1.g_pr_val, 'DD/MM/YYYY:HH24MI'), " +
                        "TO_DATE(g_par2.g_pr_val, 'DD/MM/YYYY:HH24MI')" +
                         FROM + " g_par g_par1,g_par g_par2 " +
                        WHERE + " g_par1.g_pr_cod=?" +
                        AND + " g_par2.g_pr_cod=?")) {
            ps.setInt(1, ConnecteurDonneesPropositionsSQL.INDEX_DATE_DEBUT_CAMPAGNE);
            ps.setInt(2, ConnecteurDonneesPropositionsSQL.INDEX_DATE_OUV_COMP_INTERNATS);

            try (ResultSet result = ps.executeQuery()) {
                while (result.next()) {
                    java.sql.Timestamp dateDebutCampagne = result.getTimestamp(1);
                    java.sql.Timestamp dateOuvertureInternat = result.getTimestamp(2);
                    if (dateDebutCampagne == null || dateOuvertureInternat == null) {
                        throw new AccesDonneesException(AccesDonneesExceptionMessage.CONNECTEUR_DONNEES_PROPOSITIONS_SQL_DATE);
                    }
                    nbJours = Math.toIntExact(Duration.between(
                            dateDebutCampagne.toLocalDateTime(),
                            dateOuvertureInternat.toLocalDateTime()
                            ).toDays());
                }
            }
        }
        if (nbJours == null) {
            throw new AccesDonneesException(AccesDonneesExceptionMessage.CONNECTEUR_DONNEES_PROPOSITIONS_SQL_DATE);
        }
        return nbJours;
    }

    /* Récupère les voeux sur lesquels il y a une proposition d'admission qui
    bloque une place car elle est soit en attente de réponse du candidat soit 
    acceptée par le candidat. 
     */
    private void recupererPropositionsActuelles(IndexInternats indexInternats)
            throws SQLException, VerificationException {

        try (Statement stmt = connection.createStatement()) {
            stmt.setFetchSize(100_000);
            try (ResultSet result = stmt.executeQuery(SELECT
                    + "adm.G_CN_COD,"//id candidat
                    + "adm.G_TI_COD, "//id formation affectation
                    + "adm.g_ta_cod, "//id formation affectation
                    + "adm.I_RH_COD, "//proposition avec internat
                    + "adm.C_GP_COD, "
                    + "adm.C_GI_COD, "
                    + "adm.A_TA_COD, "
                    // A_TA_CODs
                    // 1 admission en PP 
                    // 2 apprentissage 
                    // 3 admission en PC 
                    // 5 admission en CAES 
                    // 10 inscription par l'établissement
                    + "NVL(voe.a_ve_ord,0) " //ordre du voeu dans le RA
                    + FROM + " "
                    +  ADMISSIONS_TABLE + " adm "//table des admissions
                    + " LEFT JOIN "
                    + VOEUX_TABLE + " voe "
                    + " ON adm.g_cn_cod=voe.g_cn_cod "
                    + AND + " adm.g_ta_cod=voe.g_ta_cod "
                    + AND + " adm.i_rh_cod=voe.i_rh_cod, "
                    + STATUTS_VOEUX_TABLE + " sv,"//statut du voeu
                    + FOR_INSCRIPTIONS_TABLE + " ti"//données formations inscriptions
                    + WHERE
                    + " adm.a_sv_cod=sv.a_sv_cod"
                    + AND + " sv.a_sv_flg_aff=1"
                    + AND + " adm.g_ti_cod=ti.g_ti_cod"
                    + AND + " adm.a_ta_cod != 2"
                    + (sparseDataTestingMode > 0 ? AND + " MOD(adm.g_cn_cod," + sparseDataTestingMode + ")" + EQUALS_ZERO
                            + AND + "MOD(adm.g_ta_cod," + sparseDataTestingMode + ")" + EQUALS_ZERO : "")

            )) {
                while (result.next()) {

                    int gCnCod = result.getInt(1);
                    int gTiCod = result.getInt(2);
                    int gTaCod = result.getInt(3);
                    boolean iRhCod = result.getBoolean(4);
                    int cGpCod = result.getInt(5);
                    int cGiCod = result.getInt(6);
                    int aTaCod = result.getInt(7);
                    boolean estAffectationPP = (aTaCod == 1);
                    int rangRepondeurAuto = result.getInt(8);

                    GroupeAffectationUID groupeId
                            = new GroupeAffectationUID(cGpCod, gTiCod, gTaCod);

                    GroupeInternatUID internatId
                            = indexInternats.getInternat(cGiCod, groupeId);

                    if (!entree.groupesAffectations.containsKey(groupeId)) {
                        // peut arriver si les classements ou données d'appel ne sont pas renseignées 
                        groupesManquants.add(groupeId);
                    } else if (!entree.internats.containsKey(internatId)) {
                        Voeu v = new Voeu(
                                gCnCod,
                                iRhCod,
                                groupeId,
                                0,
                                0,
                                rangRepondeurAuto,
                                Voeu.StatutVoeu.AFFECTE_JOURS_PRECEDENTS,
                                !estAffectationPP);
                        entree.ajouterSiNecessaire(v);
                    } else {
                        Voeu v = new Voeu(
                                gCnCod,
                                groupeId,
                                0,
                                0,
                                internatId,
                                0,
                                rangRepondeurAuto,
                                Voeu.StatutVoeu.AFFECTE_JOURS_PRECEDENTS,
                                !estAffectationPP);
                        entree.ajouterSiNecessaire(v);
                    }

                }
            }
        }

    }

    public void recupererVoeuxAvecInternatsAClassementPropre(IndexInternats internatsIndex, boolean seulementVoeuxEnAttente)
            throws SQLException, VerificationException {

        int compteur = 0;
        int compteurIgnores = 0;

        try (Statement stmt = connection.createStatement()) {
            stmt.setFetchSize(100_000);
            String requete
                    = SELECT
                    + "v.g_cn_cod,"//id candidat
                    + "v.g_ta_cod,"//id affectation
                    + "ti.g_ti_cod,"//id inscription
                    + "cg.c_gp_cod,"//groupe de classement pédagogique
                    + (simulationAvantDebutCampagne
                            ? "NVL(cg.c_cg_ord_app,cg.c_cg_ran) rang,"
                            : "cg.c_cg_ord_app rang,")//ordre d'appel.
                    + "cg.c_cg_ord_app_aff,"//ordre d'appel
                    + "cgi.c_gi_cod,"//id internat
                    + "cgi.c_ci_ran,"//rang de classement internat
                    + "NVL(v.a_ve_typ_maj,0),"//code modification (annulation démission, modification classements erronés)
                    + "NVL(v.a_ve_ord,0),"//rang du voeu dans le répondeur automatique (null si désactivé)
                    + " sv.a_sv_flg_att,"
                    + " sv.a_sv_flg_clo,"
                    + " sv.a_sv_flg_aff"
                    + FROM
                    + CANDIDATS_TABLE + " c,"//candidats
                    + VOEUX_TABLE + " v,"//voeux
                    + STATUTS_VOEUX_TABLE + " sv,"//codes situations des voeuxEnAttente
                    + ConnecteurSQL.RECRUTEMENTS_GROUPES_TABLE + " rg,"//groupes de classement pédagogique
                    + ConnecteurSQL.CLASSEMENTS_TABLE + " cg,"//classements pédagogiques
                    + A_REC_GRP_INT + " rgi,"//groupes de classement internats
                    + CLASSEMENTS_INTERNATS_TABLE + " cgi,"//classements internats
                    + FOR_INSCRIPTIONS_TABLE + " ti"//données formations inscriptions
                    + WHERE
                    + " v.i_rh_cod=1"//voeux avec internat
                    + (seulementVoeuxEnAttente
                            ? (AND + " (sv.a_sv_flg_att=1 or sv.a_sv_flg_clo=1)")//voeu en attente ou bien clôturé (GDD)
                            : AND + " (sv.a_Sv_cod >= -40)")//inclus tous les voeux
                    + AND + " cg.i_ip_cod=5" //candidat classé
                    + AND + " cgi.i_ip_cod=5" //candidat classé
                    + AND + " c.g_ic_cod >= 0" //dossier non-annulé (décès...)
                    + AND + " ti.g_ti_eta_cla=2" //classement terminé
                    + AND + " c.g_cn_cod=v.g_cn_cod"
                    + (simulationAvantDebutCampagne ? " " : AND + " cg.C_CG_ORD_APP is not null")
                    + AND + " v.a_sv_cod=sv.a_sv_cod"
                    + AND + " v.g_cn_cod=cg.g_cn_cod"
                    + AND + " cg.c_gp_cod=rg.c_gp_cod"
                    + AND + " rg.g_ta_cod=v.g_ta_cod"
                    + AND + " rg.g_ti_cod=ti.g_ti_cod"
                    + AND + " v.g_cn_cod=cgi.g_cn_cod"
                    + AND + " cgi.c_gi_cod=rgi.c_gi_cod"
                    + AND + " "
                    + "( "
                    + "   (     " + //internat par établissement identifié par code établissement g_ea_cod_ins
                                    // -> un seul groupe de classement
                    "           rgi.g_ta_cod is null " +
                    "           AND rgi.g_ti_cod is null" +
                    "           AND rgi.g_ea_cod_ins is not null " +
                    "           AND rgi.g_ea_cod_ins=ti.g_ea_cod_ins " +
                    "           AND rg.g_ti_cod=ti.g_ti_cod" +
                    "       ) "
                    + "   OR "
                    + "   (     " + //internat par établissement identifié par code inscription g_ti_cod
                                    // -> un seul groupe de classement
                    "           rgi.g_ta_cod is null " +
                    "           AND rgi.g_ti_cod is not null" +
                    "           AND rgi.g_ea_cod_ins is null " +
                    "           AND rgi.g_ti_cod=ti.g_ti_cod " +
                    "           AND rg.g_ti_cod=ti.g_ti_cod" +
                    "       ) "
                    + "   OR "
                    + "   (     " + //internat par formation
                    "           rgi.g_ta_cod is not null " +
                    "           AND rgi.g_ta_cod=v.g_ta_cod " +
                    "           AND rgi.g_ti_cod=ti.g_ti_cod" +
                    "       )"
                    + " )"
                    + AND + " ti.g_ti_cla_int_uni IN (0,1)" //restriction aux internats à classement propre
                    + ((sparseDataTestingMode > 0) ? (AND + " MOD(v.g_cn_cod," + sparseDataTestingMode + ")" + EQUALS_ZERO
                            + AND + "MOD(v.g_ta_cod," + sparseDataTestingMode + ")" + EQUALS_ZERO) : "");

            LOGGER.log(Level.INFO, "Execution de la requete {0}", requete);

            try (ResultSet result = stmt.executeQuery(requete)) {
                while (result.next()) {
                    int gCnCod = result.getInt(1);
                    int gTaCod = result.getInt(2);
                    int gTiCod = result.getInt(3);
                    int cGpCod = result.getInt(4);
                    int rangAppel = result.getInt(5);
                    int rangAppelAffiche = result.getInt(6);
                    int cGiCod = result.getInt(7);
                    int cCiRan = result.getInt(8);
                    int typeMaj = result.getInt(9);
                    int rangRepondeurAutomatique = result.getInt(10);

                    boolean enAttente = result.getBoolean(11);
                    boolean cloture = result.getBoolean(12);//GDD
                    boolean affecte = result.getBoolean(13);//GDD

                    GroupeAffectationUID groupeId
                            = new GroupeAffectationUID(cGpCod, gTiCod, gTaCod);
                    GroupeInternatUID internatId
                            = internatsIndex.getInternat(cGiCod, groupeId);

                    if (!entree.groupesAffectations.containsKey(groupeId)) {
                        /* peut arriver si les classements ou données d'appel ne sont pas renseignées */
                        groupesManquants.add(groupeId);
                        compteurIgnores++;
                    } else if (!entree.internats.containsKey(internatId)) {
                        internatsManquants.add(internatId);
                        compteurIgnores++;
                    } else {
                        Voeu v = new Voeu(
                                gCnCod,
                                groupeId,
                                rangAppel,
                                rangAppelAffiche,
                                internatId,
                                cCiRan,
                                rangRepondeurAutomatique,
                                getStatut(enAttente, cloture, affecte),
                                false
                        );
                        v.setTypeMaj(typeMaj);
                        compteur++;
                        if (compteur % 100_000 == 0) {
                            LOGGER.log(Level.INFO, "{0} voeux récupérés", compteur);
                        }
                        entree.ajouter(v);
                    }
                }
            }
        }
        LOGGER.log(Level.INFO, "{0} voeux en attente avec internat à classement propre insérés", compteur);
        LOGGER.log(Level.INFO, "{0} voeux en attente avec internat à classement propre ignorés", compteurIgnores);
    }

    private Voeu.StatutVoeu getStatut(boolean enAttente, boolean cloture, boolean affecte) {
        if (enAttente || cloture) {
           return Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION;
        } else if (affecte) {
            return Voeu.StatutVoeu.AFFECTE_JOURS_PRECEDENTS;
        } else {
            return Voeu.StatutVoeu.REFUS_OU_DEMISSION;
        }
    }

    private void recupererVoeuxSansInternatAClassementPropre(boolean seulementVoeuxEnAttente)
            throws SQLException, VerificationException {
        int compteur = 0;
        try (Statement stmt = connection.createStatement()) {
            int fetchSize = 500_000;
            stmt.setFetchSize(fetchSize);
            String requete
                    = SELECT
                    + "v.g_cn_cod,"//id candidat
                    + "v.g_ta_cod,"//id affectation
                    + "v.i_rh_cod,"//demande internat (1) ou pas (0)
                    + "NVL(v.a_ve_ord,0),"//rang du voeu dans le répondeur automatique (null di désactivé)
                    + "ti.g_ti_cod,"//id inscription
                    + "cg.c_gp_cod,"//groupe de classement pédagogique
                    + (simulationAvantDebutCampagne
                            ? "NVL(cg.c_cg_ord_app,cg.c_cg_ran) rang,"
                            : "cg.c_cg_ord_app rang,")//ordre d'appel.
                    + "cg.c_cg_ord_app_aff rang_aff,"//ordre d'appel affiché.
                    + "NVL(v.a_ve_typ_maj,0),"//code modification (annulation démission, modification classements erronés)
                    + " sv.a_sv_flg_att,"
                    + " sv.a_sv_flg_clo,"
                    + " sv.a_sv_flg_aff"
                    + FROM
                    + CANDIDATS_TABLE +  " c,"//candidats
                    + VOEUX_TABLE + " v,"//voeux
                    + STATUTS_VOEUX_TABLE + " sv,"//codes situations des voeuxEnAttente
                    + ConnecteurSQL.RECRUTEMENTS_GROUPES_TABLE + " rg,"//groupes de classement pédagogique
                    + ConnecteurSQL.CLASSEMENTS_TABLE + " cg,"//classements pédagogiques
                    + FOR_INSCRIPTIONS_TABLE + " ti"//données formations inscriptions
                    + WHERE
                    + " cg.i_ip_cod=5" //candidat classé
                    + AND + " c.g_ic_cod >= 0" //dossier non-annulé (décès...)
                    + AND + " ti.g_ti_eta_cla=2" //classement terminé
                    + (seulementVoeuxEnAttente
                            ? (AND + " (sv.a_sv_flg_att=1 or sv.a_sv_flg_clo=1)")//voeu en attente ou bien clôturé (GDD)
                            : AND + " (sv.a_Sv_cod >= -40)")
                    + (simulationAvantDebutCampagne ? " " : AND + " cg.C_CG_ORD_APP is not null")
                    + AND + " c.g_cn_cod=v.g_cn_cod"
                    + AND + " v.a_sv_cod=sv.a_sv_cod"
                    + AND + " v.g_cn_cod=cg.g_cn_cod"
                    + AND + " cg.c_gp_cod=rg.c_gp_cod"
                    + AND + " rg.g_ta_cod=v.g_ta_cod"
                    + AND + " rg.g_ti_cod=ti.g_ti_cod"
                    //exclut les formations d'inscriptions avec internat à classement propre
                    + AND + " (v.i_rh_cod =0 or ti.g_ti_cla_int_uni NOT IN (0,1)) "
                    + ((sparseDataTestingMode > 0) ? (AND + " MOD(v.g_cn_cod," + sparseDataTestingMode + ")" + EQUALS_ZERO
                            + AND + "MOD(v.g_ta_cod," + sparseDataTestingMode + ")" + EQUALS_ZERO) : "");

            LOGGER.log(Level.INFO, "Execution de la requete {0}", requete);

            try (ResultSet result = stmt.executeQuery(requete)) {
                while (result.next()) {
                    int gCnCod = result.getInt(1);
                    int gTaCod = result.getInt(2);
                    boolean avecInternat = result.getBoolean(3);
                    int rangRepondeurAutomatique = result.getInt(4);
                    int gTiCod = result.getInt(5);
                    int cGpCod = result.getInt(6);
                    int rangAppel = result.getInt(7);
                    int rangAppelAffiche = result.getInt(8);

                    int typeMaj = result.getInt(9);

                    boolean enAttente = result.getBoolean(10);
                    boolean cloture = result.getBoolean(11);//GDD
                    boolean affecte = result.getBoolean(12);//GDD

                    GroupeAffectationUID groupeId
                            = new GroupeAffectationUID(cGpCod, gTiCod, gTaCod);

                     if (!entree.groupesAffectations.containsKey(groupeId)) {
                        /* peut arriver si les classements 
                        ou données d'appel ne sont pas renseignées */
                        groupesManquants.add(groupeId);
                        continue;
                    }

                    Voeu voeu = new Voeu(
                            gCnCod,
                            avecInternat,
                            groupeId,
                            rangAppel,
                            rangAppelAffiche,
                            rangRepondeurAutomatique,
                            getStatut(enAttente, cloture, affecte),
                            false
                    );
                     voeu.setTypeMaj(typeMaj);

                    compteur++;
                    if (compteur % fetchSize == 0) {
                        LOGGER.log(Level.INFO, "{0} voeux récupérés", compteur);
                    }

                    entree.ajouter(voeu);
                }
            }
        }
        LOGGER.log(Level.INFO, "{0} voeux en attente sans internat a classement propre", compteur);
    }

    private void recupererCandidatsAvecRepondeurAutomatique() throws SQLException {

        entree.candidatsAvecRepondeurAutomatique.clear();

        try (Statement stmt = connection.createStatement()) {
            stmt.setFetchSize(100_000);
            try (ResultSet result = stmt.executeQuery(SELECT
                    + " G_CN_COD"//id candidat
                    + FROM + " G_CAN can"//table des candidats
                    + WHERE
                    + " NVL(can.g_cn_flg_ra,0) = 1"
                    + (sparseDataTestingMode > 0 ? AND + " MOD(can.g_cn_cod," + sparseDataTestingMode + ") =0 " : "")
            )) {

                while (result.next()) {
                    int gCnCod = result.getInt(1);
                    entree.candidatsAvecRepondeurAutomatique.add(gCnCod);
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

        try (Statement stmt = connection.createStatement()) {
            stmt.setFetchSize(100_000);
            String sql = SELECT
                    + " v.G_CN_COD,v.g_ta_cod,v.I_RH_COD"
                    + FROM
                    + VOEUX_TABLE + "v,"//voeu
                    + STATUTS_VOEUX_TABLE +  "sv,"//etat voeu
                    + ADMISSIONS_TABLE +  "adm"//proposition d'admission
                    + WHERE
                    + " v.a_sv_cod = sv.a_sv_cod"
                    + AND + " sv.a_sv_flg_att=1"
                    + AND + " v.G_CN_COD=adm.G_CN_COD"
                    + AND + " v.g_ta_cod=adm.g_ta_cod"
                    + AND + " adm.A_TA_COD=1"; //admission en procédure normale

            LOGGER.info(sql);

            try (ResultSet result = stmt.executeQuery(sql)) {

                while (result.next()) {
                    int gCnCod = result.getInt(1);
                    int gTaCod = result.getInt(2);
                    boolean iRhCod = result.getBoolean(3);
                    voeux.add(new VoeuUID(gCnCod, gTaCod, iRhCod));
                }
            }
            LOGGER.log(Level.INFO, "{0} voeux r\u00e9cup\u00e9r\u00e9s", voeux.size());
        }

        return voeux;
    }

    /* flag permettant de vérifier l'interruption des données entrantes pendant le calcul des propositions */
    private static final int INDEX_FLAG_INTERRUP_DONNEES = 31;

    /* flag permettant de signaler un problème lors du calcul des propositions */
    private static final int INDEX_FLAG_ALERTE = 34;

    /* index de table stockant la date du début de campagen */
    public static final int INDEX_DATE_DEBUT_CAMPAGNE = 35;

    /* index de table stockant la date de l'ouverture complète des internats */
    public static final int INDEX_DATE_OUV_COMP_INTERNATS = 334;

    private static final Logger LOGGER = Logger.getLogger(ConnecteurDonneesPropositionsSQL.class.getSimpleName());

}