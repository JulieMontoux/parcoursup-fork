package fr.parcoursup.algos.propositions.algo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;
import fr.parcoursup.algos.propositions.Helpers;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.*;

public class TestGroupeInternat {

    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<GroupeInternat> constructor = GroupeInternat.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void constructeur_doit_copier() throws VerificationException {
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);
        GroupeInternat gi2 = new GroupeInternat(gi);
        assertNotEquals(gi, gi2);
        assertSame(gi.id, gi2.id);
        assertEquals(gi.getCapacite(), gi2.getCapacite());
    }

    @Test
    public void ajouterVoeuEnAttenteDeProposition_doit_echouer_si_groupeDejaInitialise()
            throws VerificationException {
        // True branch coverage de la ligne 103
        Parametres p = new Parametres(2, 60, 90);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);
        Voeu v1 = new Voeu(0, g.id, 1, 1, gi.id, 1, 1, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Whitebox.setInternalState(gi, "estInitialise", true);
        VerificationException exception = assertThrows(VerificationException.class, () -> gi.ajouterVoeuEnAttenteDeProposition(v1));
        assertSame(VerificationExceptionMessage.GROUPE_INTERNAT_DEJA_INITIALISE, exception.exceptionMessage);
    }

    @Test
    public void ajouterVoeuEnAttenteDeProposition_doit_echouer_si_voeuEnDoublon() throws VerificationException {
        // True branch coverage de la ligne 106
        Parametres p = new Parametres(2, 60, 90);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);
        Voeu v1 = new Voeu(0, g.id, 1, 1, gi.id, 1, 1, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        gi.ajouterVoeuEnAttenteDeProposition(v1);
        VerificationException exception = assertThrows(VerificationException.class, () -> gi.ajouterVoeuEnAttenteDeProposition(v1));
        assertSame(VerificationExceptionMessage.GROUPE_INTERNAT_VOEU_EN_DOUBLON, exception.exceptionMessage);
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void ajouterVoeuEnAttenteDeProposition_doit_reussir_si_deuxVoeuxPourUnCandidatDontUnAffecte()
            throws VerificationException {
        Parametres p = new Parametres(2, 60, 90);
        GroupeAffectation g = new GroupeAffectation(2, new GroupeAffectationUID(0, 0, 0), 2, 2, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 2);
        Voeu v1 = new Voeu(0, g.id, 1, 1, gi.id, 1, 1, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false);
        Voeu v2 = new Voeu(0, g.id, 2, 2, gi.id, 2, 2, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        gi.ajouterCandidatAffecte(v1.id.gCnCod);
        gi.ajouterVoeuEnAttenteDeProposition(v2);
    }

    @Test
    public void initialiserPositionAdmission_doit_echouer_si_nbJoursCampagneNegatif()
            throws VerificationException {
        Parametres p = new Parametres(0, 60,90);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);
        VerificationException exception = assertThrows(VerificationException.class, () -> gi.initialiserPositionAdmission(p));
        assertSame(VerificationExceptionMessage.GROUPE_INTERNAT_DATE_ANTERIEURE, exception.exceptionMessage);
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void initialiserPositionAdmission_doit_reussir_si_voeuxAuDessusDeLaBarre()
            throws VerificationException {
        // True branch coverage de la ligne 249
        Parametres p = new Parametres(2, 60, 90);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);
        Helpers.creeVoeuAvecInternatEtInjecteDependances(0, g, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 61, 1);
        gi.initialiserPositionAdmission(p);
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void initialiserPositionAdmission_doit_reussir_si_internatDejaObtenu() throws VerificationException {
        // True branch coverage de la ligne 261 et 218
        // Pour qu'elle soit couverte, il faut que le candidat soit à la fois dans candidatsEnAttente et candidatsAffectes du GroupeInternat.
        // Donc il doit avoir deux voeux, dont un affecté, et l'autre en attente.
        // Il faut également plus de candidats sur l'internat que de places disponibles.
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g = new GroupeAffectation(2, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 3);
        Voeu v1 = Helpers.creeVoeuAvecInternatEtInjecteDependances(0, g, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 1 , 1);
        Voeu v2 = Helpers.creeVoeuAvecInternatEtInjecteDependances(1, g, gi, Voeu.StatutVoeu.PROPOSITION_DU_JOUR,  2, 2);
        Voeu v3 = Helpers.creeVoeuAvecInternatEtInjecteDependances(2, g, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION,  3, 3);
        gi.initialiserPositionAdmission(p);
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void initialiserPositionAdmission_doit_reussir_si_nbJoursCampagnesSuperieursPivot()
            throws VerificationException {
        Parametres p = new Parametres(61, 60,90);
        GroupeAffectation g = new GroupeAffectation(2, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);  // Capacite l=1

        Helpers.creeVoeuAvecInternatEtInjecteDependances(0, g, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 1 , 1);
        Helpers.creeVoeuAvecInternatEtInjecteDependances(1, g, gi, Voeu.StatutVoeu.PROPOSITION_DU_JOUR,  2, 2);
        Helpers.creeVoeuAvecInternatEtInjecteDependances(2, g, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION,  3, 3);

        gi.initialiserPositionAdmission(p);
    }

    @Test
    public void constructeur_doit_echouer_si_capaciteInternatNegative() {
        VerificationException exception = assertThrows(VerificationException.class,
                () -> new GroupeInternat(new GroupeInternatUID(1, 0), -1));
        assertSame(VerificationExceptionMessage.GROUPE_INTERNAT_CAPACITE_NEGATIVE, exception.exceptionMessage);
    }

    @Test
    public void setCapacite_doit_echouer_si_capaciteInternatNegative()
            throws VerificationException {
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);  // La capacité est changée pour du négatif plus bas

        VerificationException exception = assertThrows(VerificationException.class, () -> gi.setCapacite(-1));
        assertSame(VerificationExceptionMessage.GROUPE_INTERNAT_CAPACITE_NEGATIVE, exception.exceptionMessage);
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void initialiserPositionAdmission_doit_reussir_si_candidatSousLaBarreMaisCapaciteNonRemplie()
            throws VerificationException {
        Parametres p = new Parametres(61, 60,90);
        GroupeAffectation g = new GroupeAffectation(2, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 3);
        Voeu v1 = Helpers.creeVoeuAvecInternatEtInjecteDependances(0, g, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 1, 1);
        Voeu v2 = Helpers.creeVoeuAvecInternatEtInjecteDependances(2, g, gi, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, 2, 2);
        Voeu v3 = Helpers.creeVoeuAvecInternatEtInjecteDependances(3, g, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 3, 3);
        gi.initialiserPositionAdmission(p);
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void initialiserPositionAdmission_doit_reussir_si_premierJourDeCampagne()
            throws VerificationException {
        Parametres p = new Parametres(1, 60, 90);
        GroupeAffectation g = new GroupeAffectation(2, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);
        Voeu v1 = Helpers.creeVoeuAvecInternatEtInjecteDependances(1, g, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 1, 1);
        Voeu v2 = Helpers.creeVoeuAvecInternatEtInjecteDependances(2, g, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 2, 2);
        gi.initialiserPositionAdmission(p);
    }

    @Test
    public void mettreAJourPositionAdmission_doit_echouer_si_nonInitialise() throws VerificationException {
        // True branch coverage de la ligne 289
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);
        VerificationException exception = assertThrows(VerificationException.class, gi::mettreAJourPositionAdmission);
        assertSame(VerificationExceptionMessage.GROUPE_INTERNAT_POSITION_NON_INITIALISEE, exception.exceptionMessage);
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void mettreAJourPositionAdmission_doit_reussir_si_memeCandidat() throws VerificationException {
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g1 = new GroupeAffectation(2, new GroupeAffectationUID(0, 0, 0), 10, 10, p);
        GroupeAffectation g2 = new GroupeAffectation(2, new GroupeAffectationUID(1, 1, 1), 10, 10, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 3);
        Voeu v1 = Helpers.creeVoeuSansInternatEtInjecteDependances(0, g1, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, 1);
        Voeu v2 = Helpers.creeVoeuAvecInternatEtInjecteDependances(v1.id.gCnCod, g1, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION,  v1.ordreAppel, 1);
        Voeu v3 = Helpers.creeVoeuAvecInternatEtInjecteDependances(v1.id.gCnCod, g2, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION,  2, v2.rangInternat);
        gi.initialiserPositionAdmission(p);
        gi.mettreAJourPositionAdmission();
    }

    @Test
    public void mettreAJourPositionAdmission_doit_diminuerPositionAdmissionSiSurcapacite()
            throws VerificationException {
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g = new GroupeAffectation(2, new GroupeAffectationUID(0, 0, 0), 10, 10, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 1);
        Voeu v1 = Helpers.creeVoeuSansInternatEtInjecteDependances(0, g, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, 1);
        Voeu v2 = Helpers.creeVoeuAvecInternatEtInjecteDependances(v1.id.gCnCod, g, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION,  v1.ordreAppel, 1);
        Voeu v3 = Helpers.creeVoeuAvecInternatEtInjecteDependances(1, g, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION,  2, 2);
        gi.initialiserPositionAdmission(p);
        Whitebox.setInternalState(gi, "positionAdmission", 99);
        Whitebox.setInternalState(gi, "capacite", 0);
        v2.proposer();
        assertTrue(gi.mettreAJourPositionAdmission());
        assertEquals(Math.min(v2.rangInternat - 1, v3.rangInternat - 1), gi.getPositionAdmission());
    }

    @Test
    public void voeuxTriesParOrdreAppel_doit_retourner_lesVoeuxEnAttenteTriesParOrdreAppel()
            throws VerificationException {
        Parametres p = new Parametres(59, 60,90);
        GroupeAffectation g = new GroupeAffectation(2, new GroupeAffectationUID(0, 0, 0), 10, 10, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, 0), 3);

        Voeu v2 = Helpers.creeVoeuAvecInternatEtInjecteDependances(0, g, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 2, 1);
        Voeu v4 = Helpers.creeVoeuAvecInternatEtInjecteDependances(1, g, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 4, 2);
        List<Voeu> voeux = gi.voeuxTriesParOrdreAppel();
        assertTrue(voeux.get(0).ordreAppel < voeux.get(1).ordreAppel);
    }

}