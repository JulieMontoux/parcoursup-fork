package fr.parcoursup.algos.propositions.algo;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.junit.Assert.*;

public class TestDemissionAutoVoeuxOrdonnes {

    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<DemissionAutoVoeuxOrdonnes> constructor = DemissionAutoVoeuxOrdonnes.class.getDeclaredConstructor();
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
        v5.setRepondeurActive(true);
        Collection<Voeu> voeux = Arrays.asList(v1, v2, v3, v4, v5);
        Set<Integer> candidatsAvecRepondeurAuto = new HashSet<>();
        VerificationException exception = assertThrows(
                VerificationException.class,
                () -> DemissionAutoVoeuxOrdonnes.appliquerDemissionAutomatiqueVoeuOrdonnes(voeux, candidatsAvecRepondeurAuto)
        );
        assertSame(VerificationExceptionMessage.VOEU_AVEC_REPONDEUR_NON_REFUSABLE_PAR_DEM_AUTO_VOEUX_ORDONNES, exception.exceptionMessage);
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
        Collection<Voeu> voeux = Arrays.asList(v1, v2, v3, v4, v5);
        Set<Integer> candidatsAvecRepondeurAuto = new HashSet<>();
        assertEquals(1, DemissionAutoVoeuxOrdonnes.appliquerDemissionAutomatiqueVoeuOrdonnes(voeux, candidatsAvecRepondeurAuto));
        assertEquals(Voeu.StatutVoeu.DEMISSION_AUTO_VOEU_ORDONNE_EN_ATTENTE, v4.statut);
        assertEquals(Voeu.StatutVoeu.DEMISSION_AUTO_VOEU_ORDONNE_PROPOSITION_EN_ATTENTE_REP, v5.statut);
    }

}
