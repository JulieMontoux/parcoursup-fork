package fr.parcoursup.algos.propositions.algo;

import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;

import static org.junit.Assert.assertEquals;

public class TestAlgoPropositionsEntree {

    @Test
    public void deserialiser_doit_DeserialiserLeMemeObjet() throws JAXBException {
        AlgoPropositionsEntree entree1 = new AlgoPropositionsEntree();
        String testFilename = "test-exe/tmp/parcoursup-test-AlgoPropositionsEntree-serialiser_doit_ecrire_fichier.xml";
        
        // Serialise
        Marshaller m = JAXBContext.newInstance(AlgoPropositionsEntree.class).createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(entree1, new File(testFilename));

        // Deserialise
        AlgoPropositionsEntree entree2 = AlgoPropositionsEntree.deserialiser(testFilename);
        assertEquals(entree1.parametres.nbJoursCampagne, entree2.parametres.nbJoursCampagne);
    }


}