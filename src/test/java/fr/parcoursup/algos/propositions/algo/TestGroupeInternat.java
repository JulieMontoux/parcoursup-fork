package fr.parcoursup.algos.propositions.algo;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.junit.Assert.*;

public class TestGroupeInternat {

    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<GroupeInternat> constructor = GroupeInternat.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void constructeur_doit_copier() throws VerificationException {
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);
        GroupeInternat gi2 = new GroupeInternat(gi);
        assertNotEquals(gi, gi2);
        assertSame(gi.id, gi2.id);
        assertEquals(gi.getCapacite(), gi2.getCapacite());
    }

    @Test
    public void ajouterVoeuEnAttenteDeProposition_doit_echouer_si_voeuEnDoublon() throws VerificationException {
        // True branch coverage de la ligne 106
        Parametres p = new Parametres(2, 60, 90);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, 0, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);
        Voeu v1 = new Voeu(0, g.id, 1, 1, gi.id, 1, 1, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        gi.ajouterVoeuEnAttenteDeProposition(v1);
        VerificationException exception = assertThrows(VerificationException.class, () -> gi.ajouterVoeuEnAttenteDeProposition(v1));
        assertSame(VerificationExceptionMessage.GROUPE_INTERNAT_VOEU_EN_DOUBLON, exception.exceptionMessage);
    }

    @Test
    public void constructeur_doit_echouer_si_capaciteInternatNegative() {
        VerificationException exception = assertThrows(VerificationException.class,
                () -> new GroupeInternat(new GroupeInternatUID(1, 0), -1));
        assertSame(VerificationExceptionMessage.GROUPE_INTERNAT_CAPACITE_NEGATIVE, exception.exceptionMessage);
    }

    @Test
    public void setCapacite_doit_echouer_si_capaciteInternatNegative()
            throws VerificationException {
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);  // La capacité est changée pour du négatif plus bas

        VerificationException exception = assertThrows(VerificationException.class, () -> gi.setCapacite(-1));
        assertSame(VerificationExceptionMessage.GROUPE_INTERNAT_CAPACITE_NEGATIVE, exception.exceptionMessage);
    }

}