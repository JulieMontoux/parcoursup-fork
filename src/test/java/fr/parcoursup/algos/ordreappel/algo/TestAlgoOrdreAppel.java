package fr.parcoursup.algos.ordreappel.algo;

import fr.parcoursup.algos.verification.VerificationsResultatsAlgoOrdreAppel;
import org.junit.Test;

public class TestAlgoOrdreAppel {

    @Test(expected = Test.None.class /* no exception expected */)
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
        verif.verifier(ae, as);  // verif.verifier génère une exception en cas d'échec
    }

}