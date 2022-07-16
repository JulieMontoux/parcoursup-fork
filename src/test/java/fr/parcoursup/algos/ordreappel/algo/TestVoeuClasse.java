package fr.parcoursup.algos.ordreappel.algo;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestVoeuClasse {

    @Test
    public void testEstBoursier() throws VerificationException {
        VoeuClasse voeuClasse = new VoeuClasse(10, 10, true, true);
        boolean estBoursier = voeuClasse.estBoursier();
        assertTrue(estBoursier);
    }

    @Test
    public void testEstDuSecteur() throws VerificationException {
        VoeuClasse voeuClasse = new VoeuClasse(10, 10, true, true);
        boolean estDuSecteur = voeuClasse.estDuSecteur();
        assertTrue(estDuSecteur);
    }

    @Test
    public void testCompareTo() throws VerificationException {
        VoeuClasse v1 = new VoeuClasse(10, 10, true, true);
        VoeuClasse v2 = new VoeuClasse(15, 15, true, true);
        int comparedTo = v1.compareTo(v2);
        assertEquals(comparedTo, -5);
    }

    @Test
    public void constructeur_de_VoeuClasse_doit_verifier_rang_est_coherent() {
        VerificationException exception = assertThrows(VerificationException.class, ()
                -> new VoeuClasse(1, -1, true, true));
        Assert.assertEquals(VerificationExceptionMessage.VOEU_RANGS_NEGATIFS, exception.exceptionMessage);
    }

    @Test
    public void comparateur_doit_comparer_le_type() throws VerificationException {
        VoeuClasse v1 = new VoeuClasse(1, 1, true, true);
        assertThrows(ClassCastException.class, () -> v1.equals(null));
        assertThrows(ClassCastException.class, () -> v1.equals(1));
    }

}