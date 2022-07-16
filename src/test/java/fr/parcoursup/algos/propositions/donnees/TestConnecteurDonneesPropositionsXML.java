package fr.parcoursup.algos.propositions.donnees;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import fr.parcoursup.algos.exceptions.AccesDonneesException;
import fr.parcoursup.algos.propositions.algo.AlgoPropositionsEntree;
import fr.parcoursup.algos.propositions.algo.AlgoPropositionsSortie;
import fr.parcoursup.algos.propositions.algo.Parametres;
import org.junit.Assert;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

public class TestConnecteurDonneesPropositionsXML {

    @Test
    public void recupererDonnees_doit_reussir() throws Exception {
        AlgoPropositionsEntree entree1 = Whitebox.invokeConstructor(AlgoPropositionsEntree.class);
        String testFilename = "test-exe/tmp/parcoursup-test-ConnecteurDonneesPropositionsXML-recupererDonnees_doit_ecrire_fichier.xml";
        
        // Serialise
        Marshaller m = JAXBContext.newInstance(AlgoPropositionsEntree.class).createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(entree1, new File(testFilename));

        // Deserialise
        ConnecteurDonneesPropositionsXML conn = new ConnecteurDonneesPropositionsXML(testFilename);
        AlgoPropositionsEntree entree2 = conn.recupererDonnees();
        Assert.assertEquals(
            ((Parametres) Whitebox.getInternalState(entree1, "parametres")).nbJoursCampagne,
            ((Parametres) Whitebox.getInternalState(entree2, "parametres")).nbJoursCampagne
        );
    }

    @Test
    public void recupererDonnees_doit_echouerSiMauvaisCheminDeFichier() throws Exception {
        AlgoPropositionsEntree entree1 = Whitebox.invokeConstructor(AlgoPropositionsEntree.class);
        String testFilename = "test-exe/tmp/parcoursup-test-ConnecteurDonneesPropositionsXML-recupererDonnees_doit_ecrire_fichier.xml";
        
        // Serialise
        Marshaller m = JAXBContext.newInstance(AlgoPropositionsEntree.class).createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(entree1, new File(testFilename));

        // Deserialise
        ConnecteurDonneesPropositionsXML conn = new ConnecteurDonneesPropositionsXML("test-exe/tmp/fichier_inexistant.xml");
        Throwable exception = assertThrows(AccesDonneesException.class, conn::recupererDonnees);
        assertTrue(exception.getMessage().contains("Erreur de deserialisation des donnees d'entree"));
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void exporterDonnees_doit_reussir() throws Exception {
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class);
        String testFilename = "test-exe/tmp/parcoursup-test-ConnecteurDonneesPropositionsXML-exporterDonnees_doit_ecrire_fichier.xml";
        ConnecteurDonneesPropositionsXML conn = new ConnecteurDonneesPropositionsXML(testFilename);
        conn.exporterDonnees(sortie);
    }

}