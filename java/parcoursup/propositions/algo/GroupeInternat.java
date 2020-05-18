
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
package parcoursup.propositions.algo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.annotation.XmlTransient;
import parcoursup.exceptions.VerificationException;

public class GroupeInternat {

    /* Le triplet identifiant le groupe de classement internat dans la base de données
     */
    public final GroupeInternatUID id;

    /* le nombre total de places */
    public final int capacite;

    /* le nombre de places vacantes dans cet internat */
    public int nbPlacesVacantes() {
        /* On seuille à 0,
        en cas de réduction du nombre de lits conduisant à une différence négative */
        return Integer.max(0, capacite - candidatsAffectes.size());
    }

    /* la position d'admission dans cet internat, calculée par l'algorithme */
    private int positionAdmission = 0;

    public int getPositionAdmission() {
        return positionAdmission;
    }

    /* la position maximale d'admission dans cet internat, calculée par l'algorithme */
    private int positionMaximaleAdmission = 0;

    public int getPositionMaximaleAdmission() {
        return positionMaximaleAdmission;
    }

    /* affichages internat, groupe par groupe */
    public final Map<GroupeAffectationUID, Integer> barresInternatAffichees = new HashMap<>();
    public final Map<GroupeAffectationUID, Integer> barresAppelAffichees = new HashMap<>();

    /* la liste des groupes de classement concernés par cet internat */
    @XmlTransient
    public final Set<GroupeAffectation> groupesConcernes
            = new HashSet<>();

    /* la liste des voeuxEnAttente du groupe.
    Après le calcul de la position initiale d'admission
    cette liste est triée par ordre de classement internat */
    public final List<Voeu> voeuxEnAttente = new ArrayList<>();

    /* true si et seulement si la position maximale d'admission a été calculée */
    private boolean estInitialise = false;

    /* les candidats déjà affectés dans le groupe */
    private final Set<Integer> candidatsAffectes
            = new HashSet<>();

    /* les candidats en attente dans le groupe */
    public final Set<Integer> candidatsEnAttente
            = new HashSet<>();

    public GroupeInternat(
            GroupeInternatUID id,
            int nbPlacesTotal
    ) throws VerificationException {
        if (nbPlacesTotal < 0) {
            throw new VerificationException("Incohérence dans les paramètres du constructeur de GroupeInternat");
        }
        this.id = id;
        this.capacite = nbPlacesTotal;
    }

    public GroupeInternat(GroupeInternat o) throws VerificationException {
        this(o.id, o.capacite);
    }

    void ajouterVoeuEnAttenteDeProposition(Voeu voe) throws VerificationException {
        if (estInitialise) {
            throw new VerificationException("Groupe déjà initialisé");
        }
        if (voeuxEnAttente.contains(voe)) {
            throw new VerificationException("Voeu en doublon");
        }
        voeuxEnAttente.add(voe);
        groupesConcernes.add(voe.groupe);
        if (!candidatsAffectes.contains(voe.id.gCnCod)) {
            candidatsEnAttente.add(voe.id.gCnCod);
        }
    }

    /* ajoute un candidat affecté.
    Supprime le candidat de la liste des candidats en attente , si il y a lieu*/
    public void ajouterCandidatAffecte(int gCnCod) {
        candidatsAffectes.add(gCnCod);
        candidatsEnAttente.remove(gCnCod);
    }

    public void reinitialiser() {
        candidatsAffectes.clear();
        candidatsEnAttente.clear();
        groupesConcernes.clear();
        voeuxEnAttente.clear();
        barresInternatAffichees.clear();
        barresAppelAffichees.clear();
        estInitialise = false;
    }

    public boolean estAffecte(int gCnCod) {
        return candidatsAffectes.contains(gCnCod);
    }

    /* initialise la position d'admission à son maximum
    Bmax dans le document de référence. A l'issu de cet appel,
    les voeuxEnAttente sont triés par classement internat,
    les meilleurs en premier. */
    public void initialiserPositionAdmission(Parametres parametres) throws VerificationException {

        /* La position maximale d'admission fixe une borne supérieure
        sur le rang à l'internat nécessaire pour obtenir une rpoposition à l'internat.
        Cela permet de réguler la vitesse d'ouverture de l'internat.
        
        La première mesure consiste à ouvrir la liste internat
        progressivement linéairement dans le temps:
        le premier jour seul la liste principale est éligible,
        à la date de pivot tous les candidats sont éligibles.

        La seconde mesure se base sur une estimation du rang du dernier ppelé,
        et réserve des places d'internat aux candidats les mieux classés à l'internat
        parmi ceux dont dont le classement pédagogique est sous la barre estimée.
        
        NB: en 2018 les proviseurs pouvaient réguler cette vitesse d'ouverture mais
        suite aux retours d'expérience, cette fonction a été supprimée.
        
        Plus de détails sont diponibles dans les documents décrivant les algorithmes.
         */
 /* on calcule le nombre de candidats éligibles à une admission
        dans l'internat aujourd'hui, stocké dans la variable assietteAdmission.
        On utilise les notations du document de référence 2018 */
        int m = candidatsEnAttente.size() + candidatsAffectes.size();
        int l = capacite;
        int t = parametres.nbJoursCampagne;

        final int assietteAdmission;

        if (m <= l) {
            assietteAdmission = m;
        } else if (t == 1) {
            /* le premier jour on s'en tient aux lits disponibles */
            assietteAdmission = l;
        } else if (t <= parametres.nbJoursCampagneDatePivotInternats) {
            /* jusqu'à la date pivot, on élargit progressivement
            l'assiette */
            assietteAdmission
                    = l + (m - l) * (t - 1) / parametres.nbJoursCampagneDatePivotInternats;
        } else {
            /* finalement, l'assiette est maximale */
            assietteAdmission = m;
        }

        int contingentAdmission = Integer.max(0, assietteAdmission - candidatsAffectes.size());

        if (t <= 0) {
            throw new VerificationException("Impossible d'exécuter l'algorithme"
                    + " à une date antérieure au début de la campagne,"
                    + " veuillez vérifier les données.");
        }
        if (l < 0) {
            throw new VerificationException("L'internat " + this + " a une capacité"
                    + " négative veuillez vérifier les données.");
        }
        if (assietteAdmission > m
                || contingentAdmission > candidatsEnAttente.size()
                || contingentAdmission < 0) {
            throw new VerificationException("Problème de calcul du contingent d'admission.");
        }

        if (contingentAdmission == 0) {
            positionMaximaleAdmission = 0;
        } else {

            /* on itère les candidats en attente d'internat jusqu'à arriver
            au contingent calculé. Remarque: il peut y avoir plusieurs voeuxEnAttente pour
            le même candidat, et les voeuxEnAttente sont triés par rang internat,
            donc les voeuxEnAttente d'un même candidat sont consécutifs */
            int compteurCandidat = 0;
            int dernierRangEligible = 0;

            for (Voeu voe : voeuxTriesParClassementInternat()) {

                /* sortie de boucle: le contingent est atteint */
                if (compteurCandidat == contingentAdmission) {
                    break;
                }

                /* deux cas où le voeu ne change pas la valeur du dernier rang comptabilisé
                et du nombre de candidat comptés dans le contingent.
                 Premier cas: on a vu le même candidat au passage précédent dans la boucle.
                Second cas: l'internat est déjà obtenu par le candidat*/
                if (voe.rangInternat == dernierRangEligible || voe.internatDejaObtenu()) {
                    continue;
                }

                /* Dans les cas restants, on met à jour.*/
                dernierRangEligible = voe.rangInternat;
                compteurCandidat++;

            }

            /* seconde limitation.
            On itère les candidats en attente d'internat 
            et qui sont sous la barre du dernier appelé dans leur groupe
            jusqu'à arriver à la capacité résiduelle
            Remarque: il peut y avoir plusieurs voeuxEnAttente pour
            le même candidat, et les voeuxEnAttente sont triés par rang internat,
            donc les voeuxEnAttente d'un même candidat sont consécutifs */
            if (parametres.nbJoursCampagne >= parametres.nbJoursCampagneDatePivotInternats) {
                positionMaximaleAdmission = dernierRangEligible;
            } else {
                int compteurCandidat2 = 0;
                int dernierRangEligible2 = 0;

                for (Voeu voe : voeuxTriesParClassementInternat()) {

                    /* sortie de boucle: le nombre de places vacantes est atteint */
                    if (compteurCandidat2 == nbPlacesVacantes()) {
                        break;
                    }

                    /* on ignore les voeuxEnAttente qui ne sont pas sous la barre */
                    if (voe.ordreAppel > voe.groupe.estimationRangDernierAppeleADatePivot) {
                        continue;
                    }

                    /* deux cas où le voeu ne change pas la valeur du dernier rang comptabilisé
                    et du nombre de candidat comptés dans le contingent.
                    Premier cas: on a vu le même candidat au passage précédent dans la boucle */
                    if (voe.rangInternat == dernierRangEligible2) {
                        continue;
                    }

                    /* Second cas: l'internat est déjà obtenu par le candidat */
                    if (voe.internatDejaObtenu()) {
                        continue;
                    }

                    /* Dans les cas restants, on met à jour.*/
                    dernierRangEligible2 = voe.rangInternat;
                    compteurCandidat2++;

                }

                /* la position maximale d'admission est fixée au minimumm des deux barres */
                positionMaximaleAdmission
                        = Math.min(dernierRangEligible, dernierRangEligible2);

            }

        }

        positionAdmission = positionMaximaleAdmission;

        estInitialise = true;

    }

    /* Met à jour la position d'admission si nécessaire.
    Renvoie true si la position d'admission a été effectivement mise à jour */
    public boolean mettreAJourPositionAdmission() throws VerificationException {

        if (!estInitialise) {
            throw new VerificationException("La position doit être initialisée au préalable");
        }


        /* on compte le nombre de propositions à faire.
        En cas de dépassement, on met à jour la position d'admission */
        int comptePlacesProposees = 0;
        int dernierCandidatComptabilise = -1;

        for (Voeu voe : voeuxTriesParClassementInternat()) {

            /* si on a dépassé la position d'admission, on arrête */
            if (voe.rangInternat > positionAdmission) {
                return false;
            }

            /* les propositions à un même candidat comptent pour une seule place */
            if (voe.id.gCnCod == dernierCandidatComptabilise) {
                continue;
            }

            /* le candidat a déjà l'internat, ignoré pour la mise a jour de la pos admision            
            et du rang sur liste d'attente internat */
            if (voe.internatDejaObtenu()) {
                continue;
            }

            /* si ok pour déclencher la proposition, on met à jour */
            if (voe.formationDejaObtenue()
                    || voe.estProposition()) {

                comptePlacesProposees++;
                dernierCandidatComptabilise = voe.id.gCnCod;

                if (comptePlacesProposees > nbPlacesVacantes()) {
                    /* en cas de surcapacité, il faut diminuer la position d'admission */
                    positionAdmission = voe.rangInternat - 1;
                    /* on renvoie true pour signaler la mise à jour de la posotion d'admission,
                    ce qui entraîne une itération supplémentaire de la boucle principale
                     */
                    return true;
                }
            }
        }
        return false;
    }

    /* trie les voeuxEnAttente dans l'ordre d'appel */
    public List<Voeu> voeuxTriesParClassementInternat() {
        voeuxEnAttente.sort((Voeu v1, Voeu v2) -> v1.rangInternat - v2.rangInternat);
        return Collections.unmodifiableList(voeuxEnAttente);
    }

    /* trie les voeuxEnAttente dans l'ordre d'appel */
    public List<Voeu> voeuxTriesParOrdreAppel() {
        voeuxEnAttente.sort((Voeu v1, Voeu v2) -> v1.ordreAppel - v2.ordreAppel);
        return Collections.unmodifiableList(voeuxEnAttente);
    }

    public List<Voeu> voeux() {
        return Collections.unmodifiableList(voeuxEnAttente);
    }

    @Override
    public String toString() {
        return id.toString();
    }

    private GroupeInternat() {
        id = null;
        capacite = 0;
    }

}
