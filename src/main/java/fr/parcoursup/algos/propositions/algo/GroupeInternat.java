
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
package fr.parcoursup.algos.propositions.algo;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class GroupeInternat implements Serializable {

    /* Le triplet identifiant le groupe de classement internat dans la base de données
     */
    @NotNull
    public final GroupeInternatUID id;

    /* le nombre total de places */
    private int capacite;
    public int getCapacite() { return capacite; }
    @SuppressWarnings("unused")
    public void setCapacite(int capacite) { this.capacite = capacite; }

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

     /* la liste des voeuxEnAttente du groupe.
    Après le calcul de la position initiale d'admission
    cette liste est triée par ordre de classement internat */
    public final transient List<Voeu> voeuxEnAttente = new ArrayList<>();

    /* true si et seulement si la position maximale d'admission a été calculée */
    private transient boolean estInitialise = false;

    /* les candidats déjà affectés dans le groupe */
    private final transient Set<Integer> candidatsAffectes
            = new HashSet<>();

    /* les candidats en attente dans le groupe */
    public final transient Set<Integer> candidatsEnAttente
            = new HashSet<>();

    public GroupeInternat(
            @NotNull GroupeInternatUID id,
            int capacite
    ) throws VerificationException {
        if (capacite < 0) {
            throw new VerificationException(VerificationExceptionMessage.GROUPE_INTERNAT_INCOHERENCE_CONSTRUCTEUR);
        }
        this.id = id;
        this.capacite = capacite;
    }

    public GroupeInternat(GroupeInternat o) throws VerificationException {
        this(o.id, o.capacite);
        positionAdmission = 0;
        positionMaximaleAdmission = 0;
        estInitialise = false;
    }

    void ajouterVoeuEnAttenteDeProposition(Voeu voe) throws VerificationException {
        if (estInitialise) {
            throw new VerificationException(VerificationExceptionMessage.GROUPE_INTERNAT_DEJA_INITIALISE);
        }
        if (voeuxEnAttente.contains(voe)) {
            throw new VerificationException(VerificationExceptionMessage.GROUPE_INTERNAT_VOEU_EN_DOUBLON);
        }
        voeuxEnAttente.add(voe);
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
        voeuxEnAttente.clear();
        barresInternatAffichees.clear();
        barresAppelAffichees.clear();
        estInitialise = false;
    }

    public boolean estAffecte(int gCnCod) {
        return candidatsAffectes.contains(gCnCod);
    }

    /* initialise la position d'admission à son maximum
    Bmax dans le document de référence. 
    
    La position maximale d'admission fixe une borne supérieure
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
    public int calculerBorneLineaire(Parametres parametres) throws VerificationException {
        /* On utilise les notations du document de référence 2018 */
        long enAttente = candidatsEnAttente.size();
        long affectes = candidatsAffectes.size();
        if (enAttente + affectes > Integer.MAX_VALUE) {
            throw new VerificationException(VerificationExceptionMessage.VERIFICATION_ORDRE_APPEL_TAILLE_DONNEES);
        }

        final long nbPlacesOuvertes = calculerNbPlacesOuvertes(
                enAttente + affectes, 
                capacite, 
                parametres);

        long contingentAdmission = Long.max(0, nbPlacesOuvertes - candidatsAffectes.size());

        if (nbPlacesOuvertes > enAttente + affectes
                || contingentAdmission > candidatsEnAttente.size()
                || contingentAdmission < 0) {
            throw new VerificationException(VerificationExceptionMessage.GROUPE_INTERNAT_CONTINGENT_ADMISSION);
        }

        if (contingentAdmission == 0) {
            return 0;
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
 /* Dans les cas restants, on met à jour.*/
                if ((voe.rangInternat != dernierRangEligible) && !voe.internatDejaObtenu()) {
                    dernierRangEligible = voe.rangInternat;
                    compteurCandidat++;
                }
            }
            return dernierRangEligible;
        }
    }

    /* notations du document de présentation des algorithmes.
    m est le nombre de candidats
    l est la capacité
    t le nombre de jours
     */
    private long calculerNbPlacesOuvertes(long m, long l, Parametres parametres) throws VerificationException {
        int t = parametres.nbJoursCampagne;
        if (t <= 0) {
            throw new VerificationException(VerificationExceptionMessage.GROUPE_INTERNAT_DATE_ANTERIEURE);
        }
        if (l < 0) {
            throw new VerificationException(VerificationExceptionMessage.GROUPE_INTERNAT_CAPACITE_NEGATIVE, this);
        }

        if (m <= l) {
            return m;
        } else if (t == 1) {
            /* le premier jour on s'en tient aux lits disponibles */
            return l;
        } else if (t <= parametres.nbJoursCampagneDatePivotInternats) {
            /* jusqu'à la date pivot, on élargit progressivement
            l'assiette */
            return l + (m - l) * (t - 1) / parametres.nbJoursCampagneDatePivotInternats;
        } else {
            /* finalement, l'assiette est maximale */
            return m;
        }
    }

    public int calculerBornePredicteur(Parametres parametres) {
        if (parametres.nbJoursCampagne >= parametres.nbJoursCampagneDatePivotInternats) {
            return Integer.MAX_VALUE;
        } else {
            int compteurCandidat = 0;
            int dernierRangEligible = 0;
            for (Voeu voe : voeuxTriesParClassementInternat()) {
                /* sortie de boucle: le nombre de places vacantes est atteint */
                if (compteurCandidat == nbPlacesVacantes()) {
                    break;
                }
                /* Plusieurs cas où le voeu sera ignoré au sens 
                où il ne change pas la valeur du dernier rang comptabilisé
                    et du nombre de candidat comptés dans le contingent.
                1. on ignore les voeuxEnAttente qui ne sont pas sous la barre
                2. on a vu le même candidat au passage précédent dans la boucle 
                3. l'internat est déjà obtenu par le candidat */
                final boolean ignorer
                        = (voe.ordreAppel > voe.getGroupeAffectation().estimationRangDernierAppeleADatePivot)
                        || (voe.rangInternat == dernierRangEligible)
                        || (voe.internatDejaObtenu());
                if (!ignorer) {
                    dernierRangEligible = voe.rangInternat;
                    compteurCandidat++;
                }
            }
            return dernierRangEligible;
        }
    }

    public void initialiserPositionAdmission(Parametres parametres) throws VerificationException {

        if(finReservationPlacesInternat()) {
            positionMaximaleAdmission = Integer.MAX_VALUE;
        } else {
        /* Premiere limitation: on calcule le nombre de candidats éligibles à une admission
        dans l'internat aujourd'hui, stocké dans la variable nbPlacesOuvertes.
         */
            int dernierRangEligibleLineaire = calculerBorneLineaire(parametres);

        /* seconde limitation.
            On itère les candidats en attente d'internat 
            et qui sont sous la barre du dernier appelé dans leur groupe
            jusqu'à arriver à la capacité résiduelle
            Remarque: il peut y avoir plusieurs voeuxEnAttente pour
            le même candidat, et les voeuxEnAttente sont triés par rang internat,
            donc les voeuxEnAttente d'un même candidat sont consécutifs */
            int dernierRangEligiblePredicteur = calculerBornePredicteur(parametres);

            /* la position maximale d'admission est fixée au minimumm des deux barres */
            positionMaximaleAdmission
                    = Math.min(dernierRangEligibleLineaire, dernierRangEligiblePredicteur);
        }
        positionAdmission = positionMaximaleAdmission;
        estInitialise = true;
    }

    /* la liste des groupes de classement concernés par cet internat */
    public Set<GroupeAffectation> groupesConcernes() throws VerificationException {
       checkVoeuxOntGroupesNonNuls();
       return voeux().stream()
                .map(Voeu::getGroupeAffectation)
                .collect(Collectors.toSet());
    }

    private void checkVoeuxOntGroupesNonNuls() throws VerificationException {
        if (voeuxEnAttente.stream().anyMatch(v ->
                v.getGroupeAffectation() == null
                        || (v.avecInternatAClassementPropre() && v.getInternat() == null))) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_GROUPE_NULL);
        }
    }

    private boolean finReservationPlacesInternat() throws VerificationException {
        checkVoeuxOntGroupesNonNuls();
        return voeuxEnAttente.stream()
                .map(Voeu::getGroupeAffectation)
                .anyMatch(GroupeAffectation::getFinDeReservationPlacesInternats);
    }

    /* Met à jour la position d'admission si nécessaire.
    Renvoie true si la position d'admission a été effectivement mise à jour */
    public boolean mettreAJourPositionAdmission() throws VerificationException {

        if (!estInitialise) {
            throw new VerificationException(VerificationExceptionMessage.GROUPE_INTERNAT_POSITION_NON_INITIALISEE);
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

            /* le candidat a déjà l'internat, ignoré pour la mise a jour de la pos admission
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
        voeuxEnAttente.sort(Comparator.comparingInt((Voeu v) -> v.rangInternat));
        return Collections.unmodifiableList(voeuxEnAttente);
    }

    /* trie les voeuxEnAttente dans l'ordre d'appel */
    public List<Voeu> voeuxTriesParOrdreAppel() {
        voeuxEnAttente.sort(Comparator.comparingInt((Voeu v) -> v.ordreAppel));
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
        id = new GroupeInternatUID(1, 0,0);
        capacite = 0;
    }

}
