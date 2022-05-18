package fr.parcoursup.algos.bacasable;

import fr.parcoursup.algos.bacasable.propositions.DemoPropositionsJson;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestDemoPropositionsJson {

    @Test(expected = Test.None.class /* no exception expected */)
    public void test_methode_sexecute() throws Exception {
        String[] args = {"src/test/java/fr/parcoursup/algos/bacasable/algoPropositionsEntree.json", "/tmp/sortie.json"};
        DemoPropositionsJson.main(args);
    }
    
}
