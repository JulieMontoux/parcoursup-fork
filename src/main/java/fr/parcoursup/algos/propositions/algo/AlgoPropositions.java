
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
import java.util.logging.Logger;

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
     * @param verifier détermine si les données d'entrée et de sortie sont vérifiées. En prod le paramètre est activé.
     *                 En test ou en simulation il est parfois désactivé.
     * @return Un objet de type AlgoPropositionsSortie contenant la liste des voeux mis à jour
     * @throws VerificationException en cas de défaut d'intégrité des données d'entrée
     */
    public static AlgoPropositionsSortie calcule(
            AlgoPropositionsEntree entree,
            boolean verifier) throws VerificationException {
        return calcule(entree, verifier, true);
    }

    public static AlgoPropositionsSortie calcule(AlgoPropositionsEntree entree, boolean verifier, boolean invaliderGroupesAvecalertes) throws VerificationException {

        LOGGER.info(UtilService.encadrementLog("Calcul des propositions"));

        /* vérification de l'intégrité des données d'entrée */
        if (verifier) {
            entree.loggerEtatAdmission();
            LOGGER.info(UtilService.petitEncadrementLog("Vérification de l'intégrité des données d'entrée"));
            VerificationEntreeAlgoPropositions.verifierIntegrite(entree);
        }

        AlgoPropositionsSortie sortie = calculerNouvellesPropositions(entree);

        LOGGER.info(UtilService.petitEncadrementLog("Propositions du jour " + sortie.nbPropositionsDuJour()));
        LOGGER.info(UtilService.petitEncadrementLog("Demissions Automatiques " + sortie.nbDemissions()));

        if (verifier) {
            LOGGER.info(UtilService.petitEncadrementLog("Vérification des " + sortie.nbPropositionsDuJour() + " propositions"));
            if(invaliderGroupesAvecalertes) {
                new VerificationsResultatsAlgoPropositions(entree, sortie).verifier();
            } else {
                new VerificationsResultatsAlgoPropositions(entree, sortie).verifierSansSupprimerDePropositions();
            }
        }

        LOGGER.info(UtilService.encadrementLog("Fin du calcul des propositions"));
        return sortie;
    }

    /**
     * Algorithme de calcul des propositions à envoyer aux candidats
     *
     * @param entree Les données d'entrée.
     * @return Un objet de type AlgoPropositionsSortie contenant la liste des voeux mis à jour
     * @throws VerificationException en cas de défaut d'intégrité des données d'entrée
     */
    public static AlgoPropositionsSortie calculerNouvellesPropositions(AlgoPropositionsEntree entree) throws VerificationException {

        boolean appliquerDemissionsAutomatiques =
                entree.parametres.nbJoursCampagne >= entree.parametres.nbJoursCampagneDateDebutGDD;
        if (appliquerDemissionsAutomatiques) {
            LOGGER.info("Les démissions automatiques seront appliquées.");
        } else {
            LOGGER.info("Les démissions automatiques ne seront * pas * appliquées.");
        }

        boolean appliquerRepondeurAutomatique = entree.estRepondeurAutomatiqueActivable();
        if (appliquerRepondeurAutomatique) {
            LOGGER.info("Le répondeur automatique sera utilisé.");
        } else {
            LOGGER.info("Le répondeur automatique ne sera * pas * utilisé.");
        }


        AlgoPropositionDonneesPrecalculees donneesPrecalculees = entree.getDonneesPrecalculees();
        StatutsVoeux statutsInitiaux = entree.getStatutsInitiaux();
        Map<GroupeInternatUID, Integer> barresAdmissionInternats = BarresInternats.calculerBarresInitialesInternats(
                donneesPrecalculees.barresMaximalesAdmissionInternats,
                entree.rangsEnAttenteParInternat()
        );


        /* boucle de diminutions successives des barres internats jusqu'à obtenir un ensemble de propositions
        ne générant aucune surcapacité internat */
        LOGGER.info("Recherche de barres internats ne créant pas de surcapacités");
        //boucle de la mise à jour des barres internats par diminutions succesives
        int compteurBoucleDiminutionsBarresInternat = 1;
        while (true) {

            LOGGER.info("Calcul  de barres internats ne créant pas de surcapacités");
            StatutsVoeux statutsApresPropositionsEtDemissions =
                    CalculPropositionsEtDemissions.calculerPropositionsEtDemissions(
                            barresAdmissionInternats,
                            statutsInitiaux,
                            compteurBoucleDiminutionsBarresInternat,
                            appliquerDemissionsAutomatiques,
                            appliquerRepondeurAutomatique,
                            donneesPrecalculees
                    );

            /* Test de surcapacité des internats, avec
               mise à jour de la position d'admission si nécessaire.
               Voir détails dans le documents de algorithmes de Parcoursup,
               disponible dans ce dépôt de code.
             */
            int nbBarresInternatsDiminuees = 0;
            int sommeBarresAvant = barresAdmissionInternats.values().stream().mapToInt(x -> x).sum();

            Map<GroupeInternatUID, Long> nbCandidatsAffectesParInternat
                    = entree.getNbCandidatsAffectesParInternat(statutsApresPropositionsEtDemissions);
            Set<GroupeInternatUID> internatsAvecAuMoinsUneNouvelleProposition
                    = entree.getInternatsAvecAuMoinsUneNouvelleProposition(statutsApresPropositionsEtDemissions);

            for (GroupeInternat internat : entree.internats.values()) {
                GroupeInternatUID id = internat.id;
                long nbCandidatsAffectes = nbCandidatsAffectesParInternat.getOrDefault(id, 0L);
                int capacite = internat.getCapacite();
                int barreActuelle = barresAdmissionInternats.getOrDefault(id, 0);
                boolean auMoinsUnePropositionDuJour = internatsAvecAuMoinsUneNouvelleProposition.contains(id);
                long surCapacite = nbCandidatsAffectes - capacite;
                if (
                        surCapacite > 0
                                && auMoinsUnePropositionDuJour
                                && barreActuelle > 0//redondant
                ) {
                    nbBarresInternatsDiminuees++;
                    List<Voeu> voeuxInitialementEnAttenteTriesParClassementInternatDecroissant
                            = donneesPrecalculees.voeuxInternatsInitialementEnAttenteTriesParClassementInternatDecroissant.get(internat.id);
                    int nouvelleBarre = decroitreBarreEnFonctionDeSurcapacite(
                            barreActuelle,
                            voeuxInitialementEnAttenteTriesParClassementInternatDecroissant,
                            surCapacite
                    );
                    barresAdmissionInternats.put(id, nouvelleBarre);
                }
            }
            int sommeBarresApres = barresAdmissionInternats.values().stream().mapToInt(x -> x).sum();

            if (nbBarresInternatsDiminuees != 0) {
                int diff = sommeBarresAvant - sommeBarresApres;
                LOGGER.info(UtilService.petitEncadrementLog("Calcul barre internat: diminution de " + nbBarresInternatsDiminuees + " barre(s) internat(s) (" + diff + " rangs)."));
                compteurBoucleDiminutionsBarresInternat++;
            } else {
                LOGGER.info(UtilService.petitEncadrementLog("Calcul terminé après " + compteurBoucleDiminutionsBarresInternat + "diminution(s) des barres internats."));
                return new AlgoPropositionsSortie(
                        entree,
                        barresAdmissionInternats,
                        donneesPrecalculees.barresMaximalesAdmissionInternats,
                        statutsApresPropositionsEtDemissions
                );
            }
        }
    }

    /**
     * Calcul du plus grand rang en attente inférieur à la barre d'admission
     *
     * @param barreActuelle                                                   la barre d'admission actuelle
     * @param voeuxInitialementEnAttenteTriesParClassementInternatDecroissant les voeux en attente dans cet internat
     * @param surCapacite                                                     le nombre de candidats en surcapacité
     * @return le plus grand rang en attente inférieur à la barre d'admission
     */
    private static int decroitreBarreEnFonctionDeSurcapacite(
            int barreActuelle,
            List<Voeu> voeuxInitialementEnAttenteTriesParClassementInternatDecroissant,
            long surCapacite) {
        surCapacite--;
        for (Voeu v : voeuxInitialementEnAttenteTriesParClassementInternatDecroissant) {
            if (v.rangInternat < barreActuelle) {
                surCapacite--;
                if(surCapacite <= 0) {
                    return v.rangInternat;
                }
            }
        }
        return 0;
    }

    private AlgoPropositions() {
    }

}
