package fr.parcoursup.algos.propositions.algo;

import fr.parcoursup.algos.exceptions.VerificationException;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.junit.Assert.*;

public class TestRepondeurAutomatique {

    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<RepondeurAutomatique> constructor = RepondeurAutomatique.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void appliquerRepondeurAutomatique_doit_libererPlaces() throws VerificationException {
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g1 = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, 0, p);
        GroupeAffectation g2 = new GroupeAffectation(1, new GroupeAffectationUID(1, 1, 1), 1, 1, 0, p);
        GroupeAffectation g3 = new GroupeAffectation(1, new GroupeAffectationUID(2, 2, 2), 1, 1, 0, p);
        Voeu v1 = new Voeu(1, false, g1.id, 1, 1, 1, StatutVoeu.PROPOSITION_DU_JOUR, false);
        Voeu v2 = new Voeu(1, false, g2.id, 1, 1, 2, StatutVoeu.REP_AUTO_ACCEPTE, false);
        Voeu v3 = new Voeu(1, false, g3.id, 1, 1, 3, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Collection<Voeu> voeux = Arrays.asList(v1, v2, v3);
        Set<Integer> candidats = new HashSet<>();
        candidats.add(v1.id.gCnCod);
        candidats.add(v2.id.gCnCod);
        candidats.add(v3.id.gCnCod);
        StatutsVoeux statuts = new StatutsVoeux(List.of(v1,v2,v3));
        assertEquals(List.of(v2), RepondeurAutomatique.appliquerRepondeurAutomatique(voeux, candidats, statuts, Map.of(1,1)));
        assertTrue(statuts.estDemissionAutomatiqueParRepondeurAutomatique(v2));
        assertTrue(statuts.estDemissionAutomatiqueParRepondeurAutomatique(v3));
    }

    @Test
    public void appliquerRepondeurAutomatique_doit_gerer_les_propositions_jours_precedents() throws VerificationException {
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g1 = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, 0, p);
        GroupeAffectation g2 = new GroupeAffectation(1, new GroupeAffectationUID(1, 1, 1), 1, 1, 0, p);
        GroupeAffectation g3 = new GroupeAffectation(1, new GroupeAffectationUID(2, 2, 2), 1, 1, 0, p);
        Voeu v1 = new Voeu(1, false, g1.id, 1, 1, 1, StatutVoeu.PROPOSITION_DU_JOUR, false);
        int rangInconnu = 0;
        Voeu v2 = new Voeu(1, false, g2.id, 1, 1, rangInconnu, StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE, false);
        Voeu v3 = new Voeu(1, false, g3.id, 1, 1, 3, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Collection<Voeu> voeux = Arrays.asList(v1, v2, v3);
        Set<Integer> candidats = new HashSet<>();
        candidats.add(v1.id.gCnCod);
        candidats.add(v2.id.gCnCod);
        candidats.add(v3.id.gCnCod);
        StatutsVoeux statuts = new StatutsVoeux(List.of(v1,v2,v3));
        assertEquals(List.of(v2), RepondeurAutomatique.appliquerRepondeurAutomatique(
                voeux,
                candidats,
                statuts,
                Map.of(1,1))
        );
        assertTrue(statuts.estDemissionAutomatiqueParRepondeurAutomatique(v2));
        assertTrue(statuts.estDemissionAutomatiqueParRepondeurAutomatique(v3));
    }

}