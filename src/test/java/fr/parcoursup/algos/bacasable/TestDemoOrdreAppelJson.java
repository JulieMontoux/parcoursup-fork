package fr.parcoursup.algos.bacasable;

import fr.parcoursup.algos.bacasable.ordreappel.DemoOrdreAppelJson;
import org.junit.Test;

public class TestDemoOrdreAppelJson {

    @Test
    public void test_methode_sexecute() throws Exception {
        String[] args = {"src/test/java/fr/parcoursup/algos/bacasable/algoOrdreAppelEntree.json", "/tmp/sortie.json"};
        DemoOrdreAppelJson.main(args);
    }
    
}
