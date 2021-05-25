package fr.parcoursup.algos.ordreappel.algo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TestCandidatClasse {

    @Test
    public void equals_doit_retourner_true_si_memesValeurs() {
        CandidatClasse c1 = new CandidatClasse(1, 2);
        CandidatClasse c2 = new CandidatClasse(1, 2);
        assertEquals(c1, c2);
    }

    @Test
    public void equals_doit_retourner_faux_si_valeursDifferentes() {
        CandidatClasse c1 = new CandidatClasse(1, 2);
        CandidatClasse c2 = new CandidatClasse(2, 3);
        assertNotEquals(c1, c2);
    }

    @Test
    public void toString_doit_retourner_uneStringNonVide() {
        CandidatClasse c = new CandidatClasse(1, 2);
        assertNotEquals(c.toString(), "");
    }

    @Test
    public void equals_doit_echouer_si_objetsDifferents(){
        CandidatClasse c = new CandidatClasse(1, 2);
        String s = "";
        Throwable exception = assertThrows(ClassCastException.class, () -> c.equals(s));
        assertTrue(exception.getMessage().contains("Test d'égalité imprévu"));
    }

    @Test
    public void hashCode_doit_retourner_unHashDifferent_si_ObjetsDiffents(){
        CandidatClasse c1 = new CandidatClasse(1, 2);
        CandidatClasse c2 = new CandidatClasse(2, 3);
        assertNotEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void hashCode_doit_retourner_unMemeHash_si_ObjetsMemesValeurs(){
        CandidatClasse c1 = new CandidatClasse(1, 2);
        CandidatClasse c2 = new CandidatClasse(1, 2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

}