package fr.parcoursup.algos.propositions.algo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestAlgoPropositionsSortie {

    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<AlgoPropositionsSortie> constructor = AlgoPropositionsSortie.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void setAvertissement_doit_changerAvertissementATrueSiNonAlerte() {
        Parametres p = new Parametres(2, 60, 90);
        AlgoPropositionsSortie sortie = new AlgoPropositionsSortie(p);
        assertFalse(sortie.getAvertissement());
        sortie.setAvertissement();
        assertTrue(sortie.getAvertissement());
    }

    @Test
    public void setAvertissement_doit_laisserAvertissementAFalseSiAlerte() {
        Parametres p = new Parametres(2, 60, 90);
        AlgoPropositionsSortie sortie = new AlgoPropositionsSortie(p);
        assertFalse(sortie.getAvertissement());
        sortie.setAlerte();
        sortie.setAvertissement();
        assertFalse(sortie.getAvertissement());
    }

}