
/* Copyright 2018 © Ministère de l'Enseignement Supérieur, de la Recherche et de l'Innovation,
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

@SuppressWarnings("unused")
public class GroupeAffectation implements Serializable {

    /* le id d'affectation identifiant de manière unique le groupe dans la base */
    @NotNull
    public final GroupeAffectationUID id;

    /* le nombre de recrutements souhaité par la formation */
    private int nbRecrutementsSouhaite;

    public int getNbRecrutementsSouhaite() {
        return nbRecrutementsSouhaite;
    }

    public void setNbRecrutementsSouhaite(int nb) {
        nbRecrutementsSouhaite = nb;
    }

    /* le rang limite des candidats (dans l'ordre d'appel): 
    tous les candidats de rang inférieur reçoivent une proposition */
    private int rangLimite;

    public int getRangLimite() {
        return rangLimite;
    }

    public void setRangLimite(int rang) {
        rangLimite = rang;
    }

    /**
     * évaluation du rang du dernier appelé à la date pivot.
     * utilisée pour la gestion des places d'internats
     */
    private int estimationRangDernierAppeleADateFinReservationInternats;

    public int getEstimationRangDernierAppeleADateFinReservationInternats() {
        return estimationRangDernierAppeleADateFinReservationInternats;
    }

    public void setEstimationRangDernierAppeleADateFinReservationInternats(int rang) {
        estimationRangDernierAppeleADateFinReservationInternats = rang;
    }

    /**
     * le rang du dernier appelé, affiché dans les formations avec internat
     */
    private int rangDernierAppeleAffiche = 0;

    /**
     * flag indiquant qu'on ne réserve pas de places dans cet internat
     */
    private boolean finReservationPlacesInternat;

    /**
     * @return le rang du dernier appelé affiché pour ce groupe (ne tient pas compte des annulations de démission)
     */
    public int getRangDernierAppeleAffiche() {
        return rangDernierAppeleAffiche;
    }

    public void setRangDernierAppeleAffiche(int rang) {
        rangDernierAppeleAffiche = rang;
    }

    /**
     * Constructeur d'un groupe d'affectation
     *
     * @param nbRecrutementsSouhaite le nombre de places totale dans le groupe
     * @param id                     l'id du groupe
     * @param rangLimite             le rang limite d'appel par bloc
     * @param rangDernierAppeleActuellement      le rang du dernier appelé actuellement
     * @param rangDernierAppeleReference         le rang du dernier appelé il y a quelques jours
     * @param parametres             paramètres de la campagne en cours
     * @throws VerificationException si données non cohérentes
     */
    public GroupeAffectation(
            int nbRecrutementsSouhaite,
            GroupeAffectationUID id,
            int rangLimite,
            int rangDernierAppeleActuellement,
            int rangDernierAppeleReference,
            Parametres parametres) throws VerificationException {
        if (id == null || nbRecrutementsSouhaite < 0 || rangLimite < 0 || rangDernierAppeleActuellement < 0) {
            throw new VerificationException(VerificationExceptionMessage.GROUPE_AFFECTATION_INCOHERENCE_PARAMETRES);
        }
        this.id = id;
        this.nbRecrutementsSouhaite = nbRecrutementsSouhaite;
        this.rangLimite = rangLimite;
        this.estimationRangDernierAppeleADateFinReservationInternats =
                calculerEstimationRangDernierAppeleADateFinReservationInternat(
                        rangDernierAppeleActuellement,
                        rangDernierAppeleReference,
                        rangLimite,
                        parametres
                );
    }

    /* constructeur utilisé par certains tests unitaires */
    public GroupeAffectation(
            int nbRecrutementsSouhaite,
            GroupeAffectationUID id,
            int rangLimite,
            int rangDernierAppele,
            Parametres parametres) throws VerificationException {
        this(nbRecrutementsSouhaite, id, rangLimite, rangDernierAppele, 0, parametres);
    }

    /** le coefficient utilisé pour la réservation de places le premier jour,
     * en fonction du taux de propositions l'année précédente
     */
    public static final int NB_JOURS_POUR_INTERPOLATION_INTERNAT = 4;

    /**
     * Calcule une estimation du rang du dernier appelé à la date d'ouverture des internats
     *
     * @param rangDernierAppeleActuellement le rang du dernier appelé, actuellement
     * @param rangDernierAppeleAnterieur le rang du dernier appelé à nbJoursCampagneRef =  (nbJoursCampagne - NB_JOURS_POUR_INTERPOLATION_INTERNAT)
     * @param parametres  paramètres de la campagne
     * @return l'estimation
     */
    public static int calculerEstimationRangDernierAppeleADateFinReservationInternat(
            int rangDernierAppeleActuellement,
            int rangDernierAppeleAnterieur,
            int rangLimiteAppelBloc,
            Parametres parametres
    ) throws VerificationException {
        if(rangDernierAppeleActuellement < rangDernierAppeleAnterieur) {
            throw new VerificationException(VerificationExceptionMessage.GROUPE_AFFECTATION_INCOHERENCE_RANG_DERNIER_APPELE);
        }
        if (parametres.nbJoursCampagne <= 1) {
            //le premier jour de la campagne: très conservatif on suppose que tous les candidats recevront une proposition
            return Integer.MAX_VALUE;
        } else if (parametres.nbJoursCampagne >= parametres.nbJoursCampagneDateFinReservationInternats) {
            //apres la date de fin de réservation: pas du tout conservatif, on évalue à minima
            return Math.max(rangLimiteAppelBloc, rangDernierAppeleActuellement);
        } else if(rangDernierAppeleActuellement <= 0) {
            //manque de données, on reste conservatif
            return Integer.MAX_VALUE;
        } else {
            final int estimation;
            if(rangDernierAppeleAnterieur <= 0) {
                estimation = rangDernierAppeleActuellement * (parametres.nbJoursCampagneDateFinReservationInternats - 1) / (parametres.nbJoursCampagne - 1);
            } else {
                int nbJoursrestantsAvantOuverture = parametres.nbJoursCampagneDateFinReservationInternats - parametres.nbJoursCampagne;
                estimation = rangDernierAppeleActuellement
                        + (nbJoursrestantsAvantOuverture * (rangDernierAppeleActuellement - rangDernierAppeleAnterieur)) / NB_JOURS_POUR_INTERPOLATION_INTERNAT;
            }
            return Math.max(estimation, Math.max(rangLimiteAppelBloc, rangDernierAppeleActuellement));
        }
    }


    public GroupeAffectation(GroupeAffectation o) {
        this.id = o.id;
        this.nbRecrutementsSouhaite = o.nbRecrutementsSouhaite;
        this.rangLimite = o.rangLimite;
        this.estimationRangDernierAppeleADateFinReservationInternats = o.estimationRangDernierAppeleADateFinReservationInternats;
        this.finReservationPlacesInternat = o.finReservationPlacesInternat;
        this.rangDernierAppeleAffiche = o.rangDernierAppeleAffiche;
    }

    /* signale que la formation ne réserve plus de places dans les internats */
    public void setFinDeReservationPlacesInternats() {
        finReservationPlacesInternat = true;
    }
    public boolean getFinDeReservationPlacesInternats() {
        return finReservationPlacesInternat;
    }

    /* ajoute un voeu dans le groupe */
    void ajouterVoeuEnAttenteDeProposition(Voeu voe) {
        voeuxEnAttente.add(voe);
    }

    /* ajoute un candidat affecté */
    public void ajouterCandidatAffecte(int gCnCod) {
        candidatsAffectes.add(gCnCod);
    }

    public void reinitialiser() {
        voeuxEnAttente.clear();
        candidatsAffectes.clear();
    }

    public boolean estAffecte(int gCnCod) {
        return candidatsAffectes.contains(gCnCod);
    }

    /* met a jour le statut aProposer, pour chaque voeu du groupe */
    void mettreAJourPropositions() throws VerificationException {

        int aPourvoir = nbPlacesVacantes();

        voeuxEnAttente.forEach(Voeu::nePasProposer);

        /* on calcule le nombre de propositions dues au rang limite.
           Les voeuxEnAttente désactivés pour cause de demande d'internat impossible à satisfaire
            ne sont pas pris en compte.
         */
        int dernierCandidatAvecProposition = -1;

        for (Voeu v : voeuxTriesParOrdreAppel()) {

            /* Si c'est le voeu d'un candidat qui a fait une demande d'internat et si
            le classement à l'internat de ce candidat est strictement supérieur à la barre d'admission
            dans l'internat alors le voeu reste en attente.
             */
            if (v.estDesactiveParPositionAdmissionInternat()) {
                continue;
            }

            /* On fait une proposition:
                * si il y a des places libres (aPourvoir > 0)
                * ou si la formation a positionné un rang limite d'appel supérieur au rang du candidat
                * ou si la formation est déjà obtenue (voeux avec internat)
                * ou si on vient de faire une proposition à ce candidat, deux voeux consécutifs peuvent concerner un même candidat,
                (un avec et un sans internat). Les voeux non-consécutifs concernent nécessairement des candidats différents.
             */
            if (aPourvoir > 0
                    || v.ordreAppel <= rangLimite
                    || v.formationDejaObtenue()
                    || dernierCandidatAvecProposition == v.id.gCnCod) {

                v.proposer();

                /* on décroit la capacité résiduelle si il y a lieu de le faire */
                if (!v.formationDejaObtenue() && dernierCandidatAvecProposition != v.id.gCnCod) {
                    aPourvoir--;
                }

                dernierCandidatAvecProposition = v.id.gCnCod;

            }
        }
    }

    /* le nombre de places vacantes au lancement du calcul.
    Peut être négatif en cas de modification à la baisse 
    des paramètres de surbooking. */
    public int nbPlacesVacantes() {
        return Integer.max(0, nbRecrutementsSouhaite - candidatsAffectes.size());
    }

    /* ensemble des voeuxEnAttente du groupe */
    private transient Set<Voeu> voeuxEnAttente = new HashSet<>();

    public List<Voeu> getVoeuxEnAttente() {
        return new ArrayList<>(voeuxEnAttente);
    }

    /* trie les voeuxEnAttente dans l'ordre d'appel */
    public List<Voeu> voeuxTriesParOrdreAppel() {
        List<Voeu> result = getVoeuxEnAttente();
        result.sort(Comparator.comparingInt((Voeu v) -> v.ordreAppel));
        return result;
    }

    /* tous les internats apparaissant parmi les voeux en attente */
    public Set<GroupeInternat> internatsConcernes() {
        return voeuxEnAttente.stream()
                .map(Voeu::getInternat)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private transient Set<Integer> candidatsAffectes = new HashSet<>();

    @Override
    public String toString() {
        return id.toString();
    }

    private GroupeAffectation() {
        this.id = new GroupeAffectationUID(0,0,0);
        this.nbRecrutementsSouhaite = 0;
        this.estimationRangDernierAppeleADateFinReservationInternats = 0;
        this.finReservationPlacesInternat = false;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        voeuxEnAttente = new HashSet<>();
        candidatsAffectes = new HashSet<>();
    }

}
