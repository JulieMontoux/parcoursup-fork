/* Copyright 2022 © Ministère de l'Enseignement Supérieur, de la Recherche et de
l'Innovation, Hugo Gimbert (hugo.gimbert@enseignementsup.gouv.fr)

    This file is part of Algorithmes-de-parcoursup.

    Algorithmes-de-parcoursup is free software: you can redistribute it and/or modify
    it under the terms of the Affero GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Algorithmes-de-parcoursup is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    Affero GNU General Public License for more details.

    You should have received a copy of the Affero GNU General Public License
    along with Algorithmes-de-parcoursup.  If not, see <http://www.gnu.org/licenses/>.

 */

package fr.parcoursup.algos.verification;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.propositions.algo.GroupeAffectation;
import fr.parcoursup.algos.propositions.algo.GroupeAffectationUID;
import fr.parcoursup.algos.propositions.algo.Parametres;
import fr.parcoursup.algos.propositions.algo.Voeu;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.*;
import java.util.logging.LogManager;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(VerificationsResultatsAlgoPropositions.class)
public class TestVerificationsDemAutoGDD {

    @BeforeClass
    public static void setUpBeforeClass() {
        LogManager.getLogManager().reset();
    }

    @Test()
    public void verifier_P81_doit_verifier_si_tous_voeux_archives_ou_tous_voeux_non_archives() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        Voeu proposition = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false);
        Voeu enAttente1 = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Voeu enAttente2 = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);

        Map<Integer, List<Voeu>> voeux = Collections.singletonMap(0,Arrays.asList(proposition,enAttente1,enAttente2));


        proposition.setEstArchive(true); enAttente1.setEstArchive(true); enAttente2.setEstArchive(true);
        VerificationDemAutoGDD.verifierP81(voeux);

        proposition.setEstArchive(false); enAttente1.setEstArchive(true); enAttente2.setEstArchive(true);
        VerificationDemAutoGDD.verifierP81(voeux);

        proposition.setEstArchive(false); enAttente1.setEstArchive(false); enAttente2.setEstArchive(false);
        VerificationDemAutoGDD.verifierP81(voeux);

        proposition.setEstArchive(true); enAttente1.setEstArchive(false); enAttente2.setEstArchive(false);
        VerificationDemAutoGDD.verifierP81(voeux);

        proposition.setEstArchive(true); enAttente1.setEstArchive(false);  enAttente2.setEstArchive(true);
        Throwable exception = assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP81(voeux) );
        assertTrue(exception.getMessage().contains("P8.1"));

    }

    void setRangPreferences(List<Voeu> voeux, List<Integer> rangs) {
        if(voeux.size() != rangs.size()) {
            throw new RuntimeException("Incohérence données verif dem auto");
        }
        for(int i = 0; i < voeux.size(); i++) {
            voeux.get(i).setRangPreferencesCandidat(rangs.get(i));
        }
    }

    @Test()
    public void verifier_P82_doit_verifier_si_tous_voeux_attente_prop_jour_et_dem_autos_du_jour_ont_rang() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        Voeu v0 = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false);
        Voeu v1 = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Voeu v2 = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.GDD_DEMISSION_PROP, false);
        Voeu v3 = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.GDD_DEMISSION_ATTENTE, false);

        List<Voeu> voeux = Arrays.asList(v0,v1,v2,v3);
        Map<Integer, List<Voeu>> voeuxParCandidat = Collections.singletonMap(0 , voeux);

        setRangPreferences(voeux, Arrays.asList(1,2,3,4));
        //should not throw exception
        VerificationDemAutoGDD.verifierP82(voeuxParCandidat);

        setRangPreferences(voeux, Arrays.asList(0,2,3,4));
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP82(voeuxParCandidat) );
        setRangPreferences(voeux, Arrays.asList(1,0,3,4));
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP82(voeuxParCandidat) );
        setRangPreferences(voeux, Arrays.asList(1,2,0,4));
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP82(voeuxParCandidat) );
        setRangPreferences(voeux, Arrays.asList(1,2,3,0));
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP82(voeuxParCandidat) );

    }

    @Test()
    public void verifier_P82_doit_verifier_si_tous_voeux_attente_prop_jour_et_dem_autos_du_jour_ont_rang_diiferents() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        Voeu v0 = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false);
        Voeu v1 = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Voeu v2 = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.GDD_DEMISSION_PROP, false);
        Voeu v3 = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.GDD_DEMISSION_ATTENTE, false);

        List<Voeu> voeux = Arrays.asList(v0,v1,v2,v3);
        Map<Integer, List<Voeu>> voeuxParCandidat = new HashMap<>();
        voeuxParCandidat.put(0,voeux);

        setRangPreferences(voeux, Arrays.asList(1,2,3,4));
        //should not throw exception
        VerificationDemAutoGDD.verifierP82(voeuxParCandidat);

        setRangPreferences(voeux, Arrays.asList(4,4,3,2));
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP82(voeuxParCandidat) );
        setRangPreferences(voeux, Arrays.asList(1,2,2,4));
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP82(voeuxParCandidat) );
        setRangPreferences(voeux, Arrays.asList(1,2,4,4));
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP82(voeuxParCandidat) );
        setRangPreferences(voeux, Arrays.asList(1,4,4,0));
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP82(voeuxParCandidat) );

    }

    @Test()
    public void verifier_P83_doit_verifier_si_au_plus_deux_props_par_candidat() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        Voeu vnon = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_REFUSEE, false);
        Voeu vatt = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Voeu propHorsPP = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE, true);
        Voeu propJourPrecAcceptee = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE, false);
        Voeu propJourPrecAttenteRep = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_EN_ATTENTE_DE_REPONSE_DU_CANDIDAT, false);
        Voeu propDuJour = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false);
        Voeu propDuJour2 = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false);

        propDuJour.setRangPreferencesCandidat(1);
        propDuJour2.setRangPreferencesCandidat(3);
        propJourPrecAttenteRep.setRangPreferencesCandidat(2);

        //should not throw exception
        VerificationDemAutoGDD.verifierP83(Collections.singletonMap(0 , new ArrayList<>()));
        VerificationDemAutoGDD.verifierP83(Collections.singletonMap(0 , Arrays.asList(
                vnon,vatt)));
        VerificationDemAutoGDD.verifierP83(Collections.singletonMap(0 , Arrays.asList(
                vnon,vatt,propHorsPP)));
        VerificationDemAutoGDD.verifierP83(Collections.singletonMap(0 , Arrays.asList(
                vnon,vatt,propHorsPP,propJourPrecAcceptee)));
        VerificationDemAutoGDD.verifierP83(Collections.singletonMap(0 , Arrays.asList(
                vnon,vatt,propHorsPP,propJourPrecAcceptee,propDuJour)));

        //une prop acceptee et une en attente de rep --> ok
        VerificationDemAutoGDD.verifierP83(
                Collections.singletonMap(0 , Arrays.asList(
                        vnon,vatt,propHorsPP,propJourPrecAcceptee,propJourPrecAttenteRep))
        );

        //deux props en attente de rep --> ko
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP83(
                Collections.singletonMap(0 , Arrays.asList(
                        vnon,vatt,propHorsPP,propJourPrecAcceptee,propJourPrecAttenteRep,propDuJour))
        ) );

        //deux props en attente de rep --> ko
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP83(
                Collections.singletonMap(0 , Arrays.asList(
                        vnon,vatt,propHorsPP,propJourPrecAcceptee,propJourPrecAttenteRep,propDuJour2))
        ) );

        //les hors PP ne "comptent" pas
        Whitebox.setInternalState(propJourPrecAttenteRep,"affecteHorsPP",true);
        VerificationDemAutoGDD.verifierP83(
                Collections.singletonMap(0 , Arrays.asList(
                        vnon,vatt,propHorsPP,propJourPrecAcceptee,propJourPrecAttenteRep,propDuJour2))
        );

    }

    @Test()
    public void verifier_P83_doit_verifier_si_deux_props_sont_coherentes() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        Voeu propJourPrecAcceptee = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE, false);
        Voeu propJourPrecAttenteRep = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_EN_ATTENTE_DE_REPONSE_DU_CANDIDAT, false);
        Voeu propJourPrecAttenteRep2 = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_EN_ATTENTE_DE_REPONSE_DU_CANDIDAT, false);
        Voeu propDuJour = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false);
        Voeu propDuJour2 = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false);

        propDuJour.setRangPreferencesCandidat(1);
        propDuJour2.setRangPreferencesCandidat(3);
        propJourPrecAttenteRep.setRangPreferencesCandidat(2);

        //au moins une acceptée --> ok
        VerificationDemAutoGDD.verifierP83(Collections.singletonMap(0 , Arrays.asList(
                propJourPrecAcceptee,propDuJour)));

        //au moins une acceptée --> ok
        VerificationDemAutoGDD.verifierP83(
                Collections.singletonMap(0 , Arrays.asList(
                        propJourPrecAcceptee,propJourPrecAttenteRep))
        );

        //deux en attente rep --> ko
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP83(
                Collections.singletonMap(0 , Arrays.asList(
                        propDuJour,propJourPrecAttenteRep))
        ));


        //deux props du jour --> ko
        propDuJour.setRangPreferencesCandidat(4);
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP83(
                Collections.singletonMap(0 , Arrays.asList(
                        propDuJour,propDuJour2))
        ) );

        //deux props en attente de rep --> ko
        propDuJour.setRangPreferencesCandidat(4);
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP83(
                Collections.singletonMap(0 , Arrays.asList(
                        propJourPrecAttenteRep,propJourPrecAttenteRep2))
        ) );

        //ignore les voeux hors PP: la même verif passe
        Whitebox.setInternalState(propJourPrecAttenteRep2, "affecteHorsPP", true);
        VerificationDemAutoGDD.verifierP83(
                Collections.singletonMap(0 , Arrays.asList(
                        propJourPrecAttenteRep,propJourPrecAttenteRep2))
        );

    }

    @Test()
    public void verifier_P84_doit_verifier_voeux_en_att_rang_inf() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        Voeu propDuJourRang0 = new Voeu(0, false, groupeAffectation.id, 1, 1, 0, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false);
        Voeu propDuJourRang3 = new Voeu(0, false, groupeAffectation.id, 1, 1, 3, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false);
        Voeu propJourPrecAccepteeRang3 = new Voeu(0, false, groupeAffectation.id, 1, 1, 3, Voeu.StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE, false);
        Voeu propJourPrecAttenteRang3 = new Voeu(0, false, groupeAffectation.id, 1, 1, 3, Voeu.StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_EN_ATTENTE_DE_REPONSE_DU_CANDIDAT, false);
        Voeu propJourPrecAttenteHorsPPRang15 = new Voeu(0, false, groupeAffectation.id, 1, 1, 15, Voeu.StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_EN_ATTENTE_DE_REPONSE_DU_CANDIDAT, true);
        Voeu voeuxEnAttenteRang1 = new Voeu(0, false, groupeAffectation.id, 1, 1, 1, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Voeu voeuxEnAttenteRang2 = new Voeu(0, false, groupeAffectation.id, 1, 1, 2, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);
        Voeu voeuxEnAttenteRang4 = new Voeu(0, false, groupeAffectation.id, 1, 1, 4, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, false);

        //should not throw exception
        VerificationDemAutoGDD.verifierP84(Collections.singletonMap(0 , Arrays.asList(
                propDuJourRang0,voeuxEnAttenteRang1,voeuxEnAttenteRang2)));
        VerificationDemAutoGDD.verifierP84(Collections.singletonMap(0 , Arrays.asList(
                voeuxEnAttenteRang1,voeuxEnAttenteRang2,propDuJourRang3)));
        VerificationDemAutoGDD.verifierP84(Collections.singletonMap(0 , Arrays.asList(
                voeuxEnAttenteRang1,voeuxEnAttenteRang2,propJourPrecAccepteeRang3)));
        VerificationDemAutoGDD.verifierP84(Collections.singletonMap(0 , Arrays.asList(
                propJourPrecAttenteRang3,voeuxEnAttenteRang1,voeuxEnAttenteRang2)));
        VerificationDemAutoGDD.verifierP84(Collections.singletonMap(0 , Arrays.asList(
                propJourPrecAttenteRang3,voeuxEnAttenteRang1,propJourPrecAttenteHorsPPRang15)));

        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP84(
                Collections.singletonMap(0 , Arrays.asList(
                        propDuJourRang3,voeuxEnAttenteRang4))
        ) );
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP84(
                Collections.singletonMap(0 , Arrays.asList(
                        propJourPrecAccepteeRang3,voeuxEnAttenteRang4))
        ) );
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP84(
                Collections.singletonMap(0 , Arrays.asList(
                        propJourPrecAttenteRang3,voeuxEnAttenteRang4))
        ) );

    }

    @Test()
    public void verifier_P85_doit_verifier_nouvelle_prop_si_dem() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        Voeu demAttRang3 = new Voeu(0, false, groupeAffectation.id, 1, 1, 3, Voeu.StatutVoeu.GDD_DEMISSION_ATTENTE, false);
        Voeu demPropRang4 = new Voeu(0, false, groupeAffectation.id, 1, 1, 4, Voeu.StatutVoeu.GDD_DEMISSION_PROP, false);
        Voeu propDuJourRang1 = new Voeu(0, false, groupeAffectation.id, 1, 1, 1, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false);
        Voeu propDuJourRang5 = new Voeu(0, false, groupeAffectation.id, 1, 1, 5, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false);

        //should not throw exception
        VerificationDemAutoGDD.verifierP85(Collections.singletonMap(0 , Arrays.asList(
                demAttRang3,propDuJourRang1)));
        VerificationDemAutoGDD.verifierP85(Collections.singletonMap(0 , Arrays.asList(
                demPropRang4,propDuJourRang1)));

        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP85(
                Collections.singletonMap(0 , Arrays.asList(
                        demAttRang3))
        ) );
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP85(
                Collections.singletonMap(0 , Arrays.asList(
                        demPropRang4))
        ) );
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP85(
                Collections.singletonMap(0 , Arrays.asList(
                        demAttRang3, propDuJourRang5))
        ) );
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP85(
                Collections.singletonMap(0 , Arrays.asList(
                        demPropRang4, propDuJourRang5))
        ) );

    }

}
