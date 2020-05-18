
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import parcoursup.exceptions.VerificationException;
import parcoursup.propositions.meilleursbacheliers.MeilleurBachelier;

@XmlAccessorType(XmlAccessType.FIELD)
public class Voeu {

    /* caractéristiques identifiant de manière unique le voeu dans la base de données */
    public final VoeuUID id;

    /* groupe d'affectation du voeu */
    @XmlTransient
    public GroupeAffectation groupe;

    public final GroupeAffectationUID groupeUID;

    /* rang initial du voeu dans l'ordre d'appel, avant remontée des meilleurs bacheliers */
    public final int ordreAppelInitial;

    /* rang du voeu dans l'ordre d'appel, après remontée des meilleurs bacheliers */
    int ordreAppel;

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

    public int getOrdreAppel() {
        return ordreAppel;
    }

    public void setOrdreAppel(int oa) {
        ordreAppel = oa;
    }

    /* le rang du candidat au classement internat */
    public final int rangInternat;

    /* la position du voeu dans le répondeur automatique (0 = pas de répondeur) */
    public final int rangRepondeur;

    /* est-ce que le répondeur automatique est activé */
    boolean repondeurActive = false;

    public boolean getRepondeurActive() {
        return repondeurActive;
    }

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
    @XmlTransient
    public GroupeInternat internat;
    public final GroupeInternatUID internatUID;

    public GroupeInternatUID internatID() {
        return internat == null ? null : internat.id;
    }

    /* les différents statuts d'un voeu */
    public enum StatutVoeu {
        EN_ATTENTE_DE_PROPOSITION,
        PROPOSITION_DU_JOUR,
        REP_AUTO_DEMISSION_ATTENTE, //démision auto d'un voeu en attente par le répondeur auitomatique
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

    public void refuserAutomatiquement() throws VerificationException {
        if (estAffecteHorsPP()) {
            throw new VerificationException("Le voeu affecté hors PP " + this + " ne peut être refusé automatiquement");
        }
        if (statut == StatutVoeu.AFFECTE_JOURS_PRECEDENTS) {
            statut = StatutVoeu.REP_AUTO_REFUS_PROPOSITION;
        } else if (!repondeurActive) {
            throw new VerificationException("Le statut du voeu " + this + " ne permet pas le refus automatique");
        } else if (statut != StatutVoeu.REP_AUTO_ACCEPTE
                && statut != StatutVoeu.EN_ATTENTE_DE_PROPOSITION) {
            throw new VerificationException("Le statut du voeu " + this + " ne permet pas le refus automatique");
        } else {
            statut = StatutVoeu.REP_AUTO_DEMISSION_ATTENTE;
        }
    }

    public void proposer() throws VerificationException {
        if (statut != StatutVoeu.EN_ATTENTE_DE_PROPOSITION) {
            throw new VerificationException("Le statut du voeu " + this + " ne permet pas une proposition");
        }

        if (repondeurActive) {
            statut = StatutVoeu.REP_AUTO_ACCEPTE;
        } else {
            statut = StatutVoeu.PROPOSITION_DU_JOUR;
        }
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
            GroupeAffectation groupe,
            int ordreAppel,
            int ordreAppelAffiche,
            int rangRepondeur,
            StatutVoeu statut,
            boolean affecteHorsPP
    ) throws VerificationException {
        if (affecteHorsPP && (statut != StatutVoeu.AFFECTE_JOURS_PRECEDENTS)) {
            throw new VerificationException("Inconsistence logique: un voeu hors PP doit avoir"
                    + "le statut affecteJoursPrecedents");
        }
        if (ordreAppel < 0 || rangRepondeur < 0) {
            throw new VerificationException("Rangs négatifs");
        }
        if (ordreAppel == 0 && statut != StatutVoeu.AFFECTE_JOURS_PRECEDENTS) {
            throw new VerificationException("Ordre appel manquant");
        }

        this.id = new VoeuUID(gCnCod, groupe.id.gTaCod, avecInternat);
        this.groupe = groupe;
        this.groupeUID = groupe.id;
        this.ordreAppelInitial = ordreAppel;
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
            GroupeAffectation groupe,
            int ordreAppel,
            int ordreAppelAffiche,
            GroupeInternat internat,
            int rangInternat,
            int rangRepondeur,
            StatutVoeu statut,
            boolean affecteHorsPP
    ) throws VerificationException {
        if (affecteHorsPP && (statut != StatutVoeu.AFFECTE_JOURS_PRECEDENTS)) {
            throw new VerificationException("Inconsistence logique: un voeu hors PP doit avoir"
                    + "le statut affecteJoursPrecedents");
        }
        if (ordreAppel < 0 || rangRepondeur < 0 || rangInternat < 0) {
            throw new VerificationException("Incohérence dans les paramètres du constructeur de Voeu");
        }
        if (ordreAppel == 0 && statut != StatutVoeu.AFFECTE_JOURS_PRECEDENTS) {
            throw new VerificationException("Incohérence dans les paramètres du constructeur de Voeu");
        }

        this.id = new VoeuUID(gCnCod, groupe.id.gTaCod, true);
        this.groupe = groupe;
        this.groupeUID = groupe.id;
        this.ordreAppelInitial = ordreAppel;
        this.ordreAppel = ordreAppel;
        this.ordreAppelAffiche = ordreAppelAffiche;
        this.internat = internat;
        this.internatUID = internat.id;
        this.rangInternat = rangInternat;
        this.rangRepondeur = rangRepondeur;
        this.statut = statut;
        this.affecteHorsPP = affecteHorsPP;
    }

    public Voeu(Voeu o) {
        this.id = o.id;
        this.groupeUID = new GroupeAffectationUID(o.groupeUID);
        this.groupe = null;
        this.internat = null;
        this.ordreAppelInitial = o.ordreAppelInitial;
        this.ordreAppel = o.ordreAppel;
        this.ordreAppelAffiche = o.ordreAppelAffiche;
        this.rangInternat = o.rangInternat;
        this.rangRepondeur = o.rangRepondeur;
        this.statut = o.statut;
        this.affecteHorsPP = o.affecteHorsPP;
        this.internatUID = o.internatUID;
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

    /* statuts spéciaux dus aux erreurs de classement, utiles pour la vérifiction des résultats */
    public void setAnnulationDemission() {
        annulationDemission = true;
    }

    public boolean estAnnulationDemission() {
        return annulationDemission;
    }

    private boolean annulationDemission = false;

    public void setCorrectionClassement() {
        correctionClassement = true;
    }

    /* voeu ayant subit une modif de classement, information
    utilisée lors de l'étape de vérification */
    public boolean estCorrectionClassement() {
        return correctionClassement;
    }

    private boolean correctionClassement = false;

    /* voeu affecté hors procédure principale (CAES ou PC ou inscription à la rentrée) */
    public boolean estAffecteHorsPP() {
        return affecteHorsPP;
    }

    private final boolean affecteHorsPP;

    /* proposition faite au titre du dispositif meilleurs bacheliers */
    boolean eligibleDispositifMB = false;

    public boolean getEligibleDispositifMB() {
        return eligibleDispositifMB;
    }

    public void setEligibleDispositifMB(boolean eligible) {
        eligibleDispositifMB = eligible;
    }

    /* le candidat est-il meilleur bachelier dans son lycée,
    si oui on conseve sa moyenne */
    private MeilleurBachelier mb = null;

    public void setMeilleurBachelier(MeilleurBachelier mb) {
        this.mb = mb;
    }

    public boolean estMeilleurBachelier() {
        return mb != null;
    }

    public double moyenneBac() {
        if (mb == null) {
            throw new IllegalStateException("Donnée indisponible");
        }
        return mb.moyenne;
    }

    @Override
    public String toString() {
        if (internat == null) {
            return "(" + id.toString() + ")";
        } else {
            return "(" + id.toString() + " avec demande internat " + internat.toString() + ")";
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
        if (ordreAppel > 0 && (internat == null || rangInternat > 0)) {
            statut = StatutVoeu.EN_ATTENTE_DE_PROPOSITION;
        } else {
            statut = StatutVoeu.REFUS_OU_DEMISSION;
        }
    }

    public StatutVoeu getStatut() {
        return statut;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Voeu) {
            return id.equals(((Voeu) obj).id);
        } else {
            throw new ClassCastException("Test d'égalité imprévu");
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    private Voeu() {
        id = null;
        groupe = null;
        groupeUID = null;
        ordreAppelInitial = 0;
        ordreAppelAffiche = 0;
        rangInternat = 0;
        rangRepondeur = 0;
        internat = null;
        internatUID = null;
        affecteHorsPP = false;
    }

}
