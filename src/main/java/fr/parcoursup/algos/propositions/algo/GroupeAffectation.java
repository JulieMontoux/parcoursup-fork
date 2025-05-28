
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

import java.io.Serializable;

@SuppressWarnings("unused")
public class GroupeAffectation implements Serializable {

    /* le id d'affectation identifiant de manière unique le groupe dans la base */
    @NotNull
    public final GroupeAffectationUID id;

    /* le nombre de recrutements souhaité par la formation */
    private int nbRecrutementsSouhaite;
    
    /* le nombre de candidat en attente*/
    private int a_rg_nbr_att;
    
    /* Flag pour stopper les adm sur le groupe*/
    private int a_rg_flg_adm_stop;

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
            @SuppressWarnings("NullableProblems") GroupeAffectationUID id,
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
            final int estimation = getEstimation(rangDernierAppeleActuellement, rangDernierAppeleAnterieur, parametres);
            return Math.max(estimation, Math.max(rangLimiteAppelBloc, rangDernierAppeleActuellement));
        }
    }

    private static int getEstimation(int rangDernierAppeleActuellement, int rangDernierAppeleAnterieur, Parametres parametres) {
        final int estimation;
        if(rangDernierAppeleAnterieur <= 0) {
            estimation = rangDernierAppeleActuellement * (parametres.nbJoursCampagneDateFinReservationInternats - 1) / (parametres.nbJoursCampagne - 1);
        } else {
            int nbJoursrestantsAvantOuverture = parametres.nbJoursCampagneDateFinReservationInternats - parametres.nbJoursCampagne;
            estimation = rangDernierAppeleActuellement
                    + (nbJoursrestantsAvantOuverture * (rangDernierAppeleActuellement - rangDernierAppeleAnterieur)) / NB_JOURS_POUR_INTERPOLATION_INTERNAT;
        }
        return estimation;
    }


    public GroupeAffectation(GroupeAffectation o) {
        this.id = o.id;
        this.nbRecrutementsSouhaite = o.nbRecrutementsSouhaite;
        this.rangLimite = o.rangLimite;
        this.estimationRangDernierAppeleADateFinReservationInternats = o.estimationRangDernierAppeleADateFinReservationInternats;
        this.finReservationPlacesInternat = o.finReservationPlacesInternat;
        this.rangDernierAppeleAffiche = o.rangDernierAppeleAffiche;
        this.a_rg_nbr_att = o.a_rg_nbr_att;
        this.a_rg_flg_adm_stop = o.a_rg_flg_adm_stop;
    }

    /* signale que la formation ne réserve plus de places dans les internats */
    public void setFinDeReservationPlacesInternats() {
        finReservationPlacesInternat = true;
    }
    public boolean getFinDeReservationPlacesInternats() {
        return finReservationPlacesInternat;
    }

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

	public int getA_rg_nbr_att() {
		return a_rg_nbr_att;
	}

	public void setA_rg_nbr_att(int a_rg_nbr_att) {
		this.a_rg_nbr_att = a_rg_nbr_att;
	}

	public int getA_rg_flg_adm_stop() {
		return a_rg_flg_adm_stop;
	}

	public void setA_rg_flg_adm_stop(int a_rg_flg_adm_stop) {
		this.a_rg_flg_adm_stop = a_rg_flg_adm_stop;
	}

    public boolean estOuvertAuxAdmission() {
        return a_rg_flg_adm_stop == 0;
    }
}
