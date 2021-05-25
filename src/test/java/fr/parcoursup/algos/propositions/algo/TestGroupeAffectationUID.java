package fr.parcoursup.algos.propositions.algo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestGroupeAffectationUID {

    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<GroupeAffectationUID> constructor = GroupeAffectationUID.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void constructeur_doit_copier(){
        GroupeAffectationUID g1 = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectationUID g2 = new GroupeAffectationUID(g1);
        assertTrue(
            g1 != g2
            && g1.equals(g2)
        );
    }

    @Test
    public void deux_groupeAffectionUID_sont_egaux_si_leurs_variables_dInstances_sont_egales() {
        GroupeAffectationUID g1 = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectationUID g2 = new GroupeAffectationUID(0, 0, 0);
        assertEquals(g1, g2);
    }

    @Test
    public void deux_groupeAffectionUIDsont_differents_si_leurs_variables_dInstances_sont_differentes() {
        GroupeAffectationUID g1 = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectationUID g2 = new GroupeAffectationUID(0, 0, 1);
        assertNotEquals(g1, g2);
    }

    @Test
    public void equals_doit_echouer_si_objetsDifferents(){
        GroupeAffectationUID g1 = new GroupeAffectationUID(0, 0, 0);
        String s = "";
        Throwable exception = assertThrows(ClassCastException.class, () -> g1.equals(s));
        assertTrue(exception.getMessage().contains("Test d'égalité imprévu"));
    }

    @Test
    public void equals_doit_retournerFalse_si_objetNull(){
        GroupeAffectationUID g1 = new GroupeAffectationUID(0, 0, 0);
        assertNotNull(g1);
    }

}