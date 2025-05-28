package fr.parcoursup.algos.propositions.algo;

import fr.parcoursup.algos.exceptions.VerificationException;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlRootElement;

import static fr.parcoursup.algos.donnees.ConnecteurSQL.A_AD_TYP_DEM_GDD;
import static fr.parcoursup.algos.donnees.ConnecteurSQL.A_AD_TYP_DEM_RA;
import static fr.parcoursup.algos.exceptions.VerificationExceptionMessage.VOEU_SANS_STATUT_DEMISSION_AUTOMATIQUE;

/* les différents statuts d'un voeu */
@XmlRootElement
public enum StatutVoeu {
    EN_ATTENTE_DE_PROPOSITION,
    PROPOSITION_DU_JOUR,
    REP_AUTO_DEMISSION_ATTENTE, //démission auto d'un voeu en attente par le répondeur automatique
    DEMISSION_AUTO_VOEU_ORDONNE_EN_ATTENTE, //démission auto d'un voeu en attente par la règle de GDD
    REP_AUTO_ACCEPTE,
    REP_AUTO_REFUS_PROPOSITION, //refus automatique d'une proposition via le répondeur automatique
    PROPOSITION_JOURS_PRECEDENTS_EN_ATTENTE_DE_REPONSE_DU_CANDIDAT,
    PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE,
    PROPOSITION_JOURS_PRECEDENTS_REFUSEE,
    REFUS_OU_DEMISSION,              //refus proposition ou demission voeu en attente (utilisé en simulation)
    NON_CLASSE              //voeu n'ayant pas encore été classé par la formation (utilisé en simulation)
    ;

    /***
     *
     * @return le type de démission automatique
     * @throws VerificationException si le voeu n'est pas une démission automatique
     */
    public static int getTypeDemissionAutomatique(StatutVoeu statut) throws VerificationException {
        if(!estDemissionAutomatique(statut)) {
            throw new VerificationException(VOEU_SANS_STATUT_DEMISSION_AUTOMATIQUE, statut);
        }
        return statut == DEMISSION_AUTO_VOEU_ORDONNE_EN_ATTENTE ? A_AD_TYP_DEM_GDD : A_AD_TYP_DEM_RA;
    }

    public static boolean estDemissionGDD(StatutVoeu statut) {
        return statut == DEMISSION_AUTO_VOEU_ORDONNE_EN_ATTENTE;
    }

    /* getters de statut */
    public static boolean estDemissionAutomatiqueParRepondeurAutomatique(StatutVoeu statut) {
        return statut == REP_AUTO_DEMISSION_ATTENTE
                || statut == REP_AUTO_REFUS_PROPOSITION;
    }

    public static boolean estDemissionAutomatique(StatutVoeu statut) {
        return statut == REP_AUTO_DEMISSION_ATTENTE
                || statut == REP_AUTO_REFUS_PROPOSITION
                || statut == DEMISSION_AUTO_VOEU_ORDONNE_EN_ATTENTE
                ;
    }

    public static boolean estDemissionAutomatiqueVoeuAttenteParRepondeurAutomatique(StatutVoeu statut) {
        return statut == REP_AUTO_DEMISSION_ATTENTE;
    }

    public static boolean estDemissionAutomatiqueProposition(StatutVoeu statut) {
        return statut == REP_AUTO_REFUS_PROPOSITION;
    }

    public static boolean estAcceptationAutomatique(StatutVoeu statut) {
        return statut == REP_AUTO_ACCEPTE;
    }

    public static boolean estPropositionDuJour(StatutVoeu statut) {
        return statut == REP_AUTO_ACCEPTE
                || statut == PROPOSITION_DU_JOUR;
    }

    public static boolean estProposition(StatutVoeu statut) {
        return statut == REP_AUTO_ACCEPTE
                || statut == PROPOSITION_DU_JOUR
                || statut == PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE
                || statut == PROPOSITION_JOURS_PRECEDENTS_EN_ATTENTE_DE_REPONSE_DU_CANDIDAT;
    }

    public static boolean estEnAttenteDeProposition(StatutVoeu statut) {
        return statut == EN_ATTENTE_DE_PROPOSITION;
    }

    public static boolean aEteProposeJoursPrecedents(StatutVoeu statut) {
        return statut == PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE
                || statut == PROPOSITION_JOURS_PRECEDENTS_EN_ATTENTE_DE_REPONSE_DU_CANDIDAT
                || statut == PROPOSITION_JOURS_PRECEDENTS_REFUSEE;
    }

    public static boolean estPropositionDesJoursPrecedents(@NotNull StatutVoeu statut) {
        return statut == PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE
                || statut == PROPOSITION_JOURS_PRECEDENTS_EN_ATTENTE_DE_REPONSE_DU_CANDIDAT;
    }

}
