
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
package parcoursup.propositions.algo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupeAffectation {

    /* le id d'affectation identifiant de manière unique le groupe dans la base */
    public final GroupeAffectationUID id;

    /* le nombre de recrutements souhaité par la formation */
    public final int nbRecrutementsSouhaite;

    /* le rang limite des candidats (dans l'ordre d'appel): 
    tous les candidats de rang inférieur reçoivent une proposition */
    public final int rangLimite;

    /* évaluation du rang du dernier appelé à la date pivot.
    Utilisée pour la gestion des places d'internats */
    public final int estimationRangDernierAppeleADatePivot;

    /* le rang du dernier appelé, affiché dans les formations avec internat */
    public int rangDernierAppeleAffiche = 0;

    /* constructeur */
    public GroupeAffectation(
            int nbRecrutementsSouhaite,
            GroupeAffectationUID id,
            int rangLimite,
            int rangDernierAppele) {
        if (nbRecrutementsSouhaite < 0 || rangLimite < 0 || rangDernierAppele < 0) {
            throw new RuntimeException("Incohérence dans les paramètres du constructeur de GroupeAffectation");
        }
        this.id = id;
        this.nbRecrutementsSouhaite = nbRecrutementsSouhaite;
        this.rangLimite = rangLimite;
        if (rangDernierAppele == 0 || GroupeInternat.nbJoursCampagne <= 1) {
            this.estimationRangDernierAppeleADatePivot = Integer.MAX_VALUE;
        } else if (GroupeInternat.nbJoursCampagne < GroupeInternat.nbJoursCampagneDatePivotInternats) {
            this.estimationRangDernierAppeleADatePivot
                    = Math.max(rangLimite,
                            rangDernierAppele
                            * GroupeInternat.nbJoursCampagneDatePivotInternats
                            / (GroupeInternat.nbJoursCampagne - 1)
                    );
        } else {
            this.estimationRangDernierAppeleADatePivot
                    = Math.max(rangLimite, rangDernierAppele);
        }
    }

    /* ajoute un voeu dans le groupe */
    void ajouterVoeuEnAttenteDeProposition(Voeu voe) {
        voeuxEnAttente.add(voe);
    }

    /* ajoute un candidat affecté */
    public void ajouterCandidatAffecte(int G_CN_COD) {
        candidatsAffectes.add(G_CN_COD);
    }

    public void reinitialiser() {
        voeuxEnAttente.clear();
        candidatsAffectes.clear();
    }

    public boolean estAffecte(int G_CN_COD) {
        return candidatsAffectes.contains(G_CN_COD);
    }

    /* met a jour le statut aProposer, pour chaque voeu du groupe */
    void mettreAJourPropositions() {

        int aPourvoir = nbPlacesVacantes();

        voeuxEnAttente.forEach((v) -> {
            v.nePasProposer();
        });

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
                (un avec et un sans internat). Par contre les voeux non-consécutifs concernent
                nécessairement des candidats différents.
            
                Remarque: le rang limite d'appel se base sur l'ordre d'appel du candidat
                avant les remontées MB.            
             */
            if (aPourvoir > 0
                    || v.ordreAppelInitial <= rangLimite
                    || v.formationDejaObtenue() /* variante permet d'éviter les sous-capacités internat */
                    || dernierCandidatAvecProposition == v.id.G_CN_COD) {

                v.proposer();

                /* on décroit la capacité résiduelle si il y a lieu de le faire */
                if (!v.formationDejaObtenue()
                        && dernierCandidatAvecProposition != v.id.G_CN_COD) {
                    aPourvoir--;
                }

                dernierCandidatAvecProposition = v.id.G_CN_COD;

            }
        }
    }

    /* le nombre de places vacantes au lancement du calcul.
    Peut être négatif en cas de modification à la baisse 
    des paramètres de surbooking. */
    int nbPlacesVacantes() {
        return Integer.max(0, nbRecrutementsSouhaite - candidatsAffectes.size());
    }

    /* la liste initiale des voeuxEnAttente du groupe, triée dans l'ordre d'appel du candidat.
    Remarque: c'est un ordre partiel car il peut y avoir deux voeuxEnAttente du même candidat,
    un avec internat et l'autre sans. */
    public final List<Voeu> voeuxEnAttente = new ArrayList<>();

    /* trie les voeuxEnAttente dans l'ordre d'appel */
    public List<Voeu> voeuxTriesParOrdreAppel() {
        voeuxEnAttente.sort((Voeu v1, Voeu v2) -> v1.ordreAppel - v2.ordreAppel);
        return Collections.unmodifiableList(voeuxEnAttente);
    }

    private final Set<Integer> candidatsAffectes = new HashSet<>();

    @Override
    public String toString() {
        return id.toString();
    }

}
