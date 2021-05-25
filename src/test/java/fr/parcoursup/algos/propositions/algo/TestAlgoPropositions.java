package fr.parcoursup.algos.propositions.algo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestAlgoPropositions { // A terme, il faudra peut-être inclure les codes et les tests de
                                    // verificationAlgoPropositions

    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<AlgoPropositions> constructor = AlgoPropositions.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void calcule_doit_reussir_memeSiAucunMeilleurBachelier() throws Exception {
        // True branch coverage de la ligne 75
        Parametres p = new Parametres(1, 10);

        final GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        final GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        final GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        final GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);

        final List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(0, groupeAffectation.id, 1, 0, groupeInternat.id, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false));
        voeux.add(new Voeu(0, groupeAffectation.id, 2, 0, groupeInternat.id, 2, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));

        final AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);

        AlgoPropositions.calcule(entree);
    }

    @Test
    public void calcule_doit_reussir_avec_propositionMeilleurBachelier() throws Exception {
        // True branch coverage de la ligne 159
        // Puisqu'on teste en premier que le voeu est eligible à un groupeAffectation.id,
        // on teste ensuite si il rentre dans l'internat. Si il ne rentre pas dans
        // l'internat, on diminue la position admission et on reteste si il rentre dans
        // le groupeAffectation...
        // D'où le besoin d'avoir + de places en groupeAffectation qu'en groupeInternat
        // pour le test qui doit effectivement mettre à jour la positionadmission de
        // l'internat.
        // La positionAdmission initiale de l'internat doit également être assez
        // élevée,, et pour cela on doit être à un jour assez avancé dans le calendrier.
        Parametres p = new Parametres(61, 60);

        final GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        final GroupeAffectation groupeAffectation = new GroupeAffectation(2, groupeAffectationUID, 2, 2, p);

        final GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        final GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);

        final List<Voeu> voeux = new ArrayList<>();
        Voeu v1 = new Voeu(1, groupeAffectation.id, 1, 0, groupeInternat.id, 1, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Voeu v2 = new Voeu(2, groupeAffectation.id, 2, 0, groupeInternat.id, 2, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        voeux.add(v1);
        voeux.add(v2);

        final AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);

        AlgoPropositions.calcule(entree);
    }

    @Test
    public void calcule_doit_reussir_siVerifierEstFalse() throws Exception {
        Parametres p = new Parametres(1, 60);

        final GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        final GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        final GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        final GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);

        final List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(1, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 1, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));
        voeux.add(new Voeu(2, groupeAffectation.id, 2, 2, groupeInternat.id, 2, 2, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));

        final AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);

        AlgoPropositions.calcule(entree, false);
    }

    @Test
    public void calcule_doit_reussir_siCandidatsAvecRepondeurAutomatique() throws Exception {
        Parametres p = new Parametres(1, 60);

        final GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        final GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        final GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        final GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);

        final List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(1, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 1, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));
        voeux.add(new Voeu(2, groupeAffectation.id, 2, 2, groupeInternat.id, 2, 2, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));

        final AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);

        entree.candidatsAvecRepondeurAutomatique.add(voeux.get(0).id.gCnCod);
        entree.candidatsAvecRepondeurAutomatique.add(voeux.get(1).id.gCnCod);

        AlgoPropositions.calcule(entree, false);
    }

    @Test
    public void calcule_doit_reussir_siCandidatsAvecRepondeurAutomatiqueEtAutreCandidatAffecteHorsPP() throws Exception {
        Parametres p = new Parametres(1, 60);

        final GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        final GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        final GroupeInternatUID groupeInternatUID = new GroupeInternatUID(1, 0);
        final GroupeInternat groupeInternat = new GroupeInternat(groupeInternatUID, 1);

        final List<Voeu> voeux = new ArrayList<>();
        voeux.add(new Voeu(1, groupeAffectation.id, 1, 1, groupeInternat.id, 1, 1, Voeu.StatutVoeu.AFFECTE_JOURS_PRECEDENTS, true));
        voeux.add(new Voeu(2, groupeAffectation.id, 2, 2, groupeInternat.id, 2, 2, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));
        voeux.add(new Voeu(3, groupeAffectation.id, 3, 3, groupeInternat.id, 3, 3, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false));

        final AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(groupeAffectation.id, groupeAffectation);
        entree.internats.put(groupeInternat.id, groupeInternat);

        entree.candidatsAvecRepondeurAutomatique.add(voeux.get(0).id.gCnCod);
        entree.candidatsAvecRepondeurAutomatique.add(voeux.get(1).id.gCnCod);

        AlgoPropositions.calcule(entree, false);
    }

    @Test
    public void calcule_doit_reussir_siRepondeurLiberePlaces() throws Exception {
        Parametres p = new Parametres(1, 60);

        final GroupeAffectation g1 = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        final GroupeAffectation g2 = new GroupeAffectation(1, new GroupeAffectationUID(1, 1, 1), 1, 1, p);

        final List<Voeu> voeux = new ArrayList<>();
        Voeu v1 = new Voeu(1, false, g1.id, 1, 1, 2, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Voeu v2 = new Voeu(1, false, g2.id, 1, 1, 1, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        voeux.add(v1);
        voeux.add(v2);

        final AlgoPropositionsEntree entree = new AlgoPropositionsEntree(p);
        entree.voeux.addAll(voeux);
        entree.groupesAffectations.put(g1.id, g1);
        entree.groupesAffectations.put(g2.id, g2);

        entree.candidatsAvecRepondeurAutomatique.add(v1.id.gCnCod);  // v2 est le même candidat

        AlgoPropositions.calcule(entree, false);
    }

}