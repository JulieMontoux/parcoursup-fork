package fr.parcoursup.algos.exceptions;

public enum VerificationExceptionMessage {

    MESSAGE("%s"),

    ALGO_PROPOSITIONS_ENTREE_GROUPE_AFFECTATION_DUPLIQUE("GroupeAffectation dupliqué"),
    ALGO_PROPOSITIONS_ENTREE_GROUPE_INTERNAT_DUPLIQUE("Internat dupliqué"),
    ALGO_PROPOSITIONS_ENTREE_GROUPE_INTERNAT_INCONNU("Internat inconnu %s"),
    ALGO_PROPOSITIONS_ENTREE_GROUPE_INCONNU("Groupe affectation inconnu %s"),
    ALGO_PROPOSITIONS_ENTREE_VOEU_DUPLIQUE("Ajout de voeu dupliqué"),

    CONNECTEUR_DONNEES_APPEL_SQL_GROUPE_DUPLIQUE("Groupe dupliqué %s lors de l'énumération des résultats de la requete \r\n %s"),

    DOMAINE_ONISEP_DUPLICATION_DKEY("DomaineOnisep: duplication de DKEY"),
    DOMAINE_ONISEP_CLE_RESERVEE("DomaineOnisep: clé réservée"),

    EXEMPLE_PROPOSITIONS_ERREUR_VERIFICATION("Problème détecté lors de la vérification"),

    FILIERE_REDONDANCE("ajouterFiliereAvecvoeuxCommuns: redondance"),
    FILIERE_DUPLICATION("Duplication de filiere."),

    GROUPE_AFFECTATION_INCOHERENCE_PARAMETRES("Incohérence dans les paramètres du constructeur de GroupeAffectation"),
    GROUPE_AFFECTATION_INCOHERENCE_RANG_DERNIER_APPELE("Lors de l'estimation du rang du dernier appelé, le rangDernierAppeleAnterieur doit être inférieur ou égal à rangDernierAppeleActuellement"),

    GROUPE_CLASSEMENT_TAUX_INCOHERENTS("Taux incohérents"),
    GROUPE_CLASSEMENT_POSSIBLE_DEPASSEMENT_ARITHMETIQUE("Possibilité de capacité arithmetique"),

    GROUPE_INTERNAT_INCOHERENCE_PARAMETRES("Incohérence dans les paramètres du constructeur de GroupeInternat"),
    GROUPE_INTERNAT_CAPACITE_NEGATIVE("L'internat %s a une capacité négative veuillez vérifier les données."),
    GROUPE_INTERNAT_DEJA_INITIALISE("Groupe déjà initialisé"),
    GROUPE_INTERNAT_VOEU_EN_DOUBLON("Voeu en doublon"),
    GROUPE_INTERNAT_DATE_ANTERIEURE("Impossible d'exécuter l'algorithme à une date antérieure au début de la campagne, veuillez vérifier les données."),
    GROUPE_INTERNAT_CONTINGENT_ADMISSION("Problème de calcul du contingent d'admission."),
    GROUPE_INTERNAT_VOEU_SANS_GROUPE("Voeu sans groupe d'affectation."),
    GROUPE_INTERNAT_POSITION_NON_INITIALISEE("La position doit être initialisée au préalable"),

    MODIF_CLASSEMENT_INCOHERENCE_LOGIQUE("Incohérence logique"),

    REPONDEUR_AUTOMATIQUE_INCOHERENCE_VOEU_EN_ATTENTE_AVEC_RA_MAIS_SANS_RANG("Problème intégrité données: voeu en attente de proposition d'un candidat ayant activé son RA mais qui n'a pas de rang dans le RA: %s"),

    VERIFICATION_AFFICHAGES_VIOLATION_ORDRE_LISTE_ATTENTE_SANS_INTERNAT("Violation respect ordre liste attente pour les voeux sans demande internat v1 floué par v2 où v1 est %s et v2 est %s"),
    
    VERIFICATION_ALGO_REPONDEUR_AUTOMATIQUE_VIOLATION_P7_1("RepAuto violation P7.1: 'Si un candidat a accepté automatiquement une proposition ou renoncé automatiquement à un voeu alors son répondeur automatique est activé'. Voeu en cause: %s"),
    VERIFICATION_ALGO_REPONDEUR_AUTOMATIQUE_VIOLATION_P7_2("RepAuto Violation P7.2: 'Si un candidat a activé son répondeur automatique alors il a au plus une proposition en PP, et cette proposition est acceptée'. Candidat en cause %s"),
    VERIFICATION_ALGO_REPONDEUR_AUTOMATIQUE_VIOLATION_P7_3("RepAuto violation P7.3: 'Si un candidat a renoncé automatiquement à une proposition ou un voeu en attente alors il a reçu une nouvelle proposition sur un voeu mieux classé et l'a acceptée automatiquement'. Voeu en cause %s"),
    VERIFICATION_ALGO_REPONDEUR_AUTOMATIQUE_VIOLATION_P7_4("RepAuto violation P7.4: 'Si un candidat a activé son RA alors tous ses voeux sont classés dans le RA, excepté éventuellement les propositions des jours précédents'. Voeu en cause: %s"),
    VERIFICATION_ALGO_REPONDEUR_AUTOMATIQUE_VIOLATION_P7_5("RepAuto violation P7.5: 'Si un candidat a activé son RA alors tous ses voeux ont un rang différent dans le RA.'. Voeu en cause %s"),
    VERIFICATION_ALGO_REPONDEUR_AUTOMATIQUE_VIOLATION_P7_6("RepAuto violation P7.6: 'Si un voeu du RA est une proposition alors les voeux du RA encore en attente ont un meilleur rang dans le RA. Candidat en cause %s"),
    VERIFICATION_ALGO_DEM_AUTO_VIOLATION_P8("DemAuto violation P8: 'En GDD, si un voeu a été démissionné automatiquement le jour même, alors le candidat concerné\n" +
            "    a une nouvelle proposition du jour, de rang strictement inférieur.'. Candidat en cause: gCnCod=%s"),
    VERIFICATION_ENTREE_ALGO_PROPOSITIONS_VOEU_NON_RECENSE("Voeu non recensé en entrée"),
    VERIFICATION_ENTREE_ALGO_PROPOSITIONS_DONNEES_NON_INTEGRES("Données d'entrée non intègres: %s"),

    VERIFICATION_ENTREE_ALGO_ORDRE_APPEL_TAUX_BOURSIER("Taux boursier incohérent"),
    VERIFICATION_ENTREE_ALGO_ORDRE_APPEL_TAUX_HORS_SECTEUR("Taux hors-secteur incohérent"),
    VERIFICATION_ENTREE_ALGO_ORDRE_APPEL_DUPLICATION_VOEUX("Duplication de rangs de voeu"),
    VERIFICATION_ENTREE_ALGO_ORDRE_APPEL_RANGS_VOEUX("Rang d'un voeu incohérent"),
    
    VERIFICATION_ORDRE_APPEL_TAILLE_DONNEES("Inconsistence logique dans les donnees d entrée"),
    VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_DONNEE_SORTIE_MANQUANTE("Donnée de sortie manquante"),
    VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_GCNCOD_DUPLIQUE("G_CN_COD dupliqué"),
    VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_GCNCOD_MANQUANT("G_CN_COD manquant"),
    VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_INCONSISTENCE_DONNEES("données inconsistentes"),
    VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_VIOLATION_P1("Violation P1"),
    VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_BOURSIER_DU_SECTEUR_DECROIT("Boursier du secteur %s avec rang d'appel qui décroit dans %s"),
    VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_BOURSIER_DU_SECTEUR_DEPASSE("Boursier du secteur %s dépassé par %s dans %s"),
    VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_NON_BOURSIER_DU_SECTEUR_DIMINUE_TROP("Non boursier du secteur %s avec rang  qui diminue trop dans %s"),
    VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_NON_BOURSIER_DEPASSE_CANDIDAT_DU_SECTEUR("Non-boursier %s dépassant un candidat du secteur dans g"),
    VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_BOURSIER_HORS_SECTEUR_DIMINUE_TROP("Candidat hors-secteur boursier %s avec rang  qui diminue trop dans le groupe %s"),
    VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_HORS_SECTEUR_DEPASSE_BOURSIER_HORS_SECTEUR("Candidat hors-secteur %s dépassant le boursier hors-secteur %s dans le groupe %s"),
    VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_NON_BOURSIER_HORS_SECTEUR_DIMINUE_TROP("Candidat hors-secteur non-boursier %s avec rang  qui diminue trop dans le groupe %s"),
    VERIFICATION_RESULTATS_ALGO_ORDRE_APPEL_NON_BOURSIER_HORS_SECTEUR_DEPASSE_CANDIDAT("Candidat hors-secteur non-boursier %s dépassant le candidat %s dans le groupe %s"),

    INTERNAT_CGI_NUL("Internat de c_gi_cod null"),
    INTERNAT_TYPE_INCONNU("Internat c_gi_cod=%s de type inconnu: à la fois le gti et le gta sont non nuls"),
    INTERNAT_INCONSISTENCE_INDEX("Internat c_gi_cod=%s: inconsistence de l'index"),

    VOEU_HORS_PP_NON_REFUSABLE_AUTOMATIQUEMENT("Le voeu affecté hors PP %s ne peut être refusé automatiquement"),
    VOEU_NON_REFUSABLE_AUTOMATIQUEMENT_HORS_REP_AUTO("La proposition %s ne peut être refusée automatiquement hors application du répondeur auto"),
    VOEU_SANS_RANG_NON_REFUSABLE_AUTOMATIQUEMENT("Le voeu %s sans rang dans le rep auto ne peut être refusé automatiquement"),
    VOEU_AVEC_REPONDEUR_NON_REFUSABLE_PAR_DEM_AUTO_VOEUX_ORDONNES("Le voeu %s n'est pas refusable automatiquement en GDD, il doit être traité par le répondeur automatique"),
    VOEU_REFUS_AUTOMATIQUE_IMPOSSIBLE("Le statut du voeu %s ne permet pas le refus automatique"),
    VOEU_PROPOSITION_IMPOSSIBLE("Le statut du voeu %s ne permet pas une proposition automatique"),
    VOEU_SANS_STATUT_DEMISSION_AUTOMATIQUE("Ce voeu n'est pas une démission automatique et n'a donc pas de statut associé"),
    VOEU_INCONSISTENCE_STATUT_HORS_PP("Inconsistence logique: un voeu hors PP doit avoir le statut affecteJoursPrecedents %s"),
    VOEU_RANGS_NEGATIFS("Rangs négatifs pour voeu %s"),
    VOEU_ORDRE_APPEL_MANQUANT("Ordre appel manquant pour voeu %s"),
    VOEU_INCOHERENCE_PARAMETRES("Incohérence dans les paramètres du constructeur de Voeu"),
    VOEU_INJECTION_GROUPE("Injection d'un groupe dont l'id ne correspond pas au voeu"),
    VOEU_INJECTION_INERNAT("Injection d'un internat dont l'id ne correspond pas au voeu"),
    VOEU_GROUPE_NULL("Un voeu ne peut avoir un groupeUID null"),
    VOEU_INTERNAT_NULL("Un voeu avec internat ne peut avoir un internatUID null pour voeu %s"),
    VOEU_TYPE_MAJ_INCONNUE("valeur de a_ve_typ_maj inconnue %s"),
    PARAMETRES_NULL("Passage de paramètres nuls")
    ;

    private final String message;

    VerificationExceptionMessage(String message){
        this.message = message;
    }

    public String getMessage(){
        return this.message;
    }
}
