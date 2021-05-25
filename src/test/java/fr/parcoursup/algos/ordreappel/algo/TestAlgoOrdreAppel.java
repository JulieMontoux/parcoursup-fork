package fr.parcoursup.algos.ordreappel.algo;

import static org.junit.Assert.assertTrue;

import fr.parcoursup.algos.verification.VerificationsResultatsAlgoOrdreAppel;
import org.junit.Test;

public class TestAlgoOrdreAppel {

    @Test
    public void testCalculerOrdresAppels() throws Exception {

        GroupeClassement g1 = new GroupeClassement(0, 0, 0);
        g1.ajouterVoeu(new VoeuClasse(1, 1, false, false));
        g1.ajouterVoeu(new VoeuClasse(2, 2, false, false));
        g1.ajouterVoeu(new VoeuClasse(3, 3, false, false));

        GroupeClassement g2 = new GroupeClassement(1, 0, 0);
        g2.ajouterVoeu(new VoeuClasse(4, 4, false, false));
        g2.ajouterVoeu(new VoeuClasse(5, 5, false, false));
        g2.ajouterVoeu(new VoeuClasse(6, 6, false, false));

        AlgoOrdreAppelEntree ae = new AlgoOrdreAppelEntree();
        ae.groupesClassements.add(g1);
        ae.groupesClassements.add(g2);

        AlgoOrdreAppelSortie as = AlgoOrdreAppel.calculerOrdresAppels(ae);

        VerificationsResultatsAlgoOrdreAppel verif = new VerificationsResultatsAlgoOrdreAppel();
        verif.verifier(ae, as);  // Pas besoin d'assert, car le verif.verifier lance déjà une exception en cas d'échec
        // Dans l'idéal, on ne devrait pas avoir à appeler une classe d'un autre paquet, mais ici le test est déjà écrit, donc rien ne sert de le réécrire.
        // En revanche, il faudra s'assurer que la méthode vérifier est bien testée dans son propre paquet.
        assertTrue(true);

    }

}