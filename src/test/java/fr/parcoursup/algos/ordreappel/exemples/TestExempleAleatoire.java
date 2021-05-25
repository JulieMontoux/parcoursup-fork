
package fr.parcoursup.algos.ordreappel.exemples;
import static org.junit.Assert.assertEquals;

import fr.parcoursup.algos.bacasable.ordreappel.ExempleAleatoire;
import org.junit.Test;

public class TestExempleAleatoire {

    @Test
    public void nom_doit_retourner_uneValeurFixe(){
        ExempleAleatoire e = new ExempleAleatoire(10);
        assertEquals(e.nom(), "exemple_aleatoire");
    }

}