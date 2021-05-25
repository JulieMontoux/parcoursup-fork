
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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /* rang du voeu dans l'ordre d'appel, tel qu'affiché dans l'interface.
    Il y a quelques rares cas d'erreurs de classement par les formations 
    (de l'ordre de la centaine par an). Dans ce cas un candidat peut être
    remonté dans l'ordre d'appel, ce qui crée deux candidats avec le
    même ordre d'appel, avec priorité au candidat
    remonté. Dans ce cas le champ C_CG_ORD_APP utilisé par l'algorithme
    est incrémenté pour tous les candidats suivants, afin de maintenir 
    la propriété d'unicité du candidat pour un ordre d'appel donnée. Le champ
    C_CG_ORD_APP_AFF reste, lui égal à sa valeur initiale, 
    sauf pour le candidat qui a bénéficié de la remontée dans le classement. 
    C'est le champ C_CG_ORD_APP_AFF qui est affiché au candidat surle site de
    Parcoursup, et utilisé pour la mise à jour des affichages. */
    public final int ordreAppelAffiche;

    /* le rang du candidat au classement internat */
    public final int rangInternat;

    /* la position du voeu dans le répondeur automatique (0 = pas de répondeur) */
    public final int rangRepondeur;

    /* est-ce que le répondeur automatique est activé */
    private boolean repondeurActive;
    public boolean getRepondeurActive() { return repondeurActive; }
    public void setRepondeurActive(boolean b) { repondeurActive= b; }

    /* le rang du voeu sur liste d'attente, si en attente */
    int rangListeAttente = 0;

    public int getRangListeAttente() {
        return rangListeAttente;
    }

    public void setRangListeAttente(int rang) {
        rangListeAttente = rang;
    }

    /* y a t il une demande d'internat avec classement sur ce voeu ? */
    public boolean avecInternatAClassementPropre() {
        return internat != null;
    }

    /* le candidat a-t'il déjà une offre dans cet internat (pour une autre formation) ?*/
    public boolean internatDejaObtenu() {
        return internat != null && internat.estAffecte(id.gCnCod);
    }

    /* le candidat a-t'il déjà une offre dans cette formation (sans internat)? */
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

    /* les différents statuts d'un voeu */
    @XmlRootElement
    public enum StatutVoeu {
        EN_ATTENTE_DE_PROPOSITION,
        PROPOSITION_DU_JOUR,
        REP_AUTO_DEMISSION_ATTENTE, //démission auto d'un voeu en attente par le répondeur auitomatique
        REP_AUTO_ACCEPTE,
        AFFECTE_JOURS_PRECEDENTS,
        REP_AUTO_REFUS_PROPOSITION, //refus automatique d'une proposition via le répondeur automatique
        REFUS_OU_DEMISSION              //refus proposition ou demission voeu en attente
    }

    StatutVoeu statut;

    /* getters de statut */
    public boolean estDemissionAutomatique() {
        return statut == StatutVoeu.REP_AUTO_DEMISSION_ATTENTE
                || statut == StatutVoeu.REP_AUTO_REFUS_PROPOSITION;
    }

    public boolean estDemissionAutomatiqueVoeuAttente() {
        return statut == StatutVoeu.REP_AUTO_DEMISSION_ATTENTE;
    }

    public boolean estDemissionAutomatiqueProposition() {
        return statut == StatutVoeu.REP_AUTO_REFUS_PROPOSITION;
    }

    public boolean estAcceptationAutomatique() {
        return statut == StatutVoeu.REP_AUTO_ACCEPTE;
    }

    public boolean estProposition() {
        return statut == StatutVoeu.REP_AUTO_ACCEPTE
                || statut == StatutVoeu.PROPOSITION_DU_JOUR
                || statut == StatutVoeu.AFFECTE_JOURS_PRECEDENTS;
    }

    public boolean estRefus() {
        return statut == StatutVoeu.REP_AUTO_REFUS_PROPOSITION
	        || statut == StatutVoeu.REP_AUTO_DEMISSION_ATTENTE
            || statut == StatutVoeu.REFUS_OU_DEMISSION;
    }

    public boolean estPropositionDuJour() {
        return statut == StatutVoeu.REP_AUTO_ACCEPTE
                || statut == StatutVoeu.PROPOSITION_DU_JOUR;
    }

    public boolean estEnAttenteDeProposition() {
        return statut == StatutVoeu.EN_ATTENTE_DE_PROPOSITION;
    }

    public boolean estAffecteJoursPrecedents() {
        return statut == StatutVoeu.AFFECTE_JOURS_PRECEDENTS;
    }

    public StatutVoeu getStatut() {
        return statut;
    }

    public void refuserAutomatiquement() throws VerificationException {
        if (estAffecteHorsPP()) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_HORS_PP_NON_REFUSABLE_AUTOMATIQUEMENT, this);
        }
        if (statut == StatutVoeu.AFFECTE_JOURS_PRECEDENTS) {
            statut = StatutVoeu.REP_AUTO_REFUS_PROPOSITION;
        } else if (!repondeurActive) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_REFUS_AUTOMATIQUE_IMPOSSIBLE, this);
        } else if (statut != StatutVoeu.REP_AUTO_ACCEPTE
                && statut != StatutVoeu.EN_ATTENTE_DE_PROPOSITION) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_REFUS_AUTOMATIQUE_IMPOSSIBLE, this);
        } else {
            statut = StatutVoeu.REP_AUTO_DEMISSION_ATTENTE;
        }
    }

    public void proposer() throws VerificationException {
        if (statut != StatutVoeu.EN_ATTENTE_DE_PROPOSITION) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_REFUS_AUTOMATIQUE_IMPOSSIBLE, this);
        }
        statut = repondeurActive ? StatutVoeu.REP_AUTO_ACCEPTE : StatutVoeu.PROPOSITION_DU_JOUR;
    }

    public void nePasProposer() {
        statut = StatutVoeu.EN_ATTENTE_DE_PROPOSITION;
    }

    /* vérifie si le voeu est désactivé du fait d'une demande d'internat */
    public boolean estDesactiveParPositionAdmissionInternat() {
        /* si le candidat demande l'internat mais que son classement
            à l'internat ne passe pas la barre définie par la position
            d'admission, alors on en fait pas de proposition */
        return ((internat != null)
                && !internatDejaObtenu()
                && rangInternat > internat.getPositionAdmission());
    }

    /* constructeur d'un voeu sans internat ou avec internat sans classement propre 
    (obligatoire ou non-sélectif) */
    public Voeu(
            int gCnCod,
            boolean avecInternat,
            GroupeAffectationUID groupeUID,
            int ordreAppel,
            int ordreAppelAffiche,
            int rangRepondeur,
            StatutVoeu statut,
            boolean affecteHorsPP
    ) throws VerificationException {
        if (affecteHorsPP && (statut != StatutVoeu.AFFECTE_JOURS_PRECEDENTS)) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_INCONSISTENCE_STATUT_HORS_PP);
        }
        if (ordreAppel < 0 || rangRepondeur < 0) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_RANGS_NEGATIFS);
        }
        if (ordreAppel == 0 && statut != StatutVoeu.AFFECTE_JOURS_PRECEDENTS) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_ORDRE_APPEL_MANQUANT);
        }
        if(groupeUID == null) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_GROUPE_NULL);
        }

        this.id = new VoeuUID(gCnCod, groupeUID.gTaCod, avecInternat);
        this.groupeUID = groupeUID;
        this.ordreAppel = ordreAppel;
        this.ordreAppelAffiche = ordreAppelAffiche;
        this.internat = null;
        this.internatUID = null;
        this.rangInternat = 0;
        this.rangRepondeur = rangRepondeur;
        this.statut = statut;
        this.affecteHorsPP = affecteHorsPP;
    }

    /* constructeur d'un voeu avec internat à classement propre */
    public Voeu(
            int gCnCod,
            GroupeAffectationUID groupeUID,
            int ordreAppel,
            int ordreAppelAffiche,
            GroupeInternatUID internatUID,
            int rangInternat,
            int rangRepondeur,
            StatutVoeu statut,
            boolean affecteHorsPP
    ) throws VerificationException {
        if (affecteHorsPP && (statut != StatutVoeu.AFFECTE_JOURS_PRECEDENTS)) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_INCONSISTENCE_STATUT_HORS_PP);
        }
        if (ordreAppel < 0 || rangRepondeur < 0 || rangInternat < 0) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_INCOHERENCE_PARAMETRES);
        }
        if (ordreAppel == 0 && statut != StatutVoeu.AFFECTE_JOURS_PRECEDENTS) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_INCOHERENCE_PARAMETRES);
        }
        if(groupeUID == null) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_GROUPE_NULL);
        }
        if(internatUID == null) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_INTERNAT_NULL);
        }
        this.id = new VoeuUID(gCnCod, groupeUID.gTaCod, true);
        this.groupeUID = groupeUID;
        this.ordreAppel = ordreAppel;
        this.ordreAppelAffiche = ordreAppelAffiche;
        this.internatUID = internatUID;
        this.rangInternat = rangInternat;
        this.rangRepondeur = rangRepondeur;
        this.statut = statut;
        this.affecteHorsPP = affecteHorsPP;
    }

    public Voeu(Voeu o) {
        this.id = o.id;
        this.groupeUID = o.groupeUID;
        this.groupe = null;
        this.internat = null;
        this.ordreAppel = o.ordreAppel;
        this.ordreAppelAffiche = o.ordreAppelAffiche;
        this.rangInternat = o.rangInternat;
        this.rangRepondeur = o.rangRepondeur;
        this.statut = o.statut;
        this.repondeurActive = o.repondeurActive;
        this.affecteHorsPP = o.affecteHorsPP;
        this.internatUID = o.internatUID;
        this.typeMaj = o.typeMaj;
        this.rangListeAttente = o.rangListeAttente;
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
            return "(" + id.toString() + ")";
        } else {
            return "(" + id.toString() + " avec demande internat " + internatUID.toString() + ")";
        }
    }

    /* utilisé par le simulateur */
    public void simulerEtape() {
        if (estPropositionDuJour()) {
            statut = StatutVoeu.AFFECTE_JOURS_PRECEDENTS;
        }
    }

    /* utilisé par le simulateur */
    public void simulerRefus() {
        statut = StatutVoeu.REFUS_OU_DEMISSION;
    }

    /* utilisé par le simulateur */
    public void simulerAcceptation() {
        statut = StatutVoeu.AFFECTE_JOURS_PRECEDENTS;
    }

    /* utilisé par le simulateur */
    public void simulerEnAttente() {
        if (ordreAppel > 0 && (internatUID == null || rangInternat > 0)) {
            statut = StatutVoeu.EN_ATTENTE_DE_PROPOSITION;
        } else {
            statut = StatutVoeu.REFUS_OU_DEMISSION;
        }
    }

    //flag a_ve_typ_maj gardant trace des éventuelles modifs de classement
    /*
    0 : RAS
    1 : Annulation de démission d'un voeu (on ne tient pas compte de ces candidats dans le calcul des listes d'attente)
    10 : Modification de classement : Candidat non classé devient classé (on ne tient pas compte de ces candidats dans le calcul des listes d'attente)
    30 : Utilisé dans la mise à jour des classements erronés pour les candidats qui passent de classés à non classés dans le
        classement corrigé (On tient compte de ces candidats dans le calcul des listes d'attente))
    */
    private int typeMaj;
    public static final int ANNULATION_DEMISSION_TYPE_MAJ = 1;
    public static final int MODIF_CLASSEMENT_TYPE_MAJ1 = 10;
    public static final int MODIF_CLASSEMENT_TYPE_MAJ2 = 30;

    private static final Set<Integer> typeMajValeursPossibles = Stream.of(0,1,10,11,30,31).collect(Collectors.toCollection(HashSet::new));
    public void setTypeMaj(int typeMaj) throws VerificationException {
        if(!typeMajValeursPossibles.contains(typeMaj)) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_TYPE_MAJ_INCONNUE, typeMaj);
        }
        this.typeMaj = typeMaj;
    }

    public boolean estAnnulationDemission() {
        return (typeMaj % 2 == ANNULATION_DEMISSION_TYPE_MAJ);
    }

    /* voeu ayant subit une modif de classement, information
    utilisée lors de l'étape de vérification */
    public boolean estCorrectionClassement() {
        return (typeMaj / 10 != 0);
    }

    public boolean ignoreDansLeCalculDesRangSurListesDattente() {
        return estAnnulationDemission() || typeMaj == MODIF_CLASSEMENT_TYPE_MAJ1;
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

    private Voeu() {
        id = new VoeuUID(0,0,false);
        groupe = null;
        groupeUID = new GroupeAffectationUID(0,0,0);
        ordreAppel = 0;
        ordreAppelAffiche = 0;
        rangInternat = 0;
        rangRepondeur = 0;
        internat = null;
        internatUID = null;
        affecteHorsPP = false;
        repondeurActive = false;
        statut = null;
    }


}
