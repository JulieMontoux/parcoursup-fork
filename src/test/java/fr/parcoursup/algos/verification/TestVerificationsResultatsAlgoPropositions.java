package fr.parcoursup.algos.verification;

import java.util.*;
import java.util.logging.LogManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fr.parcoursup.algos.propositions.Helpers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import fr.parcoursup.algos.propositions.algo.AlgoPropositionsEntree;
import fr.parcoursup.algos.propositions.algo.AlgoPropositionsSortie;
import fr.parcoursup.algos.propositions.algo.GroupeAffectation;
import fr.parcoursup.algos.propositions.algo.GroupeAffectationUID;
import fr.parcoursup.algos.propositions.algo.GroupeInternat;
import fr.parcoursup.algos.propositions.algo.GroupeInternatUID;
import fr.parcoursup.algos.propositions.algo.Parametres;
import fr.parcoursup.algos.propositions.algo.Voeu;

@RunWith(PowerMockRunner.class)
@PrepareForTest(VerificationsResultatsAlgoPropositions.class)
public class TestVerificationsResultatsAlgoPropositions {

    @BeforeClass
    public static void setUpBeforeClass() {
        LogManager.getLogManager().reset();
    }

    @Test
    public void verifier_doit_reussir_si_sansInternat_et_proposition_correlee_de_ordreAppel() throws Exception {
        // P1
        Parametres p = new Parametres(1, 0);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, false, groupeAffectation.id, 2, 2, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));
        

        VerificationsResultatsAlgoPropositions verificationsResultatsAlgoPropositions = new VerificationsResultatsAlgoPropositions();
        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.injecterGroupesEtInternatsDansVoeux();

        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);

        verificationsResultatsAlgoPropositions.verifier(entree, sortie);
    }

    @Test
    public void verifier_doit_reussir_si_avecInternat_et_proposition_internat_correlee_ordreAppel()
            throws Exception {
        // P2
        Parametres p = new Parametres(1, 0);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, groupeAffectation.id, 2, 2, groupeInternat.id, 2, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION,
                false));

        VerificationsResultatsAlgoPropositions verificationsResultatsAlgoPropositions = new VerificationsResultatsAlgoPropositions();
        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        entree.injecterGroupesEtInternatsDansVoeux();
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.internats.add(groupeInternat);

        verificationsResultatsAlgoPropositions.verifier(entree, sortie);
    }

    @Test
    public void verifier_doit_reussir_si_c1_internat_et_c2_nonInternat_et_c1_rangInternat_inferieur_a_c2_rangInternat()
            throws Exception {
        // P3
        Parametres p = new Parametres(1, 0);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, groupeAffectation.id, 2, 2, groupeInternat.id, 2, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION,
                false));

        VerificationsResultatsAlgoPropositions verificationsResultatsAlgoPropositions = new VerificationsResultatsAlgoPropositions();
        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        entree.injecterGroupesEtInternatsDansVoeux();
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.internats.add(groupeInternat);

        verificationsResultatsAlgoPropositions.verifier(entree, sortie);
    }

    @Test
    public void verifier_doit_reussir_si_nombreDePropositions_est_inferieur_au_nombreDePlacesVacantes_et_candidatsEnAttenteDeProposition_sont_avecDemandeInternat_et_rangInternat_superieur_a_la_barreAdmission_de_lInternat() throws Exception {
        //P4
        Parametres p = new Parametres(1, 0);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(2, groupeAffectationUID, 0, 0, p);

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);
        Whitebox.setInternalState(groupeInternat, "positionMaximaleAdmission", 1);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, groupeAffectation.id, 2, 2, groupeInternat.id, 2, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));

        VerificationsResultatsAlgoPropositions verificationsResultatsAlgoPropositions = new VerificationsResultatsAlgoPropositions();
        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        entree.injecterGroupesEtInternatsDansVoeux();
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.internats.add(groupeInternat);

        verificationsResultatsAlgoPropositions.verifier(entree, sortie);
    }

    @Test
    public void verifier_doit_reussir_si_P4_non_satisfait_et_loggerEtAfficher_et_passage_CompensableParLeVoeu() throws Exception {
        Parametres p = new Parametres(1, 0);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(2, groupeAffectationUID, 0, 0, p);

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);
        Whitebox.setInternalState(groupeInternat, "positionMaximaleAdmission", 2);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, groupeAffectation.id, 2, 2, groupeInternat.id, 2, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));

        VerificationsResultatsAlgoPropositions verificationsResultatsAlgoPropositions = new VerificationsResultatsAlgoPropositions();  //TODO était initiallement en false, verifier que ça ne casse rien
        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        entree.injecterGroupesEtInternatsDansVoeux();
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.internats.add(groupeInternat);

        verificationsResultatsAlgoPropositions.verifier(entree, sortie);
    }

    @Test
    public void verifier_doit_reussir_si_P4_non_satisfait_et_loggerEtAfficher_et_passage_sansClassementInternat() throws Exception {
        Parametres p = new Parametres(1, 0);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(2, groupeAffectationUID, 0, 0, p);

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);
        Whitebox.setInternalState(groupeInternat, "positionMaximaleAdmission", 2);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, false, groupeAffectation.id, 2, 2, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));

        VerificationsResultatsAlgoPropositions verificationsResultatsAlgoPropositions = new VerificationsResultatsAlgoPropositions();  //TODO était initiallement en false, verifier que ça ne casse rien
        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        entree.injecterGroupesEtInternatsDansVoeux();
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.internats.add(groupeInternat);

        verificationsResultatsAlgoPropositions.verifier(entree, sortie);
    }

    @Test
    public void verifier_doit_reussir_si_P4_surCapacite() throws Exception {
        Parametres p = new Parametres(1, 0);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);
        Whitebox.setInternalState(groupeInternat, "positionMaximaleAdmission", 2);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, false, groupeAffectation.id, 2, 2, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));

        VerificationsResultatsAlgoPropositions verificationsResultatsAlgoPropositions = new VerificationsResultatsAlgoPropositions();  //TODO était initiallement en false, verifier que ça ne casse rien
        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        entree.injecterGroupesEtInternatsDansVoeux();
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.internats.add(groupeInternat);

        verificationsResultatsAlgoPropositions.verifier(entree, sortie);
    }

    @Test
    public void verifier_doit_reussir_si_P5_satisfait() throws Exception {
        Parametres p = new Parametres(1, 0);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(2, groupeAffectationUID, 0, 0, p);  // La formation ne prend qu'une personne

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 2);
        Whitebox.setInternalState(groupeInternat, "positionMaximaleAdmission", 1);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(1, groupeAffectation.id, 2, 2, groupeInternat.id, 2, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));

        VerificationsResultatsAlgoPropositions verificationsResultatsAlgoPropositions = new VerificationsResultatsAlgoPropositions();
        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);
        entree.injecterGroupesEtInternatsDansVoeux();
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.internats.add(groupeInternat);

        verificationsResultatsAlgoPropositions.verifier(entree, sortie);
    }


    @Test
    public void verifier_doit_reussir_meme_si_un_groupe_valide_et_un_groupe_non_valide() throws Exception {
        Parametres p = new Parametres(1, 0);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        GroupeAffectationUID groupeValideUID = new GroupeAffectationUID(1, 1, 1);
        GroupeAffectation groupeValide = new GroupeAffectation(1, groupeValideUID, 1, 1, p);

        List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));
        voeux.add(new Voeu(1, false, groupeAffectation.id, 2, 2, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(2, false, groupeValide.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));

        VerificationsResultatsAlgoPropositions verificationsResultatsAlgoPropositions = new VerificationsResultatsAlgoPropositions();  //TODO était initiallement en false avec ecriture dans /tmp/test-parcoursup.txt, verifier que ça ne casse rien
        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.groupesAffectations.put(groupeValide.id, groupeValide);
        entree.injecterGroupesEtInternatsDansVoeux();

        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
        sortie.voeux.addAll(voeux);
        sortie.groupes.add(groupeAffectation);
        sortie.groupes.add(groupeValide);

        verificationsResultatsAlgoPropositions.verifier(entree, sortie);
    }

    @Test
    public void verifier_doit_reussir_si_multiples_internats() throws Exception {
        Parametres p = new Parametres(1, 0);

        VerificationsResultatsAlgoPropositions verificationsResultatsAlgoPropositions = new VerificationsResultatsAlgoPropositions();
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
        verificationsResultatsAlgoPropositions.verifier(entree, sortie);
    }

    @Test
    public void verifier_doit_reussir_si_multiples_groupeAffectation() throws Exception {
        Parametres p = new Parametres(1, 0);

        VerificationsResultatsAlgoPropositions verificationsResultatsAlgoPropositions = new VerificationsResultatsAlgoPropositions();
        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);

        for (int i=0; i<125; i++){
            GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(i, i, i);
            GroupeAffectation groupeAffectation = new GroupeAffectation(2, groupeAffectationUID, 0, 0, p);
            entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
            sortie.groupes.add(groupeAffectation);
        }

        verificationsResultatsAlgoPropositions.verifier(entree, sortie);
    }

    @Test
    public void verifier_doit_reussir_meme_si_internatPositionAdmission_superieureA_internatPositionMaximaleAdmission() throws Exception {
        Parametres p = new Parametres(1, 0);

        GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);
        Whitebox.setInternalState(groupeInternat, "positionMaximaleAdmission", 0);

        Whitebox.setInternalState(groupeInternat, "positionAdmission", 1);

        VerificationsResultatsAlgoPropositions verificationsResultatsAlgoPropositions = new VerificationsResultatsAlgoPropositions();  //TODO était initiallement en false, verifier que ça ne casse rien
        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.internats.put(groupeInternat.id, groupeInternat);
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
        sortie.internats.add(groupeInternat);

        verificationsResultatsAlgoPropositions.verifier(entree, sortie);
    }

    public void verifier_doit_reussir_et_logger_avertissement_si_aucune_proposition_supprimee() {
        // Coverage de la ligne 291.
        // Ne marche pas encore : Pour que ça fonctionne, il faut que le groupe soit invalidé, et pour cela il faut regarder ligne 209 puis on doit passer à la ligne 290
        // En fait, il faut trouver comment invalider un voeu sans pour autant être proposition du jour et donc ne pas se faire rajouter dans la liste des groupes à supprimer.
        // Donc c'est ligne 197 que ça doit être appelé
        // Il faut absolument avoir une exception pour invalider (catch de la ligne 207), mais en même temps il ne faut pas être en mode interrompreSiAlterte sinon on throw dans le catch et donc on n'invalide jamais. Donc il faut avoir une erreur entre 197 et 205 mais sans faire appel au loggerEtAfficher, il faut donc obligatoirement faire appel à la fonction alerter quelque part. Et pour que les autres puissent passer par "alerter", il faut obligatoirement une propositionDuJour quelque part.
        // En fait il ne faut pas de propositionDuJour dans sortie pour les groupes si on ne veut pas qu'il y en ait un de supprimé, mais en même temps il faut un groupeNonValide (et donc une proposition du jour pour satisfaire les conditions de 197). Donc le code en 291 semble inatteignable.
    }

    @Test
    public void verifier_doit_nettoyer_les_entrees_sorties_si_echec() throws Exception {
        //Coverage de la ligne 312
        //On doit être en mode nePasEchouerSiLoggerOuAfficher et on doit faire une exception/passer par alerter entre 114 et 307
        Parametres p = new Parametres(1, 0);
        VerificationsResultatsAlgoPropositions verificationsResultatsAlgoPropositions = new VerificationsResultatsAlgoPropositions();  //TODO était initiallement en false avec ecriture dans /tmp/test-parcoursup.txt, verifier que ça ne casse rien
        AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
        verificationsResultatsAlgoPropositions.verifier(entree, sortie);
    }

    @Test
    public void clotureTransitiveDependances_doit_etendre_les_groupesAIgnorer() throws Exception {
        //Objectif: Coverage des lignes 604 à 608
        
        Parametres p = new Parametres(1, 0);

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