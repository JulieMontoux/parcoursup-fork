/* Copyright 2018, 2018 Loïc Broquet (lbroquet@online.fr),
    2020 CATIE (contact@catie.fr)

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

package fr.parcoursup.algos.ordreappel.algo;

import static java.util.Comparator.comparingInt;
import java.util.List;
import java.util.Random;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;
import org.junit.Assert;
import org.junit.Test;

public class TestGroupeClassement {

    @Test
    public void testAjouterVoeu() throws Exception {
        GroupeClassement gc = new GroupeClassement(1, 50, 50);
        VoeuClasse v = new VoeuClasse(2, 2, true, true);
        gc.ajouterVoeu(v);
        assertTrue(gc.voeuxClasses.contains(v));
    }

    @Test
    public void constructuer_de_GroupeClassement_doit_verifier_taux_sont_coherents() {
        VerificationException exception = assertThrows(VerificationException.class, ()
                -> new GroupeClassement(1, -1, 0));
        Assert.assertEquals(VerificationExceptionMessage.GROUPE_CLASSEMENT_TAUX_INCOHERENTS, exception.exceptionMessage);
        exception = assertThrows(VerificationException.class, ()
                -> new GroupeClassement(1, 101, 0));
        Assert.assertEquals(VerificationExceptionMessage.GROUPE_CLASSEMENT_TAUX_INCOHERENTS, exception.exceptionMessage);
        exception = assertThrows(VerificationException.class, ()
                -> new GroupeClassement(1, 0, -1));
        Assert.assertEquals(VerificationExceptionMessage.GROUPE_CLASSEMENT_TAUX_INCOHERENTS, exception.exceptionMessage);
        exception = assertThrows(VerificationException.class, ()
                -> new GroupeClassement(1, 0, 101));
        Assert.assertEquals(VerificationExceptionMessage.GROUPE_CLASSEMENT_TAUX_INCOHERENTS, exception.exceptionMessage);
    }

    @Test
    public void ordreAppel_vide_si_voeuxClasses_vide() throws VerificationException {
        GroupeClassement instance = new GroupeClassement(0, 0, 0);

        OrdreAppel ordreAppel = instance.calculerOrdreAppel();

        assertTrue(ordreAppel.candidats.isEmpty());
    }

    @Test
    public void ordreAppel_trie_par_rang() throws VerificationException {
        GroupeClassement instance = new GroupeClassement(0, 0, 0);
        ajouteVoeuxRangAleatoire(instance, 100);

        OrdreAppel ordreAppel = instance.calculerOrdreAppel();

        assertTrieParRang(ordreAppel);
    }

    /* Ne peut pas être rajouté car `voeux` n'est plus disponible dans `ordreAppel`
    @Test
    public final void boursierNonResident_prioritaire_si_contraintes_insatiables() {
        GroupeClassement instance = new GroupeClassement(0, 50, 50);
        instance.ajouterVoeu(new VoeuClasse(0, 0, false, true));
        instance.ajouterVoeu(new VoeuClasse(1, 1, true, false));

        OrdreAppel ordreAppel = instance.calculerOrdreAppel();

        assertTrue(ordreAppel.voeux.get(0).estBoursier());
    }
    */

    private void ajouteVoeuxRangAleatoire(GroupeClassement instance, int nb) throws VerificationException {
        Random random = new Random();
        for (int i = 0; i < nb; ++i) {
            instance.ajouterVoeu(new VoeuClasse(i, 1 + random.nextInt(Integer.MAX_VALUE - 1), false, false));
        }
    }

    private void assertTrieParRang(OrdreAppel ordreAppel) {
        List<CandidatClasse> listeTriee = ordreAppel.candidats.stream()
                .sorted(comparingInt(c -> c.rangAppel))
                .collect(toList());
        assertEquals(listeTriee, ordreAppel.candidats);
    }

}