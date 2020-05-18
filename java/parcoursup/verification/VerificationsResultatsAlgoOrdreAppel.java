/*
    Copyright 2019 © Ministère de l'Enseignement Supérieur, de la Recherche et de
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
package parcoursup.verification;

import parcoursup.exceptions.VerificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import parcoursup.ordreappel.algo.AlgoOrdreAppelEntree;
import parcoursup.ordreappel.algo.AlgoOrdreAppelSortie;
import parcoursup.ordreappel.algo.CandidatClasse;
import parcoursup.ordreappel.algo.GroupeClassement;
import parcoursup.ordreappel.algo.OrdreAppel;
import parcoursup.ordreappel.algo.VoeuClasse;

/* Permet de vérifier un certain nombre de propriétés statiques
    des sorties de l'algorithme. Sans garantir la correction du code,
    cela garantit que les résultats produits satisfont les principales propriétés
    énoncées dans le document.
    Des tests complémentaires sont effectués en base par des scripts PL/SQL.

En production, certains cas particuliers adviennent, par exemple:

    * réinsertion de voeux suite à des demandes candidats d'annuler une démission sur un voeu,
        ce sont les voeux pour lesquels estAnnulationDemission() est true.
        Entretemps, des candidats moins bien positionnés dans l'ordre d'appel
        peuvent avoir reçu une proposition, ce cas est pris en compte dans P1.

    * modifications de classements par les formations (rare),
        suite à erreurs de saisie. Dans ce cas les candidats ayant déjà
        bénéficié d'une proposition la conserve et prennent la tête de l'ordre
        d'appel. L'ordre d'appel des candidats restants est recalculé sur la base
        du nouveau classement.

Les vérifications tiennet comptent de ces cas particuliers (e.g. ajout du flag estAnnulationDemission().

Afin de ne pas bloquer l'envoi quotidien des propositions qaudn un nouveau cas particulier
est découvert, on implémente un mode de vérification non-bloquant:
en cas de violation d'une propriété, le groupe de classement
et les éventuels internats associés sont exclus de la génération des propositions.
On calcule la zone d'influence de l'erreur, c'est-à-dire la composante
connexe contenant la formation ou l'internat concerné dans le graphe dont les
sommets sont ces formations et internats et les arètes sont induites par les voeux
en attente.


 */
public class VerificationsResultatsAlgoOrdreAppel {

    /* Vérifie le résultat du calcul et renvoie une exception en cas de problème */
    public void verifier(AlgoOrdreAppelEntree entree, AlgoOrdreAppelSortie sortie) throws VerificationException {
        log("Vérification des propriétés attendues d'un des " + sortie.ordresAppel.size()
                + " ordres d'appel");

        /* Vérification des propriétés 
        Propriete 1. Pour tout k, au moins qB pourcents des k premiers candidats sont boursiers ;
        ou sinon, aucun candidat parmi Ck+1,…,Cn n’est boursier. 
                
        Propriete 2. Un candidat à la fois boursier et du secteur
        qui a le rang r dans le classement pédagogique 
        n’est jamais doublé par personne et aura donc un rang inférieur 
        ou égal à r dans l’ordre d’appel. 
        
        Propriete 3. Un candidat du secteur
        et non boursier qui a le rang r dans le classement pédagogique 
        ne peut-être doublé que par des boursiers et aura un rang compris 
        entre r et r + 1 + r * qB∕(100 - qB)) 
        dans l’ordre d’appel. 
        
        Propriete 4. Un candidat hors-secteur boursier qui a le rang r 
        dans le classement pédagogique ne peut être doublé que par des candidats du secteur, 
        boursiers ou non, et aura donc un rang au plus r +  R * qR ∕ (100 - qR) dans l’ordre d’appel.
         */

        for (GroupeClassement gc : entree.groupesClassements) {
            int cGpCod = gc.cGpCod;

            OrdreAppel oa = sortie.ordresAppel.get(cGpCod);
            if (oa == null) {
                throw new VerificationException("Donnée de sortie manquante");
            }

            /* mappe chaque candidat (gCnCod) vers son rang d'appel */
            Map<Integer, Integer> rangsAppel = new HashMap<>();
            for (CandidatClasse cc : oa.candidats) {
                int gCnCod = cc.gCnCod;
                if (rangsAppel.containsKey(gCnCod)) {
                    throw new VerificationException("G_CN_COD dupliqué");
                }
                rangsAppel.put(cc.gCnCod, cc.rangAppel);
            }

            for (VoeuClasse v : gc.voeuxClasses) {
                int gCnCod = v.gCnCod;
                Integer rangAppel = rangsAppel.get(gCnCod);
                if (rangAppel == null) {
                    throw new VerificationException("G_CN_COD manquant");
                }
                if (v.getRangAppel() != rangAppel) {
                    throw new VerificationException("données inconsistentes");
                }
            }

            verifierP1(gc);
            verifierP2(gc);
            verifierP3(gc);
            verifierP4(gc);

        }

    }

    /* Verif de P1. Pour tout k, au moins qB pourcents des k premiers candidats sont boursiers ;
        ou sinon, aucun candidat parmi Ck+1,…,Cn n’est boursier. 
     */
    void verifierP1(GroupeClassement g) throws VerificationException {
        /* on réordonne les voeux par ordre d'appel */
        g.voeuxClasses.sort((VoeuClasse v1, VoeuClasse v2) -> v1.getRangAppel() - v2.getRangAppel());

        /* on vérifie P1 */
        Set<Integer> candidats = new HashSet<>();
        Set<Integer> boursiers = new HashSet<>();
        boolean seulementDesNonBoursiers = false;
        for (VoeuClasse v : g.voeuxClasses) {
            candidats.add(v.gCnCod);
            if (v.estBoursier()) {
                boursiers.add(v.gCnCod);
                if (seulementDesNonBoursiers) {
                    throw new VerificationException("Violation P1");
                }
            }
            int tauxEffectif = 100 * boursiers.size() / candidats.size();
            if (tauxEffectif < g.tauxMinBoursiersPourcents) {
                seulementDesNonBoursiers = true;
            }
        }
    }

    /* Verif de P2: Un candidat à la fois boursier et du secteur
        (ou dans un groupe sans taux de candidats du secteur)
        qui a le rang r dans le classement pédagogique 
        n’est jamais doublé par personne et aura donc un rang inférieur 
        ou égal à r dans l’ordre d’appel. 
     */
    void verifierP2(GroupeClassement g) throws VerificationException {
        /* on vérifie P2 */
        for (VoeuClasse v1 : g.voeuxClasses) {
            if (v1.estBoursier()
                    && (g.tauxMinDuSecteurPourcents == 0 || v1.estDuSecteur())) {
                if (v1.getRangAppel() > v1.rang) {
                    throw new VerificationException(
                            "Boursier du secteur " + v1.gCnCod
                            + "avec rang d'appel qui décroit dans " + g);
                }
                for (VoeuClasse v2 : g.voeuxClasses) {
                    if (v2.rang > v1.rang && v2.getRangAppel() < v1.getRangAppel()) {
                        throw new VerificationException(
                                "Boursier du secteur " + v1
                                + " dépassé par " + v2
                                + " dans " + g);
                    }
                }
            }
        }
    }

    /* Verif de P3: Un candidat du secteur
        et non boursier qui a le rang r dans le classement pédagogique 
        ne peut-être doublé que par des boursiers et aura un rang compris 
        entre r et r + 1 + r * qB∕(100 - qB)) 
        dans l’ordre d’appel.  
     */
    void verifierP3(GroupeClassement g) throws VerificationException {
        /* on vérifie P3 suelement si le taux min de non boursiers est contraignant */

        if (g.tauxMinBoursiersPourcents >= 100) {
            return;
        }

        for (VoeuClasse v1 : g.voeuxClasses) {

            if (!v1.estBoursier()
                    && (v1.estDuSecteur() || g.tauxMinDuSecteurPourcents == 0)) {
                int diminutionMax = 1 + v1.rang * g.tauxMinBoursiersPourcents / (100 - g.tauxMinBoursiersPourcents);
                if (v1.getRangAppel() > v1.rang + diminutionMax) {
                    throw new VerificationException(
                            "Non boursier du secteur " + v1
                            + "avec rang  qui diminue trop dans " + g);
                }
                for (VoeuClasse v2 : g.voeuxClasses) {
                    if (!v2.estBoursier() && v2.rang > v1.rang && v2.getRangAppel() < v1.getRangAppel()) {
                        throw new VerificationException(
                                "Non-boursier " + v2
                                + " dépassant un candidat du secteur dans  " + g);
                    }
                }
            }
        }
    }

    /* Verif de P4: Un candidat hors-secteur boursier qui a le rang r 
        dans le classement pédagogique ne peut être doublé que par des candidats du secteur, 
        boursiers ou non, et aura donc un rang au plus r +  R * qR ∕ (100 - qR) 
    dans l’ordre d’appel. 
     */
    void verifierP4(GroupeClassement g) throws VerificationException {
        /* on vérifie P4 seulement si il y a des candidats hors-secteurs */
        if (g.tauxMinDuSecteurPourcents == 0) {
            return;
        }

        for (VoeuClasse v1 : g.voeuxClasses) {
            if (v1.estBoursier() && !v1.estDuSecteur()) {
                int diminutionMax = 1 + v1.rang * g.tauxMinDuSecteurPourcents / (100 - g.tauxMinDuSecteurPourcents);
                if (v1.getRangAppel() > v1.rang + diminutionMax) {
                    throw new VerificationException(
                            "Candidat hors-secteur boursier " + v1.gCnCod
                            + " avec rang  qui diminue trop dans le groupe " + g.cGpCod);
                }
                for (VoeuClasse v2 : g.voeuxClasses) {
                    if (!v2.estDuSecteur() && v2.rang > v1.rang && v2.getRangAppel() < v1.getRangAppel()) {
                        throw new VerificationException(
                                "Candidat hors-secteur " + v2.gCnCod
                                + " dépassant le boursier hors-secteur " + v1.gCnCod
                                + "dans le groupe " + g.cGpCod
                        );
                    }
                }
            }
        }
    }

    private static void log(String msg) {
        LOGGER.info(msg);
    }

    private static final Logger LOGGER = Logger.getLogger(VerificationsResultatsAlgoOrdreAppel.class.getSimpleName());

}
