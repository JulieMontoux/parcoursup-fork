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



import java.util.Arrays;
import java.util.Collections;
import java.util.logging.LogManager;

import static org.junit.Assert.assertThrows;


public class TestVerificationsDemAutoGDD {

    @BeforeClass
    public static void setUpBeforeClass() {
        LogManager.getLogManager().reset();
    }

    @Test()
    public void verifier_P8_doit_verifier_nouvelle_prop_si_dem() throws Exception {
        Parametres p = new Parametres(1, 0, 90);

        GroupeAffectationUID groupeAffectationUID = new GroupeAffectationUID(0, 0, 0);
        GroupeAffectation groupeAffectation = new GroupeAffectation(1, groupeAffectationUID, 0, 0, p);

        Voeu demAttRang3 = new Voeu(0, false, groupeAffectation.id, 1, 1, 3, Voeu.StatutVoeu.DEMISSION_AUTO_VOEU_ORDONNE_EN_ATTENTE, false);
        Voeu demPropRang4 = new Voeu(0, false, groupeAffectation.id, 1, 1, 4, Voeu.StatutVoeu.DEMISSION_AUTO_VOEU_ORDONNE_PROPOSITION_EN_ATTENTE_REP, false);
        Voeu propDuJourRang1 = new Voeu(0, false, groupeAffectation.id, 1, 1, 1, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false);
        Voeu propDuJourRang5 = new Voeu(0, false, groupeAffectation.id, 1, 1, 5, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, false);

        //should not throw exception
        VerificationDemAutoGDD.verifierP8(Collections.singletonMap(0 , Arrays.asList(
                demAttRang3,propDuJourRang1)));
        VerificationDemAutoGDD.verifierP8(Collections.singletonMap(0 , Arrays.asList(
                demPropRang4,propDuJourRang1)));

        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP8(
                Collections.singletonMap(0 , Arrays.asList(
                        demAttRang3))
        ) );
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP8(
                Collections.singletonMap(0 , Arrays.asList(
                        demPropRang4))
        ) );
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP8(
                Collections.singletonMap(0 , Arrays.asList(
                        demAttRang3, propDuJourRang5))
        ) );
        assertThrows(VerificationException.class, () -> VerificationDemAutoGDD.verifierP8(
                Collections.singletonMap(0 , Arrays.asList(
                        demPropRang4, propDuJourRang5))
        ) );

    }

}
