package fr.parcoursup.algos.propositions.algo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestVoeuUID {

    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<VoeuUID> constructor = VoeuUID.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void deux_voeuUID_sont_egaux_si_leurs_variables_dInstances_sont_egales() {
        VoeuUID v1 = new VoeuUID(0, 0, false);
        VoeuUID v2 = new VoeuUID(0, 0, false);
        assertEquals(v1, v2);
    }

    @Test
    public void deux_voeuUID_sont_differents_si_leurs_variables_dInstances_sont_differentes() {
        VoeuUID v1 = new VoeuUID(0, 0, false);
        VoeuUID v2 = new VoeuUID(0, 0, true);
        assertNotEquals(v1, v2);
    }

    @Test
    public void equals_doit_echouer_si_objetsDifferents(){
        VoeuUID v1 = new VoeuUID(0, 0, false);
        String s = "";
        Throwable exception = assertThrows(ClassCastException.class, () -> v1.equals(s));
        assertTrue(exception.getMessage().contains("Test d'égalité imprévu"));
    }

    @Test
    public void toString_doit_etre_nonVide(){
        VoeuUID v1 = new VoeuUID(0, 0, false);
        assertNotEquals(v1.toString(), "");
        VoeuUID v2 = new VoeuUID(1, 1, true);
        assertNotEquals(v2.toString(), "");
    }

}