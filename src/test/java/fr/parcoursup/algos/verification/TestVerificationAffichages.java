package fr.parcoursup.algos.verification;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.propositions.Helpers;
import fr.parcoursup.algos.propositions.algo.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.logging.LogManager;

import static fr.parcoursup.algos.exceptions.VerificationExceptionMessage.VERIFICATION_AFFICHAGES_VIOLATION_ORDRE_LISTE_ATTENTE_SANS_INTERNAT;
import static org.junit.Assert.*;

public class TestVerificationAffichages {

    @BeforeClass
    public static void setUpBeforeClass() {
        LogManager.getLogManager().reset();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void verifier_doit_reussir_si_proprietes_respectees() throws Exception {
        Parametres p = new Parametres(0,0,0);
        VerificationAffichages.verifierRangsSurListeAttente(List.of());
    }

    @Test
    public void verifier_doit_echouer_si_proprietes_non_respectees() throws Exception {
        Parametres p = new Parametres(0,0,0);
        GroupeAffectation g
                = new GroupeAffectation(
                0,
                new GroupeAffectationUID(0, 0, 0),
                0,
                0,
                0,
                p);
        Voeu v1 = Helpers.creeVoeuSansInternatEtInjecteDependances(0, g, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 1);
        Voeu v2 = Helpers.creeVoeuSansInternatEtInjecteDependances(1, g, StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 2);
        v1.setRangListeAttente(2);
        v2.setRangListeAttente(1);
        VerificationException ex =  assertThrows(VerificationException.class, () -> VerificationAffichages.verifierRangsSurListeAttente(List.of(v1,v2)));
        assertEquals(VERIFICATION_AFFICHAGES_VIOLATION_ORDRE_LISTE_ATTENTE_SANS_INTERNAT, ex.exceptionMessage);
    }

}
