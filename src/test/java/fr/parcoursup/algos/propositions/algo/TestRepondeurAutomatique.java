package fr.parcoursup.algos.propositions.algo;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;
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
    public void appliquerRepondeurAutomatique_doit_echouer_si_voeuEnAttenteNaPasDeRepondeurAutomatiqueActive() throws VerificationException {
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);
        Voeu v1 = new Voeu(0, g.id, 1, 1, gi.id, 1, 1,
                Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION,
                false);
        Collection<Voeu> voeux = new ArrayList<>();
        v1.setRepondeurActive(false);
        voeux.add(v1);
        Set<Integer> candidats = new HashSet<>();
        candidats.add(v1.id.gCnCod);
        VerificationException exception = assertThrows(
                VerificationException.class,
                () -> RepondeurAutomatique.appliquerRepondeurAutomatique(voeux, candidats)
        );
        assertSame(VerificationExceptionMessage.REPONDEUR_AUTOMATIQUE_INCOHERENCE_VOEU_EN_ATTENTE_AVEC_RA_MAIS_SANS_RANG, exception.exceptionMessage);
    }

    @Test
    public void appliquerRepondeurAutomatique_doit_libererPlaces() throws VerificationException {
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g1 = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeAffectation g2 = new GroupeAffectation(1, new GroupeAffectationUID(1, 1, 1), 1, 1, p);
        GroupeAffectation g3 = new GroupeAffectation(1, new GroupeAffectationUID(2, 2, 2), 1, 1, p);
        Voeu v1 = new Voeu(1, false, g1.id, 1, 1, 1, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false);
        v1.setRepondeurActive(true);
        Voeu v2 = new Voeu(1, false, g2.id, 1, 1, 2, Voeu.StatutVoeu.REP_AUTO_ACCEPTE, false);
        v2.setRepondeurActive(true);
        Voeu v3 = new Voeu(1, false, g3.id, 1, 1, 3, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        v3.setRepondeurActive(true);
        Collection<Voeu> voeux = Arrays.asList(v1, v2, v3);
        Set<Integer> candidats = new HashSet<>();
        candidats.add(v1.id.gCnCod);
        candidats.add(v2.id.gCnCod);
        candidats.add(v3.id.gCnCod);
        assertEquals(1, RepondeurAutomatique.appliquerRepondeurAutomatique(voeux, candidats));
        assertEquals(Voeu.StatutVoeu.REP_AUTO_REFUS_PROPOSITION, v2.statut);
        assertEquals(Voeu.StatutVoeu.REP_AUTO_DEMISSION_ATTENTE, v3.statut);
    }

    @Test
    public void appliquerRepondeurAutomatique_doit_gerer_les_propositions_jours_precedents() throws VerificationException {
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g1 = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeAffectation g2 = new GroupeAffectation(1, new GroupeAffectationUID(1, 1, 1), 1, 1, p);
        GroupeAffectation g3 = new GroupeAffectation(1, new GroupeAffectationUID(2, 2, 2), 1, 1, p);
        Voeu v1 = new Voeu(1, false, g1.id, 1, 1, 1, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false);
        v1.setRepondeurActive(true);
        int rangInconnu = 0;
        Voeu v2 = new Voeu(1, false, g2.id, 1, 1, rangInconnu, Voeu.StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE, false);
        v2.setRepondeurActive(true);
        Voeu v3 = new Voeu(1, false, g3.id, 1, 1, 3, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        v3.setRepondeurActive(true);
        Collection<Voeu> voeux = Arrays.asList(v1, v2, v3);
        Set<Integer> candidats = new HashSet<>();
        candidats.add(v1.id.gCnCod);
        candidats.add(v2.id.gCnCod);
        candidats.add(v3.id.gCnCod);
        assertEquals(1, RepondeurAutomatique.appliquerRepondeurAutomatique(voeux, candidats));
        assertEquals(Voeu.StatutVoeu.REP_AUTO_REFUS_PROPOSITION, v2.statut);
        assertEquals(Voeu.StatutVoeu.REP_AUTO_DEMISSION_ATTENTE, v3.statut);
    }

}