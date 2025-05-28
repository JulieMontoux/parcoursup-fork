package fr.parcoursup.algos.verification;

import fr.parcoursup.algos.propositions.Helpers;
import fr.parcoursup.algos.propositions.algo.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.reflect.Whitebox;


import java.util.*;
import java.util.logging.LogManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class TestVerificationsResultatsAlgoPropositions {

    @BeforeClass
    public static void setUpBeforeClass() {
        LogManager.getLogManager().reset();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_reussir_si_sansInternat_et_proposition_correlee_de_ordreAppel() throws Exception {
        // P1
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, 0, p);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, false, groupeAffectation.id, 1, 1, 0, StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, false, groupeAffectation.id, 2, 2, 0, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));
        

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);

        AlgoPropositionsSortie sortie = new AlgoPropositionsSortie(p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);

        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_reussir_si_avecInternat_et_proposition_internat_correlee_ordreAppel()
            throws Exception {
        // P2
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, 0, p);

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, groupeAffectation.id, 2, 2, groupeInternat.id, 2, 0, StatutVoeu.EN_ATTENTE_DE_PROPOSITION,
                false));

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        AlgoPropositionsSortie sortie = new AlgoPropositionsSortie(p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.internats.add(groupeInternat);

        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_reussir_si_c1_internat_et_c2_nonInternat_et_c1_rangInternat_inferieur_a_c2_rangInternat()
            throws Exception {
        // P3
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, 0, p);

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, groupeAffectation.id, 2, 2, groupeInternat.id, 2, 0, StatutVoeu.EN_ATTENTE_DE_PROPOSITION,
                false));

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        AlgoPropositionsSortie sortie = new AlgoPropositionsSortie(p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.internats.add(groupeInternat);

        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_reussir_si_nombreDePropositions_est_inferieur_au_nombreDePlacesVacantes_et_candidatsEnAttenteDeProposition_sont_avecDemandeInternat_et_rangInternat_superieur_a_la_barreAdmission_de_lInternat() throws Exception {
        //P4
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(2, groupeAffectationUID, 0, 0, 0, p);

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, groupeAffectation.id, 2, 2, groupeInternat.id, 2, 0, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        AlgoPropositionsSortie sortie = new AlgoPropositionsSortie(p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.internats.add(groupeInternat);
        sortie.barresMaximalesAdmissionInternats.put(groupeInternat.id, 1);
        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_reussir_si_P4_non_satisfait_et_loggerEtAfficher_et_passage_CompensableParLeVoeu() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(2, groupeAffectationUID, 0, 0, 0, p);

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, groupeAffectation.id, 2, 2, groupeInternat.id, 2, 0, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        AlgoPropositionsSortie sortie = new AlgoPropositionsSortie(p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.internats.add(groupeInternat);
        sortie.barresMaximalesAdmissionInternats.put(groupeInternat.id, 2);


        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_reussir_si_P4_non_satisfait_et_loggerEtAfficher_et_passage_sansClassementInternat() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(2, groupeAffectationUID, 0, 0, 0, p);

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, false, groupeAffectation.id, 2, 2, 0, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        AlgoPropositionsSortie sortie = new AlgoPropositionsSortie(p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.internats.add(groupeInternat);
        sortie.barresMaximalesAdmissionInternats.put(groupeInternat.id, 2);


        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_reussir_si_P4_surCapacite() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, 0, p);

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, false, groupeAffectation.id, 2, 2, 0, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        AlgoPropositionsSortie sortie = new AlgoPropositionsSortie(p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.internats.add(groupeInternat);
        sortie.barresMaximalesAdmissionInternats.put(groupeInternat.id, 2);

        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_reussir_si_P5_satisfait() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(2, groupeAffectationUID, 0, 0, 0, p);  // La formation ne prend qu'une personne

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 2);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, groupeAffectation.id, 2, 2, groupeInternat.id, 2, 0, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        AlgoPropositionsSortie sortie = new AlgoPropositionsSortie(p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.internats.add(groupeInternat);
        sortie.barresMaximalesAdmissionInternats.put(groupeInternat.id, 1);

        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }


    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_reussir_meme_si_un_groupe_valide_et_un_groupe_non_valide() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, 0, p);

        GroupeAffectationUID groupeValideUID = new GroupeAffectationUID(1, 1, 1);
        GroupeAffectation groupeValide = new GroupeAffectation(1, groupeValideUID, 1, 1, 0, p);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, false, groupeAffectation.id, 1, 1, 0, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));
        voeux.add(new Voeu(1, false, groupeAffectation.id, 2, 2, 0, StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(2, false, groupeValide.id, 1, 1, 0, StatutVoeu.PROPOSITION_DU_JOUR, false));

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.groupesAffectations.put(groupeValide.id, groupeValide);

        AlgoPropositionsSortie sortie = new AlgoPropositionsSortie(p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.groupes.add(groupeValide);

        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_reussir_si_multiples_internats() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        AlgoPropositionsSortie sortie = new AlgoPropositionsSortie(p);

        for (int i=0; i<125; i++){
            GroupeInternatUID groupeInternatUID = new GroupeInternatUID(i, i);
            GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);
            entree.internats.put(groupeInternat.id, groupeInternat);
            sortie.internats.add(groupeInternat);
            sortie.barresMaximalesAdmissionInternats.put(groupeInternat.id, 1);
        }
        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_reussir_si_multiples_groupeAffectation() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        AlgoPropositionsSortie sortie = new AlgoPropositionsSortie(p);

        for (int i=0; i<125; i++){
            GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(i, i, i);
            GroupeAffectation groupeAffectation = new GroupeAffectation(2, groupeAffectationUID, 0, 0, 0, p);
            entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
            sortie.groupes.add(groupeAffectation);
        }

        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_reussir_meme_si_internatPositionAdmission_superieureA_internatPositionMaximaleAdmission() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.internats.put(groupeInternat.id, groupeInternat);
        AlgoPropositionsSortie sortie = new AlgoPropositionsSortie(p);
        sortie.internats.add(groupeInternat);
        sortie.barresMaximalesAdmissionInternats.put(groupeInternat.id, 1);
        sortie.barresAdmissionInternats.put(groupeInternat.id, 1);

        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_nettoyer_les_entrees_sorties_si_echec() throws Exception {
        //Coverage de la ligne 312
        //On doit être en mode nePasEchouerSiLoggerOuAfficher et on doit faire une exception/passer par alerter entre 114 et 307
        Parametres p = new Parametres(1, 0, 90);
        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        AlgoPropositionsSortie sortie = new AlgoPropositionsSortie(p);

        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }

    @Test
    public void clotureTransitiveDependances_doit_etendre_les_groupesAIgnorer() throws Exception {
        //Objectif: Coverage des lignes 604 à 608
        
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, 0, p);
        
        GroupeAffectationUID groupeAffectation2UID = new GroupeAffectationUID(1, 1, 1);
        GroupeAffectation groupeAffectation2 = new GroupeAffectation(1, groupeAffectation2UID, 0, 0, 0, p);
        
        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);

        Voeu v1 = Helpers.creeVoeuAvecInternat(0,groupeAffectation, groupeInternat, StatutVoeu.EN_ATTENTE_DE_PROPOSITION,1, 1);
        Voeu v2 = Helpers.creeVoeuSansInternatEtInjecteDependances(1,groupeAffectation, StatutVoeu.EN_ATTENTE_DE_PROPOSITION,1);
        Voeu v3 = Helpers.creeVoeuAvecInternat(2,groupeAffectation2, groupeInternat, StatutVoeu.EN_ATTENTE_DE_PROPOSITION,2, 2);

        List<Voeu> voeux = Arrays.asList(v1, v2, v3);

        Set<GroupeAffectationUID> groupesAIgnorer = new HashSet<>();
        groupesAIgnorer.add(groupeAffectation.id);

        assertFalse(groupesAIgnorer.contains(groupeAffectation2.id));
        Map<GroupeAffectationUID, List<Voeu>> voeuxParFormation = new HashMap<>();
        Map<GroupeInternatUID, List<Voeu>> voeuxParInternat = new HashMap<>();
        voeux.forEach(v -> voeuxParFormation.computeIfAbsent(v.groupeUID, g -> new ArrayList<>()).add(v));
        voeux.forEach(v -> { if(v.avecInternatAClassementPropre()) { voeuxParInternat.computeIfAbsent(v.internatUID, g -> new ArrayList<>()).add(v); } });

        Set<GroupeAffectationUID> groupesInvalides =
                Whitebox.invokeMethod(
                VerificationsResultatsAlgoPropositions.class,
                "clotureTransitiveDependances",
                List.of(groupeAffectation, groupeAffectation2),
                groupesAIgnorer,
                voeuxParFormation,
                voeuxParInternat
        );
        assertTrue(groupesInvalides.contains(groupeAffectation2.id));
    }

}