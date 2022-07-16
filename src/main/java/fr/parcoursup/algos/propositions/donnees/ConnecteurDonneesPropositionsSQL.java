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
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static fr.parcoursup.algos.donnees.ConnecteurSQL.*;
import static fr.parcoursup.algos.donnees.SQLStringsConstants.*;
import static fr.parcoursup.algos.propositions.algo.Voeu.StatutVoeu.*;


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
        this.config = new ConnecteurDonneesPropositionSQLConfig();
    }

    /* ce connecteur est utilisé par le simulateur */
    public ConnecteurDonneesPropositionsSQL(
            Connection connection,
            ConnecteurDonneesPropositionSQLConfig config
    ) {
        this.connection = connection;
        this.config = config;
    }

    final private ConnecteurDonneesPropositionSQLConfig config;

    private static final String NB_JRS_EQUALS = " NB_JRS=? ";

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
                if (config.verifierInterruptionFluxDonneesEntrantes
                        && !estVerouille) {
                    throw new AccesDonneesException(AccesDonneesExceptionMessage.CONNECTEUR_DONNEES_PROPOSITIONS_SQL_MAUVAIS_G_PR_COD, INDEX_FLAG_INTERRUP_DONNEES);
                }
            }
        }

        LOGGER.info("Récupération du nombre de jours depuis l'ouverture de la campagne");
        int nbJoursCampagne = getNbJoursCampagne();
        if(config.simulerNbJrs > 0) {
            //fonction utilisée en simulation
            nbJoursCampagne = config.simulerNbJrs;
        }
        LOGGER.log(Level.INFO, "{0} jours.", nbJoursCampagne);

        LOGGER.info("Récupération du nombre de jours de campagne à la date pivot");
        int nbJoursCampagneDatePivotInternats
                = getNbJoursCampagneDatePivotInternats();
        LOGGER.log(Level.INFO, "{0} jours.", nbJoursCampagneDatePivotInternats);

        LOGGER.info("Récupération de la date de début de la GDD");
        int nbJoursCampagneDateDebutGDD
                = getNbJoursCampagneDateDebutGDD();
        LOGGER.log(Level.INFO, "{0} jours.", nbJoursCampagneDateDebutGDD);

        LOGGER.info("Récupération de la date de fin d'ordonnancement des voeux en GDD");
        int nbJoursCampagneDateFinOrdonnancementGDD
                = getNbJoursCampagneFinOrdonnancementGDD();
        LOGGER.log(Level.INFO, "{0} jours.", nbJoursCampagneDateFinOrdonnancementGDD);

        Parametres parametres = new Parametres(
                nbJoursCampagne,
                nbJoursCampagneDatePivotInternats,
                nbJoursCampagneDateDebutGDD,
                nbJoursCampagneDateFinOrdonnancementGDD
        );
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
            boolean retroCompatibilityMode = !config.verifierInterruptionFluxDonneesEntrantes;
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

            boolean seulementVoeuxClotures = entree.getParametres().nbJoursCampagne >= entree.getParametres().nbJoursCampagneDateDebutGDD;
            if(seulementVoeuxClotures) {
                LOGGER.info("*******************************************************************************");
                LOGGER.info("date postérieure au début de la GDD: récupération des voeux clôturés uniquement");
                LOGGER.info("*******************************************************************************");
            }

            LOGGER.info("Récupération des voeux en attente avec demande internat dans un internat ayant son propre classement");
            recupererVoeuxAvecInternatsAClassementPropre(
                    entree.internatsIndex,
                    config.recupererSeulementVoeuxEnAttente,
                    config.recupererSeulementVoeuxClasses,
                    seulementVoeuxClotures
            );

            LOGGER.info("Récupération des voeux en attente sans internat, ou avec internat n'ayant pas son propre classemnt");
            recupererVoeuxSansInternatAClassementPropre(
                    config.recupererSeulementVoeuxEnAttente,
                    config.recupererSeulementVoeuxClasses,
                    seulementVoeuxClotures
            );

            LOGGER.info("Récupération des propositions non refusées ");
            //NB: en simulation, récupérer les propositions après les voeux, i.e. conserver les appels à recupererVoeuxA* et recupererPropositionsActuelles dans cet ordre
            recupererPropositions(entree.internatsIndex, config.inclurePropositionsRefusees);

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

    /**
     * factorise l'ajout d'un voeu à une requête d'exportation
     * @param ps la requête d'exportation
     * @param voe le voeu à ajouter
     * @param ajouterCGiCod option d'ajout ou non du cgicod
     * @throws SQLException exception
     */
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
        if(config.effacerEntreesDuMemeJour) {
            LOGGER.info("Préparation de la table A_ADM_PROP avant export");
            try (PreparedStatement ps
                         = connection.prepareStatement(
                    DELETE_FROM + ADMISSIONS_TABLE_SORTIE + WHERE + NB_JRS_EQUALS)) {
                ps.setInt(1, sortie.parametres.nbJoursCampagne);
                ps.execute();
            }
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
                    addToBatchAndExecuteIfCounter(ps, ++count);
                }
            }

            ps.executeBatch();
            LOGGER.log(Level.INFO, "{0} propositions exportées.", count);

        }

    }

    private void exporterDemissionsAutomatiques(AlgoPropositionsSortie sortie) throws SQLException, VerificationException {

        if (config.effacerEntreesDuMemeJour) {
            LOGGER.info("Préparation de la table A_ADM_DEM avant export");
            try (PreparedStatement ps
                         = connection.prepareStatement(DELETE_FROM + A_ADM_DEM + WHERE + NB_JRS_EQUALS)) {
                ps.setInt(1, sortie.parametres.nbJoursCampagne);
                ps.execute();
            }
        }

        try (PreparedStatement ps = connection.prepareStatement(
                INSERT_INTO + A_ADM_DEM
                + "(G_CN_COD,g_ta_cod,I_RH_COD,C_GP_COD,G_TI_COD,C_GI_COD,EST_DEM_PROP,A_AD_TYP_DEM,NB_JRS)"
                + VALUES + "(?,?,?,?,?,?,?,?,?)")) {
            int count = 0;

            for (Voeu voe : sortie.voeux) {

                if (voe.estDemissionAutomatique()) {
                    ajouterVoeu(ps, voe, true);
                    ps.setBoolean(7, voe.estDemissionAutomatiqueProposition());
                    ps.setInt(8, voe.getTypeDemissionAutomatique());
                    ps.setInt(9, sortie.parametres.nbJoursCampagne);
                    addToBatchAndExecuteIfCounter(ps, ++count);
                }
            }

            ps.executeBatch();
            LOGGER.log(Level.INFO, "{0} démissions automatiques exportées.", count);

        }
    }

    private void exporterPredicteurRangDernierAppele(Collection<GroupeAffectation> groupes,
            Parametres parametres) throws SQLException {

        if (config.effacerEntreesDuMemeJour) {
            LOGGER.info("Préparation de la table A_ADM_PRED_DER_APP avant export");
            try (PreparedStatement ps
                         = connection.prepareStatement(DELETE_FROM + A_ADM_PRED_DER_APP + WHERE + NB_JRS_EQUALS)) {
                ps.setInt(1, parametres.nbJoursCampagne);
                ps.execute();
            }
        }

        LOGGER.info("Export des prédicteurs dans A_ADM_PRED_DER_APP");
        try (PreparedStatement ps = connection.prepareStatement(
                  INSERT_INTO + A_ADM_PRED_DER_APP
                + "(g_ta_cod,C_GP_COD,A_RG_RAN_DER,NB_JRS)"
                + VALUES + "(?,?,?,?)")) {

            for (GroupeAffectation g : groupes) {
                ps.setInt(1, g.id.gTaCod);
                ps.setInt(2, g.id.cGpCod);
                ps.setInt(3,
                        (g.getEstimationRangDernierAppeleADateFinReservationInternats() == Integer.MAX_VALUE)
                                ? -1
                                : g.getEstimationRangDernierAppeleADateFinReservationInternats()
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

        if (config.effacerEntreesDuMemeJour) {
            LOGGER.info("Préparation de la table A_REC_GRP_INT_PROP avant export");
            try (PreparedStatement ps = connection.prepareStatement(
                    DELETE_FROM + A_REC_GRP_INT_PROP + WHERE
                            + "C_GI_COD !=0 " + AND + " NB_JRS=?")) {
                ps.setInt(1, sortie.parametres.nbJoursCampagne);
                int deleted = ps.executeUpdate();
                LOGGER.info(deleted + " entrées ont été supprimées de " + A_REC_GRP_INT_PROP);
            }
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
                        + FROM + A_REC_GRP_INT_PROP
                        + WHERE + "(NB_JRS < ?)"
                        + AND + "C_GI_COD " + EQUALS_ZERO
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

        if (config.effacerEntreesDuMemeJour) {
            LOGGER.info("Préparation de la table " + A_REC_GRP_INT_PROP + " avant export");
            try (PreparedStatement ps = connection.prepareStatement(
                    DELETE_FROM + A_REC_GRP_INT_PROP + " " + WHERE + "C_GI_COD=0" + AND + "NB_JRS=?")) {
                ps.setInt(1, sortie.parametres.nbJoursCampagne);
                int deleted = ps.executeUpdate();
                LOGGER.info(deleted + " entrées ont été supprimées de " + A_REC_GRP_INT_PROP);
            }
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

        if (config.effacerEntreesDuMemeJour) {
            LOGGER.info("Préparation de la table A_VOE_PROP avant export");
            try (PreparedStatement ps = connection.prepareStatement(DELETE_FROM + A_VOE_PROP + WHERE + "NB_JRS=?")) {
                ps.setInt(1, sortie.parametres.nbJoursCampagne);
                ps.execute();
            }
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
                    addToBatchAndExecuteIfCounter(ps,++count);
                }
            }

            ps.executeBatch();
            LOGGER.log(Level.INFO, "{0} rangs sur liste d attente exportés.", count);

        }

    }

    private void addToBatchAndExecuteIfCounter(PreparedStatement ps, int count) throws SQLException {
        ps.addBatch();
        if (++count % 100000 == 0) {
            LOGGER.log(Level.INFO, "Exportation des voeux {0} a {1}",
                    new Object[]{count - 100000, count});
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

    /**
     * Récupère la liste de sgroupes d'affectation
     * @param parametres les paramètres de la campgane ne cours
     * @param retroCompatibitilite permet d'adapter la requete à
     *     la non existence du champ a_rg_flg_adm_stop sur les anciennes bases (avant 2020).
     *     Ce flag n'est pas utilisé en prod.
     * @return la liste des groupes, indexés par id
     * @throws SQLException erreur SQL
     * @throws VerificationException erreur de vérification des données
     */
    public Map<GroupeAffectationUID, GroupeAffectation>
            recupererGroupesAffectation(
                    Parametres parametres,
                    boolean retroCompatibitilite)
            throws SQLException, VerificationException {

        Map<GroupeAffectationUID, GroupeAffectation> resultat = new HashMap<>();

        boolean colAdmStopExists = true;
        boolean colFinReservationInternatExists = true;

        /* Ce mode, désactivé par défaut,
        * permet d'exécuter l'algorithme d'appel
        * et les simulations sur des bases archivées.
        */
        if (retroCompatibitilite) {
            colAdmStopExists = checkColumnExists(RECRUTEMENTS_GROUPES_TABLE, "a_rg_flg_adm_stop");
            colFinReservationInternatExists = checkColumnExists(RECRUTEMENTS_FORMATIONS_TABLE, "a_rc_flg_fin_res_pla");
        } 


        LOGGER.info("Récupération du rang du dernier appelé dans chaque groupe");
        Map<GroupeAffectationUID,Integer> rangsDernierAppeles = new HashMap<>();
        try (Statement stmt = connection.createStatement()) {
            stmt.setFetchSize(1_000_000);
            String sql =
                    SELECT + "rec.g_ta_cod," +
                            "rec.g_ti_cod," +
                            "rec.c_gp_cod," +
                            " MAX(NVL(C_CG_ORD_APP ,-1)) \n" +
                            FROM + ADMISSIONS_TABLE_SORTIE + " adm, " +
                            CLASSEMENTS_TABLE + " cla, " +
                            RECRUTEMENTS_GROUPES_TABLE + " rec " +
                            WHERE +
                            "adm.g_cn_cod=cla.g_cn_cod \n" +
                            AND + "adm.c_gp_cod=cla.c_gp_cod\n" +
                            AND + "adm.c_gp_cod=rec.c_gp_cod\n" +
                            AND + "adm.g_ta_cod=rec.g_ta_cod\n" +
                            AND + "adm.g_ti_cod=rec.g_ti_cod\n" +
                            AND + " rec.a_rg_nbr_sou > 0 \n" +
                            AND + " cla.i_ip_cod=5 \n" +
                            AND + " cla.c_cg_ord_app is not null\n" +
                            GROUP_BY + " rec.g_ta_cod,rec.g_ti_cod,rec.c_gp_cod";

            LOGGER.info(sql);
            try (ResultSet result = stmt.executeQuery(sql)) {

                while (result.next()) {

                    int gTaCod = result.getInt(1);
                    int gTiCod = result.getInt(2);
                    int cGpCod = result.getInt(3);
                    rangsDernierAppeles.put(
                            new GroupeAffectationUID(cGpCod, gTiCod, gTaCod),
                            result.getInt(4)
                    );
                }
            }
        }

        LOGGER.log(Level.INFO,
                "Récupération du rang du dernier appelé il y a "
                        + GroupeAffectation.NB_JOURS_POUR_INTERPOLATION_INTERNAT
                        + " jours dans chaque groupe.");
        Map<GroupeAffectationUID,Integer> rangsDernierAppelesReference = new HashMap<>();
        if(parametres.nbJoursCampagne > GroupeAffectation.NB_JOURS_POUR_INTERPOLATION_INTERNAT) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    SELECT + "rec.g_ta_cod," +
                                "rec.g_ti_cod," +
                                "rec.c_gp_cod," +
                                " MAX(NVL(C_CG_ORD_APP ,-1)) \n" +
                                FROM + ADMISSIONS_TABLE_SORTIE + " adm, " +
                                CLASSEMENTS_TABLE + " cla, " +
                                RECRUTEMENTS_GROUPES_TABLE + " rec " +
                                WHERE +
                                "adm.g_cn_cod=cla.g_cn_cod \n" +
                                AND + "adm.c_gp_cod=cla.c_gp_cod\n" +
                                AND + "adm.c_gp_cod=rec.c_gp_cod\n" +
                                AND + "adm.g_ta_cod=rec.g_ta_cod\n" +
                                AND + "adm.g_ti_cod=rec.g_ti_cod\n" +
                                AND + " rec.a_rg_nbr_sou > 0 \n" +
                                AND + " cla.i_ip_cod=5 \n" +
                                AND + " cla.c_cg_ord_app is not null\n" +
                                AND + " adm.nb_jrs <= ?" +
                                GROUP_BY + " rec.g_ta_cod,rec.g_ti_cod,rec.c_gp_cod"
            )) {
                 stmt.setFetchSize(1_000_000);
                 int dernierJoursCampagneAvecPropositions = parametres.nbJoursCampagne - 1;
                 stmt.setInt(1, dernierJoursCampagneAvecPropositions - GroupeAffectation.NB_JOURS_POUR_INTERPOLATION_INTERNAT);
                 try (ResultSet result = stmt.executeQuery()) {
                    while (result.next()) {
                        int gTaCod = result.getInt(1);
                        int gTiCod = result.getInt(2);
                        int cGpCod = result.getInt(3);
                        rangsDernierAppelesReference.put(
                                new GroupeAffectationUID(cGpCod, gTiCod, gTaCod),
                                result.getInt(4)
                        );
                    }
                }
            }
        }

        try (Statement stmt = connection.createStatement()) {
            stmt.setFetchSize(1_000_000);
            String sql = SELECT + "C_GP_COD,"
                    + " rec.G_TI_COD,"
                    + " rec.g_ta_cod,"
                    + (config.ignorerSurbooking ? "A_RG_PLA capacite,"
                        : config.simulationAvantDebutCampagne ? " NVL(A_RG_NBR_SOU,A_RG_PLA) capacite,"
                        : " A_RG_NBR_SOU capacite,")
                    + " NVL(a_rg_ran_lim,0),"
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
                    int blocageAdmissionGroupe = result.getInt(6);
                    boolean finReservationPlacesInternat = (result.getInt(7) == 1);
                    if (blocageAdmissionGroupe != 0) {
                        nbRecrutementsSouhaite = 0;
                        rangLimite = 0;
                    }

                    GroupeAffectationUID id
                            = new GroupeAffectationUID(cGpCod, gTiCod, gTaCod);

                    int rangDernierAppele = rangsDernierAppeles.getOrDefault(id, 0);
                    int rangDernierAppeleReference = rangsDernierAppelesReference.getOrDefault(id, 0);

                    GroupeAffectation g = new GroupeAffectation(
                            nbRecrutementsSouhaite,
                            id,
                            rangLimite,
                            rangDernierAppele,
                            rangDernierAppeleReference,
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


    /**
     * @param debutInclus date de début de la période, inclus
     * @param finExclus date de fin de la période, exclus
     * @return le nombre de jours dans l'intervalle entre les deux dates des deux timestamps en entrée,
     * en incluant la première date et en excluent la seconde.
     * Le résultat est indépendant de la composante horaire des deux timestamps.
     * Exemple: si la date des deux timestamps est la même alors le résultat est 0.
     * Exemple: si la date de debutInclus est la veille de finExclus alors le résultat est 1.
     * @throws AccesDonneesException problème SQL
     */
    private static int nbJoursEntre(java.sql.Timestamp debutInclus, java.sql.Timestamp finExclus) throws AccesDonneesException {
        if (finExclus == null || debutInclus == null) {
            throw new AccesDonneesException(AccesDonneesExceptionMessage.CONNECTEUR_DONNEES_PROPOSITIONS_SQL_DATE);
        }
        LocalDateTime dateDebutInclus = debutInclus.toLocalDateTime().toLocalDate().atStartOfDay();
        LocalDateTime dateFinExclus = finExclus.toLocalDateTime().toLocalDate().atStartOfDay();
        long nbJours = Duration.between(dateDebutInclus, dateFinExclus).toDays();
        return Math.toIntExact(nbJours);
    }

    /**
     * @return le nombre de jours depuis le début de la campagne, valant 1 le premier jour,
     * incréméneté à chaque changement de date
     * @throws SQLException problème SQL
     * @throws AccesDonneesException problème SQL
     */
    public int getNbJoursCampagne() throws SQLException, AccesDonneesException {

        Integer nbJours = null;

        try (PreparedStatement ps = connection.prepareStatement(
                SELECT + "SYSDATE,TO_DATE(g_par.g_pr_val, 'DD/MM/YYYY:HH24MI')" +
                FROM + G_PAR + WHERE + "g_pr_cod=?")) {
            ps.setInt(1, ConnecteurDonneesPropositionsSQL.INDEX_DATE_DEBUT_CAMPAGNE);
            try (ResultSet result = ps.executeQuery()) {
                while (result.next()) {
                    java.sql.Timestamp maintenant = result.getTimestamp(1);
                    java.sql.Timestamp  dateDebutCampagne = result.getTimestamp(2);
                    nbJours = nbJoursEntre(dateDebutCampagne, maintenant) + 1;
                }
            }
        }
        if(nbJours == null) {
            throw new AccesDonneesException(AccesDonneesExceptionMessage.CONNECTEUR_DONNEES_PROPOSITIONS_SQL_DATE);
        } else {
            return nbJours;
        }
    }

    private int getNbJoursCampagneEvenement(int gPrCod) throws SQLException, AccesDonneesException {
        Integer nbJours = null;

        try (PreparedStatement ps = connection.prepareStatement(
                SELECT + ""+
                        "TO_DATE(g_par1.g_pr_val, 'DD/MM/YYYY:HH24MI'), " +
                        "TO_DATE(g_par2.g_pr_val, 'DD/MM/YYYY:HH24MI')" +
                        FROM + " g_par g_par1,g_par g_par2 " +
                        WHERE + " g_par1.g_pr_cod=?" +
                        AND + " g_par2.g_pr_cod=?")) {
            ps.setInt(1, ConnecteurDonneesPropositionsSQL.INDEX_DATE_DEBUT_CAMPAGNE);
            ps.setInt(2, gPrCod);

            try (ResultSet result = ps.executeQuery()) {
                while (result.next()) {
                    java.sql.Timestamp dateDebutCampagne = result.getTimestamp(1);
                    java.sql.Timestamp dateDebut = result.getTimestamp(2);
                    if (dateDebutCampagne == null || dateDebut == null) {
                        throw new AccesDonneesException(AccesDonneesExceptionMessage.CONNECTEUR_DONNEES_PROPOSITIONS_SQL_DATE);
                    }
                    nbJours = nbJoursEntre(dateDebutCampagne, dateDebut) + 1;
                }
            }
        }
        if (nbJours == null) {
            throw new AccesDonneesException(AccesDonneesExceptionMessage.CONNECTEUR_DONNEES_PROPOSITIONS_SQL_DATE);
        }
        return nbJours;
    }

    private int getNbJoursCampagneDateDebutGDD() throws SQLException, AccesDonneesException {
        return getNbJoursCampagneEvenement(ConnecteurDonneesPropositionsSQL.INDEX_DATE_DEBUT_GDD);
    }

    private int getNbJoursCampagneFinOrdonnancementGDD() throws SQLException, AccesDonneesException {
        return getNbJoursCampagneEvenement(ConnecteurDonneesPropositionsSQL.INDEX_DATE_FIN_ORDONNANCEMENT_GDD);
    }

    /* Récupère le nombre de jours depuis le début de campagne à la date pivot internat.
    Vaut 1 si les dates coincident  */
    private int getNbJoursCampagneDatePivotInternats() throws SQLException, AccesDonneesException {
        return getNbJoursCampagneEvenement(ConnecteurDonneesPropositionsSQL.INDEX_DATE_OUV_COMP_INTERNATS);
    }

    /* Récupère les voeux sur lesquels il y a une proposition d'admission qui
    bloque une place car elle est soit en attente de réponse du candidat soit 
    acceptée par le candidat. 
     */
    private void recupererPropositions(IndexInternats indexInternats, boolean inclurePropositionsRefusees)
            throws SQLException, VerificationException {

        Map<VoeuUID, Voeu> voeux
                = entree.voeux.stream().collect(Collectors.toMap(
                        voeu -> voeu.id,
                        voeu -> voeu
        ));

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
                    // 8 gestion des démissions (GDD)
                    // 10 inscription par l'établissement
                    + "NVL(voe.a_ve_ord,0), " //ordre du voeu dans le RA
                    + "NVL(sv.a_sv_flg_oui,1)," //le candidat a t'il accepté la proposition
                    + "NVL(sv.a_sv_flg_aff,1)" //le candidat a t'il accepté ou pas refusé la proposition
                    + FROM + " "
                    +  ADMISSIONS_TABLE + " adm "//table des admissions
                    + " LEFT JOIN "
                    + STATUTS_VOEUX_TABLE + " sv "//statut du voeu
                    + " ON "
                    + " adm.a_sv_cod=sv.a_sv_cod"
                    + " LEFT JOIN "
                    + VOEUX_TABLE + " voe "
                    + " ON adm.g_cn_cod=voe.g_cn_cod "
                    + AND + " adm.g_ta_cod=voe.g_ta_cod "
                    + AND + " adm.i_rh_cod=voe.i_rh_cod, "
                    + FOR_INSCRIPTIONS_TABLE + " ti"//données formations inscriptions
                    + WHERE
                    + (inclurePropositionsRefusees ? "" : " sv.a_sv_flg_aff=1 AND ")
                    + " adm.g_ti_cod=ti.g_ti_cod"
                    + AND + " adm.a_ta_cod != 2"
                    + (config.sparseDataTestingMode > 0 ? AND + " MOD(adm.g_cn_cod," + config.sparseDataTestingMode + ")" + EQUALS_ZERO
                            + AND + "MOD(adm.g_ta_cod," + config.sparseDataTestingMode + ")" + EQUALS_ZERO : "")

            )) {
                while (result.next()) {

                    int gCnCod = result.getInt(1);
                    int gTiCod = result.getInt(2);
                    int gTaCod = result.getInt(3);
                    boolean avecInternat = result.getBoolean(4);
                    int cGpCod = result.getInt(5);
                    int cGiCod = result.getInt(6);
                    int aTaCod = result.getInt(7);
                    boolean estAffectationPP = (aTaCod == 1 || aTaCod == 8);
                    int rangOrdrePreferencesCandidat = result.getInt(8);
                    boolean propositionAcceptee = result.getBoolean(9);
                    boolean propositionNonRefusee = result.getBoolean(10);

                    GroupeAffectationUID groupeId
                            = new GroupeAffectationUID(cGpCod, gTiCod, gTaCod);

                    GroupeInternatUID internatId
                            = indexInternats.getInternat(cGiCod, groupeId);

                    //Recupere le voeu si il est déjà connu
                    //pas forcément le cas (propositions en CAES, etc...)
                    Voeu old = voeux.get(new VoeuUID(gCnCod,gTaCod,avecInternat));

                    if (!entree.groupesAffectations.containsKey(groupeId)) {
                        // peut arriver si les classements ou données d'appel ne sont pas renseignées 
                        groupesManquants.add(groupeId);
                    } else if (!avecInternat || !entree.internats.containsKey(internatId)) {
                        Voeu v = new Voeu(
                                gCnCod,
                                avecInternat,
                                groupeId,
                                (old == null) ? 0 : old.ordreAppel,
                                (old == null) ? 0 : old.ordreAppelAffiche,
                                rangOrdrePreferencesCandidat,
                                propositionAcceptee ? PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE
                                        : propositionNonRefusee ? PROPOSITION_JOURS_PRECEDENTS_EN_ATTENTE_DE_REPONSE_DU_CANDIDAT
                                                                : PROPOSITION_JOURS_PRECEDENTS_REFUSEE,
                                !estAffectationPP);
                        entree.ajouterOuRemplacer(v);//Remarque: hors simulation c'est un ajout simple, car les autres voeux sont des voeux en attente
                    } else {
                        Voeu v = new Voeu(
                                gCnCod,
                                groupeId,
                                (old == null) ? 0 : old.ordreAppel,
                                (old == null) ? 0 : old.ordreAppelAffiche,
                                internatId,
                                (old == null) ? 0 : old.rangInternat,
                                rangOrdrePreferencesCandidat,
                                propositionAcceptee ? PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE
                                        : propositionNonRefusee ? PROPOSITION_JOURS_PRECEDENTS_EN_ATTENTE_DE_REPONSE_DU_CANDIDAT
                                        : PROPOSITION_JOURS_PRECEDENTS_REFUSEE,
                                !estAffectationPP);//Remarque: hors simulation c'est un ajout simple, car les autres voeux sont des voeux en attente
                        entree.ajouterOuRemplacer(v);
                    }

                }
            }
        }

    }

    public void recupererVoeuxAvecInternatsAClassementPropre(
            IndexInternats internatsIndex,
            boolean seulementVoeuxEnAttente,
            boolean recupererSeulementVoeuxClasses,
            boolean seulementVoeuxClotures
    )
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
                    + (config.simulationAvantDebutCampagne
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
                    + AND + (
                                    (seulementVoeuxEnAttente && seulementVoeuxClotures) ? " sv.a_sv_flg_clo=1 " :
                                    seulementVoeuxEnAttente ? " (sv.a_sv_flg_att=1 or sv.a_sv_flg_clo=1)" :
                                            " (sv.a_Sv_cod > -40)" )
                    + (recupererSeulementVoeuxClasses
                            ? (AND + " cg.i_ip_cod=5" //candidat classé
                                + AND + " c.g_ic_cod >= 0" //dossier non-annulé (décès...)
                                + AND + " ti.g_ti_eta_cla=2") //classement terminé
                            :""
                    )
                    + AND + " c.g_cn_cod=v.g_cn_cod"
                    + (config.simulationAvantDebutCampagne ? " " : AND + " cg.C_CG_ORD_APP is not null")
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
                    + ((config.sparseDataTestingMode > 0) ? (AND + " MOD(v.g_cn_cod," + config.sparseDataTestingMode + ")" + EQUALS_ZERO
                            + AND + "MOD(v.g_ta_cod," + config.sparseDataTestingMode + ")" + EQUALS_ZERO) : "");

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

                    boolean estEnAttente = result.getBoolean(11);
                    boolean estCloture = result.getBoolean(12);//GDD
                    boolean estAffecte = result.getBoolean(13);//GDD

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
                        Voeu.StatutVoeu statut = getStatut(estEnAttente, estCloture, estAffecte);
                        if(!recupererSeulementVoeuxClasses && rangAppel == 0) {
                            statut = Voeu.StatutVoeu.NON_CLASSE;
                        }
                        Voeu v = new Voeu(
                                gCnCod,
                                groupeId,
                                rangAppel,
                                rangAppelAffiche,
                                internatId,
                                cCiRan,
                                rangRepondeurAutomatique,
                                statut,
                                false
                        );
                        v.setTypeMaj(typeMaj);
                        v.setEstArchive(estCloture);
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
            return PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE;
        } else {
            return Voeu.StatutVoeu.REFUS_OU_DEMISSION;
        }
    }

    private void recupererVoeuxSansInternatAClassementPropre(
            boolean seulementVoeuxEnAttente,
            boolean seulementVoeuxClasses,
            boolean seulementVoeuxClotures)
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
                    + (config.simulationAvantDebutCampagne
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
                    + (seulementVoeuxClasses ? " cg.i_ip_cod=5" //candidat classé
                        + AND + " c.g_ic_cod >= 0" //dossier non-annulé (décès...)
                        + AND + " ti.g_ti_eta_cla=2" + AND//classement terminé
                        : ""
                        )
                    + (
                    (seulementVoeuxEnAttente && seulementVoeuxClotures) ? " sv.a_sv_flg_clo=1 " :
                            seulementVoeuxEnAttente ? " (sv.a_sv_flg_att=1 or sv.a_sv_flg_clo=1)" :
                                    " (sv.a_Sv_cod > -40)" )
                    + AND
                    + (config.simulationAvantDebutCampagne ? " " :  " cg.C_CG_ORD_APP is not null" + AND)
                    + " c.g_cn_cod=v.g_cn_cod"
                    + AND + " v.a_sv_cod=sv.a_sv_cod"
                    + AND + " v.g_cn_cod=cg.g_cn_cod"
                    + AND + " cg.c_gp_cod=rg.c_gp_cod"
                    + AND + " rg.g_ta_cod=v.g_ta_cod"
                    + AND + " rg.g_ti_cod=ti.g_ti_cod"
                    //exclut les formations d'inscriptions avec internat à classement propre
                    + AND + " (v.i_rh_cod =0 or ti.g_ti_cla_int_uni NOT IN (0,1)) "
                    + ((config.sparseDataTestingMode > 0) ? (AND + " MOD(v.g_cn_cod," + config.sparseDataTestingMode + ")" + EQUALS_ZERO
                            + AND + "MOD(v.g_ta_cod," + config.sparseDataTestingMode + ")" + EQUALS_ZERO) : "");

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

                    boolean estEnAttente = result.getBoolean(10);
                    boolean estArchive = result.getBoolean(11);//GDD
                    boolean estAffecte = result.getBoolean(12);

                    GroupeAffectationUID groupeId
                            = new GroupeAffectationUID(cGpCod, gTiCod, gTaCod);

                     if (!entree.groupesAffectations.containsKey(groupeId)) {
                        /* peut arriver si les classements 
                        ou données d'appel ne sont pas renseignées */
                        groupesManquants.add(groupeId);
                        continue;
                    }
                    Voeu.StatutVoeu statut = getStatut(estEnAttente, estArchive, estAffecte);
                    if(!config.recupererSeulementVoeuxClasses && rangAppel == 0) {
                        statut = Voeu.StatutVoeu.NON_CLASSE;
                    }
                    Voeu voeu = new Voeu(
                            gCnCod,
                            avecInternat,
                            groupeId,
                            rangAppel,
                            rangAppelAffiche,
                            rangRepondeurAutomatique,
                            statut,
                            false
                    );
                     voeu.setTypeMaj(typeMaj);
                     voeu.setEstArchive(estArchive);

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
                    + (config.sparseDataTestingMode > 0 ? AND + " MOD(can.g_cn_cod," + config.sparseDataTestingMode + ") =0 " : "")
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

    /* Date à partir de la quelle seuls les voeux cloturés sont considérés */
    public static final int INDEX_DATE_DEBUT_GDD = 316;

    /* Date à partir de la quelle seuls les voeux cloturés sont considérés */
    public static final int INDEX_DATE_FIN_ORDONNANCEMENT_GDD = 437;

    private static final Logger LOGGER = Logger.getLogger(ConnecteurDonneesPropositionsSQL.class.getSimpleName());

}