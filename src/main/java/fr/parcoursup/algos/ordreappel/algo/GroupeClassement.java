
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
package fr.parcoursup.algos.ordreappel.algo;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;

import java.io.Serializable;
import java.util.*;

public class GroupeClassement implements Serializable {

    /*le code identifiant le groupe de classement dans la base de données 
        Remarque: un même groupe de classement peut être commun à plusieurs formations
     */
    public final int cGpCod;

    /* le taux minimum de boursiers dans ce groupe d'appel 
        (nombre min de boursiers pour 100 candidats) */
    public final int tauxMinBoursiersPourcents;

    /* le taux minimum de candidats du secteur dans ce groupe d'appel 
        (nombre min de candidats du secteur pour 100 candidats) */
    public final int tauxMinDuSecteurPourcents;

    /* la liste des candidats du groupe de classement */
    public final List<VoeuClasse> voeuxClasses = new ArrayList<>();

    public GroupeClassement(
            int cGpCod,
            int tauxMinBoursiersPourcents,
            int tauxMinResidentsPourcents) throws VerificationException {

        if (tauxMinBoursiersPourcents < 0
                || tauxMinBoursiersPourcents > 100
                || tauxMinResidentsPourcents < 0
                || tauxMinResidentsPourcents > 100) {
            throw new VerificationException(VerificationExceptionMessage.GROUPE_CLASSEMENT_TAUX_INCOHERENTS);
        }

        this.cGpCod = cGpCod;
        this.tauxMinBoursiersPourcents = tauxMinBoursiersPourcents;
        this.tauxMinDuSecteurPourcents = tauxMinResidentsPourcents;
    }

    public void ajouterVoeu(VoeuClasse v) {
        voeuxClasses.add(v);
    }

    /* extrait le meilleur voeu des différentes files d'attente */
    private VoeuClasse selectionnerMeilleurVoeu(
            boolean contrainteTauxBoursier,
            boolean contrainteTauxResident,
            EnumMap<VoeuClasse.TypeCandidat, Queue<VoeuClasse>> filesAttente) {

        /* on fait la liste des candidats satisfaisant
                les deux contraintes à la fois, ordonnée par rang de classement */
        PriorityQueue<VoeuClasse> eligibles = new PriorityQueue<>();

        for (Queue<VoeuClasse> queue : filesAttente.values()) {
            if (!queue.isEmpty()) {
                VoeuClasse voe = queue.peek();
                if ((voe.estBoursier() || !contrainteTauxBoursier)
                        && (voe.estDuSecteur() || !contrainteTauxResident)) {
                    eligibles.add(voe);
                }
            }
        }

        /* stocke le meilleur candidat à appeler tout en respectant
            les deux contraintes si possible 
            ou à défaut seulement la contrainte sur le taux boursier */
        final VoeuClasse meilleur;

        if (!eligibles.isEmpty()) {
            meilleur = eligibles.peek();
        } else {
            /* la liste peut être vide dans le cas où les deux contraintes 
                ne peuvent être satisfaites à la fois. 
                Dans ce cas nécessairement il y a une contrainte sur chacun des deux taux 
                (donc au moins un boursier non encore sélectionné) 
                et il ne reste plus de boursier du secteur, 
                donc il reste au moins un boursier hors-secteur */
            
            if (!(contrainteTauxBoursier && contrainteTauxResident)) {
                //non atteignable
                throw new AssertionError("Si aucun candidat n'est éligible les deux contraintes sont normalement actives");
            }
            if (!filesAttente.get(VoeuClasse.TypeCandidat.BOURSIER_DU_SECTEUR).isEmpty()) {
                //non atteignable
                throw new AssertionError("Si aucun candidat n'est éligible il n'est censé rester aucun boursier du secteur");
            }
            if (filesAttente.get(VoeuClasse.TypeCandidat.BOURSIER_HORS_SECTEUR).isEmpty()) {
                //non atteignable
                throw new AssertionError("Si aucun candidat n'est éligible il est censé rester au moins un boursier hors secteur");
            }

            Queue<VoeuClasse> candidatsBoursierNonResident
                    = filesAttente.get(VoeuClasse.TypeCandidat.BOURSIER_HORS_SECTEUR);

            meilleur = candidatsBoursierNonResident.peek();
        }

        if (meilleur == null) {
            //non atteignable
            throw new AssertionError("Au moins un candidat doit avoir été sélectionné");
        }

        /* suppression du candidat choisi de sa file d'attente */
        Queue<VoeuClasse> queue = filesAttente.get(meilleur.typeCandidat);
        VoeuClasse poll = queue.poll();
        if (poll != meilleur) {
            throw new AssertionError("Le meilleur est toujours en tête de la file à dépiler");
        }

        return meilleur;
    }

    /* calcul de l'ordre d'appel */
    public OrdreAppel calculerOrdreAppel() throws VerificationException {

        /* prévention d'un depassement arithmétique possible théoriquement */
        if (voeuxClasses.size() >= Integer.MAX_VALUE) {
            throw new VerificationException(VerificationExceptionMessage.GROUPE_CLASSEMENT_POSSIBLE_DEPASSEMENT_ARITHMETIQUE);
        }

        /* on crée autant de listes de candidats que de types de candidats */
        EnumMap<VoeuClasse.TypeCandidat, Queue<VoeuClasse>> filesAttente
                = new EnumMap<>(VoeuClasse.TypeCandidat.class);

        for (VoeuClasse.TypeCandidat type : VoeuClasse.TypeCandidat.values()) {
            filesAttente.put(type, new LinkedList<>());
        }

        /* Chaque voeu classé est ventilé dans la liste correspondante, 
        en fonction du type du candidat. 
        Les quatre listes obtenues sont ordonnées par rang de classement, 
        comme l'est la liste voeuxClasses. */
        long nbBoursiersTotal = 0;
        long nbResidentsTotal = 0;

        /* on trie les candidats par classement, 
        les candidats les mieux classés en tête de liste  */
        voeuxClasses.sort(Comparator.comparingInt((VoeuClasse v) -> v.rang));

        for (VoeuClasse voe : voeuxClasses) {

            /* on ajoute le voeu à la fin de la file (FIFO) correspondante */
            filesAttente.get(voe.typeCandidat).add(voe);

            if (voe.estBoursier()) {
                nbBoursiersTotal++;
            }
            if (voe.estDuSecteur()) {
                nbResidentsTotal++;
            }
        }

        long nbAppeles = 0;
        long nbBoursiersAppeles = 0;
        long nbResidentsAppeles = 0;

        /* la boucle ajoute les candidats un par un à la liste suivante,
            dans l'ordre d'appel */
        List<VoeuClasse> ordreAppel = new ArrayList<>();

        while (ordreAppel.size() < voeuxClasses.size()) {

            /* on calcule lequel ou lesquels des critères boursiers et candidats du secteur 
                contraignent le choix du prochain candidat dans l'ordre d'appel */
            boolean contrainteTauxBoursier
                    = (nbBoursiersAppeles < nbBoursiersTotal)
                    && (nbBoursiersAppeles * 100 < tauxMinBoursiersPourcents * (1 + nbAppeles));

            boolean contrainteTauxResident
                    = (nbResidentsAppeles < nbResidentsTotal)
                    && (nbResidentsAppeles * 100 < tauxMinDuSecteurPourcents * (1 + nbAppeles));

            final VoeuClasse meilleur = selectionnerMeilleurVoeu(
                    contrainteTauxBoursier,
                    contrainteTauxResident,
                    filesAttente);

            /* ajout du meilleur candidat à l'ordre d'appel*/
            ordreAppel.add(meilleur);

            /* mise à jour des compteurs */
            nbAppeles++;

            if (meilleur.estBoursier()) {
                nbBoursiersAppeles++;
            }
            if (meilleur.estDuSecteur()) {
                nbResidentsAppeles++;
            }
        }

        /* mise à jour des ordres d'appel */
        int rangAppel = 1;
        for (VoeuClasse v : ordreAppel) {
            v.setRangAppel(rangAppel);
            rangAppel++;
        }

        /* retourne les candidats classés dans l'ordre d'appel */
        return new OrdreAppel(ordreAppel);

    }

    public void verifierCoherenceTaux() throws VerificationException {
        if (tauxMinBoursiersPourcents < 0 || tauxMinBoursiersPourcents > 100) {
            throw new VerificationException(VerificationExceptionMessage.VERIFICATION_ENTREE_ALGO_ORDRE_APPEL_TAUX_BOURSIER);
        }
        if (tauxMinDuSecteurPourcents < 0 || tauxMinDuSecteurPourcents > 100) {
            throw new VerificationException(VerificationExceptionMessage.VERIFICATION_ENTREE_ALGO_ORDRE_APPEL_TAUX_HORS_SECTEUR);
        }
    }

    public void verifierRangs() throws VerificationException {
        Set<Integer> rangs = new HashSet<>();
        for (VoeuClasse v : voeuxClasses) {
            if (v.rang <= 0) {
                throw new VerificationException(VerificationExceptionMessage.VERIFICATION_ENTREE_ALGO_ORDRE_APPEL_RANGS_VOEUX);
            }
            if (rangs.contains(v.rang)) {
                throw new VerificationException(VerificationExceptionMessage.VERIFICATION_ENTREE_ALGO_ORDRE_APPEL_DUPLICATION_VOEUX);
            } else {
                rangs.add(v.rang);
            }
        }
    }

    @Override
    public String toString() {
        return "cGpCod=" + cGpCod;
    }
}
