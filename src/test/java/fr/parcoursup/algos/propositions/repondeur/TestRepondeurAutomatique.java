package fr.parcoursup.algos.propositions.repondeur;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import fr.parcoursup.algos.propositions.algo.GroupeAffectation;
import fr.parcoursup.algos.propositions.algo.GroupeAffectationUID;
import fr.parcoursup.algos.propositions.algo.GroupeInternat;
import fr.parcoursup.algos.propositions.algo.GroupeInternatUID;
import fr.parcoursup.algos.propositions.algo.Parametres;
import fr.parcoursup.algos.propositions.algo.Voeu;

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
    public void comparerVoeux_doit_echouer_si_voeuxAffectesJoursPrecedents() throws VerificationException {
        Parametres p = new Parametres(59, 60);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);
        Voeu v1 = new Voeu(0, g.id, 0, 0, gi.id, 1, 1, Voeu.StatutVoeu.AFFECTE_JOURS_PRECEDENTS, false);
        Voeu v2 = new Voeu(1, g.id, 0, 0, gi.id, 1, 1, Voeu.StatutVoeu.AFFECTE_JOURS_PRECEDENTS, false);
        
        Throwable exception = assertThrows(IllegalStateException.class, () -> RepondeurAutomatique.comparerVoeux(v1, v2));
        assertTrue(exception.getMessage().contains("ne peut avoir qu'une proposition active"));
    }

    @Test
    public void comparerVoeux_doit_echouer_si_rangRepondeurNegatif() throws VerificationException {
        Parametres p = new Parametres(59, 60);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);
        Voeu v1 = new Voeu(0, g.id, 1, 1, gi.id, 1, 1, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Voeu v2 = new Voeu(1, g.id, 2, 2, gi.id, 1, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        
        Throwable exception = assertThrows(IllegalStateException.class, () -> RepondeurAutomatique.comparerVoeux(v1, v2));
        assertTrue(exception.getMessage().contains("dans l'ordre du rep auto"));
    }

    @Test
    public void comparerVoeux_doit_renvoyerMoins1SiV1PrefereAV2() throws VerificationException {
        Parametres p = new Parametres(59, 60);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);
        Voeu v1 = new Voeu(0, g.id, 1, 1, gi.id, 1, 1, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Voeu v2 = new Voeu(1, g.id, 2, 2, gi.id, 1, 0, Voeu.StatutVoeu.AFFECTE_JOURS_PRECEDENTS, false);

        assertEquals(-1, RepondeurAutomatique.comparerVoeux(v1, v2));
        assertEquals(1, RepondeurAutomatique.comparerVoeux(v2, v1));
    }

    @Test
    public void reponsesAutomatiques_doit_echouer_si_voeuAffecteHorsPP() throws VerificationException {
        Parametres p = new Parametres(59, 60);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);
        Voeu v1 = new Voeu(0, g.id, 1, 1, gi.id, 1, 1, Voeu.StatutVoeu.AFFECTE_JOURS_PRECEDENTS, true);
        Collection<Voeu> voeux = new ArrayList<>();
        voeux.add(v1);
        
        VerificationException exception = assertThrows(VerificationException.class, () -> RepondeurAutomatique.reponsesAutomatiques(voeux));
        assertSame(VerificationExceptionMessage.REPONDEUR_AUTOMATIQUE_INCOHERENCE_VOEU_EN_ATTENTE_AVEC_RA_MAIS_SANS_RANG, exception.exceptionMessage);
    }

    @Test
    public void reponsesAutomatiques_doit_libererPlaces() throws VerificationException {
        Parametres p = new Parametres(59, 60);
        GroupeAffectation g1 = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeAffectation g2 = new GroupeAffectation(1, new GroupeAffectationUID(1, 1, 1), 1, 1, p);
        GroupeAffectation g3 = new GroupeAffectation(1, new GroupeAffectationUID(2, 2, 2), 1, 1, p);
        Voeu v1 = new Voeu(1, false, g1.id, 1, 1, 1, Voeu.StatutVoeu.REP_AUTO_ACCEPTE, false);
        Whitebox.setInternalState(v1, "repondeurActive", true);
        Voeu v2 = new Voeu(1, false, g2.id, 1, 1, 2, Voeu.StatutVoeu.REP_AUTO_ACCEPTE, false);
        Whitebox.setInternalState(v2, "repondeurActive", true);
        Voeu v3 = new Voeu(1, false, g3.id, 1, 1, 3, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Whitebox.setInternalState(v3, "repondeurActive", true);
        Collection<Voeu> voeux = Arrays.asList(v1, v2, v3);
        assertEquals(1, RepondeurAutomatique.reponsesAutomatiques(voeux));
    }

}