package fr.parcoursup.algos.propositions.algo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestGroupeInternatUID {

    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<GroupeInternatUID> constructor = GroupeInternatUID.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void deux_groupeInternatUID_sont_egaux_si_leurs_variables_dInstances_sont_egales() {
        GroupeInternatUID g1 = new GroupeInternatUID(1, 1);
        GroupeInternatUID g2 = new GroupeInternatUID(1, 1);
        assertEquals(g1, g2);
    }

    @Test
    public void deux_GroupeInternatUID_sont_differents_si_leurs_variables_dInstances_sont_differentes() {
        GroupeInternatUID g1 = new GroupeInternatUID(1, 1);
        GroupeInternatUID g2 = new GroupeInternatUID(1, 2);
        assertNotEquals(g1, g2);
    }

    @Test
    public void toString_doit_etreNonVide(){
        GroupeInternatUID g1 = new GroupeInternatUID(1, 1);
        assertNotEquals(g1.toString(), "");
    }

    @Test
    public void equals_doit_echouer_si_objetsDifferents(){
        GroupeInternatUID g1 = new GroupeInternatUID(1, 1);
        String s = "";
        Throwable exception = assertThrows(ClassCastException.class, () -> g1.equals(s));
        assertTrue(exception.getMessage().contains("Test d'égalité imprévu"));
    }

}