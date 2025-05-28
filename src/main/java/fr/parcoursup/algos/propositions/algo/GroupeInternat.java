
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
     * affichages internat, groupe par groupe
     */
    public final Map<GroupeAffectationUID, Integer> barresInternatAffichees = new HashMap<>();
    public final Map<GroupeAffectationUID, Integer> barresAppelAffichees = new HashMap<>();

    /**
     * La liste des voeuxEnAttente du groupe.
     */
    private transient List<Voeu> voeuxEnAttente = new ArrayList<>();

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
    }

    /**
     * Ajoute un voeu en attente de proposition dans ce groupe.
     * @param voe le voeu à ajouter
     * @throws VerificationException si les données ne sont pas intègres.
     */
    void ajouterVoeuEnAttenteDeProposition(Voeu voe) throws VerificationException {
        if (voeuxEnAttente.contains(voe)) {
            throw new VerificationException(VerificationExceptionMessage.GROUPE_INTERNAT_VOEU_EN_DOUBLON);
        }
        voeuxEnAttente.add(voe);
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
     * @param parametres                     les paramètres de la campagne en cours
     * @param estimationsRangsDernierAppeles les estimations du rang du dernier appelé
     * @return le rang maximal calculé
     */
    public int calculerRangMaximalAdmissionInternatSelonEstimationRangDernierAppele(
            Parametres parametres,
            List<Voeu> voeuxDansCetInternat,
            Map<GroupeAffectationUID, Integer> estimationsRangsDernierAppeles) {
        Set<Integer> candidatsAffectes =
                voeuxDansCetInternat.stream()
                .filter(v -> StatutVoeu.estProposition(v.statut))
                .map(v -> v.id.gCnCod)
                .collect(Collectors.toSet());
        List<Voeu> voeuxTriesParClassementInternat =
                voeuxDansCetInternat.stream()
                        .sorted(Comparator.comparing(v -> v.rangInternat))
                        .collect(Collectors.toList());

        if (parametres.nbJoursCampagne >= parametres.nbJoursCampagneDateFinReservationInternats) {
            return Integer.MAX_VALUE;
        } else {
            int compteurCandidat = 0;
            int rangInternatDuDernierCandidatAjoute = 0;
            for (Voeu voe : voeuxTriesParClassementInternat) {
                /* sortie de boucle: le nombre de places vacantes est atteint */
                if (compteurCandidat == Integer.max(0,capacite - candidatsAffectes.size())) {
                    break;
                }
                int estimationRangDernierAppele = estimationsRangsDernierAppeles.get(voe.groupeUID);
                /* Plusieurs cas où le voeu sera ignoré au sens 
                où il ne change pas la valeur du dernier rang comptabilisé
                    et du nombre de candidat comptés dans le contingent.
                1. on ignore les voeuxEnAttente qui ne sont pas sous la barre
                2. on a vu le même candidat au passage précédent dans la boucle 
                3. l'internat est déjà obtenu par le candidat */
                boolean voeuComptabilisableDansLeNombreDAdmis
                        = (voe.ordreAppel <= estimationRangDernierAppele)
                        && (voe.rangInternat != rangInternatDuDernierCandidatAjoute)
                        && (!candidatsAffectes.contains(voe.id.gCnCod));
                if (voeuComptabilisableDansLeNombreDAdmis) {
                    rangInternatDuDernierCandidatAjoute = voe.rangInternat;
                    compteurCandidat++;
                }
            }
            return rangInternatDuDernierCandidatAjoute;
        }
    }

    @Override
    public String toString() {
        return id.toString();
    }

    /**
     * Utilisé par les désérialisations Json et XML
     */
    @SuppressWarnings("unused")
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
    }

}
