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
import javax.xml.bind.JAXBException;
import parcoursup.propositions.algo.AlgoPropositions;
import parcoursup.propositions.algo.AlgoPropositionsEntree;
import parcoursup.propositions.algo.AlgoPropositionsSortie;
import parcoursup.propositions.algo.GroupeAffectation;
import parcoursup.propositions.algo.GroupeInternat;
import parcoursup.propositions.algo.Voeu;
import parcoursup.propositions.meilleursbacheliers.MeilleurBachelier;
import parcoursup.verification.VerificationsResultatsAlgoPropositions;

public abstract class ExemplePropositions {

    private static final Logger LOGGER = Logger.getLogger(ExemplePropositions.class.getSimpleName());

    /* nom de l'exemple */
    abstract String nom();

    /* crée des données d'entrée */
    abstract AlgoPropositionsEntree donneesEntree() throws Exception;

    AlgoPropositionsEntree entree;

    public void execute(boolean log) throws Exception {
        entree = donneesEntree();

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
            entree.voeux.removeIf((Voeu v) -> (v.getStatut() == Voeu.StatutVoeu.refusSimule));
            entree.voeux.removeIf((Voeu v) -> v.estDemissionAutomatique());
            
            if (GroupeInternat.nbJoursCampagne == 2) {
                simulerMBC();
            }


            GroupeInternat.nbJoursCampagne++;
            for (Voeu v : entree.voeux) {
                v.simulerEtape();
            }

        }
        LOGGER.info("Terminé le " + GroupeInternat.nbJoursCampagne + " jour de campagne");

    }

    Map<Voeu, Integer> jourProposition = new HashMap<>();

    boolean envoyerPropositions(boolean log) throws JAXBException, Exception {

        if (log) {
            entree.serialiser(null);
        }

        AlgoPropositionsSortie sortie = AlgoPropositions.calcule(entree);

        if (log) {
            sortie.serialiser(null);
        }

        VerificationsResultatsAlgoPropositions verif = new VerificationsResultatsAlgoPropositions("log", true);
        verif.verifier(entree, sortie);

        boolean propositionDuJour = false;
        for (Voeu v : entree.voeux) {
            if (v.estPropositionDuJour()) {
                propositionDuJour = true;
                jourProposition.put(v, GroupeInternat.nbJoursCampagne);
            }
            if (v.eligibleDispositifMB) {
                entree.propositionsMeilleursBacheliers.add(v.id);
            }
        }
        
        return propositionDuJour;
    }

    void simulerReponses() {

        int nbRefus = 0;
        for (Voeu v : entree.voeux) {
            if (v.getStatut() == Voeu.StatutVoeu.enAttenteDeReponseDuCandidat) {
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
            int G_CN_COD = v.id.G_CN_COD;
            if (v.rangRepondeur > 0
                    && !entree.candidatsAvecRepondeurAutomatique.contains(G_CN_COD)) {
                if (!candidates.containsKey(G_CN_COD)) {
                    candidates.put(G_CN_COD, new ArrayList<>());
                }
                candidates.get(G_CN_COD).add(v);
            }
        }

        /* un candidat sur quatre active son répondeur */
        for (int G_CN_COD : candidates.keySet()) {
            if (random.nextInt(4) == 0) {
                /* on active le répondeur */
 /* au plus une proposition */
                List<Voeu> voeux = candidates.get(G_CN_COD);
                Collections.shuffle(voeux);
                boolean propositionTrouvee = false;
                for (Voeu v : voeux) {
                    if (propositionTrouvee && (v.estProposition() || v.rangRepondeur == 0)) {
                        v.simulerRefus();
                    } else if (v.estProposition()) {
                        propositionTrouvee = true;
                    }
                }
                entree.candidatsAvecRepondeurAutomatique.add(G_CN_COD);
            }
        }

    }

    void simulerDesactivationRepAuto() {
        /* un quart des candidat sdésactive leur répondeur */
        entree.candidatsAvecRepondeurAutomatique.removeIf(c -> random.nextInt(4) == 0);
    }

    void simulerMBC() {
        entree.meilleursBacheliers.clear();

        Set<Integer> candidats = new HashSet<>();
        for (Voeu v : entree.voeux) {
            candidats.add(v.id.G_CN_COD);
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
            entree.nbPlacesMeilleursBacheliers.put(
                    ga.id.G_TA_COD,
                    Math.max(1, ga.nbRecrutementsSouhaite / 2));
        }
    }

    private final Random random = new Random();

}
