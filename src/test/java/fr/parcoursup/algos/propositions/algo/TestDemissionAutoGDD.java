package fr.parcoursup.algos.propositions.algo;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.junit.Assert.*;

public class TestDemissionAutoGDD {

    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<DemissionAutoGDD> constructor = DemissionAutoGDD.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void appliquerDemissionsAutomatiqueGDD_doit_echouer_si_voeuEnAttenteARepondeurAutomatiqueActive() throws VerificationException {
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g1 = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeAffectation g2 = new GroupeAffectation(1, new GroupeAffectationUID(1, 1, 1), 1, 1, p);
        GroupeAffectation g3 = new GroupeAffectation(1, new GroupeAffectationUID(2, 2, 2), 1, 1, p);
        Voeu v1 = new Voeu(1, false, g1.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE, false);
        Voeu v2 = new Voeu(1, false, g2.id, 1, 1, 1, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Voeu v3 = new Voeu(1, false, g2.id, 1, 1, 2, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false);
        Voeu v4 = new Voeu(1, false, g3.id, 1, 1, 3, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Voeu v5 = new Voeu(1, false, g3.id, 1, 1, 4, Voeu.StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_EN_ATTENTE_DE_REPONSE_DU_CANDIDAT, false);
        v1.setEstArchive(false);
        v2.setEstArchive(true);
        v3.setEstArchive(true);
        v4.setEstArchive(true);
        v5.setEstArchive(true);
        v5.setRepondeurActive(true);
        Collection<Voeu> voeux = Arrays.asList(v1, v2, v3, v4, v5);
        Set<Integer> candidatsAvecRepondeurAuto = new HashSet<>();
        VerificationException exception = assertThrows(
                VerificationException.class,
                () -> DemissionAutoGDD.appliquerDemissionAutomatiqueGDD(voeux, candidatsAvecRepondeurAuto)
        );
        assertSame(VerificationExceptionMessage.VOEU_AVEC_REPONDEUR_NON_REFUSABLE_PAR_DEM_AUTO_GDD, exception.exceptionMessage);
    }

    @Test
    public void appliquerDemissionsAutomatiqueGDD_doit_libererPlaces() throws VerificationException {
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g1 = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeAffectation g2 = new GroupeAffectation(1, new GroupeAffectationUID(1, 1, 1), 1, 1, p);
        GroupeAffectation g3 = new GroupeAffectation(1, new GroupeAffectationUID(2, 2, 2), 1, 1, p);
        Voeu v1 = new Voeu(1, false, g1.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE, false);
        Voeu v2 = new Voeu(1, false, g2.id, 1, 1, 1, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Voeu v3 = new Voeu(1, false, g2.id, 1, 1, 2, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false);
        Voeu v4 = new Voeu(1, false, g3.id, 1, 1, 3, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Voeu v5 = new Voeu(1, false, g3.id, 1, 1, 4, Voeu.StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_EN_ATTENTE_DE_REPONSE_DU_CANDIDAT, false);
        v1.setEstArchive(false);
        v2.setEstArchive(true);
        v3.setEstArchive(true);
        v4.setEstArchive(true);
        v5.setEstArchive(true);
        Collection<Voeu> voeux = Arrays.asList(v1, v2, v3, v4, v5);
        Set<Integer> candidatsAvecRepondeurAuto = new HashSet<>();
        assertEquals(1, DemissionAutoGDD.appliquerDemissionAutomatiqueGDD(voeux, candidatsAvecRepondeurAuto));
        assertEquals(Voeu.StatutVoeu.GDD_DEMISSION_ATTENTE, v4.statut);
        assertEquals(Voeu.StatutVoeu.GDD_DEMISSION_PROP, v5.statut);
    }

}