package fr.parcoursup.algos.propositions.algo;

import fr.parcoursup.algos.exceptions.VerificationException;
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
    public void appliquerDemissionsAutomatiqueGDD_doit_libererPlaces() throws VerificationException {
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g1 = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, 0, p);
        GroupeAffectation g2 = new GroupeAffectation(1, new GroupeAffectationUID(1, 1, 1), 1, 1, 0, p);
        GroupeAffectation g3 = new GroupeAffectation(1, new GroupeAffectationUID(2, 2, 2), 1, 1, 0, p);
        Voeu v1 = new Voeu(1, false, g1.id, 1, 1, 0, StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE, false);
        Voeu v2 = new Voeu(1, false, g2.id, 1, 1, 1, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Voeu v3 = new Voeu(1, false, g2.id, 1, 1, 2, StatutVoeu.PROPOSITION_DU_JOUR, false);
        Voeu v4 = new Voeu(1, false, g3.id, 1, 1, 3, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Voeu v5 = new Voeu(1, false, g3.id, 1, 1, 4, StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_EN_ATTENTE_DE_REPONSE_DU_CANDIDAT, false);
        List<Voeu> voeux = Arrays.asList(v1, v2, v3, v4, v5);
        StatutsVoeux statuts = new StatutsVoeux(voeux);
        DemissionAutoVoeuxOrdonnes.appliquerDemissionAutomatiqueVoeuOrdonnes(
                statuts,
                Map.of(1,2),
                Set.of()
        );
        assertEquals(StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE, statuts.getStatut(v1));
        assertEquals(StatutVoeu.EN_ATTENTE_DE_PROPOSITION, statuts.getStatut(v2));
        assertEquals(StatutVoeu.PROPOSITION_DU_JOUR, statuts.getStatut(v3));
        assertEquals(StatutVoeu.DEMISSION_AUTO_VOEU_ORDONNE_EN_ATTENTE, statuts.getStatut(v4));
        assertEquals(StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_EN_ATTENTE_DE_REPONSE_DU_CANDIDAT, statuts.getStatut(v5));
    }

}