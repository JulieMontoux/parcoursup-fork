package fr.parcoursup.algos.ordreappel.algo;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertTrue;

import fr.parcoursup.algos.exceptions.VerificationException;
import org.junit.Test;

public class TestOrdreAppel {

    @Test
    public void testConstructeur() throws VerificationException {
        VoeuClasse v1 = new VoeuClasse(1, 1, true, true);
        v1.setRangAppel(1);
        VoeuClasse v2 = new VoeuClasse(2, 2, true, true);
        v2.setRangAppel(2);
        VoeuClasse v3 = new VoeuClasse(3, 3, true, true);
        v3.setRangAppel(3);

        List<VoeuClasse> voeux = new ArrayList<>();
        voeux.add(v1);
        voeux.add(v2);
        voeux.add(v3);

        OrdreAppel oa = new OrdreAppel(voeux);
        for(int i=1 ; i<oa.candidats.size() ; i++){
            assertTrue(oa.candidats.get(i-1).rangAppel <= oa.candidats.get(i).rangAppel);
        }
    }

}