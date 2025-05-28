/* Copyright 2018 © Ministère de l'Enseignement Supérieur, de la Recherche et de
l'Innovation,
    Hugo Gimbert (hugo.gimbert@enseignementsup.gouv.fr) 

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
package fr.parcoursup.algos.bacasable.propositions;

import fr.parcoursup.algos.donnees.Serialisation;
import fr.parcoursup.algos.exceptions.AccesDonneesException;
import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;
import fr.parcoursup.algos.propositions.algo.*;
import fr.parcoursup.algos.verification.VerificationsResultatsAlgoPropositions;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ExemplePropositions {

    private static final Logger LOGGER = Logger.getLogger(ExemplePropositions.class.getSimpleName());

    /* crée des données d'entrée */
    abstract AlgoPropositionsEntree donneesEntree() throws VerificationException;

    AlgoPropositionsEntree entree;

    StatutsVoeux statuts;

    public void execute(boolean log) throws AccesDonneesException, VerificationException {
        entree = donneesEntree();
        statuts = entree.getStatutsInitiaux();

        int nbJours = entree.getParametres().nbJoursCampagne;
        int datePivot = entree.getParametres().nbJoursCampagneDateFinReservationInternats;
        int dateGDD = entree.getParametres().nbJoursCampagneDateDebutGDD;

        try {
            while (true) {

                entree.loggerEtatAdmission();

                boolean continuer = envoyerPropositions(log);
                if (!continuer) {
                    break;
                }

                simulerReponses();

                simulerActivationRepAuto();
                simulerDesactivationRepAuto();

                /* suppression des refus */
                entree.voeux.removeIf((Voeu v) -> statuts.estRefusOuDemission(v));
                entree.voeux.removeIf((Voeu v) -> statuts.estDemissionAutomatiqueParRepondeurAutomatique(v));

                statuts.simulerEtape();

                nbJours++;
                entree.setParametres(new Parametres(nbJours, datePivot, dateGDD));

            }
            LOGGER.log(Level.INFO, "Terminé le {0} jour de campagne",
                    nbJours);

        } catch (VerificationException ex) {
            new Serialisation<AlgoPropositionsEntree>()
                    .serialiserEtCompresser(entree, AlgoPropositionsEntree.class);
            throw ex;
        }
    }

    boolean envoyerPropositions(boolean log) throws VerificationException, AccesDonneesException {

        if (log) {
            new Serialisation<AlgoPropositionsEntree>()
                    .serialiserEtCompresser(entree, AlgoPropositionsEntree.class);
        }

        AlgoPropositionsSortie sortie = AlgoPropositions.calcule(entree);

        if (log) {
            new Serialisation<AlgoPropositionsSortie>()
                    .serialiserEtCompresser(sortie, AlgoPropositionsSortie.class);
        }

        new VerificationsResultatsAlgoPropositions(entree,sortie).verifier();

        if (sortie.hasAlerte() || sortie.getAvertissement()) {
            throw new VerificationException(VerificationExceptionMessage.EXEMPLE_PROPOSITIONS_ERREUR_VERIFICATION);
        }

        return entree.voeux.stream().anyMatch(v -> statuts.estPropositionDuJour(v));
    }

    void simulerReponses() {

        int nbRefus = 0;
        for (Voeu v : entree.voeux) {
            if (statuts.estPropositionDuJour(v)) {
                boolean refuse = (random.nextInt(3) == 0);
                if (refuse) {
                    nbRefus++;
                    statuts.simulerRefusProposition();
                }
            }
        }

        LOGGER.log(Level.INFO, "{0} refus simulés.", nbRefus);

    }

    void simulerActivationRepAuto() {

        /* liste des candidats avec des voeux avec des rangs mais 
        n'ayant pas activé leur rep auto */
        Map<Integer, List<Voeu>> candidates = new HashMap<>();
        for (Voeu v : entree.voeux) {
            int gCnCod = v.id.gCnCod;
            if (v.getRangPreferencesCandidat() > 0
                    && !entree.candidatsAvecRepondeurAutomatique.contains(gCnCod)) {
                candidates.computeIfAbsent(gCnCod, z -> new ArrayList<>()).add(v);
            }
        }

        /* un candidat sur quatre active son répondeur */
        candidates.forEach((key, voeux) -> {
            if (random.nextInt(4) == 0) {
                /* on active le répondeur */
                /* au plus une proposition */
                int gCnCod = key;
                Collections.shuffle(voeux);
                boolean propositionTrouvee = false;
                for (Voeu v : voeux) {
                    if (propositionTrouvee && (statuts.estProposition(v) || v.getRangPreferencesCandidat() == 0)) {
                        statuts.simulerRefusProposition();
                    } else if (statuts.estProposition(v)) {
                        propositionTrouvee = true;
                    }
                }
                entree.candidatsAvecRepondeurAutomatique.add(gCnCod);
            }
        });

    }

    void simulerDesactivationRepAuto() {
        /* un quart des candidat sdésactive leur répondeur */
        entree.candidatsAvecRepondeurAutomatique.removeIf(c -> random.nextInt(4) == 0);
    }

    private final Random random = new Random();

}
