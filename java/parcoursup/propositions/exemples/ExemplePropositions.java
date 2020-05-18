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
package parcoursup.propositions.exemples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import parcoursup.donnees.Serialisation;
import parcoursup.exceptions.AccesDonneesException;
import parcoursup.propositions.algo.AlgoPropositions;
import parcoursup.propositions.algo.AlgoPropositionsEntree;
import parcoursup.propositions.algo.AlgoPropositionsSortie;
import parcoursup.propositions.algo.GroupeAffectation;
import parcoursup.propositions.algo.Voeu;
import parcoursup.propositions.meilleursbacheliers.MeilleurBachelier;
import parcoursup.exceptions.VerificationException;
import parcoursup.verification.VerificationsResultatsAlgoPropositions;

public abstract class ExemplePropositions {

    private static final Logger LOGGER = Logger.getLogger(ExemplePropositions.class.getSimpleName());

    /* nom de l'exemple */
    abstract String nom();

    /* crée des données d'entrée */
    abstract AlgoPropositionsEntree donneesEntree() throws VerificationException;

    AlgoPropositionsEntree entree;

    public void execute(boolean log) throws VerificationException, AccesDonneesException {
        entree = donneesEntree();
        int nbJours = 1;

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
            entree.voeux.removeIf((Voeu v) -> (v.getStatut() == Voeu.StatutVoeu.REFUS_OU_DEMISSION));
            entree.voeux.removeIf(Voeu::estDemissionAutomatique);

            if (nbJours == 2) {
                simulerMBC();
            }

            for (Voeu v : entree.voeux) {
                v.simulerEtape();
            }

        }
        LOGGER.log(Level.INFO, "Termin\u00e9 le {0} jour de campagne",
                nbJours);

    }

    boolean envoyerPropositions(boolean log) throws VerificationException, AccesDonneesException {

        if (log) {
            new Serialisation<AlgoPropositionsEntree>()
                    .serialiserEtCompresser(entree,AlgoPropositionsEntree.class);
        }

        AlgoPropositionsSortie sortie = AlgoPropositions.calcule(entree);

        if (log) {
            new Serialisation<AlgoPropositionsSortie>()
                    .serialiserEtCompresser(sortie,AlgoPropositionsSortie.class);
        }

        VerificationsResultatsAlgoPropositions verif
                = new VerificationsResultatsAlgoPropositions();
        verif.verifier(entree, sortie);

        if (sortie.getAlerte() || sortie.getAvertissement()) {
            throw new VerificationException("Problème détecté lors de la vérification");
        }

        boolean propositionDuJour = false;
        for (Voeu v : entree.voeux) {
            if (v.estPropositionDuJour()) {
                propositionDuJour = true;
            }
            if (v.getEligibleDispositifMB()) {
                entree.propositionsMeilleursBacheliers.add(v.id);
            }
        }

        return propositionDuJour;
    }

    void simulerReponses() {

        int nbRefus = 0;
        for (Voeu v : entree.voeux) {
            if (v.getStatut() == Voeu.StatutVoeu.PROPOSITION_DU_JOUR) {
                boolean refuse = (random.nextInt(3) == 0);
                if (refuse) {
                    nbRefus++;
                    v.simulerRefus();
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
            if (v.rangRepondeur > 0
                    && !entree.candidatsAvecRepondeurAutomatique.contains(gCnCod)) {
                if (!candidates.containsKey(gCnCod)) {
                    candidates.put(gCnCod, new ArrayList<>());
                }
                candidates.get(gCnCod).add(v);
            }
        }

        /* un candidat sur quatre active son répondeur */
        candidates.entrySet().forEach(entry -> {
            if (random.nextInt(4) == 0) {
                /* on active le répondeur */
 /* au plus une proposition */
                int gCnCod = entry.getKey();
                List<Voeu> voeux = entry.getValue();
                Collections.shuffle(voeux);
                boolean propositionTrouvee = false;
                for (Voeu v : voeux) {
                    if (propositionTrouvee && (v.estProposition() || v.rangRepondeur == 0)) {
                        v.simulerRefus();
                    } else if (v.estProposition()) {
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

    void simulerMBC() {
        entree.meilleursBacheliers.clear();

        Set<Integer> candidats = new HashSet<>();
        for (Voeu v : entree.voeux) {
            candidats.add(v.id.gCnCod);
        }

        for (int G_CN_COD : candidats) {
            if (random.nextInt(4) == 0) {
                entree.meilleursBacheliers.add(
                        new MeilleurBachelier(
                                G_CN_COD,
                                10.0 + random.nextInt(100) / 10.0
                        )
                );
            }
        }

        for (GroupeAffectation ga : entree.groupesAffectations.values()) {
            entree.nbPlacesMeilleursBacheliers.put(ga.id.gTaCod,
                    Math.max(1, ga.nbRecrutementsSouhaite / 2));
        }
    }

    private final Random random = new Random();

}
