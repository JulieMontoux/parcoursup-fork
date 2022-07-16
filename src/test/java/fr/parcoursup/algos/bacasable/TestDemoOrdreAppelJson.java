package fr.parcoursup.algos.bacasable;

import fr.parcoursup.algos.bacasable.ordreappel.DemoOrdreAppelJson;
import org.junit.Test;

public class TestDemoOrdreAppelJson {

    @Test(expected = Test.None.class /* no exception expected */)
    public void test_methode_sexecute() throws Exception {
        String[] args = {"src/test/java/fr/parcoursup/algos/bacasable/algoOrdreAppelEntree.json", "test-exe/tmp/sortie.json"};
        DemoOrdreAppelJson.main(args);
    }
    
}
