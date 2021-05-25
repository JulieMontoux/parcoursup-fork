package fr.parcoursup.algos.propositions.algo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;
import fr.parcoursup.algos.propositions.Helpers;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestGroupeAffectation {

    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<GroupeAffectation> constructor = GroupeAffectation.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void constructeur_doit_copier() throws VerificationException {
        Parametres p = new Parametres(2, 60);
        GroupeAffectation g1 = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeAffectation g2 = new GroupeAffectation(g1, p);
        assertTrue(
            g1.id == g2.id
            && g1.getNbRecrutementsSouhaite() == g2.getNbRecrutementsSouhaite()
            && g1.getRangLimite() == g2.getRangLimite()
            && g1.getRangDernierAppeleAffiche() == g2.getRangDernierAppeleAffiche()
        );
    }

    @Test
    public void constructeur_doit_echouer_si_parametresNegatifs() {
        // False branch coverage de la ligne 55
        Parametres p = new Parametres(1, 1);
        VerificationException exception1 = assertThrows(VerificationException.class, () -> new GroupeAffectation(1, null, 1, 1, p));
        VerificationException exception2 = assertThrows(VerificationException.class, () -> new GroupeAffectation(-1, new GroupeAffectationUID(0, 0, 0), 1, 1, p));
        VerificationException exception3 = assertThrows(VerificationException.class, () -> new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), -1, 1, p));
        VerificationException exception4 = assertThrows(VerificationException.class, () -> new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, -1, p));
        assertSame(VerificationExceptionMessage.GROUPE_AFFECTATION_INCOHERENCE_PARAMETRES, exception1.exceptionMessage);
        assertSame(VerificationExceptionMessage.GROUPE_AFFECTATION_INCOHERENCE_PARAMETRES, exception2.exceptionMessage);
        assertSame(VerificationExceptionMessage.GROUPE_AFFECTATION_INCOHERENCE_PARAMETRES, exception3.exceptionMessage);
        assertSame(VerificationExceptionMessage.GROUPE_AFFECTATION_INCOHERENCE_PARAMETRES, exception4.exceptionMessage);
    }

    @Test
    public void constructeur_doit_reussir_si_milieuCampagne() throws Exception {
        // True branch coverage de la ligne 63
        Parametres p = new Parametres(2, 60);
        new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 2, 2, p);
    }

    @Test
    public void mettreAJourPropositions_doit_reussir_si_aucunePlacePossible() throws VerificationException {
        // False branch coverage de la ligne 128
        Parametres p = new Parametres(2, 60);
        GroupeAffectation g = new GroupeAffectation(0, new GroupeAffectationUID(0, 0, 0), 0, 2, p);
        Helpers.creeVoeuSansInternatEtInjecteDependances(1, g, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 1);
        g.mettreAJourPropositions();
    }

    @Test
    public void setRangLimite_doit_changerLeRangLimite() throws VerificationException {
        Parametres p = new Parametres(2, 60);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        assertEquals(1, g.getRangLimite());
        g.setRangLimite(2);
        assertEquals(2, g.getRangLimite());
    }

    @Test
    public void getRangDernierAppeleAffiche_retourne_rangDernierAppeleAffiche() throws VerificationException {
        Parametres p = new Parametres(2, 60);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        assertEquals(0, g.getRangDernierAppeleAffiche());
        g.setRangDernierAppeleAffiche(1);
        assertEquals(1, g.getRangDernierAppeleAffiche());
    }

}