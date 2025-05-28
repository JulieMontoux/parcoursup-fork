package fr.parcoursup.algos.propositions.algo;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class TestVoeu {

    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<Voeu> constructor = Voeu.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void constructeur_doit_copier() throws VerificationException {
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, 0, p);
        Voeu v1 = new Voeu(0, false, g.id, 1, 1, 1, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false, false, false, null);
        Voeu v2 = new Voeu(v1, v1.statut);
        //noinspection NewObjectEquality
        assertTrue(
            v1 != v2
            && v1.id.equals(v2.id)
        );
    }

    @Test
    public void estDemissionAutomatiqueProposition_doit_retournerTrue_si_StatutVoeuEstDemissionAutoProposition()
            throws VerificationException {
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, 0, p);
        Voeu v = new Voeu(0, false, g.id, 1, 1, 1, StatutVoeu.REP_AUTO_REFUS_PROPOSITION, false, false, false, null);
        assertTrue(StatutVoeu.estDemissionAutomatiqueProposition(v.statut));
    }

    @Test
    public void estDemissionAutomatiqueProposition_doit_retournerFalse_si_StatutVoeuEstEnAttenteProposition()
            throws VerificationException {
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, 0, p);
        Voeu v = new Voeu(0, false, g.id, 1, 1, 1, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        assertFalse(StatutVoeu.estDemissionAutomatiqueProposition(v.statut));
    }

    @Test
    public void constructeur_doit_echouer_si_horsPPEtEnAttenteProposition() throws VerificationException {
        // True branch coverage de la ligne 180
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, 0, p);
        VerificationException exception = assertThrows(VerificationException.class, () -> {
            Voeu v = new Voeu(0, false, g.id, 1, 1, 1, StatutVoeu.PROPOSITION_DU_JOUR, true);
        });
        assertSame(VerificationExceptionMessage.VOEU_INCONSISTENCE_STATUT_HORS_PP, exception.exceptionMessage);
    }

    @Test
    public void constructeur_doit_echouer_si_ordreAppelNegatif() throws VerificationException {
        // True branch coverage de la ligne 184
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, 0, p);
        VerificationException exception = assertThrows(VerificationException.class, () -> {
            Voeu v = new Voeu(0, false, g.id, -1, 1, 1, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        });
        assertSame(VerificationExceptionMessage.VOEU_RANGS_NEGATIFS, exception.exceptionMessage);
    }

    @Test
    public void constructeur_doit_echouer_si_ordreAppelZeroEtNonAffecteJoursPrecedents()
            throws VerificationException {
        // True branch coverage de la ligne 187
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, 0, p);
        VerificationException exception = assertThrows(VerificationException.class, () -> {
            Voeu v = new Voeu(0, false, g.id, 0, 0, 1, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        });
        assertSame(VerificationExceptionMessage.VOEU_ORDRE_APPEL_MANQUANT, exception.exceptionMessage);
    }

    @Test
    public void constructeurInternat_doit_echouer_si_horsPPEtEnAttenteProposition() throws VerificationException {
        // True branch coverage de la ligne 213
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, 0, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0, 0), 1);
        VerificationException exception = assertThrows(VerificationException.class, () -> {
            Voeu v = new Voeu(0, g.id, 1, 1, gi.id, 1, 1, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, true);
        });
        assertSame(VerificationExceptionMessage.VOEU_INCONSISTENCE_STATUT_HORS_PP, exception.exceptionMessage);
    }

    @Test
    public void constructeurInternat_doit_echouer_si_ordreAppelNegatif() throws VerificationException {
        // True branch coverage de la ligne 217
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, 0, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0, 0), 1);
        VerificationException exception = assertThrows(VerificationException.class, () -> {
            Voeu v = new Voeu(0, g.id, -1, 1, gi.id, 1, 1, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        });
        assertSame(VerificationExceptionMessage.VOEU_RANGS_NEGATIFS, exception.exceptionMessage);
    }

    @Test
    public void constructeurInternat_doit_echouer_si_ordreAppelZeroEtNonAffecteJoursPrecedents()
            throws VerificationException {
        // True branch coverage de la ligne 220
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, 0, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0, 0), 1);
        VerificationException exception = assertThrows(VerificationException.class, () -> {
            Voeu v = new Voeu(0, g.id, 0, 0, gi.id, 1, 1, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        });
        assertSame(VerificationExceptionMessage.VOEU_ORDRE_APPEL_MANQUANT, exception.exceptionMessage);
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void toString_doit_reussir_avecDemandeInternat() throws VerificationException {
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, 0, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0, 0), 1);
        Voeu v = new Voeu(0, g.id, 1, 1, gi.id, 1, 1, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        assertTrue(v.toString().contains("avec demande internat"));
    }

}