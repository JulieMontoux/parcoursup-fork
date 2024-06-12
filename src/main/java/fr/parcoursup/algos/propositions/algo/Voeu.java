
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

import fr.parcoursup.algos.exceptions.ClassCastExceptionMessage;
import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

import static fr.parcoursup.algos.donnees.ConnecteurSQL.A_AD_TYP_DEM_GDD;
import static fr.parcoursup.algos.donnees.ConnecteurSQL.A_AD_TYP_DEM_RA;
import static fr.parcoursup.algos.exceptions.VerificationExceptionMessage.VOEU_SANS_STATUT_DEMISSION_AUTOMATIQUE;

/* annotation required for statut to be serialized */
@XmlAccessorType(XmlAccessType.FIELD)
public class Voeu implements Serializable {

    /* caractéristiques identifiant de manière unique le voeu dans la base de données */
    @NotNull
    public final VoeuUID id;

    /* groupe d'affectation du voeu */
    private transient GroupeAffectation groupe;

    public GroupeAffectation getGroupeAffectation() { return groupe; }
    public void setGroupeAffectation(GroupeAffectation groupe) throws VerificationException {
        if(groupe == null || !groupe.id.equals(groupeUID)) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_INJECTION_GROUPE);
        }
        this.groupe = groupe;
    }

    @NotNull
    public final GroupeAffectationUID groupeUID;

    /* rang initial du voeu dans l'ordre d'appel */
    public final int ordreAppel;

    /* Rang du voeu dans l'ordre d'appel, tel qu'affiché dans l'interface.
    Il y a quelques rares cas d'erreurs de classement par les formations 
    (de l'ordre de la centaine par an). Dans ce cas un candidat peut être
    remonté dans l'ordre d'appel, ce qui crée deux candidats avec le
    même ordre d'appel, avec priorité au candidat
    remonté. Dans ce cas le champ C_CG_ORD_APP utilisé par l'algorithme
    est incrémenté pour tous les candidats suivants, afin de maintenir 
    la propriété d'unicité du candidat pour un ordre d'appel donnée. Le champ
    C_CG_ORD_APP_AFF reste, lui égal à sa valeur initiale, 
    sauf pour le candidat qui a bénéficié de la remontée dans le classement. 
    C'est le champ C_CG_ORD_APP_AFF qui est affiché au candidat sur le site de
    Parcoursup, et utilisé pour la mise à jour des affichages. */
    public final int ordreAppelAffiche;

    /* le rang du candidat au classement internat */
    public final int rangInternat;

    /* la position du voeu dans la liste de voeux hiérarchisés du candidat (0 = pas de hiérarchisation, 1 = voeu préféré). Calculé en SQL par NVL(A_VOE.a_ve_ord,0). */
    private int rangPreferencesCandidat;

    /* le voeu doit-il être ignoré dans le calcul des rangs sur liste d'atte,te (par exemple dan sle cas d'une annulation de démission demandée par le candidat,
    ou en cas de modif de classemnt erronné) */
    public final boolean ignorerDansLeCalculRangsListesAttente;

    /* le voeu doit-il être ignoré dans le calcul des barres internats affichées aux candidats en attente, par exemple
     en cas d'affectation directe par une CAES */
    public final boolean ignorerDansLeCalculBarresInternatAffichees;


    public int getRangPreferencesCandidat() { return rangPreferencesCandidat; }
    public void setRangPreferencesCandidat(int rangPreferencesCandidat) { this.rangPreferencesCandidat = rangPreferencesCandidat; }

    /* est-ce que le répondeur automatique est activé.
    * Utilisé pour les vérifications des données. */
    private boolean repondeurActive;
    public boolean getRepondeurActive() { return repondeurActive; }
    public void setRepondeurActive(boolean b) { repondeurActive = b; }

    /* le rang du voeu sur liste d'attente, si en attente */
    int rangListeAttente = 0;
    
    /* le rang du voeu sur liste d'attente de la veille, si en attente */
    int rangListeAttenteVeille = 0;

    public int getRangListeAttente() {
        return rangListeAttente;
    }

    public void setRangListeAttente(int rang) {
        rangListeAttente = rang;
    }
    
    public void setRangListeAttenteVeille(int rang) {
    	rangListeAttenteVeille = rang;
    }
    
    public int getRangListeAttenteVeille() {
        return rangListeAttenteVeille;
    }

    /* y a-t-il une demande d'internat avec classement sur ce voeu ? */
    public boolean avecInternatAClassementPropre() {
        return internat != null;
    }

    /* le candidat a-t-il déjà une offre dans cet internat (pour une autre formation) ?*/
    public boolean internatDejaObtenu() {
        return internat != null && internat.estAffecte(id.gCnCod);
    }

    /* le candidat a-t-il déjà une offre dans cette formation (sans internat) ? */
    public boolean formationDejaObtenue() {
        return groupe != null && groupe.estAffecte(id.gCnCod);
    }

    /* le groupe de classement internat. */
    private transient GroupeInternat internat;
    public GroupeInternat getInternat() { return internat; }
    public void setInternat(GroupeInternat internat) throws VerificationException {
        if( (internat == null && internatUID != null)
                || (internat != null && internatUID == null)
                || (internat != null && !internat.id.equals(internatUID))
        ) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_INJECTION_INERNAT);
        }
        this.internat = internat;
    }

    public final GroupeInternatUID internatUID;


    /***
     *
     * @return le type de démission automatique
     * @throws VerificationException si le voeu n'est pas une démission automatique
     */
    public int getTypeDemissionAutomatique() throws VerificationException {
        if(!estDemissionAutomatique()) {
            throw new VerificationException(VOEU_SANS_STATUT_DEMISSION_AUTOMATIQUE, this);
        }
        return (statut == StatutVoeu.DEMISSION_AUTO_VOEU_ORDONNE_EN_ATTENTE || statut == StatutVoeu.DEMISSION_AUTO_VOEU_ORDONNE_PROPOSITION_EN_ATTENTE_REP) ? A_AD_TYP_DEM_GDD : A_AD_TYP_DEM_RA;
    }

    public boolean estDemissionGDD() {
        return (statut == StatutVoeu.DEMISSION_AUTO_VOEU_ORDONNE_EN_ATTENTE || statut == StatutVoeu.DEMISSION_AUTO_VOEU_ORDONNE_PROPOSITION_EN_ATTENTE_REP);
    }

    /* les différents statuts d'un voeu */
    @XmlRootElement
    public enum StatutVoeu {
        EN_ATTENTE_DE_PROPOSITION,
        PROPOSITION_DU_JOUR,
        REP_AUTO_DEMISSION_ATTENTE, //démission auto d'un voeu en attente par le répondeur automatique
        DEMISSION_AUTO_VOEU_ORDONNE_EN_ATTENTE, //démission auto d'un voeu en attente par la règle de GDD
        DEMISSION_AUTO_VOEU_ORDONNE_PROPOSITION_EN_ATTENTE_REP, //démission auto d'une propositions par la règle de GDD
        REP_AUTO_ACCEPTE,
        REP_AUTO_REFUS_PROPOSITION, //refus automatique d'une proposition via le répondeur automatique
        PROPOSITION_JOURS_PRECEDENTS_EN_ATTENTE_DE_REPONSE_DU_CANDIDAT,
        PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE,
        PROPOSITION_JOURS_PRECEDENTS_REFUSEE,
        REFUS_OU_DEMISSION,              //refus proposition ou demission voeu en attente (utilisé en simulation)
        NON_CLASSE              //voeu n'ayant pas encore été classé par la formation (utilisé en simulation)
    }

    StatutVoeu statut;

    /* getters de statut */
    public boolean estDemissionAutomatiqueParRepondeurAutomatique() {
        return statut == StatutVoeu.REP_AUTO_DEMISSION_ATTENTE
                || statut == StatutVoeu.REP_AUTO_REFUS_PROPOSITION;
    }

    public boolean estDemissionAutomatique() {
        return statut == StatutVoeu.REP_AUTO_DEMISSION_ATTENTE
                || statut == StatutVoeu.REP_AUTO_REFUS_PROPOSITION
                || statut == StatutVoeu.DEMISSION_AUTO_VOEU_ORDONNE_EN_ATTENTE
                || statut == StatutVoeu.DEMISSION_AUTO_VOEU_ORDONNE_PROPOSITION_EN_ATTENTE_REP
                ;
    }

    public boolean estDemissionAutomatiqueVoeuAttenteParRepondeurAutomatique() {
        return statut == StatutVoeu.REP_AUTO_DEMISSION_ATTENTE;
    }

    public boolean estDemissionAutomatiqueProposition() {
        return statut == StatutVoeu.REP_AUTO_REFUS_PROPOSITION
                || statut == StatutVoeu.DEMISSION_AUTO_VOEU_ORDONNE_PROPOSITION_EN_ATTENTE_REP;
    }

    public boolean estAcceptationAutomatique() {
        return statut == StatutVoeu.REP_AUTO_ACCEPTE;
    }

    public boolean estPropositionDuJour() {
        return statut == StatutVoeu.REP_AUTO_ACCEPTE
                || statut == StatutVoeu.PROPOSITION_DU_JOUR;
    }

    public boolean estProposition() {
        return statut == StatutVoeu.REP_AUTO_ACCEPTE
                || statut == StatutVoeu.PROPOSITION_DU_JOUR
                || statut == StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE
                || statut == StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_EN_ATTENTE_DE_REPONSE_DU_CANDIDAT;
    }

    /**
     *
     * @return true si une proposo=tiion sur ce voeu a été faite les jours précédents
     */
    public boolean aEteProposeJoursPrecedents() {
        return statut == StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE
                || statut == StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_EN_ATTENTE_DE_REPONSE_DU_CANDIDAT
                || statut == StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_REFUSEE;
    }


    public boolean estEnAttenteDeProposition() {
        return statut == StatutVoeu.EN_ATTENTE_DE_PROPOSITION;
    }

    public boolean estPropJoursPrecedentsEnAttenteDeReponseCandidat() {
        return statut == StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_EN_ATTENTE_DE_REPONSE_DU_CANDIDAT;
    }

    public StatutVoeu getStatut() {
        return statut;
    }

    public void setStatut(StatutVoeu s) {
        this.statut = s;
    }

    public void refuserAutomatiquementParApplicationRepondeurAutomatique() throws VerificationException {
        if (estAffecteHorsPP()) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_HORS_PP_NON_REFUSABLE_AUTOMATIQUEMENT, this);
        } else if (estProposition()) {
            statut = StatutVoeu.REP_AUTO_REFUS_PROPOSITION;
        } else if (!repondeurActive) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_REFUS_AUTOMATIQUE_IMPOSSIBLE, this);
        } else if (statut != StatutVoeu.EN_ATTENTE_DE_PROPOSITION) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_REFUS_AUTOMATIQUE_IMPOSSIBLE, this);
        } else {
            statut = StatutVoeu.REP_AUTO_DEMISSION_ATTENTE;
        }
    }

    public void refuserAutomatiquementParApplicationDemissionVoeuxOrdonnes() throws VerificationException {
        if(!estPropositionDuJour() && !estEnAttenteDeProposition() && !estPropJoursPrecedentsEnAttenteDeReponseCandidat()) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_NON_REFUSABLE_AUTOMATIQUEMENT_HORS_REP_AUTO, this);
        } else if (!aEteProposeJoursPrecedents() && rangPreferencesCandidat <= 0) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_SANS_RANG_NON_REFUSABLE_AUTOMATIQUEMENT, this);
        } else if (repondeurActive) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_AVEC_REPONDEUR_NON_REFUSABLE_PAR_DEM_AUTO_VOEUX_ORDONNES, this);
        } else if(estEnAttenteDeProposition()){
            statut = StatutVoeu.DEMISSION_AUTO_VOEU_ORDONNE_EN_ATTENTE;
        } else {
            statut = StatutVoeu.DEMISSION_AUTO_VOEU_ORDONNE_PROPOSITION_EN_ATTENTE_REP;
        }
    }

    public void proposer() throws VerificationException {
        if (statut != StatutVoeu.EN_ATTENTE_DE_PROPOSITION) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_PROPOSITION_IMPOSSIBLE, this);
        }
        statut = repondeurActive ? StatutVoeu.REP_AUTO_ACCEPTE : StatutVoeu.PROPOSITION_DU_JOUR;
    }

    public void nePasProposer() {
        statut = StatutVoeu.EN_ATTENTE_DE_PROPOSITION;
    }

    /* vérifie si le voeu est désactivé du fait d'une demande d'internat */
    public boolean estDesactiveParPositionAdmissionInternat() {
        /* si le candidat demande l'internat, mais que son classement
            à l'internat ne passe pas la barre définie par la position
            d'admission, alors on en fait pas de proposition */
        return ((internat != null)
                && !internatDejaObtenu()
                && rangInternat > internat.getPositionAdmission());
    }

    /* constructeur d'un voeu sans internat ou avec internat sans classement propre 
    (obligatoire ou non sélectif) */
    public Voeu(
            int gCnCod,
            boolean avecInternat,
            @NotNull GroupeAffectationUID groupeUID,
            int ordreAppel,
            int ordreAppelAffiche,
            int rangPreferencesCandidat,
            StatutVoeu statut,
            boolean affecteHorsPP,
            boolean ignorerDansLeCalculRangsListesAttente,
            boolean ignorerDansLeCalculBarresInternatAffichees
    ) throws VerificationException {

        this.id = new VoeuUID(gCnCod, groupeUID.gTaCod, avecInternat);
        this.groupeUID = groupeUID;
        this.ordreAppel = ordreAppel;
        this.ordreAppelAffiche = ordreAppelAffiche;
        this.internat = null;
        this.internatUID = null;
        this.rangInternat = 0;
        this.rangPreferencesCandidat = rangPreferencesCandidat;
        this.statut = statut;
        this.affecteHorsPP = affecteHorsPP;
        this.ignorerDansLeCalculRangsListesAttente = ignorerDansLeCalculRangsListesAttente;
        this.ignorerDansLeCalculBarresInternatAffichees = ignorerDansLeCalculBarresInternatAffichees;

        if (affecteHorsPP && !aEteProposeJoursPrecedents()) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_INCONSISTENCE_STATUT_HORS_PP, this.id);
        }
        if (ordreAppel < 0 || rangPreferencesCandidat < 0) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_RANGS_NEGATIFS, this.id);
        }
        if (ordreAppel == 0 && !aEteProposeJoursPrecedents() && statut != StatutVoeu.NON_CLASSE) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_ORDRE_APPEL_MANQUANT, this.id);
        }

    }

    public Voeu(
            int gCnCod,
            boolean avecInternat,
            @NotNull GroupeAffectationUID groupeUID,
            int ordreAppel,
            int ordreAppelAffiche,
            int rangPreferencesCandidat,
            StatutVoeu statut,
            boolean affecteHorsPP
    ) throws VerificationException {
        this(gCnCod,avecInternat,groupeUID,ordreAppel,ordreAppelAffiche,rangPreferencesCandidat,statut,affecteHorsPP, false,false);
    }

        /* constructeur d'un voeu avec internat à classement propre */
    public Voeu(
            int gCnCod,
            @NotNull GroupeAffectationUID groupeUID,
            int ordreAppel,
            int ordreAppelAffiche,
            GroupeInternatUID internatUID,
            int rangInternat,
            int rangPreferencesCandidat,
            StatutVoeu statut,
            boolean affecteHorsPP,
            boolean ignorerDansLeCalculRangsListesAttente,
            boolean ignorerDansLeCalculBarresInternatAffichees
    ) throws VerificationException {

        this.id = new VoeuUID(gCnCod, groupeUID.gTaCod, true);
        this.groupeUID = groupeUID;
        this.ordreAppel = ordreAppel;
        this.ordreAppelAffiche = ordreAppelAffiche;
        this.internatUID = internatUID;
        this.rangInternat = rangInternat;
        this.rangPreferencesCandidat = rangPreferencesCandidat;
        this.statut = statut;
        this.affecteHorsPP = affecteHorsPP;
        this.ignorerDansLeCalculRangsListesAttente = ignorerDansLeCalculRangsListesAttente;
        this.ignorerDansLeCalculBarresInternatAffichees = ignorerDansLeCalculBarresInternatAffichees;

        if (affecteHorsPP && !aEteProposeJoursPrecedents()) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_INCONSISTENCE_STATUT_HORS_PP, this.id);
        }
        if (ordreAppel < 0 || rangPreferencesCandidat < 0 || rangInternat < 0) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_RANGS_NEGATIFS, this.id);
        }
        if (ordreAppel == 0 && !aEteProposeJoursPrecedents() && statut != StatutVoeu.NON_CLASSE) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_ORDRE_APPEL_MANQUANT, this.id);
        }
        if(internatUID == null) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_INTERNAT_NULL, this.id);
        }
    }

    public Voeu(
            int gCnCod,
            @NotNull GroupeAffectationUID groupeUID,
            int ordreAppel,
            int ordreAppelAffiche,
            GroupeInternatUID internatUID,
            int rangInternat,
            int rangPreferencesCandidat,
            StatutVoeu statut,
            boolean affecteHorsPP) throws VerificationException {
        this(gCnCod,groupeUID,ordreAppel,ordreAppelAffiche,internatUID,rangInternat,rangPreferencesCandidat,statut,affecteHorsPP,false,false);
    }

    public Voeu(Voeu o) {
        this.id = o.id;
        this.groupeUID = o.groupeUID;
        this.groupe = null;
        this.internat = null;
        this.ordreAppel = o.ordreAppel;
        this.ordreAppelAffiche = o.ordreAppelAffiche;
        this.rangInternat = o.rangInternat;
        this.rangPreferencesCandidat = o.rangPreferencesCandidat;
        this.statut = o.statut;
        this.repondeurActive = o.repondeurActive;
        this.affecteHorsPP = o.affecteHorsPP;
        this.internatUID = o.internatUID;
        this.rangListeAttente = o.rangListeAttente;
        this.ignorerDansLeCalculRangsListesAttente = o.ignorerDansLeCalculRangsListesAttente;
        this.ignorerDansLeCalculBarresInternatAffichees = o.ignorerDansLeCalculBarresInternatAffichees;
    }

    public void ajouterAuxGroupes() throws VerificationException {
        if (estEnAttenteDeProposition()) {
            groupe.ajouterVoeuEnAttenteDeProposition(this);
            if (internat != null) {
                internat.ajouterVoeuEnAttenteDeProposition(this);
            }
        }
        if (estProposition()) {
            groupe.ajouterCandidatAffecte(id.gCnCod);
            if (internat != null) {
                internat.ajouterCandidatAffecte(id.gCnCod);
            }
        }
    }

    /* voeu affecté hors procédure principale (CAES ou PC ou inscription à la rentrée) */
    public boolean estAffecteHorsPP() {
        return affecteHorsPP;
    }

    private final boolean affecteHorsPP;

    @Override
    public String toString() {
        if (internatUID == null) {
            return "(" + id + ")";
        } else {
            return "(" + id + " avec demande internat " + internatUID + ")";
        }
    }

    /* utilisé par le simulateur */
    public void simulerEtape() {
        if (estPropositionDuJour()) {
            statut = StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_EN_ATTENTE_DE_REPONSE_DU_CANDIDAT;
        }
    }

    /* utilisé par le simulateur */
    public void simulerRefusProposition() {
        statut = StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_REFUSEE;
    }

    public void simulerRefusAvantProposition() {
        statut = StatutVoeu.REFUS_OU_DEMISSION;
    }

    /* utilisé par le simulateur */
    public void simulerAcceptation() {
        statut = StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE;
    }

    /* utilisé par le simulateur */
    public void simulerEnAttenteSaufSiNonClasse() {
        if (ordreAppel > 0 && (internatUID == null || rangInternat > 0)) {
            statut = StatutVoeu.EN_ATTENTE_DE_PROPOSITION;
        } else {
            statut = StatutVoeu.NON_CLASSE;
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Voeu) {
            return id.equals(((Voeu) obj).id);
        } else {
            throw new ClassCastException(ClassCastExceptionMessage.GLOBAL_TEST_EGALITE_IMPREVU.getMessage());
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Utilisé par les désérialisations Json et XML
     */
    private Voeu() {
        id = new VoeuUID(0,0,false);
        groupe = null;
        groupeUID = new GroupeAffectationUID(0,0,0);
        ordreAppel = 0;
        ordreAppelAffiche = 0;
        rangInternat = 0;
        rangPreferencesCandidat = 0;
        internat = null;
        internatUID = null;
        affecteHorsPP = false;
        repondeurActive = false;
        statut = null;
        ignorerDansLeCalculRangsListesAttente = false;
        ignorerDansLeCalculBarresInternatAffichees = false;
    }


}
