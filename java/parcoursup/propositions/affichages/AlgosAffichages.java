/* 

    Copyright 2019 © Ministère de l'Enseignement Supérieur, de la Recherche et de
    l'Innovation, Hugo Gimbert (hugo.gimbert@enseignementsup.gouv.fr)

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
package parcoursup.propositions.affichages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import parcoursup.propositions.algo.AlgoPropositionsSortie;
import parcoursup.propositions.algo.GroupeAffectation;
import parcoursup.propositions.algo.GroupeInternat;
import parcoursup.propositions.algo.Voeu;
import parcoursup.propositions.algo.VoeuUID;
import parcoursup.verification.VerificationAffichages;
import parcoursup.exceptions.VerificationException;

public class AlgosAffichages {

    private static final Logger LOGGER = Logger.getLogger(AlgosAffichages.class.getSimpleName());

    public static void mettreAJourAffichages(
            AlgoPropositionsSortie sortie,
            Set<VoeuUID> voeuxAvecPropositionDansMemeFormation,
            Set<VoeuUID> propositionsDuJour) throws VerificationException {

        Map<GroupeAffectation, List<Voeu>> voeuxParGroupes = new HashMap<>();
        Map<GroupeInternat, List<Voeu>> voeuxParInternat = new HashMap<>();

        for (Voeu v : sortie.voeux) {
            if (v.estAffecteJoursPrecedents() || v.estAffecteHorsPP()) {
                continue;
            }
            if (!voeuxParGroupes.containsKey(v.groupe)) {
                voeuxParGroupes.put(v.groupe, new ArrayList<>());
            }
            voeuxParGroupes.get(v.groupe).add(v);
            if (v.internat != null) {
                if (!voeuxParInternat.containsKey(v.internat)) {
                    voeuxParInternat.put(v.internat, new ArrayList<>());
                }
                voeuxParInternat.get(v.internat).add(v);
            }
        }

        LOGGER.log(Level.INFO,
                "Mise a jour des rangs sur liste d'attente "
                + "et derniers appelés affichés");
        for (Entry<GroupeAffectation, List<Voeu>> entry : voeuxParGroupes.entrySet()) {
            GroupeAffectation groupe = entry.getKey();
            List<Voeu> voeux = entry.getValue();
            mettreAJourRangsListeAttente(
                    voeux,
                    voeuxAvecPropositionDansMemeFormation,
                    propositionsDuJour);
            mettreAJourRangDernierAppeleAffiche(groupe, voeux);
        }

        LOGGER.log(Level.INFO,
                "Mise a jour des rangs des derniers appeles affichés dans"
                + "les internats");
        for (Entry<GroupeInternat, List<Voeu>> entry : voeuxParInternat.entrySet()) {
            GroupeInternat internat = entry.getKey();
            List<Voeu> voeux = entry.getValue();
            mettreAJourRangDernierAppeleAffiche(internat, voeux);
        }

        LOGGER.log(Level.INFO,
                "Vérification des rangs sur liste attente");

        for(GroupeAffectation groupe : sortie.groupes) {
            VerificationAffichages.verifierRangsSurListeAttente(groupe);
        }

    }

    /* met à jour les rangs sur liste d'attente */
    private static void mettreAJourRangsListeAttente(
            List<Voeu> voeux,
            Set<VoeuUID> voeuxAvecPropositionDansMemeFormation,
            Set<VoeuUID> propositionsDuJour) {

        int dernierCandidatEnAttente = -1;
        int nbCandidatsEnAttente = 0;

        //initialisation
        voeux.forEach(voeu -> 
            voeu.setRangListeAttente(0)
        );

        voeux.sort((Voeu v1, Voeu v2) -> v1.getOrdreAppel() - v2.getOrdreAppel());
        for (Voeu voeu : voeux) {

            if (voeu.estEnAttenteDeProposition()) {
                /* on ne tient pas compte des candidats ayant eu 
            une proposition dans la même formation */
 /* afin que les rangs sur liste d'attente affichés aux candidats soient monotones,
            on ne tient pas compte des annulations de démissions 
            et des modifications de classement.
            Il peut y avoir deux voeux consecutifs pour le même candidat: un avec 
            et un sans internat.
                 */
                if (!voeuxAvecPropositionDansMemeFormation.contains(voeu.id)
                        && !propositionsDuJour.contains(
                                new VoeuUID(voeu.id.gCnCod, voeu.id.gTaCod, !voeu.id.iRhCod)
                        )
                        && !voeu.estAnnulationDemission()
                        && !voeu.estCorrectionClassement()
                        && voeu.id.gCnCod != dernierCandidatEnAttente) {
                    nbCandidatsEnAttente++;
                    dernierCandidatEnAttente = voeu.id.gCnCod;
                }

                voeu.setRangListeAttente(Math.max(1, nbCandidatsEnAttente));

            }

        }

    }

    /* Met à jour le rang du dernier appelé affiché pour ce groupe d'affectation */
    private static void mettreAJourRangDernierAppeleAffiche(
            GroupeAffectation groupe,
            List<Voeu> voeux
    ) {
        /* Parmi les propositions du jour, on cherche celle qui a le plus haut
            rang dans l'ordre d'appel. On trie les voeux en attente du moins bien classé 
            au mieux classé, c'est à dire les plus hauts rangs en tête de liste. 
        
            On s'arrête au premier voeu en attente de proposition,
        hors cas spéciaux comme les voeux bloqués par des demandes internat
        ou les demandes d'annulations de démissions par un candidat.
         */
        groupe.setRangDernierAppeleAffiche(0);

        voeux.sort((Voeu v1, Voeu v2) -> v1.getOrdreAppel() - v2.getOrdreAppel());

        for (Voeu voe : voeux) {
            if (voe.estProposition()) {
                groupe.setRangDernierAppeleAffiche(voe.ordreAppelAffiche);
            } else if (
                    voe.estEnAttenteDeProposition()
                    && !voe.estCorrectionClassement()
                    && !voe.estAnnulationDemission()
                    && !(voe.avecInternatAClassementPropre()
                            && voe.estDesactiveParPositionAdmissionInternat())
                    ) {
                //on s'arrete au premier candidat en attente de proposition
                //pour de bonnes raisons: ni correction de classement,
                //ni annulation de démission
                //ni pour cause de barre internat
                break;
            }
        }
    }

    /* Met à jour le rang du dernier appelé affiché, pour chaque groupe d'affectation */
    public static void mettreAJourRangDernierAppeleAffiche(
            GroupeInternat internat,
            List<Voeu> voeux) {

        /* Il y a deux barres par formation utilisant cet internat */
        internat.barresAppelAffichees.clear();
        internat.barresInternatAffichees.clear();
        for (GroupeAffectation g : internat.groupesConcernes) {

            /* parmi les propositions, on cherche celle qui a le plus haut
            rang dans le classement internat. On trie les voeux internats du moins bien classé 
            au mieux classé, c'est à dire les plus hauts rangs en tête de liste. */
            voeux.sort((Voeu v1, Voeu v2)
                    -> v2.rangInternat - v1.rangInternat);
            for (Voeu voe : voeux) {
                if (voe.groupe == g && voe.estProposition()) {
                    internat.barresInternatAffichees.put(g.id, voe.rangInternat);
                    break;
                }
            }

            if (internat.barresInternatAffichees.get(g.id) == null) {
                //pas de proposition aujourd'hui dans ce groupe
                internat.barresAppelAffichees.put(g.id, 0);
                internat.barresInternatAffichees.put(g.id, 0);
                continue;
            }

            /* dans chaque formation,,
            on calcule la proposition de plus haut rang dans l'ordre d'appel 
            qui est sous la barre internat affichée et sous le rang de laquelle aucun candidat
            n'est en attente de proposition. */
            internat.barresAppelAffichees.put(g.id, 0);
            voeux.sort((Voeu v1, Voeu v2)
                    -> v1.getOrdreAppel() - v2.getOrdreAppel());

            for (Voeu voe : voeux) {
                if (voe.groupe != g) {
                    //voeu hors groupe: on ignore
                } else if (voe.estProposition()) {
                    //proposition: on augmente la barre affichée
                    internat.barresAppelAffichees.put(g.id, voe.ordreAppelAffiche);
                } else if (voe.estCorrectionClassement()
                        || voe.estAnnulationDemission()) {
                    //cas exceptionnels: on continue
                } else if (voe.estEnAttenteDeProposition()
                        && (voe.rangInternat <= internat.barresInternatAffichees.get(g.id))) {
                    //en attente et sous la barre internat affichée: fin de l'augmentation
                    break;
                }
            }

        }

    }

    private AlgosAffichages() {
    }

}
