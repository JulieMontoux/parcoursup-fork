
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
package fr.parcoursup.algos.propositions.algo;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.utils.UtilService;
import fr.parcoursup.algos.verification.VerificationEntreeAlgoPropositions;
import fr.parcoursup.algos.verification.VerificationsResultatsAlgoPropositions;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AlgoPropositions {

    private static final Logger LOGGER = Logger.getLogger(AlgoPropositions.class.getSimpleName());

    /* la boucle principale du calcul des propositions à envoyer */
    public static AlgoPropositionsSortie calcule(
            AlgoPropositionsEntree entree) throws VerificationException {
        return calcule(entree, true);
    }

    /**
     * Algorithme de calcul des propositions à envoyer aux candidats
     *
     * @param entree   Les données d'entrée.
     * @param verifier si ce paramétre est true, les données d'entrée sont vérifiées. En prod le paramètre est activé. En simulation il est parfois désactivé.
     * @return Un objet de type AlgoPropositionsSortie contenant la liste des voeux mis à jour
     * @throws VerificationException en cas de défaut d'intégrité des données d'entrée
     */
    public static AlgoPropositionsSortie calcule(
            AlgoPropositionsEntree entree,
            boolean verifier) throws VerificationException {

    	LOGGER.info(UtilService.encadrementLog("Calcul des propositions"));

        LOGGER.info(UtilService.petitEncadrementLog("Préparation des données"));
        entree.injecterGroupesEtInternatsDansVoeux();

        LOGGER.info(UtilService.petitEncadrementLog("Début du calcul propositions"));
        /* chaque passage dans cette boucle correspond à un calcul des propositions
        à envoyer suivi d'une étape de réponse automatique par les candidats
        ayant activé leur répondeur automatique */
        int nbIterations = 0;
        while (true) {
            nbIterations++;
//
//            LOGGER.info("***********************************************\n" +
//                    "*********  Itération " + nbIterations + "   *******\n" +
//                    "***********************************************"
//            );
            
            LOGGER.info(UtilService.petitEncadrementLog("Itération : " + nbIterations));
            
            

            LOGGER.info("Préparation des groupes ");
            preparerGroupes(entree);

            /* vérification de l'intégrité des données d'entrée */
            if (verifier) {
                entree.loggerEtatAdmission();
                LOGGER.info(UtilService.petitEncadrementLog("Vérification de l'intégrité des données d'entrée"));
                VerificationEntreeAlgoPropositions.verifierIntegrite(entree);
            }

            calculerNouvellesPropositions(entree);

            long placesLibereesParDemAutoGDD = 0;
            boolean appliquerdemAutoGDD =
                    entree.parametres.nbJoursCampagne >= entree.parametres.nbJoursCampagneDateDebutGDD;
            if(appliquerdemAutoGDD) {
                LOGGER.info(UtilService.petitEncadrementLog("Application des démissions automatiques des voeux en attente en GDD"));
                placesLibereesParDemAutoGDD = DemissionAutoVoeuxOrdonnes.appliquerDemissionAutomatiqueVoeuOrdonnes(entree);
            }

            long placesLibereesParRepAuto = RepondeurAutomatique.appliquerRepondeurAutomatique(entree);

            if (placesLibereesParRepAuto == 0 && placesLibereesParDemAutoGDD == 0) {
                break;
            }

        }

        LOGGER.info(UtilService.petitEncadrementLog("Préparation données de sortie"));

        AlgoPropositionsSortie sortie = new AlgoPropositionsSortie(entree);

        LOGGER.info(UtilService.petitEncadrementLog("Propositions : " + sortie.nbPropositionsDuJour()));
        LOGGER.info(UtilService.petitEncadrementLog("Demissions Automatiques " + sortie.nbDemissions()));

        if (verifier) {
        	LOGGER.info(UtilService.petitEncadrementLog("V\u00e9rification des "+sortie.nbPropositionsDuJour()+" propositions"));
            new VerificationsResultatsAlgoPropositions(entree, sortie).verifier();
        }
        
        LOGGER.info(UtilService.encadrementLog("Fin du calcul des propositions"));
        return sortie;
    }

    /* ventile les voeux encore en attente dans les groupes concernés */
    public static void preparerGroupes(AlgoPropositionsEntree entree) throws VerificationException {
        /* réinitialisation des groupes */
        entree.groupesAffectations.values().stream().parallel().forEach(
                GroupeAffectation::reinitialiser
        );
        entree.internats.values().stream().parallel().forEach(
                GroupeInternat::reinitialiser
        );

        /* ajout des voeux aux groupes */
        for (Voeu v : entree.voeux) {
            v.setRepondeurActive((v.getRangPreferencesCandidat() > 0)
                    && entree.candidatsAvecRepondeurAutomatique.contains(v.id.gCnCod));
            v.ajouterAuxGroupes();
        }
    }

    public static void calculerNouvellesPropositions(AlgoPropositionsEntree entree) throws VerificationException {
        /* groupes à mettre à jour */
    	/*EVOL 2024 : On ne traite pas le groupes si il a le flag adm_stop. */
        Set<GroupeAffectation> groupesAMettreAJour = new HashSet<>(entree.groupesAffectations.values().stream().filter(g -> g.getA_rg_flg_adm_stop() == 0).collect(Collectors.toList()));

        /* initialisation des positions maximales d'admission dans les internats */
        for (GroupeInternat internat : entree.internats.values()) {
            internat.initialiserPositionAdmission(entree.getParametres());
        }

        int compteurBoucle = 0;
        while (!groupesAMettreAJour.isEmpty()) {

        	
            compteurBoucle++;

            LOGGER.info(UtilService.petitEncadrementLog("It\u00e9ration "+compteurBoucle+" : "
                            + "mise \u00e0 jour des propositions dans "+groupesAMettreAJour.size()+" groupes d''affectations"));

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
            de tous les internats sans mettre à jour systématiquement les propositions :
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

            Une propriété d'équité intéressante :
            le résultat ne dépend pas de l'ordre dans lequel on itère sur les internats et
            les formations.
             */
            groupesAMettreAJour.clear();

            for (GroupeInternat internat : entree.internats.values()) {
                boolean maj = internat.mettreAJourPositionAdmission();
                if (maj) {
                	/*EVOL 2024 : On ne traite pas le groupe si il a le flag adm_stop. */
                    groupesAMettreAJour.addAll(internat.groupesConcernes().stream().filter(g -> g.getA_rg_flg_adm_stop() == 0).collect(Collectors.toList()));
                }
            }

        }

        LOGGER.info(UtilService.petitEncadrementLog("Calcul terminé après "+compteurBoucle+" itération(s)."));

    }

    private AlgoPropositions() {
    }

}
