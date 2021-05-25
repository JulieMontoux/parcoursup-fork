package fr.parcoursup.algos.verification;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import fr.parcoursup.algos.ordreappel.algo.AlgoOrdreAppelEntree;
import fr.parcoursup.algos.ordreappel.algo.AlgoOrdreAppelSortie;
import fr.parcoursup.algos.ordreappel.algo.GroupeClassement;
import fr.parcoursup.algos.ordreappel.algo.OrdreAppel;
import fr.parcoursup.algos.ordreappel.algo.VoeuClasse;

import static org.junit.Assert.*;

public class TestVerificationsResultatsAlgoOrdreAppel {

    @BeforeClass
	public static void setUpBeforeClass() {
		LogManager.getLogManager().reset();
    }

    @Test
    public void verifier_doit_reussir_si_proprietes_respectees() throws Exception {
        VoeuClasse vc1 = new VoeuClasse(0, 1, false, false);
        VoeuClasse vc2 = new VoeuClasse(1, 2, false, false);
        VoeuClasse vb3 = new VoeuClasse(2, 3, true, false);
        GroupeClassement gc = new GroupeClassement(0, 50, 0);
        gc.ajouterVoeu(vc1);
        gc.ajouterVoeu(vc2);
        gc.ajouterVoeu(vb3);
        AlgoOrdreAppelEntree ae = new AlgoOrdreAppelEntree();
        ae.groupesClassements.add(gc);

        vc1.setRangAppel(2);
        vc2.setRangAppel(3);
        vb3.setRangAppel(1);
        List<VoeuClasse> voeuxClasse = new ArrayList<>();
        voeuxClasse.add(vc1);
        voeuxClasse.add(vc2);
        voeuxClasse.add(vb3);
        AlgoOrdreAppelSortie as = new AlgoOrdreAppelSortie();
        OrdreAppel ordreAppel = new OrdreAppel(voeuxClasse);
        as.ordresAppel.put(gc.cGpCod, ordreAppel);
        
        new VerificationsResultatsAlgoOrdreAppel().verifier(ae, as);
        assertTrue(true);

    }

    @Test
    public void verifier_doit_echouer_si_proprietes_nonRespectees() throws Exception {
        VoeuClasse vc1 = new VoeuClasse(0, 1, false, false);
        VoeuClasse vc2 = new VoeuClasse(1, 2, false, false);
        VoeuClasse vb3 = new VoeuClasse(2, 3, true, false);
        GroupeClassement gc = new GroupeClassement(0, 50, 0);
        gc.ajouterVoeu(vc1);
        gc.ajouterVoeu(vc2);
        gc.ajouterVoeu(vb3);
        AlgoOrdreAppelEntree ae = new AlgoOrdreAppelEntree();
        ae.groupesClassements.add(gc);

        vc1.setRangAppel(1);
        vc2.setRangAppel(2);
        vb3.setRangAppel(3);
        List<VoeuClasse> voeuxClasse = new ArrayList<>();
        voeuxClasse.add(vc1);
        voeuxClasse.add(vc2);
        voeuxClasse.add(vb3);
        AlgoOrdreAppelSortie as = new AlgoOrdreAppelSortie();
        OrdreAppel ordreAppel = new OrdreAppel(voeuxClasse);
        as.ordresAppel.put(gc.cGpCod, ordreAppel);

        assertThrows(VerificationException.class, () -> new VerificationsResultatsAlgoOrdreAppel().verifier(ae, as));
    }

    @Test
    public void verifier_doit_echouer_si_G_CN_COD_duplique() throws Exception {
        VoeuClasse vc1 = new VoeuClasse(0, 1, false, false);
        VoeuClasse vc2 = new VoeuClasse(0, 2, false, false);
        VoeuClasse vb3 = new VoeuClasse(2, 3, true, false);
        GroupeClassement gc = new GroupeClassement(0, 50, 0);
        gc.ajouterVoeu(vc1);
        gc.ajouterVoeu(vc2);
        gc.ajouterVoeu(vb3);
        AlgoOrdreAppelEntree ae = new AlgoOrdreAppelEntree();
        ae.groupesClassements.add(gc);

        vc1.setRangAppel(1);
        vc2.setRangAppel(2);
        vb3.setRangAppel(3);
        List<VoeuClasse> voeuxClasse = new ArrayList<>();
        voeuxClasse.add(vc1);
        voeuxClasse.add(vc2);
        voeuxClasse.add(vb3);
        AlgoOrdreAppelSortie as = new AlgoOrdreAppelSortie();
        OrdreAppel ordreAppel = new OrdreAppel(voeuxClasse);
        as.ordresAppel.put(gc.cGpCod, ordreAppel);

        VerificationException exception = assertThrows(VerificationException.class, () -> new VerificationsResultatsAlgoOrdreAppel().verifier(ae, as));
        Assert.assertEquals(VerificationExceptionMessage.VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_GCNCOD_DUPLIQUE, exception.exceptionMessage);
    }

    @Test
    public void verifier_doit_echouer_si_G_CN_COD_manquantDansSortie() throws Exception {
        VoeuClasse vc1 = new VoeuClasse(0, 1, false, false);
        VoeuClasse vc2 = new VoeuClasse(1, 2, false, false);
        VoeuClasse vb3 = new VoeuClasse(2, 3, true, false);
        GroupeClassement gc = new GroupeClassement(0, 50, 0);
        gc.ajouterVoeu(vc1);
        gc.ajouterVoeu(vc2);
        gc.ajouterVoeu(vb3);
        AlgoOrdreAppelEntree ae = new AlgoOrdreAppelEntree();
        ae.groupesClassements.add(gc);

        vc1.setRangAppel(1);
        vc2.setRangAppel(2);
        vb3.setRangAppel(3);
        List<VoeuClasse> voeuxClasse = new ArrayList<>();
        voeuxClasse.add(vc1);
        voeuxClasse.add(vc2);
        AlgoOrdreAppelSortie as = new AlgoOrdreAppelSortie();
        OrdreAppel ordreAppel = new OrdreAppel(voeuxClasse);
        as.ordresAppel.put(gc.cGpCod, ordreAppel);

        VerificationException exception = assertThrows(VerificationException.class, () -> new VerificationsResultatsAlgoOrdreAppel().verifier(ae, as));
        assertEquals(exception.exceptionMessage, VerificationExceptionMessage.VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_GCNCOD_MANQUANT);
    }

    @Test
    public void verifierP1_doit_reussir_si_taux_boursier_respecte() throws Exception {
        VoeuClasse vc1 = new VoeuClasse(0, 1, false, false);
        VoeuClasse vc2 = new VoeuClasse(1, 2, false, false);
        VoeuClasse vb3 = new VoeuClasse(2, 3, true, false);
        GroupeClassement gc = new GroupeClassement(0, 50, 0);
        gc.ajouterVoeu(vc1);
        gc.ajouterVoeu(vc2);
        gc.ajouterVoeu(vb3);
        vc1.setRangAppel(2);
        vc2.setRangAppel(3);
        vb3.setRangAppel(1);
        
        new VerificationsResultatsAlgoOrdreAppel().verifierP1(gc);
        assertTrue(true);

    }

    @Test
    public void verifierP1_doit_echouer_si_taux_boursier_non_respecte() throws Exception {
        VoeuClasse vc1 = new VoeuClasse(0, 1, false, false);
        VoeuClasse vc2 = new VoeuClasse(1, 2, false, false);
        VoeuClasse vb3 = new VoeuClasse(2, 3, true, false);
        GroupeClassement gc = new GroupeClassement(0, 50, 0);
        gc.ajouterVoeu(vc1);
        gc.ajouterVoeu(vc2);
        gc.ajouterVoeu(vb3);
        vc1.setRangAppel(1);
        vc2.setRangAppel(3);
        vb3.setRangAppel(2);

        VerificationException exception = assertThrows(VerificationException.class, () -> new VerificationsResultatsAlgoOrdreAppel().verifierP1(gc));
        assertEquals(exception.exceptionMessage, VerificationExceptionMessage.VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_VIOLATION_P1);
    }

    @Test
    public void verifierP2_doit_reussir_si_candidatEstBoursierEtEstDuSecteur_et_rangAppel_inferieurOuEgal_a_rang() throws Exception {
        VoeuClasse vc1 = new VoeuClasse(0, 1, false, false);
        VoeuClasse vbs2 = new VoeuClasse(1, 2, true, true);
        VoeuClasse vb3 = new VoeuClasse(2, 3, true, false);
        GroupeClassement gc = new GroupeClassement(0, 50, 0);
        gc.ajouterVoeu(vc1);
        gc.ajouterVoeu(vbs2);
        gc.ajouterVoeu(vb3);
        vc1.setRangAppel(2);
        vbs2.setRangAppel(1);
        vb3.setRangAppel(3);

        new VerificationsResultatsAlgoOrdreAppel().verifierP2(gc);
        assertTrue(true);
    }

    @Test
    public void verifierP2_doit_echouer_si_candidatEstBoursierEtEstDuSecteur_et_rangAppel_superieur_a_rang() throws Exception {
        VoeuClasse vc1 = new VoeuClasse(0, 1, false, false);
        VoeuClasse vbs2 = new VoeuClasse(1, 2, true, true);
        VoeuClasse vb3 = new VoeuClasse(2, 3, true, false);
        GroupeClassement gc = new GroupeClassement(0, 50, 0);
        gc.ajouterVoeu(vc1);
        gc.ajouterVoeu(vbs2);
        gc.ajouterVoeu(vb3);
        vc1.setRangAppel(1);
        vbs2.setRangAppel(3);
        vb3.setRangAppel(2);
        
        VerificationException exception = assertThrows(VerificationException.class, () -> new VerificationsResultatsAlgoOrdreAppel().verifierP2(gc));
        assertSame(VerificationExceptionMessage.VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_BOURSIER_DU_SECTEUR_DECROIT, exception.exceptionMessage);
    }

    @Test
    public void verifierP2_doit_echouer_si_candidatEstBoursierEtEstDuSecteur_et_estDepasse() throws Exception {
        VoeuClasse vc1 = new VoeuClasse(0, 1, false, false);
        VoeuClasse vbs2 = new VoeuClasse(1, 2, true, true);
        VoeuClasse vb3 = new VoeuClasse(2, 3, true, false);
        GroupeClassement gc = new GroupeClassement(0, 50, 0);
        gc.ajouterVoeu(vc1);
        gc.ajouterVoeu(vbs2);
        gc.ajouterVoeu(vb3);

        vc1.setRangAppel(3);
        vbs2.setRangAppel(2);
        vb3.setRangAppel(1);

        VerificationException exception = assertThrows(VerificationException.class, () -> new VerificationsResultatsAlgoOrdreAppel().verifierP2(gc));
        assertSame(VerificationExceptionMessage.VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_BOURSIER_DU_SECTEUR_DEPASSE, exception.exceptionMessage);
    }

    @Test
    public void verifierP3_doit_reussir_si_candidatEstNonBoursierEtEstDuSecteur_et_doubleParBoursier() throws Exception {
        VoeuClasse vc1 = new VoeuClasse(0, 1, false, false);
        VoeuClasse vs2 = new VoeuClasse(1, 2, false, true);
        VoeuClasse vb3 = new VoeuClasse(2, 3, true, false);
        GroupeClassement gc = new GroupeClassement(0, 50, 0);
        gc.ajouterVoeu(vc1);
        gc.ajouterVoeu(vs2);
        gc.ajouterVoeu(vb3);
        vc1.setRangAppel(2);
        vs2.setRangAppel(3);
        vb3.setRangAppel(1);

        new VerificationsResultatsAlgoOrdreAppel().verifierP3(gc);
        assertTrue(true);
    }

    @Test
    public void verifierP3_doit_echouer_si_candidatEstNonBoursierEtEstDuSecteur_et_doubleParNonBoursier() throws Exception {
        VoeuClasse vc1 = new VoeuClasse(0, 1, false, false);
        VoeuClasse vs2 = new VoeuClasse(1, 2, false, true);
        VoeuClasse vs3 = new VoeuClasse(2, 3, false, true);
        GroupeClassement gc = new GroupeClassement(0, 50, 0);
        gc.ajouterVoeu(vc1);
        gc.ajouterVoeu(vs2);
        gc.ajouterVoeu(vs3);
        vc1.setRangAppel(1);
        vs2.setRangAppel(3);
        vs3.setRangAppel(2);

        VerificationException exception = assertThrows(VerificationException.class, () -> new VerificationsResultatsAlgoOrdreAppel().verifierP3(gc));
        assertSame(VerificationExceptionMessage.VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_NON_BOURSIER_DEPASSE_CANDIDAT_DU_SECTEUR, exception.exceptionMessage);
    }

    @Test
    public void verifierP3_doit_echouer_si_candidatEstNonBoursierEtEstDuSecteur_et_rangDiminueTrop() throws Exception {
        VoeuClasse vc1 = new VoeuClasse(0, 1, false, false);
        VoeuClasse vc2 = new VoeuClasse(1, 2, false, false);
        VoeuClasse vs3 = new VoeuClasse(2, 3, false, true);
        GroupeClassement gc = new GroupeClassement(0, 50, 0);
        gc.ajouterVoeu(vc1);
        gc.ajouterVoeu(vc2);
        gc.ajouterVoeu(vs3);
        vc1.setRangAppel(1);
        vc2.setRangAppel(3);
        vs3.setRangAppel(10);

        VerificationException exception = assertThrows(VerificationException.class, () -> new VerificationsResultatsAlgoOrdreAppel().verifierP3(gc));
        assertSame(VerificationExceptionMessage.VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_NON_BOURSIER_DU_SECTEUR_DIMINUE_TROP, exception.exceptionMessage);
    }

    @Test
    public void verifierP4_doit_reussir_si_candidatEstBoursierEtEstHorsSecteur_et_doubleParSecteur() throws Exception {
        VoeuClasse vc1 = new VoeuClasse(0, 1, false, false);
        VoeuClasse vb2 = new VoeuClasse(1, 2, true, false);
        VoeuClasse vs3 = new VoeuClasse(2, 3, false, true);
        GroupeClassement gc = new GroupeClassement(0, 0, 0);
        gc.ajouterVoeu(vc1);
        gc.ajouterVoeu(vb2);
        gc.ajouterVoeu(vs3);
        vc1.setRangAppel(1);
        vb2.setRangAppel(3);
        vs3.setRangAppel(2);

        new VerificationsResultatsAlgoOrdreAppel().verifierP4(gc);
        assertTrue(true);
    }

    @Test
    public void verifierP4_doit_echouer_si_candidatEstBoursierEtEstHorsSecteur_et_doubleParHorsSecteur() throws Exception {
        VoeuClasse vc1 = new VoeuClasse(0, 1, false, false);
        VoeuClasse vb2 = new VoeuClasse(1, 2, true, false);
        VoeuClasse vb3 = new VoeuClasse(2, 3, true, false);
        GroupeClassement gc = new GroupeClassement(0, 0, 50);
        gc.ajouterVoeu(vc1);
        gc.ajouterVoeu(vb2);
        gc.ajouterVoeu(vb3);
        vc1.setRangAppel(1);
        vb2.setRangAppel(3);
        vb3.setRangAppel(2);

        VerificationException exception = assertThrows(VerificationException.class, () -> new VerificationsResultatsAlgoOrdreAppel().verifierP4(gc));
        assertSame(VerificationExceptionMessage.VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_HORS_SECTEUR_DEPASSE_BOURSIER_HORS_SECTEUR, exception.exceptionMessage);
    }

    @Test
    public void verifierP4_doit_echouer_si_candidatEstBoursierEtEstHorsSecteur_et_rangDiminueTrop() throws Exception {
        VoeuClasse vc1 = new VoeuClasse(0, 1, false, false);
        VoeuClasse vb2 = new VoeuClasse(1, 2, true, false);
        VoeuClasse vb3 = new VoeuClasse(2, 3, true, false);
        GroupeClassement gc = new GroupeClassement(0, 0, 50);
        gc.ajouterVoeu(vc1);
        gc.ajouterVoeu(vb2);
        gc.ajouterVoeu(vb3);
        vc1.setRangAppel(1);
        vb2.setRangAppel(2);
        vb3.setRangAppel(10);

        VerificationException exception = assertThrows(VerificationException.class, () -> new VerificationsResultatsAlgoOrdreAppel().verifierP4(gc));
        assertSame(VerificationExceptionMessage.VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_BOURSIER_HORS_SECTEUR_DIMINUE_TROP, exception.exceptionMessage);
    }

    @Test
    public void verifier_doit_echouer_si_ordreAppelNullEnSortie() throws Exception {
        // True branch coverage de la ligne 106
        VoeuClasse vc1 = new VoeuClasse(0, 1, false, false);
        VoeuClasse vc2 = new VoeuClasse(1, 2, false, false);
        VoeuClasse vb3 = new VoeuClasse(2, 3, true, false);
        GroupeClassement gc = new GroupeClassement(0, 50, 0);
        gc.ajouterVoeu(vc1);
        gc.ajouterVoeu(vc2);
        gc.ajouterVoeu(vb3);
        AlgoOrdreAppelEntree ae = new AlgoOrdreAppelEntree();
        ae.groupesClassements.add(gc);

        Whitebox.setInternalState(vc1, "rangAppel", 2);
        Whitebox.setInternalState(vc2, "rangAppel", 3);
        Whitebox.setInternalState(vb3, "rangAppel", 1);
        
        AlgoOrdreAppelSortie as = new AlgoOrdreAppelSortie();
        
        VerificationException exception = assertThrows(VerificationException.class, () -> new VerificationsResultatsAlgoOrdreAppel().verifier(ae, as));
        assertSame(VerificationExceptionMessage.VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_DONNEE_SORTIE_MANQUANTE, exception.exceptionMessage);
    }

    @Test
    public void verifier_doit_reussir_si_DeuxCentVoeux() throws Exception {
        // False branch coverage de la ligne 100
        AlgoOrdreAppelEntree ae = new AlgoOrdreAppelEntree();
        AlgoOrdreAppelSortie as = new AlgoOrdreAppelSortie();
        for(int j=0 ; j<200 ; j++){
            GroupeClassement gc = new GroupeClassement(j, 50, 0);
            for(int i=0 ; i<1 ; i++){  // Inutile de boucler sur les voeux, mais je le laisse au cas où
                VoeuClasse v = new VoeuClasse(i, 1 + i, false, false);
                gc.ajouterVoeu(v);
            }
            ae.groupesClassements.add(gc);

            List<VoeuClasse> voeuxClasse = new ArrayList<>();
            for(int i=0 ; i<1 ; i++){
                VoeuClasse v = gc.voeuxClasses.get(i);
                Whitebox.setInternalState(v, "rangAppel", i);
                voeuxClasse.add(v);
            }
            OrdreAppel ordreAppel = new OrdreAppel(voeuxClasse);
            as.ordresAppel.put(gc.cGpCod, ordreAppel);
        }
        
        new VerificationsResultatsAlgoOrdreAppel().verifier(ae, as);
    }

    @Test
    public void verifier_doit_echouer_si_rangAppelNull() throws Exception {
        // True branch coverage de la ligne 123
        // Il faut qu'un groupeClassement soit présent en entrée, que l'ordreAppel correspondant soit en sortie, mais que le candidatClasse associé ne soit pas présent en sortie.
        VoeuClasse vc1 = new VoeuClasse(0, 1, false, false);
        GroupeClassement gc = new GroupeClassement(0, 50, 0);
        gc.ajouterVoeu(vc1);
        AlgoOrdreAppelEntree ae = new AlgoOrdreAppelEntree();
        ae.groupesClassements.add(gc);

        Whitebox.setInternalState(vc1, "rangAppel", 1);
        AlgoOrdreAppelSortie as = new AlgoOrdreAppelSortie();
        OrdreAppel ordreAppel = new OrdreAppel(new ArrayList<>());  // Pas de voeuxClasses en sortie
        as.ordresAppel.put(gc.cGpCod, ordreAppel);  // Mais l'ordreAppel est bien présent
        
        VerificationException exception = assertThrows(VerificationException.class, () -> new VerificationsResultatsAlgoOrdreAppel().verifier(ae, as));
        assertSame(VerificationExceptionMessage.VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_GCNCOD_MANQUANT, exception.exceptionMessage);
    }

    @Test
    public void verifier_doit_reussir_si_tauxMinBoursier100Pourcents() throws Exception {
        // True branch coverage de la ligne 203
        VoeuClasse vc1 = new VoeuClasse(0, 1, false, false);
        Whitebox.setInternalState(vc1, "rangAppel", 1);
        GroupeClassement gc = new GroupeClassement(0, 100, 0);
        gc.ajouterVoeu(vc1);
        AlgoOrdreAppelEntree ae = new AlgoOrdreAppelEntree();
        ae.groupesClassements.add(gc);

        List<VoeuClasse> voeuxClasse = new ArrayList<>();
        voeuxClasse.add(vc1);
        AlgoOrdreAppelSortie as = new AlgoOrdreAppelSortie();
        OrdreAppel ordreAppel = new OrdreAppel(voeuxClasse);
        as.ordresAppel.put(gc.cGpCod, ordreAppel);
        
        new VerificationsResultatsAlgoOrdreAppel().verifier(ae, as);
    }

}