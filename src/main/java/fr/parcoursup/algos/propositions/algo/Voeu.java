
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
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
import java.util.Objects;

/* annotation required for statut to be serialized */
@XmlAccessorType(XmlAccessType.FIELD)
public class Voeu implements Serializable {

    /* caractéristiques identifiant de manière unique le voeu dans la base de données */
    @NotNull
    public final VoeuUID id;

    @NotNull
    public final GroupeAffectationUID groupeUID;

    /* rang initial du voeu dans l'ordre d'appel */
    public final int ordreAppel;

    public final @NotNull StatutVoeu statut;

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
    private final int rangPreferencesCandidat;

    /* le voeu doit-il être ignoré dans le calcul des rangs sur liste d'atte,te (par exemple dan sle cas d'une annulation de démission demandée par le candidat,
    ou en cas de modif de classemnt erronné) */
    public final boolean ignorerDansLeCalculRangsListesAttente;

    /* le voeu doit-il être ignoré dans le calcul des barres internats affichées aux candidats en attente, par exemple
     en cas d'affectation directe par une CAES */
    public final boolean ignorerDansLeCalculBarresInternatAffichees;


    public int getRangPreferencesCandidat() { return rangPreferencesCandidat; }

    /* est-ce que le répondeur automatique est activé.
    * Utilisé pour les vérifications des données. */
    boolean estRepondeurActivable() { return rangPreferencesCandidat > 0; }

    /* le rang du voeu sur liste d'attente, si en attente */
    private int rangListeAttente = 0;
    
    /* le rang du voeu sur liste d'attente de la veille, si en attente */
    private int rangListeAttenteVeille;

    public int getRangListeAttente() {
        return rangListeAttente;
    }

    public void setRangListeAttente(int rang) {
        rangListeAttente = rang;
    }

    public int getRangListeAttenteVeille() {
        return rangListeAttenteVeille;
    }

    public void setRangListeAttenteVeille(int rang) {
        this.rangListeAttenteVeille = rang;
    }

    public final @Nullable GroupeInternatUID internatUID;

    /* y a-t-il une demande d'internat avec classement sur ce voeu ? */
    public boolean avecInternatAClassementPropre() {
        return internatUID != null;
    }

    @Nullable CandidatAffecteInternat getAffectationInternat() {
        if(internatUID == null) return null;
        else return new CandidatAffecteInternat(this.id.gCnCod, this.internatUID);
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
            @NotNull StatutVoeu statut,
            boolean affecteHorsPP,
            boolean ignorerDansLeCalculRangsListesAttente,
            boolean ignorerDansLeCalculBarresInternatAffichees,
            Integer rangListeAttenteVeille
    ) throws VerificationException {

        this.id = new VoeuUID(gCnCod, groupeUID.gTaCod, avecInternat);
        this.groupeUID = groupeUID;
        this.ordreAppel = ordreAppel;
        this.ordreAppelAffiche = ordreAppelAffiche;
        this.internatUID = null;
        this.rangInternat = 0;
        this.rangPreferencesCandidat = rangPreferencesCandidat;
        this.statut = statut;
        this.affecteHorsPP = affecteHorsPP;
        this.ignorerDansLeCalculRangsListesAttente = ignorerDansLeCalculRangsListesAttente;
        this.ignorerDansLeCalculBarresInternatAffichees = ignorerDansLeCalculBarresInternatAffichees;
        this.rangListeAttenteVeille = Objects.requireNonNullElse(rangListeAttenteVeille, 0);

        if (affecteHorsPP && !StatutVoeu.aEteProposeJoursPrecedents(statut)) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_INCONSISTENCE_STATUT_HORS_PP, this.id);
        }
        if (ordreAppel < 0 || rangPreferencesCandidat < 0) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_RANGS_NEGATIFS, this.id);
        }
        if (ordreAppel == 0 && !StatutVoeu.aEteProposeJoursPrecedents(statut) && statut != StatutVoeu.NON_CLASSE) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_ORDRE_APPEL_MANQUANT, this.id);
        }

    }


        /* constructeur d'un voeu avec internat à classement propre */
    @SuppressWarnings("NullableProblems")
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
            boolean ignorerDansLeCalculBarresInternatAffichees,
            Integer rangListeAttenteVeille
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
        this.rangListeAttenteVeille = Objects.requireNonNullElse(rangListeAttenteVeille,0);

        if (affecteHorsPP && !StatutVoeu.aEteProposeJoursPrecedents(statut)) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_INCONSISTENCE_STATUT_HORS_PP, this.id);
        }
        if (ordreAppel < 0 || rangPreferencesCandidat < 0 || rangInternat < 0) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_RANGS_NEGATIFS, this.id);
        }
        if (ordreAppel == 0 && !StatutVoeu.aEteProposeJoursPrecedents(statut) && statut != StatutVoeu.NON_CLASSE) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_ORDRE_APPEL_MANQUANT, this.id);
        }
        if(internatUID == null) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_INTERNAT_NULL, this.id);
        }
    }


    @SuppressWarnings({"unused", "CopyConstructorMissesField"})
    private Voeu(Voeu o) throws VerificationException {
        throw new VerificationException(VerificationExceptionMessage.MESSAGE, "La copie de voeu n'est pas autorisée");
    }

    public Voeu(Voeu o, @NotNull StatutVoeu statut) {
        this.statut = statut;

        this.id = o.id;
        this.groupeUID = o.groupeUID;
        this.ordreAppel = o.ordreAppel;
        this.ordreAppelAffiche = o.ordreAppelAffiche;
        this.rangInternat = o.rangInternat;
        this.rangPreferencesCandidat = o.rangPreferencesCandidat;
        this.affecteHorsPP = o.affecteHorsPP;
        this.internatUID = o.internatUID;
        this.rangListeAttente = o.rangListeAttente;
        this.ignorerDansLeCalculRangsListesAttente = o.ignorerDansLeCalculRangsListesAttente;
        this.ignorerDansLeCalculBarresInternatAffichees = o.ignorerDansLeCalculBarresInternatAffichees;
        this.rangListeAttenteVeille = o.rangListeAttenteVeille;
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

    /**
     * Utilisé par les désérialisations Json et XML
     */
    @SuppressWarnings("unused")
    private Voeu() {
        id = new VoeuUID(0,0,false);
        groupeUID = new GroupeAffectationUID(0,0,0);
        ordreAppel = 0;
        ordreAppelAffiche = 0;
        rangInternat = 0;
        rangPreferencesCandidat = 0;
        internatUID = null;
        affecteHorsPP = false;
        statut = StatutVoeu.NON_CLASSE;
        ignorerDansLeCalculRangsListesAttente = false;
        ignorerDansLeCalculBarresInternatAffichees = false;
        rangListeAttenteVeille = 0;
    }

    /* utilisé pour les tests */
    public Voeu(
            int gCnCod,
            boolean avecInternat,
            @NotNull GroupeAffectationUID groupeUID,
            int ordreAppel,
            int ordreAppelAffiche,
            int rangPreferencesCandidat,
            @NotNull StatutVoeu statut,
            boolean affecteHorsPP
    ) throws VerificationException {
        this.id = new VoeuUID(gCnCod, groupeUID.gTaCod, avecInternat);
        this.groupeUID = groupeUID;
        this.ordreAppel = ordreAppel;
        this.ordreAppelAffiche = ordreAppelAffiche;
        this.internatUID = null;
        this.rangInternat = 0;
        this.rangPreferencesCandidat = rangPreferencesCandidat;
        this.statut = statut;
        this.affecteHorsPP = affecteHorsPP;
        this.ignorerDansLeCalculRangsListesAttente = false;
        this.ignorerDansLeCalculBarresInternatAffichees = false;
        this.rangListeAttenteVeille =  0;

        if (affecteHorsPP && !StatutVoeu.aEteProposeJoursPrecedents(statut)) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_INCONSISTENCE_STATUT_HORS_PP, this.id);
        }
        if (ordreAppel < 0 || rangPreferencesCandidat < 0) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_RANGS_NEGATIFS, this.id);
        }
        if (ordreAppel == 0 && !StatutVoeu.aEteProposeJoursPrecedents(statut) && statut != StatutVoeu.NON_CLASSE) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_ORDRE_APPEL_MANQUANT, this.id);
        }

    }

    /* utilisé pour les tests */
    public Voeu(
            int gCnCod,
            @NotNull GroupeAffectationUID groupeUID,
            int ordreAppel,
            int ordreAppelAffiche,
            @Nullable GroupeInternatUID internatUID,
            int rangInternat,
            int rangPreferencesCandidat,
            @NotNull StatutVoeu statut,
            boolean affecteHorsPP) throws VerificationException {
        this.id = new VoeuUID(gCnCod, groupeUID.gTaCod, true);
        this.groupeUID = groupeUID;
        this.ordreAppel = ordreAppel;
        this.ordreAppelAffiche = ordreAppelAffiche;
        this.internatUID = internatUID;
        this.rangInternat = rangInternat;
        this.rangPreferencesCandidat = rangPreferencesCandidat;
        this.statut = statut;
        this.affecteHorsPP = affecteHorsPP;
        this.ignorerDansLeCalculRangsListesAttente = false;
        this.ignorerDansLeCalculBarresInternatAffichees = false;
        this.rangListeAttenteVeille = 0;

        if (affecteHorsPP && !StatutVoeu.aEteProposeJoursPrecedents(statut)) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_INCONSISTENCE_STATUT_HORS_PP, this.id);
        }
        if (ordreAppel < 0 || rangPreferencesCandidat < 0 || rangInternat < 0) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_RANGS_NEGATIFS, this.id);
        }
        if (ordreAppel == 0 && !StatutVoeu.aEteProposeJoursPrecedents(statut) && statut != StatutVoeu.NON_CLASSE) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_ORDRE_APPEL_MANQUANT, this.id);
        }
        if(internatUID == null) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_INTERNAT_NULL, this.id);
        }

    }




}
