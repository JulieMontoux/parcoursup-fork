package fr.parcoursup.algos.propositions.donnees.testConnecteurDonneesPropositionsSQL;

import java.util.Map;
import java.util.Set;
import static org.junit.Assert.assertThrows;

import fr.parcoursup.algos.donnees.ConnecteurSQL;
import fr.parcoursup.algos.exceptions.AccesDonneesException;
import fr.parcoursup.algos.propositions.donnees.ConnecteurDonneesPropositionsSQL;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import fr.parcoursup.algos.propositions.algo.AlgoPropositionsEntree;
import fr.parcoursup.algos.propositions.algo.GroupeAffectation;
import fr.parcoursup.algos.propositions.algo.GroupeAffectationUID;
import fr.parcoursup.algos.propositions.algo.GroupeInternat;
import fr.parcoursup.algos.propositions.algo.GroupeInternatUID;
import fr.parcoursup.algos.propositions.algo.Parametres;
import fr.parcoursup.algos.propositions.algo.VoeuUID;

public class TestConnecteurDonneesPropositionsSQLImportation extends TestConnecteurDonneesPropositionsSQL {

    public TestConnecteurDonneesPropositionsSQLImportation(String name) throws Exception {

        super(name);

    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Helpers
    //
    ///////////////////////////////////////////////////////////////////////////
    int recupere_nombre_jours_depuis_debut_campagne(
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
    ) throws Exception {

        int nbJoursCampagne = Whitebox.invokeMethod(
                connecteurDonneesPropositions,
                "getNbJoursCampagne"
        );

        return nbJoursCampagne;

    }

    @Test
    public void test_recuperation_verification_nombre_jours_depuis_debut_campagne_avec_connecteur_prod_doit_retourner_nombre_superieur_a_zero() throws Exception {

        this.setBddDateDebutCampagne("20/05/2020:0000");

        int nbJoursCampagne;
        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            nbJoursCampagne = this.recupere_nombre_jours_depuis_debut_campagne(connecteurDonneesPropositions);

        }

        assertTrue(nbJoursCampagne > 0);

    }
    
    
    int recupere_nombre_jours_campagne_a_date_pivot_internats(
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
    ) throws Exception {

        int nbJoursCampagneDatePivotInternats = Whitebox.invokeMethod(
                connecteurDonneesPropositions,
                "getNbJoursCampagneDatePivotInternats"
        );

        return nbJoursCampagneDatePivotInternats;

    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Tests
    //
    ///////////////////////////////////////////////////////////////////////////
    @Test
    public void test_recuperation_verification_nombre_jours_ecoules_entre_dates_debut_campagne_et_ouverture_complete_internats_avec_connecteur_prod_doit_reussir() throws Exception {

        this.setBddDateDebutCampagne("20/05/2020:0000");
        this.setBddDateOuvertureCompleteInternats("21/05/2020:0000");

        int nbJoursCampagneDatePivotInternats;
        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            nbJoursCampagneDatePivotInternats = this.recupere_nombre_jours_campagne_a_date_pivot_internats(
                    connecteurDonneesPropositions
            );
        }

        assertEquals(2, nbJoursCampagneDatePivotInternats);

    }

    AlgoPropositionsEntree recupere_donnees_candidats_avec_repondeur_automatique(
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions,
            Parametres parametres
    ) throws Exception {

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(parametres);

        Whitebox.setInternalState(
                connecteurDonneesPropositions,
                "entree",
                entree
        );

        Whitebox.invokeMethod(
                connecteurDonneesPropositions,
                "recupererCandidatsAvecRepondeurAutomatique"
        );

        return entree;

    }

    @Test
    public void test_recuperation_verification_candidats_avec_repondeur_automatique_avec_connecteur_toutes_donnees_doit_reussir() throws Exception {

        Parametres parametres = new Parametres(
                this.recupereBddNombreJoursEcoulesDepuisDebutCampagne(),
                this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats()
        );

        AlgoPropositionsEntree entree;
        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            entree = this.recupere_donnees_candidats_avec_repondeur_automatique(
                    connecteurDonneesPropositions,
                    parametres
            );
        }

        assertEquals(entree.candidatsAvecRepondeurAutomatique.size(), 1);
        // 1 candidat avec répondeur automatique dans le scénario établi

    }

    @Test
    public void test_recuperation_verification_candidats_avec_repondeur_automatique_avec_connecteur_prod_doit_reussir() throws Exception {

        Parametres parametres = new Parametres(
                this.recupereBddNombreJoursEcoulesDepuisDebutCampagne(),
                this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats()
        );

        AlgoPropositionsEntree entree;
        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            entree = this.recupere_donnees_candidats_avec_repondeur_automatique(
                    connecteurDonneesPropositions,
                    parametres
            );
        }

        assertEquals(entree.candidatsAvecRepondeurAutomatique.size(), 1);
        // 1 candidat avec répondeur automatique dans le scénario établi

    }


    @Test
    public void test_recuperation_verification_groupes_affectation_avec_connecteur_toutes_donnees_doit_reussir() throws Exception {

        Parametres parametres = new Parametres(
                this.recupereBddNombreJoursEcoulesDepuisDebutCampagne(),
                this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats()
        );

        Map<GroupeAffectationUID, GroupeAffectation> groupesAffectation;
        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            groupesAffectation = this.recupere_groupes_affectation(
                    connecteurDonneesPropositions,
                    parametres
            );
        }

        assertEquals(groupesAffectation.size(), 2);
        
    }

    
    Map<GroupeAffectationUID, GroupeAffectation> recupere_groupes_affectation(
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions,
            Parametres parametres
    ) throws Exception {

        Map<GroupeAffectationUID, GroupeAffectation> groupesAffectation = Whitebox.invokeMethod(
                connecteurDonneesPropositions,
                "recupererGroupesAffectation",
                parametres,
                false // paramètre retroCompatibitilite
        );

        return groupesAffectation;

    }
    
    
    @Test
    public void test_recuperation_verification_groupes_affectation_avec_connecteur_prod_doit_reussir() throws Exception {

        Parametres parametres = new Parametres(
                this.recupereBddNombreJoursEcoulesDepuisDebutCampagne(),
                this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats()
        );

        Map<GroupeAffectationUID, GroupeAffectation> groupesAffectation;
        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            groupesAffectation = this.recupere_groupes_affectation(
                    connecteurDonneesPropositions,
                    parametres
            );
        }

        assertEquals(groupesAffectation.size(), 2);

    }

    Map<GroupeInternatUID, GroupeInternat> recupere_internats(
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
    ) throws Exception {

        Map<GroupeInternatUID, GroupeInternat> internats = Whitebox.invokeMethod(
                connecteurDonneesPropositions,
                "recupererInternats"
        );

        return internats;

    }

    @Test
    public void test_recuperation_verification_internats_avec_connecteur_toutes_donnees_doit_reussir() throws Exception {

        Map<GroupeInternatUID, GroupeInternat> internats;
        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            internats = this.recupere_internats(connecteurDonneesPropositions);
        }

        assertEquals(internats.size(), 2);

    }

    @Test
    public void test_recuperation_verification_internats_avec_connecteur_prod_doit_reussir() throws Exception {

        Map<GroupeInternatUID, GroupeInternat> internats;
        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            internats = this.recupere_internats(connecteurDonneesPropositions);
        }

        assertEquals(internats.size(), 2);

    }

    AlgoPropositionsEntree recupere_voeux_avec_demande_internat(
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions,
            Parametres parametres,
            boolean seulementVoeuxEnAttente
    ) throws Exception {

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(parametres);

        Whitebox.setInternalState(connecteurDonneesPropositions, "entree", entree);

        Map<GroupeAffectationUID, GroupeAffectation> groupesAffectation = Whitebox.invokeMethod(
                connecteurDonneesPropositions,
                "recupererGroupesAffectation",
                parametres,
                false // paramètre retroCompatibitilite
        );

        for (GroupeAffectation g : groupesAffectation.values()) {
            entree.ajouter(g);
        }

        Map<GroupeInternatUID, GroupeInternat> internats = Whitebox.invokeMethod(
                connecteurDonneesPropositions,
                "recupererInternats"
        );

        for (GroupeInternat internat : internats.values()) {
            entree.ajouter(internat);
        }

        Whitebox.invokeMethod(
                connecteurDonneesPropositions,
                "recupererVoeuxAvecInternatsAClassementPropre",
                entree.internatsIndex,
                seulementVoeuxEnAttente,
                true
        );

        return entree;

    }

    @Test
    public void test_recuperation_verification_tous_voeux_avec_demande_internat_avec_connecteur_toutes_donnees_doit_reussir() throws Exception {

        Parametres parametres = new Parametres(
                this.recupereBddNombreJoursEcoulesDepuisDebutCampagne(),
                this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats()
        );

        AlgoPropositionsEntree entree;
        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            entree = this.recupere_voeux_avec_demande_internat(
                    connecteurDonneesPropositions,
                    parametres,
                    false // paramètre seulementVoeuxEnAttente
            );
        }

        assertEquals(entree.voeux.size(), 4);
        // 3 voeux en attente + 1 voeu affecté dans le scénario établi

    }

    @Test
    public void test_recuperation_verification_tous_voeux_avec_demande_internat_avec_connecteur_prod_doit_reussir() throws Exception {

        Parametres parametres = new Parametres(
                this.recupereBddNombreJoursEcoulesDepuisDebutCampagne(),
                this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats()
        );

        AlgoPropositionsEntree entree;
        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            entree = this.recupere_voeux_avec_demande_internat(
                    connecteurDonneesPropositions,
                    parametres,
                    false // paramètre seulementVoeuxEnAttente
            );
        }

        assertEquals(entree.voeux.size(), 4);
        // 3 voeux en attente + 1 voeu affecté dans le scénario établi

    }

    @Test
    public void test_recuperation_verification_voeux_en_attente_seulement_avec_demande_internat_avec_connecteur_toutes_donnees_doit_reussir() throws Exception {

        Parametres parametres = new Parametres(
                this.recupereBddNombreJoursEcoulesDepuisDebutCampagne(),
                this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats()
        );

        AlgoPropositionsEntree entree;
        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            entree = this.recupere_voeux_avec_demande_internat(
                    connecteurDonneesPropositions,
                    parametres,
                    true // paramètre seulementVoeuxEnAttente
            );
        }

        assertEquals(3, entree.voeux.size());
        // 3 voeux en attente dans le scénario établi

    }

    @Test
    public void test_recuperation_verification_voeux_en_attente_seulement_avec_demande_internat_avec_connecteur_prod_doit_reussir() throws Exception {

        Parametres parametres = new Parametres(
                this.recupereBddNombreJoursEcoulesDepuisDebutCampagne(),
                this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats()
        );

        AlgoPropositionsEntree entree;
        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            entree = this.recupere_voeux_avec_demande_internat(
                    connecteurDonneesPropositions,
                    parametres,
                    true // paramètre seulementVoeuxEnAttente
            );
        }

        assertEquals(entree.voeux.size(), 3);
        // 3 voeux en attente dans le scénario établi

    }

    AlgoPropositionsEntree recupere_voeux_sans_demande_internat(
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions,
            Parametres parametres,
            boolean seulementVoeuxEnAttente
    ) throws Exception {

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(parametres);

        Whitebox.setInternalState(connecteurDonneesPropositions, "entree", entree);

        Map<GroupeAffectationUID, GroupeAffectation> groupesAffectation = Whitebox.invokeMethod(
                connecteurDonneesPropositions,
                "recupererGroupesAffectation",
                parametres,
                false // paramètre retroCompatibitilite
        );

        for (GroupeAffectation g : groupesAffectation.values()) {
            entree.ajouter(g);
        }

        Map<GroupeInternatUID, GroupeInternat> internats = Whitebox.invokeMethod(
                connecteurDonneesPropositions,
                "recupererInternats"
        );

        for (GroupeInternat internat : internats.values()) {
            entree.ajouter(internat);
        }

        Whitebox.invokeMethod(
                connecteurDonneesPropositions,
                "recupererVoeuxSansInternatAClassementPropre",
                seulementVoeuxEnAttente,
                true
        );

        return entree;

    }

    @Test
    public void test_recuperation_verification_tous_voeux_en_attente_sans_demande_internat_avec_connecteur_toutes_donnees_doit_reussir() throws Exception {

        Parametres parametres = new Parametres(
                this.recupereBddNombreJoursEcoulesDepuisDebutCampagne(),
                this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats()
        );

        AlgoPropositionsEntree entree;
        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            entree = this.recupere_voeux_sans_demande_internat(
                    connecteurDonneesPropositions,
                    parametres,
                    false // paramètre seulementVoeuxEnAttente
            );
        }

        assertEquals(entree.voeux.size(), 1);
        // 1 voeu affecté dans le scénario établi

    }

    @Test
    public void test_recuperation_verification_tous_voeux_en_attente_sans_demande_internat_avec_connecteur_prod_doit_reussir() throws Exception {

        Parametres parametres = new Parametres(
                this.recupereBddNombreJoursEcoulesDepuisDebutCampagne(),
                this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats()
        );

        AlgoPropositionsEntree entree;
        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            entree = this.recupere_voeux_sans_demande_internat(
                    connecteurDonneesPropositions,
                    parametres,
                    false // paramètre seulementVoeuxEnAttente
            );
        }

        assertEquals(entree.voeux.size(), 1);
        // 1 voeu affecté dans le scénario établi

    }

    @Test
    public void test_recuperation_verification_voeux_en_attente_seulement_sans_demande_internat_avec_connecteur_toutes_donnees_doit_reussir() throws Exception {

        Parametres parametres = new Parametres(
                this.recupereBddNombreJoursEcoulesDepuisDebutCampagne(),
                this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats()
        );

        AlgoPropositionsEntree entree;
        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            entree = this.recupere_voeux_sans_demande_internat(
                    connecteurDonneesPropositions,
                    parametres,
                    true // paramètre seulementVoeuxEnAttente
            );
        }

        assertEquals(entree.voeux.size(), 0);
        // aucun voeu en attente dans le scénario établi

    }

    @Test
    public void test_recuperation_verification_voeux_en_attente_seulement_sans_demande_internat_avec_connecteur_prod_doit_reussir() throws Exception {

        Parametres parametres = new Parametres(
                this.recupereBddNombreJoursEcoulesDepuisDebutCampagne(),
                this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats()
        );

        AlgoPropositionsEntree entree;
        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            entree = this.recupere_voeux_sans_demande_internat(
                    connecteurDonneesPropositions,
                    parametres,
                    true // paramètre seulementVoeuxEnAttente
            );
        }

        assertEquals(entree.voeux.size(), 0);
        // aucun voeu en attente dans le scénario établi

    }

    public AlgoPropositionsEntree recupere_propositions_actuelles(
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions,
            Parametres parametres
    ) throws Exception {

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(parametres);

        Whitebox.setInternalState(connecteurDonneesPropositions, "entree", entree);

        for (GroupeInternat internat : connecteurDonneesPropositions.recupererInternats().values()) {
            entree.ajouter(internat);
        }
        Whitebox.invokeMethod(
                connecteurDonneesPropositions,
                "recupererPropositionsActuelles",
                entree.internatsIndex
        );

        return entree;

    }

    @Test
    public void test_recuperation_propositions_actuelles_avec_connecteur_toutes_donnees_doit_reussir() throws Exception {

        Parametres parametres = new Parametres(
                this.recupereBddNombreJoursEcoulesDepuisDebutCampagne(),
                this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats()
        );

        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            AlgoPropositionsEntree entree = this.recupere_propositions_actuelles(
                    connecteurDonneesPropositions,
                    parametres
            );
        }

    }

    @Test
    public void test_recuperation_propositions_actuelles_avec_connecteur_prod_doit_reussir() throws Exception {

        Parametres parametres = new Parametres(
                this.recupereBddNombreJoursEcoulesDepuisDebutCampagne(),
                this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats()
        );

        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            AlgoPropositionsEntree entree = this.recupere_propositions_actuelles(
                    connecteurDonneesPropositions,
                    parametres
            );
        }

    }

    public AlgoPropositionsEntree recupere_donnees(
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions,
            Parametres parametres
    ) throws Exception {

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(parametres);

        Whitebox.setInternalState(connecteurDonneesPropositions, "entree", entree);

        Whitebox.invokeMethod(
                connecteurDonneesPropositions,
                "recupererDonnees"
        );

        return entree;

    }

    @Test
    public void test_recuperation_donnees_avec_connecteur_prod_doit_reussir_si_base_verouillee() throws Exception {

        this.setValeurFlagInterruptionFluxDonnees(1);
        // on active le flag FLAG_INTERRUP_DONNEES dans la table G_PAR

        Parametres parametres = new Parametres(
                this.recupereBddNombreJoursEcoulesDepuisDebutCampagne(),
                this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats()
        );

        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            // rappel : par défaut le connecteur est configuré avec : verifierInterruptionFluxDonneesEntrantes = true
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            AlgoPropositionsEntree entree = this.recupere_donnees(
                    connecteurDonneesPropositions,
                    parametres
            );
        }

    }

    @Test
    public void test_recuperation_donnees_avec_connecteur_prod_doit_echouer_si_base_verouillee() throws Exception {

        this.setValeurFlagInterruptionFluxDonnees(0);
        // on desactive le flag FLAG_INTERRUP_DONNEES dans la table G_PAR

        Throwable exception = assertThrows(AccesDonneesException.class, () -> {

            Parametres parametres = new Parametres(
                    this.recupereBddNombreJoursEcoulesDepuisDebutCampagne(),
                    this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats()
            );

            try (ConnecteurSQL connecteurSQL
                    = getConnecteurDonneesProd()) {
                // rappel : par défaut le connecteur est configuré avec : verifierInterruptionFluxDonneesEntrantes = true
                ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                        = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
                // rappel : le connecteur "prod" est configuré avec : verifierInterruptionFluxDonneesEntrantes = true
                AlgoPropositionsEntree entree = this.recupere_donnees(
                        connecteurDonneesPropositions,
                        parametres
                );
            }

        });

    }

    public Set<VoeuUID> test_recuperation_proposition_anterieure_dans_meme_formation(
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions,
            Parametres parametres
    ) throws Exception {

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(parametres);

        Whitebox.setInternalState(connecteurDonneesPropositions, "entree", entree);

        Set<VoeuUID> voeuxAvecPropositionAnterieureDansMemeFormation = Whitebox.invokeMethod(
                connecteurDonneesPropositions,
                "recupererVoeuxAvecPropositionAnterieureDansMemeFormation"
        );

        return voeuxAvecPropositionAnterieureDansMemeFormation;

    }

    @Test
    public void test_recuperation_voeux_avec_proposition_anterieure_dans_meme_formation_avec_connecteur_toutes_donnees_doit_reussir() throws Exception {

        Parametres parametres = new Parametres(
                this.recupereBddNombreJoursEcoulesDepuisDebutCampagne(),
                this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats()
        );

        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            Set<VoeuUID> voeuxAvecPropositionAnterieureDansMemeFormation = this.test_recuperation_proposition_anterieure_dans_meme_formation(
                    connecteurDonneesPropositions,
                    parametres
            );
        }

    }

    @Test
    public void test_recuperation_voeux_avec_proposition_anterieure_dans_meme_formation_avec_connecteur_prod_doit_reussir() throws Exception {

        Parametres parametres = new Parametres(
                this.recupereBddNombreJoursEcoulesDepuisDebutCampagne(),
                this.recupereBddNombreTotalJoursEntreDebutCampagneEtOuvertureCompleteInternats()
        );

        try (ConnecteurSQL connecteurSQL
                = getConnecteurDonneesProd()) {
            ConnecteurDonneesPropositionsSQL connecteurDonneesPropositions
                    = new ConnecteurDonneesPropositionsSQL(connecteurSQL.connection());
            Set<VoeuUID> voeuxAvecPropositionAnterieureDansMemeFormation = this.test_recuperation_proposition_anterieure_dans_meme_formation(
                    connecteurDonneesPropositions,
                    parametres
            );
        }

    }

}
