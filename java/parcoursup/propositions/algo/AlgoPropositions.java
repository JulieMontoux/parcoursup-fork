
/* Copyright 2018 © Ministère de l'Enseignement Supérieur, de la Recherche et de
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
package parcoursup.propositions.algo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import parcoursup.propositions.meilleursbacheliers.AlgoMeilleursBacheliers;
import parcoursup.propositions.meilleursbacheliers.AlgoMeilleursBacheliersDonnees;
import parcoursup.propositions.meilleursbacheliers.MeilleurBachelier;
import parcoursup.propositions.repondeur.RepondeurAutomatique;
import static parcoursup.verification.VerificationEntreeAlgoPropositions.verifierIntegrite;
import parcoursup.verification.VerificationResultatsAlgoMeilleurBachelier;

public class AlgoPropositions {

    private static final Logger LOGGER = Logger.getLogger(AlgoPropositions.class.getSimpleName());

    /* la boucle principale du calcul des propositions à envoyer */
    public static AlgoPropositionsSortie calcule(AlgoPropositionsEntree entree) throws IOException, Exception {

        LOGGER.info("Début calcul propositions");

        LOGGER.info("Initialisation des infos Meilleurs Bacheliers");
        Map<Integer, MeilleurBachelier> meilleursBacheliers = new HashMap<>();
        for (MeilleurBachelier mb : entree.meilleursBacheliers) {
            meilleursBacheliers.put(mb.G_CN_COD, mb);
        }
        for (Voeu v : entree.voeux) {
            MeilleurBachelier mb = meilleursBacheliers.get(v.id.G_CN_COD);
            if (mb != null) {
                v.setMeilleurBachelier(mb);
            }
        }

        /* chaque passage dans cette boucle correspond à un calcul des propositions
        à envoyer suivi d'une étape de réponse automatique par les candidats
        ayant activé leur répondeur automatique */
        while (true) {

            preparerGroupes(entree);

            entree.loggerEtatAdmission();

            /* vérification de l'intégrité des données d'entrée */
            LOGGER.info("Vérification de l'intégrité des données d'entrée");
            verifierIntegrite(entree);

            if (entree.nbPlacesMeilleursBacheliers.isEmpty()) {
                LOGGER.info("Aucune place réservée aux meilleurs bacheliers");
            } else if (entree.meilleursBacheliers.isEmpty()) {
                LOGGER.info("Aucun candidat n'est meilleur bachelier");
            } else {
                /* Remontées des meilleurs bacheliers */
                AlgoMeilleursBacheliersDonnees donneesMB = new AlgoMeilleursBacheliersDonnees();
                donneesMB.meilleursBacheliers.addAll(entree.meilleursBacheliers);
                donneesMB.nbPlacesMeilleursBacheliers.putAll(entree.nbPlacesMeilleursBacheliers);
                donneesMB.propositionsMeilleursBacheliers.addAll(entree.propositionsMeilleursBacheliers);
                for (Voeu v : entree.voeux) {
                    if (v.eligibleDispositifMB && v.estProposition()) {
                        donneesMB.propositionsMeilleursBacheliers.add(v.id);
                    }
                }
                donneesMB.groupes.addAll(entree.groupesAffectations.values());
                AlgoMeilleursBacheliers.appliquerDispositifMeilleursBacheliers(donneesMB);

                LOGGER.info("Vérification des résultats de l'algo MB");
                /* vérification de l'intégrité des données d'entrée après remontée MB */
                VerificationResultatsAlgoMeilleurBachelier.verifier(donneesMB);

                LOGGER.info("Vérification de l'intégrité des données d'entrée"
                        + " après remontée MB");
                verifierIntegrite(entree);

            }

            /* groupes à mettre à jour */
            Set<GroupeAffectation> groupesAMettreAJour = new HashSet<>();
            groupesAMettreAJour.addAll(entree.groupesAffectations.values());

            /* initialisation des positions maximales d'admission dans les internats */
            for (GroupeInternat internat : entree.internats.values()) {
                internat.initialiserPositionAdmission();
            }

            int compteurBoucle = 0;
            while (groupesAMettreAJour.size() > 0) {

                compteurBoucle++;

                LOGGER.log(Level.INFO, "Itération " + compteurBoucle
                        + ": mise à jour des propositions dans {0} groupes d'affectations",
                        groupesAMettreAJour.size()
                );

                /* calcul des propositions à effectuer,
                étant données les positions actuelles d'admissions aux internats */
                for (GroupeAffectation gc : groupesAMettreAJour) {
                    gc.mettreAJourPropositions();
                }

                /* Test de surcapacité des internats, avec
               mise à jour de la position d'admission si nécessaire.

            Baisser la position d'admission d'un internat ne diminue
            pas le nombre de candidats dans les autres internats, voire augmente ces nombres,
            car les formations devront potentiellement descendre plus bas dans l'ordre d'appel.

            Par conséquent, on peut mettre à jour toutes les positions d'admission
            de tous les internats sans mettre à jour systématiquement les propositions:
            si un internat est détecté en surcapacité avant la mise
            à jour des propositions, il l'aurait été également après la mise à jour des propositions.
            (Mais la réciproque est fausse en général).

            De cette manière, on reste bien dans l'ensemble E des vecteurs de positions
            d'admission supérieurs sur chaque composante au vecteur de positions d'admission
            le plus permissif possible parmi tous ceux respectant les contraintes
            de capacité des internats et situés en deçà des positions maximales
            d'admission.

            Ce vecteur est égal, sur chaque composante, à la valeur minimum de cette
            composante parmi les éléments de E.

            La boucle termine quand les contraintes de capacité des internats
            sont satisfaites, c'est-à-dire quand ce minimum global est atteint.

            Une propriété de symétrie i.e. d'équité intéressante:
            le résultat ne dépend pas de l'ordre dans lequel on itère sur les internats et
            les formations.
                 */
                groupesAMettreAJour.clear();

                for (GroupeInternat internat : entree.internats.values()) {
                    boolean maj = internat.mettreAJourPositionAdmission();
                    if (maj) {
                        groupesAMettreAJour.addAll(internat.groupesConcernes);
                    }
                }

            }

            LOGGER.log(Level.INFO, "Calcul terminé après {0} itération(s).", compteurBoucle);

            /* remise à leur valeur initiale des ordres d'appels
            potentiellement modifiés par le dispositif MB */
            for (Voeu v : entree.voeux) {
                v.ordreAppel = v.ordreAppelInitial;
            }

            int placesLibereesParRepAuto = 0;

            if (!entree.candidatsAvecRepondeurAutomatique.isEmpty()) {
                LOGGER.log(Level.INFO, "Préparation des données du répondeur automatique,"
                        + "{0} candidats l''ont activé",
                        entree.candidatsAvecRepondeurAutomatique.size()
                );

                Collection<Voeu> voeuxDesCandidatsAvecRepAuto = new ArrayList<>();
                for (Voeu v : entree.voeux) {
                    /* le répondeur automatique ne tient pas compte des voeuxEnAttente hors PP */
                    if (v.estAffecteHorsPP()) {
                        continue;
                    }
                    if (entree.candidatsAvecRepondeurAutomatique.contains(v.id.G_CN_COD)) {
                        voeuxDesCandidatsAvecRepAuto.add(v);
                    }
                }
                LOGGER.log(Level.INFO, "{0} voeux avec répondeur automatique", voeuxDesCandidatsAvecRepAuto.size());

                placesLibereesParRepAuto
                        = RepondeurAutomatique.reponsesAutomatiques(voeuxDesCandidatsAvecRepAuto);
            } else {
                LOGGER.info("Aucun candidat n'a activé le répondeur automatique");
            }

            /* si le répondeur ne libère pas de place, le calcul est terminé */
            if (placesLibereesParRepAuto == 0) {
                LOGGER.info("Aucune place libérée par le répondeur automatique");
                break;
            } else {
                LOGGER.log(Level.INFO, "Le répondeur automatique a libéré {0} places",
                        placesLibereesParRepAuto);
            }

        }

        LOGGER.info(
                "Préparation données de sortie");

        AlgoPropositionsSortie sortie = new AlgoPropositionsSortie();
        sortie.voeux.addAll(entree.voeux);
        sortie.internats.addAll(entree.internats.values());
        sortie.groupes.addAll(entree.groupesAffectations.values());

        LOGGER.log(Level.INFO, "Propositions {0}", sortie.propositionsDuJour().count());
        LOGGER.log(Level.INFO, "Demissions Automatiques {0}", sortie.demissions().count());

        return sortie;

    }

    /* ventile les voeux encore en attente dans les groupes concernés */
    private static void preparerGroupes(AlgoPropositionsEntree entree) {
        /* réinitialisation des groupes */
        for (GroupeAffectation groupe : entree.groupesAffectations.values()) {
            groupe.reinitialiser();
        }
        for (GroupeInternat internat : entree.internats.values()) {
            internat.reinitialiser();
        }

        /* ajout des voeux aux groupes  et remise à leurs valeurs initiales
            des ordres d'appels (modifiables temporairement par dispositif MB) */
        for (Voeu v : entree.voeux) {
            v.ordreAppel = v.ordreAppelInitial;
            v.repondeurActive = (v.rangRepondeur > 0)
                    && entree.candidatsAvecRepondeurAutomatique.contains(v.id.G_CN_COD);
            v.ajouterAuxGroupes();
        }
    }

    private AlgoPropositions() {
    }

}
