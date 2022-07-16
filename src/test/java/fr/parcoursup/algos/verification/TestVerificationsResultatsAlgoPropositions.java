package fr.parcoursup.algos.verification;

import fr.parcoursup.algos.propositions.Helpers;
import fr.parcoursup.algos.propositions.algo.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.*;
import java.util.logging.LogManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(VerificationsResultatsAlgoPropositions.class)
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
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, false, groupeAffectation.id, 2, 2, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));
        

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.injecterGroupesEtInternatsDansVoeux();

        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
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
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, groupeAffectation.id, 2, 2, groupeInternat.id, 2, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION,
                false));

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        entree.injecterGroupesEtInternatsDansVoeux();
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
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
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, groupeAffectation.id, 2, 2, groupeInternat.id, 2, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION,
                false));

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        entree.injecterGroupesEtInternatsDansVoeux();
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
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
        GroupeAffectation groupeAffectation = new GroupeAffectation(2, groupeAffectationUID, 0, 0, p);

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);
        Whitebox.setInternalState(groupeInternat, "positionMaximaleAdmission", 1);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, groupeAffectation.id, 2, 2, groupeInternat.id, 2, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        entree.injecterGroupesEtInternatsDansVoeux();
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.internats.add(groupeInternat);
        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_reussir_si_P4_non_satisfait_et_loggerEtAfficher_et_passage_CompensableParLeVoeu() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(2, groupeAffectationUID, 0, 0, p);

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);
        Whitebox.setInternalState(groupeInternat, "positionMaximaleAdmission", 2);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, groupeAffectation.id, 2, 2, groupeInternat.id, 2, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        entree.injecterGroupesEtInternatsDansVoeux();
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.internats.add(groupeInternat);

        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_reussir_si_P4_non_satisfait_et_loggerEtAfficher_et_passage_sansClassementInternat() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(2, groupeAffectationUID, 0, 0, p);

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);
        Whitebox.setInternalState(groupeInternat, "positionMaximaleAdmission", 2);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, false, groupeAffectation.id, 2, 2, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        entree.injecterGroupesEtInternatsDansVoeux();
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.internats.add(groupeInternat);

        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_reussir_si_P4_surCapacite() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);
        Whitebox.setInternalState(groupeInternat, "positionMaximaleAdmission", 2);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, false, groupeAffectation.id, 2, 2, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        entree.injecterGroupesEtInternatsDansVoeux();
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.internats.add(groupeInternat);

        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_reussir_si_P5_satisfait() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(2, groupeAffectationUID, 0, 0, p);  // La formation ne prend qu'une personne

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 2);
        Whitebox.setInternalState(groupeInternat, "positionMaximaleAdmission", 1);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, groupeAffectation.id, 2, 2, groupeInternat.id, 2, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        entree.injecterGroupesEtInternatsDansVoeux();
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.internats.add(groupeInternat);

        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }


    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_reussir_meme_si_un_groupe_valide_et_un_groupe_non_valide() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        GroupeAffectationUID groupeValideUID = new GroupeAffectationUID(1, 1, 1);
        GroupeAffectation groupeValide = new GroupeAffectation(1, groupeValideUID, 1, 1, p);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));
        voeux.add(new Voeu(1, false, groupeAffectation.id, 2, 2, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(2, false, groupeValide.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.groupesAffectations.put(groupeValide.id, groupeValide);
        entree.injecterGroupesEtInternatsDansVoeux();

        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.groupes.add(groupeValide);

        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_reussir_si_multiples_internats() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);

        for (int i=0; i<125; i++){
            GroupeInternatUID groupeInternatUID = new GroupeInternatUID(i, i);
            GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);
            Whitebox.setInternalState(groupeInternat, "positionMaximaleAdmission", 1);
            entree.internats.put(groupeInternat.id, groupeInternat);
            sortie.internats.add(groupeInternat);
        }
        entree.injecterGroupesEtInternatsDansVoeux();
        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_reussir_si_multiples_groupeAffectation() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);

        for (int i=0; i<125; i++){
            GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(i, i, i);
            GroupeAffectation groupeAffectation = new GroupeAffectation(2, groupeAffectationUID, 0, 0, p);
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
        Whitebox.setInternalState(groupeInternat, "positionMaximaleAdmission", 0);

        Whitebox.setInternalState(groupeInternat, "positionAdmission", 1);

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.internats.put(groupeInternat.id, groupeInternat);
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
        sortie.internats.add(groupeInternat);

        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_nettoyer_les_entrees_sorties_si_echec() throws Exception {
        //Coverage de la ligne 312
        //On doit être en mode nePasEchouerSiLoggerOuAfficher et on doit faire une exception/passer par alerter entre 114 et 307
        Parametres p = new Parametres(1, 0, 90);
        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);

        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void clotureTransitiveDependances_doit_etendre_les_groupesAIgnorer() throws Exception {
        //Objectif: Coverage des lignes 604 à 608
        
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);
        
        GroupeAffectationUID groupeAffectation2UID = new GroupeAffectationUID(1, 1, 1);
        GroupeAffectation groupeAffectation2 = new GroupeAffectation(1, groupeAffectation2UID, 0, 0, p);
        
        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);
        Whitebox.setInternalState(groupeInternat, "positionMaximaleAdmission", 2);
        
        Voeu v1 = Helpers.creeVoeuAvecInternatEtInjecteDependances(0,groupeAffectation, groupeInternat, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION,1, 1);
        Voeu v2 = Helpers.creeVoeuSansInternatEtInjecteDependances(1,groupeAffectation, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION,1);
        Voeu v3 = Helpers.creeVoeuAvecInternatEtInjecteDependances(2,groupeAffectation2, groupeInternat, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION,2, 2);

        List<Voeu> voeux = Arrays.asList(v1, v2, v3);

        Set<GroupeAffectation> groupesAIgnorer = new HashSet<>();
        groupesAIgnorer.add(groupeAffectation);

        assertFalse(groupesAIgnorer.contains(groupeAffectation2));
        Map<GroupeAffectation, List<Voeu>> voeuxParFormation = new HashMap<>();
        Map<GroupeInternat, List<Voeu>> voeuxParInternat = new HashMap<>();
        voeux.forEach(v -> voeuxParFormation.computeIfAbsent(v.getGroupeAffectation(), g -> new ArrayList<>()).add(v));
        voeux.forEach(v -> { if(v.avecInternatAClassementPropre()) { voeuxParInternat.computeIfAbsent(v.getInternat(), g -> new ArrayList<>()).add(v); } });

        Whitebox.invokeMethod(VerificationsResultatsAlgoPropositions.class, "clotureTransitiveDependances", groupesAIgnorer, voeuxParFormation, voeuxParInternat);
        assertTrue(groupesAIgnorer.contains(groupeAffectation2));
    }

}