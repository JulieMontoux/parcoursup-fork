
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class GroupeInternat implements Serializable {

    /**
     *  Le triplet identifiant le groupe de classement internat dans la base de données
     */
    @NotNull
    public final GroupeInternatUID id;

    /**
     *  le nombre total de places dans ce groupe d'affectation internat
     */
    private int capacite;
    public int getCapacite() { return capacite; }
    @SuppressWarnings("unused")
    public void setCapacite(int capacite) throws VerificationException {
        if(capacite < 0) {
            throw new VerificationException(VerificationExceptionMessage.GROUPE_INTERNAT_CAPACITE_NEGATIVE, this.id);
        }
        this.capacite = capacite;
    }

    /**
     * @return le nombre de places vacantes dans cet internat
     */
    public int nbPlacesVacantes() {
        /* On seuille à 0,
        en cas de réduction du nombre de lits conduisant à une différence négative */
        return Integer.max(0, capacite - candidatsAffectes.size());
    }

    /**
     * la position d'admission dans cet internat, calculée par l'algorithme d'admission
     */
    private int positionAdmission = 0;
    public int getPositionAdmission() {
        return positionAdmission;
    }

    /**
     * la position maximale d'admission dans cet internat
     * calculée par initialiserPositionAdmission
     */
    private int positionMaximaleAdmission = 0;

    /**
     * Renvoie la valeur de la position maximale d'admission.
     * @return la valeur
     */
    public int getPositionMaximaleAdmission() {
        return positionMaximaleAdmission;
    }

    /**
     * affichages internat, groupe par groupe
     */
    public final Map<GroupeAffectationUID, Integer> barresInternatAffichees = new HashMap<>();
    public final Map<GroupeAffectationUID, Integer> barresAppelAffichees = new HashMap<>();

    /**
     * La liste des voeuxEnAttente du groupe.
     */
    private transient List<Voeu> voeuxEnAttente = new ArrayList<>();

    /**
     * flag indiquant si la position maximale d'admission a été calculée,
     * précondition pour l'exécution de l'algorithme d'admission
     */
    private transient boolean estInitialise = false;

    /**
     *  les candidats déjà affectés dans le groupe, au début de la dernière itération
     *  de l'algorithme d'affectation
     */
    private transient Set<Integer> candidatsAffectes
            = new HashSet<>();

    /**
     * Constructeur d'un groupe d'affectation internat
     * @param id l'id du groupe
     * @param capacite la capacité du groupe
     * @throws VerificationException si la capacité est strictement négative
     */
    public GroupeInternat(
            @NotNull GroupeInternatUID id,
            int capacite
    ) throws VerificationException {
        if (capacite < 0) {
            throw new VerificationException(VerificationExceptionMessage.GROUPE_INTERNAT_CAPACITE_NEGATIVE, id);
        }
        this.id = id;
        this.capacite = capacite;
    }

    /**
     * Constructeur par copie
     * @param o le groupe à copier
     * @throws VerificationException si les données ne sont pas correctes.
     */
    public GroupeInternat(GroupeInternat o) throws VerificationException {
        this(o.id, o.capacite);
        positionAdmission = 0;
        positionMaximaleAdmission = 0;
        estInitialise = false;
    }

    /**
     * Ajoute un voeu en attente de proposition dans ce groupe.
     * @param voe le voeu à ajouter
     * @throws VerificationException si les données ne sont pas intègres.
     */
    void ajouterVoeuEnAttenteDeProposition(Voeu voe) throws VerificationException {
        if (estInitialise) {
            throw new VerificationException(VerificationExceptionMessage.GROUPE_INTERNAT_DEJA_INITIALISE);
        }
        if (voeuxEnAttente.contains(voe)) {
            throw new VerificationException(VerificationExceptionMessage.GROUPE_INTERNAT_VOEU_EN_DOUBLON);
        }
        voeuxEnAttente.add(voe);
    }

    /**
     *  Ajoute un candidat affecté (avec proposition d'admission) dans cet internat
     *  et supprime le candidat de la liste des candidats en attente , si il y a lieu.
     * @param gCnCod le code candidat
     */
    public void ajouterCandidatAffecte(int gCnCod) {
        candidatsAffectes.add(gCnCod);
    }

    /**
     * Réinitialise les données du groupe.
     */
    public void reinitialiser() {
        candidatsAffectes.clear();
        voeuxEnAttente.clear();
        barresInternatAffichees.clear();
        barresAppelAffichees.clear();
        estInitialise = false;
    }

    /**
     * détermine si un candidat a actuellement une proposition d'admission dans cet internat
     * @param gCnCod le code candidat
     * @return true si le candidat a une proposition dans cet internat
     */
    public boolean estAffecte(int gCnCod) {
        return candidatsAffectes.contains(gCnCod);
    }

    /**
     * Estime le rang maximal dans le classsement internat d'un candidat susceptible de recevoir
     * une proposition d'admission. Pour cela l'algorithme  itère les candidats en attente d'internat
     * et qui sont sous la barre de l'estimateur du rang du dernier appelé dans leur groupe d'affectation,
     * jusqu'à arriver à la capacité résiduelle.
     * Remarque: il peut y avoir plusieurs voeuxEnAttente pour
     * le même candidat, et les voeuxEnAttente sont triés par rang internat,
     * donc les voeuxEnAttente d'un même candidat sont consécutifs.
     *
     * @param parametres les paramètres de la campagne en cours
     * @return le rang maximal calculé
     */
    public int calculerRangMaximalAdmissionInternatSelonEstimationRangDernierAppele(Parametres parametres) {
        if (parametres.nbJoursCampagne >= parametres.nbJoursCampagneDateFinReservationInternats) {
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
                        = (voe.ordreAppel > voe.getGroupeAffectation().getEstimationRangDernierAppeleADateFinReservationInternats())
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

    /**
     * Initialise la position d'admission et la position maximale d'admission dans ce groupe internat.
     * @param parametres de la campagne en cours.
     * @throws VerificationException si certains voeux contiennent des groupes d'affectation nul
     * ou si l'algorithme est lancé avant le début de la campagne.
     */
    public void initialiserPositionAdmission(Parametres parametres) throws VerificationException {

        if(parametres.nbJoursCampagne <= 0) {
            throw new VerificationException(VerificationExceptionMessage.GROUPE_INTERNAT_DATE_ANTERIEURE);
        }

        if(finReservationPlacesInternat()) {
            positionMaximaleAdmission = Integer.MAX_VALUE;
        } else {
            /* on utilise le prédicteur basé sur le rang du dernier appelé */
            positionMaximaleAdmission
                    = calculerRangMaximalAdmissionInternatSelonEstimationRangDernierAppele(parametres);
        }
        positionAdmission = positionMaximaleAdmission;
        estInitialise = true;
    }

    /**
     * @return l'ensemble des groupes de classement concernés par cet internat
     * @throws VerificationException si un des voeux a un groupe d'affectation null
     */
    public Set<GroupeAffectation> groupesConcernes() throws VerificationException {
       verifierVoeuxOntGroupesNonNuls();
       return voeux().stream()
                .map(Voeu::getGroupeAffectation)
                .collect(Collectors.toSet());
    }

    /**
     * Vérification d'intégrité de l'injection des dépendances dans les données d'entrée
     * @throws VerificationException si un des voeux en attente du groupe n'est pas associé à un groupe d'affectation
     */
    private void verifierVoeuxOntGroupesNonNuls() throws VerificationException {
        if (voeuxEnAttente.stream().anyMatch(v ->
                v.getGroupeAffectation() == null
                        || (v.avecInternatAClassementPropre() && v.getInternat() == null))) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_GROUPE_NULL);
        }
    }

    /**
     * détermine si la réservation de places d'internat est active dans ce groupe internat
     * @return true si inactive, false si active
     * @throws VerificationException si les données d'entrée ne sont pas intègres
     */
    private boolean finReservationPlacesInternat() throws VerificationException {
        verifierVoeuxOntGroupesNonNuls();
        return voeuxEnAttente.stream()
                .map(Voeu::getGroupeAffectation)
                .anyMatch(GroupeAffectation::getFinDeReservationPlacesInternats);
    }

    /**
     * Met à jour la position d'admission.
     * @return true si la position d'admission a changé de valeur.
     * @throws VerificationException si le groupe n'a pas été initialisé au préalable.
     */
    public boolean mettreAJourPositionAdmission() throws VerificationException {

        if (!estInitialise) {
            throw new VerificationException(VerificationExceptionMessage.GROUPE_INTERNAT_POSITION_NON_INITIALISEE);
        }

        /* on compte le nombre de propositions à faire.
        En cas de dépassement, on met à jour la position d'admission */
        int comptePlacesProposees = 0;
        int dernierCandidatComptabilise = -1;

        for (Voeu voe : voeuxTriesParClassementInternat()) {

            /* le candidat a déjà l'internat, ignoré pour la mise a jour de la pos admission */
            if (estAffecte(voe.id.gCnCod)) {
                continue;
            }

            /* plusieurs propositions à un même candidat comptent pour une seule place */
            if (voe.id.gCnCod == dernierCandidatComptabilise) {
                continue;
            }

            /* si on a dépassé la position d'admission sans dépasser la capacité,
             alors il n'est pas nécessaire de mettre à jour la position d'admission */
            if (voe.rangInternat > positionAdmission) {
                return false;
            }

            /* si le voeu est une proposition, on met à jour le compteur du nombre de places proposées */
            if (voe.estProposition()) {

                comptePlacesProposees++;
                dernierCandidatComptabilise = voe.id.gCnCod;

                if (comptePlacesProposees > nbPlacesVacantes()) {
                    /* en cas de surcapacité, il faut diminuer la position d'admission,
                    en deça du rang du voeu ayant créé la surcapacité */
                    positionAdmission = voe.rangInternat - 1;
                    /* on renvoie true pour signaler la mise à jour de la position d'admission,
                    ce qui entraîne une itération supplémentaire de la boucle principale
                     */
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * trie les voeuxEnAttente dans l'ordre du classement internat
     * @return la liste de voeux triés
     */
    public List<Voeu> voeuxTriesParClassementInternat() {
        voeuxEnAttente.sort(Comparator.comparingInt((Voeu v) -> v.rangInternat));
        return Collections.unmodifiableList(voeuxEnAttente);
    }

    /**
     * trie les voeuxEnAttente dans l'ordre d'appel
     * @return la liste de voeux triés
     */
    public List<Voeu> voeuxTriesParOrdreAppel() {
        voeuxEnAttente.sort(Comparator.comparingInt((Voeu v) -> v.ordreAppel));
        return Collections.unmodifiableList(voeuxEnAttente);
    }

    /**
     * Renvoie la liste des voeux en attente dans ce groupe internat
     * @return la liste des voeux
     */
    public List<Voeu> voeux() {
        return Collections.unmodifiableList(voeuxEnAttente);
    }

    @Override
    public String toString() {
        return id.toString();
    }

    /**
     * Utilisé par les désérialisations Json et XML
     */
    private GroupeInternat() {
        id = new GroupeInternatUID(1, 0,0);
        capacite = 0;
    }

    /**
     * Utilisé par la désérialisation Java
     * @param in input stream
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        voeuxEnAttente = new ArrayList<>();
        candidatsAffectes = new HashSet<>();
        estInitialise = false;
    }


    public boolean aUnVoeuDansGroupe(Set<GroupeAffectationUID> groupesNonExportes) {
        return voeuxEnAttente.stream().anyMatch(v -> groupesNonExportes.contains(v.groupeUID));
    }
}
