
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

import javax.xml.bind.annotation.XmlTransient;
import parcoursup.propositions.meilleursbacheliers.MeilleurBachelier;

public class Voeu {

    /* caractéristiques identifiant de manière unique le voeu dans la base de données */
    public final VoeuUID id;

    /* groupe d'affectation du voeu */
    @XmlTransient
    public final GroupeAffectation groupe;

    /* rang initial du voeu dans l'ordre d'appel, avant remontée des meilleurs bacheliers */
    public final int ordreAppelInitial;

    /* rang du voeu dans l'ordre d'appel, après remontée des meilleurs bacheliers */
    public int ordreAppel;

    /* le rang du candidat au classement internat */
    public final int rangInternat;

    /* la position du voeu dans le répondeur automatique (0 = pas de répondeur) */
    public final int rangRepondeur;

    /* est-ce que le répondeur automatique est activé */
    public boolean repondeurActive = false;

    /* le rang du voeu sur liste d'attente, si en attente */
    public int rangListeAttente = 0;

    /* y a t il une demande d'internat avec classement sur ce voeu ? */
    public boolean avecInternatAClassementPropre() {
        return internat != null;
    }

    /* le candidat a-t'il déjà une offre dans cet internat (pour une autre formation) ?*/
    public boolean internatDejaObtenu() {
        return internat != null && internat.estAffecte(id.G_CN_COD);
    }

    /* le candidat a-t'il déjà une offre dans cette formation (sans internat)? */
    public boolean formationDejaObtenue() {
        return groupe != null && groupe.estAffecte(id.G_CN_COD);
    }

    /* le groupe de classement internat. */
    @XmlTransient
    public final GroupeInternat internat;

    public GroupeInternatUID internatID() {
        return internat == null ? null : internat.id;
    }

    /* les différents statuts d'un voeu */
    public enum StatutVoeu {
        enAttenteDeProposition,
        enAttenteDeReponseDuCandidat,
        demissionAutoVoeuAttente,
        acceptationAutomatique,
        affecteJoursPrecedents,
        demissionAutoProposition,
        refusSimule                 //utilisé par le simulateur
    };

    StatutVoeu statut;

    /* getters de statut */
    public boolean estDemissionAutomatique() {
        return statut == StatutVoeu.demissionAutoVoeuAttente
                || statut == StatutVoeu.demissionAutoProposition;
    }

    public boolean estDemissionAutomatiqueVoeuAttente() {
        return statut == StatutVoeu.demissionAutoVoeuAttente;
    }

    public boolean estDemissionAutomatiqueProposition() {
        return statut == StatutVoeu.demissionAutoProposition;
    }

    public boolean estAcceptationAutomatique() {
        return statut == StatutVoeu.acceptationAutomatique;
    }

    public boolean estProposition() {
        return statut == StatutVoeu.acceptationAutomatique
                || statut == StatutVoeu.enAttenteDeReponseDuCandidat
                || statut == StatutVoeu.affecteJoursPrecedents;
    }

    public boolean estPropositionDuJour() {
        return statut == StatutVoeu.acceptationAutomatique
                || statut == StatutVoeu.enAttenteDeReponseDuCandidat;
    }

    public boolean estEnAttenteDeProposition() {
        return statut == StatutVoeu.enAttenteDeProposition;
    }

    public boolean estAffecteJoursPrecedents() {
        return statut == StatutVoeu.affecteJoursPrecedents;
    }

    public void refuserAutomatiquement() {
        if (estAffecteHorsPP()) {
            throw new RuntimeException("Le voeu affecté hors PP " + this + " ne peut être refusé automatiquement");
        }
        if (statut == StatutVoeu.affecteJoursPrecedents) {
            statut = StatutVoeu.demissionAutoProposition;
        } else if (!repondeurActive) {
            throw new RuntimeException("Le statut du voeu " + this + " ne permet pas le refus automatique");
        } else if (statut != StatutVoeu.acceptationAutomatique
                && statut != StatutVoeu.enAttenteDeProposition) {
            throw new RuntimeException("Le statut du voeu " + this + " ne permet pas le refus automatique");
        } else {
            statut = StatutVoeu.demissionAutoVoeuAttente;
        }
    }

    public void proposer() {
        if (statut != StatutVoeu.enAttenteDeProposition) {
            throw new RuntimeException("Le statut du voeu " + this + " ne permet pas une proposition");
        }

        if (repondeurActive) {
            statut = StatutVoeu.acceptationAutomatique;
        } else {
            statut = StatutVoeu.enAttenteDeReponseDuCandidat;
        }
    }

    public void nePasProposer() {
        statut = StatutVoeu.enAttenteDeProposition;
    }

    /* vérifie si le voeu est désactivé du fait d'une demande d'internat */
    public boolean estDesactiveParPositionAdmissionInternat() {
        /* si le candidat demande l'internat mais que son classement
            à l'internat ne passe pas la barre définie par la position
            d'admission, alors on en fait pas de proposition */
        return ((internat != null)
                && !internatDejaObtenu()
                && rangInternat > internat.positionAdmission);
    }

    /* constructeur d'un voeu sans internat ou avec internat sans classement propre 
    (obligatoire ou non-sélectif) */
    public Voeu(
            int G_CN_COD,
            boolean avecInternat,
            GroupeAffectation groupe,
            int ordreAppel,
            int rangRepondeur,
            StatutVoeu statut,
            boolean affecteHorsPP
    ) {
        if (affecteHorsPP && (statut != StatutVoeu.affecteJoursPrecedents)) {
            throw new RuntimeException("Inconsistence logique: un voeu hors PP doit avoir"
                    + "le statut affecteJoursPrecedents");
        }
        if (ordreAppel < 0 || rangRepondeur < 0) {
            throw new RuntimeException("Incohérence dans les paramètres du constructeur de Voeu");
        }
        if (ordreAppel == 0 && statut != StatutVoeu.affecteJoursPrecedents) {
            throw new RuntimeException("Incohérence dans les paramètres du constructeur de Voeu");
        }

        this.id = new VoeuUID(G_CN_COD, groupe.id.G_TA_COD, avecInternat);
        this.groupe = groupe;
        this.ordreAppelInitial = ordreAppel;
        this.ordreAppel = ordreAppel;
        this.internat = null;
        this.rangInternat = 0;
        this.rangRepondeur = rangRepondeur;
        this.statut = statut;
        this.affecteHorsPP = affecteHorsPP;
    }

    /* constructeur d'un voeu avec internat à classement propre */
    public Voeu(
            int G_CN_COD,
            GroupeAffectation groupe,
            int ordreAppel,
            GroupeInternat internat,
            int rangInternat,
            int rangRepondeur,
            StatutVoeu statut,
            boolean affecteHorsPP
    ) {
        if (affecteHorsPP && (statut != StatutVoeu.affecteJoursPrecedents)) {
            throw new RuntimeException("Inconsistence logique: un voeu hors PP doit avoir"
                    + "le statut affecteJoursPrecedents");
        }
        if (ordreAppel < 0 || rangRepondeur < 0 || rangInternat < 0) {
            throw new RuntimeException("Incohérence dans les paramètres du constructeur de Voeu");
        }
        if (ordreAppel == 0 && statut != StatutVoeu.affecteJoursPrecedents) {
            throw new RuntimeException("Incohérence dans les paramètres du constructeur de Voeu");
        }

        this.id = new VoeuUID(G_CN_COD, groupe.id.G_TA_COD, true);
        this.groupe = groupe;
        this.ordreAppelInitial = ordreAppel;
        this.ordreAppel = ordreAppel;
        this.internat = internat;
        this.rangInternat = rangInternat;
        this.rangRepondeur = rangRepondeur;
        this.statut = statut;
        this.affecteHorsPP = affecteHorsPP;
    }

    public void ajouterAuxGroupes() {
        if (estEnAttenteDeProposition()) {
            groupe.ajouterVoeuEnAttenteDeProposition(this);
            if (internat != null) {
                internat.ajouterVoeuEnAttenteDeProposition(this);
            }
        }
        if (estProposition()) {
            groupe.ajouterCandidatAffecte(id.G_CN_COD);
            if (internat != null) {
                internat.ajouterCandidatAffecte(id.G_CN_COD);
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
    public boolean eligibleDispositifMB = false;

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
            throw new RuntimeException("Donnée indisponible");
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
            statut = StatutVoeu.affecteJoursPrecedents;
        }
    }

    /* utilisé par le simulateur */
    public void simulerRefus() {
        statut = StatutVoeu.refusSimule;
    }

    public StatutVoeu getStatut() {
        return statut;
    }

}
