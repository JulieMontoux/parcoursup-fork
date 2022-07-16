package fr.parcoursup.algos.propositions.algo;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestAlgoPropositionsEntree {

    @Test
    public void constructeur_doit_copier() throws VerificationException {
        Parametres p = new Parametres(2, 60, 90);
        AlgoPropositionsEntree entree1 = new AlgoPropositionsEntree();
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);
        entree1.ajouter(g);
        entree1.ajouter(gi);

        Voeu v1 = new Voeu(1, g.id, 1, 1, gi.id, 1, 1, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Voeu v2 = new Voeu(2, false, g.id, 2, 2, 2, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        entree1.ajouter(v1);
        entree1.ajouter(v2);

        AlgoPropositionsEntree entree2 = new AlgoPropositionsEntree(entree1);
        // assertEquals(entree1.parametres, entree2.parametres);
        assertTrue(
            entree1.parametres.nbJoursCampagne == entree2.parametres.nbJoursCampagne
            && entree1.parametres.nbJoursCampagneDateFinReservationInternats == entree2.parametres.nbJoursCampagneDateFinReservationInternats
        );
        assertEquals(entree1.voeux, entree2.voeux);
        assertEquals(entree1.groupesAffectations.size(), entree2.groupesAffectations.size());  //TODO GroupeAffectation devrait avoir un equals pour les tester directement
        assertEquals(entree1.internats.size(), entree2.internats.size());
        assertEquals(entree1.candidatsAvecRepondeurAutomatique, entree2.candidatsAvecRepondeurAutomatique);
    }

    @Test
    public void ajouter_doit_ajouterGroupeAffectation() throws VerificationException {
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree();
        entree.ajouter(g);
        assertEquals(g, entree.groupesAffectations.get(g.id));
    }

    @Test
    public void ajouter_doit_ajouterGroupeInternat() throws VerificationException {
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree();
        entree.ajouter(gi);
        assertEquals(gi, entree.internats.get(gi.id));
    }

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

    @Test
    public void ajouter_doit_echouer_siGroupeAffectationDejaPresent() throws VerificationException {
        Parametres p = new Parametres(2, 60, 90);
        AlgoPropositionsEntree entree = new AlgoPropositionsEntree();
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        entree.ajouter(g);
        VerificationException exception = assertThrows(VerificationException.class, () -> entree.ajouter(g));
        assertSame(VerificationExceptionMessage.ALGO_PROPOSITIONS_ENTREE_GROUPE_AFFECTATION_DUPLIQUE, exception.exceptionMessage);
    }

    @Test
    public void ajouter_doit_echouer_siGroupeInternatDejaPresent() throws VerificationException {
        AlgoPropositionsEntree entree = new AlgoPropositionsEntree();
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);
        entree.ajouter(gi);
        VerificationException exception = assertThrows(VerificationException.class, () -> entree.ajouter(gi));
        assertSame(VerificationExceptionMessage.ALGO_PROPOSITIONS_ENTREE_GROUPE_INTERNAT_DUPLIQUE, exception.exceptionMessage);
    }

    @Test
    public void ajouter_doit_echouer_siVoeuDejaPresent() throws VerificationException {
        Parametres p = new Parametres(2, 60, 90);
        AlgoPropositionsEntree entree = new AlgoPropositionsEntree();
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        Voeu v = new Voeu(0, false, g.id, 1, 1, 1, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        entree.ajouter(v);
        VerificationException exception = assertThrows(VerificationException.class, () -> entree.ajouter(v));
        assertSame(VerificationExceptionMessage.ALGO_PROPOSITIONS_ENTREE_VOEU_DUPLIQUE, exception.exceptionMessage);
    }

    @Test
    public void injecterGroupesEtInternatsDansVoeux_doit_peuplerGroupeEtInternatDansVoeu()
            throws VerificationException {
        Parametres p = new Parametres(2, 60, 90);
        AlgoPropositionsEntree entree = new AlgoPropositionsEntree();
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);
        entree.ajouter(g);
        entree.ajouter(gi);

        Voeu v1 = new Voeu(1, g.id, 1, 1, gi.id, 1, 1, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Voeu v2 = new Voeu(2, false, g.id, 2, 2, 2, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        entree.ajouter(v1);
        entree.ajouter(v2);
        entree.injecterGroupesEtInternatsDansVoeux();
        assertEquals(g, v1.getGroupeAffectation());
        assertEquals(gi, v1.getInternat());
        assertNull(v2.getInternat());
    }

    @Test
    public void afterUnmarshal_doit_appeler_injecterGroupeEtInternatsDansVoeux() throws VerificationException {
        Parametres p = new Parametres(2, 60, 90);
        AlgoPropositionsEntree entree = new AlgoPropositionsEntree();
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);
        entree.ajouter(g);
        entree.ajouter(gi);

        Voeu v1 = new Voeu(1, g.id, 1, 1, gi.id, 1, 1, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Voeu v2 = new Voeu(2, false, g.id, 2, 2, 2, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        entree.ajouter(v1);
        entree.ajouter(v2);
        entree.afterUnmarshal(null, null);
        assertEquals(g, v1.getGroupeAffectation());
        assertEquals(gi, v1.getInternat());
        assertNull(v2.getInternat());
    }

}