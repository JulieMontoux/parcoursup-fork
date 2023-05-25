package fr.parcoursup.algos.propositions.algo;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;
import fr.parcoursup.algos.propositions.Helpers;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Random;

import static fr.parcoursup.algos.propositions.algo.GroupeAffectation.NB_JOURS_POUR_INTERPOLATION_INTERNAT;
import static org.junit.Assert.*;

public class TestGroupeAffectation {

    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<GroupeAffectation> constructor = GroupeAffectation.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void constructeur_doit_copier() throws VerificationException {
        Parametres p = new Parametres(2, 60, 90);
        GroupeAffectation g1 = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        GroupeAffectation g2 = new GroupeAffectation(g1);
        assertTrue(
                g1.id == g2.id
                        && g1.getNbRecrutementsSouhaite() == g2.getNbRecrutementsSouhaite()
                        && g1.getRangLimite() == g2.getRangLimite()
                        && g1.getRangDernierAppeleAffiche() == g2.getRangDernierAppeleAffiche()
        );
    }

    @Test
    public void constructeur_doit_echouer_si_parametresNegatifs() {
        // False branch coverage de la ligne 55
        Parametres p = new Parametres(1, 1, 90);
        VerificationException exception1 = assertThrows(VerificationException.class, () -> new GroupeAffectation(1, null, 1, 1, p));
        VerificationException exception2 = assertThrows(VerificationException.class, () -> new GroupeAffectation(-1, new GroupeAffectationUID(0, 0, 0), 1, 1, p));
        VerificationException exception3 = assertThrows(VerificationException.class, () -> new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), -1, 1, p));
        VerificationException exception4 = assertThrows(VerificationException.class, () -> new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, -1, p));
        assertSame(VerificationExceptionMessage.GROUPE_AFFECTATION_INCOHERENCE_PARAMETRES, exception2.exceptionMessage);
        assertSame(VerificationExceptionMessage.GROUPE_AFFECTATION_INCOHERENCE_PARAMETRES, exception3.exceptionMessage);
        assertSame(VerificationExceptionMessage.GROUPE_AFFECTATION_INCOHERENCE_PARAMETRES, exception4.exceptionMessage);
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void constructeur_doit_reussir_si_milieuCampagne() throws Exception {
        // True branch coverage de la ligne 63
        Parametres p = new Parametres(2, 60, 90);
        new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 2, 2, p);
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void mettreAJourPropositions_doit_reussir_si_aucunePlacePossible() throws VerificationException {
        // False branch coverage de la ligne 128
        Parametres p = new Parametres(2, 60, 90);
        GroupeAffectation g = new GroupeAffectation(0, new GroupeAffectationUID(0, 0, 0), 0, 2, p);
        Helpers.creeVoeuSansInternatEtInjecteDependances(1, g, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 1);
        g.mettreAJourPropositions();
    }

    @Test
    public void setRangLimite_doit_changerLeRangLimite() throws VerificationException {
        Parametres p = new Parametres(2, 60, 90);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        assertEquals(1, g.getRangLimite());
        g.setRangLimite(2);
        assertEquals(2, g.getRangLimite());
    }

    @Test
    public void getRangDernierAppeleAffiche_retourne_rangDernierAppeleAffiche() throws VerificationException {
        Parametres p = new Parametres(2, 60, 90);
        GroupeAffectation g = new GroupeAffectation(1, new GroupeAffectationUID(0, 0, 0), 1, 1, p);
        assertEquals(0, g.getRangDernierAppeleAffiche());
        g.setRangDernierAppeleAffiche(1);
        assertEquals(1, g.getRangDernierAppeleAffiche());
    }

    @Test
    public void calculerEstimationRangDernierAppeleADateFinReservationInternat_doit_echouer_si_valeurs_incoherentes() {
        GroupeAffectationUID gid = new GroupeAffectationUID(0, 0, 0);
        VerificationException verif = assertThrows(VerificationException.class,
                () -> new GroupeAffectation(0, gid, 1, 10, 11, new Parametres(1, 60, 90))
                        .getEstimationRangDernierAppeleADateFinReservationInternats()
        );
        assertEquals(VerificationExceptionMessage.GROUPE_AFFECTATION_INCOHERENCE_RANG_DERNIER_APPELE, verif.exceptionMessage);
    }

    @Test
    public void calculerEstimationRangDernierAppeleADateFinReservationInternat_retoure_valeur_correcte() throws VerificationException {
        Random r = new Random();

        GroupeAffectationUID gid = new GroupeAffectationUID(r.nextInt(), r.nextInt(), r.nextInt());

        //MAX_INT le premier jour
        assertEquals(
                Integer.MAX_VALUE,
                GroupeAffectation.calculerEstimationRangDernierAppeleADateFinReservationInternat(
                        10,
                        0,
                        500,
                        new Parametres(1, 12,90)
                )
        );
        //pas d'estimation après la date pivot
        assertEquals(
                555,
                GroupeAffectation.calculerEstimationRangDernierAppeleADateFinReservationInternat(
                        10,
                        0,
                        555,
                        new Parametres(13, 12,90)
                )
        );
        //pas d'estimation après la date pivot
        assertEquals(
                777,
                GroupeAffectation.calculerEstimationRangDernierAppeleADateFinReservationInternat(
                        777,
                        100,
                        555,
                        new Parametres(13, 12,90)
                )
        );
        //avant la date pivot, au moins le rang limite d'appel par bloc
        assertEquals(
                500,
                GroupeAffectation.calculerEstimationRangDernierAppeleADateFinReservationInternat(
                        10,
                        0,
                        500,
                        new Parametres(10, 12,90)
                )
        );
        //avant la date pivot, au moins le rang d'appelé actuel
        assertEquals(
                777,
                GroupeAffectation.calculerEstimationRangDernierAppeleADateFinReservationInternat(
                        777,
                        777,
                        50,
                        new Parametres(10, 13, 90)
                )
        );
        //avant la date pivot, interpolation linéaire si rangAppelelAnterieur non disponible
        assertEquals(
                2 * 500,
                GroupeAffectation.calculerEstimationRangDernierAppeleADateFinReservationInternat(
                        500,
                        0,
                        50,
                        new Parametres(11, 21, 90)
                )
        );
        //avant la date pivot, interpolation affine si rangAppelelAnterieur est disponible
        int vitesseDescente = 5;
        int nbJourstoGo = 10;
        int rangDernierAppeleActuel = 500;
        int rangDernierAppeleAnterieur = rangDernierAppeleActuel - NB_JOURS_POUR_INTERPOLATION_INTERNAT * vitesseDescente;
        int rangEstime = rangDernierAppeleActuel + nbJourstoGo * vitesseDescente;
        assertEquals(
                rangEstime,
                GroupeAffectation.calculerEstimationRangDernierAppeleADateFinReservationInternat(
                        rangDernierAppeleActuel,
                        rangDernierAppeleAnterieur,
                        50,
                        new Parametres(11, 11 + nbJourstoGo, 11 + 80)
                )
        );

    }
}
