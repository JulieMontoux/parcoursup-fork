
package fr.parcoursup.algos.propositions.affichages;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.propositions.algo.*;
import fr.parcoursup.algos.utils.UtilService;
import fr.parcoursup.algos.verification.VerificationAffichages;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public class AlgosAffichages {

    private static final Logger LOGGER = Logger.getLogger(AlgosAffichages.class.getSimpleName());

    public static void mettreAJourAffichages(
            AlgoPropositionsSortie sortie,
            Set<VoeuUID> propositionsDuJour) throws VerificationException {

        Map<GroupeAffectationUID, List<Voeu>> voeuxParGroupes
                = sortie.voeux.stream()
                .filter(v -> !StatutVoeu.aEteProposeJoursPrecedents(v.statut) && !v.estAffecteHorsPP())
                .collect(Collectors.groupingBy(v -> v.groupeUID));
        @SuppressWarnings("DataFlowIssue") Map<GroupeInternatUID, List<Voeu>> voeuxParInternat =
                sortie.voeux.stream()
                        .filter(Voeu::avecInternatAClassementPropre)
                        .filter(v -> !StatutVoeu.aEteProposeJoursPrecedents(v.statut) && !v.estAffecteHorsPP())
                        .collect(Collectors.groupingBy(v -> v.internatUID));
        Map<GroupeAffectationUID, GroupeAffectation> groupesParId = sortie.getGroupesParId();
        Map<GroupeInternatUID, GroupeInternat> internatsParId = sortie.getInternatsParId();


        LOGGER.info(UtilService.petitEncadrementLog("Mise à jour des rangs sur liste d'attente et derniers appelés affichés"));
        for (Entry<GroupeAffectationUID, List<Voeu>> entry : voeuxParGroupes.entrySet()) {
            GroupeAffectation groupe = groupesParId.get(entry.getKey());
            List<Voeu> voeux = entry.getValue();
            mettreAJourRangsListeAttente(
                    voeux,
                    propositionsDuJour,
                    sortie.parametres.nbJoursCampagne,
                    groupe);
            mettreAJourRangDernierAppeleAffiche(groupe, voeux, sortie.barresAdmissionInternats);
        }

        LOGGER.info(UtilService.petitEncadrementLog("Mise à jour des rangs des derniers appeles affichés dans les internats"));
        for (Entry<GroupeInternatUID, List<Voeu>> entry : voeuxParInternat.entrySet()) {
            GroupeInternatUID internatId = entry.getKey();
            GroupeInternat internat = internatsParId.get(internatId);
            List<Voeu> voeuxDansCetInternat = entry.getValue();
            List<GroupeAffectationUID> groupesConcernes = sortie.groupesAffectationsConcernesParInternat(internatId);
            mettreAJourRangDernierAppeleAffiche(internat, voeuxDansCetInternat, groupesConcernes);
        }

        LOGGER.info(UtilService.petitEncadrementLog("Vérification des rangs sur liste attente"));
        for(GroupeAffectation groupe : sortie.groupes) {
           VerificationAffichages.verifierRangsSurListeAttente(voeuxParGroupes.get(groupe.id));
        }

    }
    
    /**
     * Si un candidat n'a pas de rang dans la liste d'attente de la veille, alors c'est qu'il est réintégré.
     * Du coup son rang dans la liste d'attente doit être égal au rang du candidat suivant dans cette liste.
     * @param voeux les voeux
     * @param voeu le voeu à réintégrer
     * @param rangListeAttente le rang dans la liste d'attente
     * @return le rang de réintégration
     */
    private static int getRangListeAttenteReintegration(List<Voeu> voeux, Voeu voeu, int rangListeAttente) {
    	/* On parcoure les voeux */
    	for (Voeu v : voeux) {
    		/* on recherche le voeux suivant dans la liste qui ne soit pas en réintégration et sur un autre candidat   */
    		if (v.ordreAppel > voeu.ordreAppel && v.getRangListeAttenteVeille() > 0 && v.id.gCnCod != voeu.id.gCnCod) {
    			/* Et on prend le min */
    			return Math.min(rangListeAttente, v.getRangListeAttenteVeille());
    		}
    	}
    	
    	return rangListeAttente;
    }

    /* met à jour les rangs sur liste d'attente */
    public static void mettreAJourRangsListeAttente(
            List<Voeu> voeux,
            Set<VoeuUID> propositionsDuJour,
            int nbJoursCampagne,
            GroupeAffectation groupe) {

        int dernierCandidatEnAttente = -1;
        int nbCandidatsEnAttente = 0;
        int nbRangVeille;

        //initialisation
        groupe.setA_rg_nbr_att(0);
        voeux.forEach(voeu -> 
            voeu.setRangListeAttente(0)
        );
        
        voeux.sort(Comparator.comparingInt((Voeu v) -> v.ordreAppel));
        
        
        for (Voeu voeu : voeux) {
        	/* Voeu en attente et sans internat */
            if (StatutVoeu.estEnAttenteDeProposition(voeu.statut) && voeu.internatUID == null) {
                /* on ne tient pas compte des candidats ayant eu 
            une proposition dans la même formation */
            /* afin que les rangs sur liste d'attente affichés aux candidats soient monotones,
            on ne tient pas compte des annulations de démissions 
            et des modifications de classement.
            Il peut y avoir deux voeux consécutifs pour le même candidat: un avec
            et un sans internat.
                 */
                if (//!voeuxAvecPropositionDansMemeFormation.contains(voeu.id) && --Si un candidat est réintégré, il aura une proposition, mais on veut quand meme le compter ici
            		!propositionsDuJour.contains(new VoeuUID(voeu.id.gCnCod, voeu.id.gTaCod, !voeu.id.iRhCod)) 
            		//&& !voeu.ignorerDansLeCalculRangsListesAttente  -- idem candidats réintégrés on veut les compter
            		&& voeu.id.gCnCod != dernierCandidatEnAttente) {
                	 
                	//On incrémente 
                	nbCandidatsEnAttente++;
                    dernierCandidatEnAttente = voeu.id.gCnCod;
                    groupe.setA_rg_nbr_att(groupe.getA_rg_nbr_att()+1);
                }

                /* Jour 1 on maj le rang avec le nombre de candidat en attente.*/
                if (nbJoursCampagne == 1) {
                	voeu.setRangListeAttente(Math.max(1, nbCandidatsEnAttente));
                }else {
                	/* Au dela du jour 1*/	
                	/* Si on a un rang de la veille  On le récupère */
                	if (voeu.getRangListeAttenteVeille() > 0) {
                    	nbRangVeille = voeu.getRangListeAttenteVeille();
                	}else {
                		/* Sinon le voeux n'a pas de rang de la veille, c'est que le candidat a été réintégré donc on le calcule */
                		nbRangVeille = getRangListeAttenteReintegration(voeux, voeu, nbCandidatsEnAttente);
                	}
                		
            		/* On se repositionne sur le rang de la veille si il est plus petit */
            		if (nbCandidatsEnAttente > nbRangVeille) {
            			nbCandidatsEnAttente = nbRangVeille;
            		}             
            		
                	voeu.setRangListeAttente(nbCandidatsEnAttente);
                }      
            }
        }

    }

    /* Met à jour le rang du dernier appelé affiché pour ce groupe d'affectation */
    private static void mettreAJourRangDernierAppeleAffiche(
            GroupeAffectation groupe,
            List<Voeu> voeux,
            Map<GroupeInternatUID, Integer> barresAdmissionInternats) {
        /* Parmi les propositions du jour, on cherche celle qui a le plus haut
            rang dans l'ordre d'appel. On trie les voeux en attente du moins bien classé 
            au mieux classé, c'est à dire les plus hauts rangs en tête de liste. 
        
            On s'arrête au premier voeu en attente de proposition,
        hors cas spéciaux comme les voeux bloqués par des demandes internat
        ou les demandes d'annulations de démissions par un candidat.
         */
        groupe.setRangDernierAppeleAffiche(0);

        voeux.sort(Comparator.comparingInt((Voeu v) -> v.ordreAppel));

        for (Voeu voe : voeux) {
            if (StatutVoeu.estProposition(voe.statut)) {
                int aff = Math.max(groupe.getRangDernierAppeleAffiche(), voe.ordreAppelAffiche);
                groupe.setRangDernierAppeleAffiche(aff);
            } else if (
                    StatutVoeu.estEnAttenteDeProposition(voe.statut)
                    && !voe.ignorerDansLeCalculRangsListesAttente
                    && !(voe.avecInternatAClassementPropre() && voe.rangInternat > barresAdmissionInternats.get(voe.internatUID))
                    ) {
                //on s'arrete au premier candidat en attente de proposition
                //pour de bonnes raisons: ni correction de classement,
                //ni annulation de démission
                //ni pour cause de barre internat
                break;
            }
        }
    }

    /* Met à jour le rang du dernier appelé affiché, pour chaque groupe d'affectation.
    Tous les voeux sont des voeux pour cet internat.
    */
    @SuppressWarnings("StatementWithEmptyBody")
    public static void mettreAJourRangDernierAppeleAffiche(
            GroupeInternat internat,
            List<Voeu> voeux,
            List<GroupeAffectationUID> groupesConcernes
    ) {

        /* Il y a deux barres par formation utilisant cet internat */
        internat.barresAppelAffichees.clear();
        internat.barresInternatAffichees.clear();
        for (GroupeAffectationUID gid : groupesConcernes) {

            /* parmi les propositions, on cherche celle qui a le plus haut
            rang dans le classement internat. On trie les voeux internats du moins bien classé 
            au mieux classé, c'est à dire les plus hauts rangs en tête de liste. */
            OptionalInt rangDernierAppeleInternat
                    = voeux.stream()
                    .filter(v -> StatutVoeu.estProposition(v.statut))
                    .filter(v -> v.groupeUID.equals(gid))
                    .mapToInt(v -> v.rangInternat)
                    .max();
            if (rangDernierAppeleInternat.isPresent()) {
                internat.barresInternatAffichees.put(gid, rangDernierAppeleInternat.getAsInt());
            }

            if (isNull(internat.barresInternatAffichees.get(gid))) {
                //pas de proposition aujourd'hui dans ce groupe
                internat.barresAppelAffichees.put(gid, 0);
                internat.barresInternatAffichees.put(gid, 0);
                continue;
            }

            /* dans chaque formation,,
            on calcule la proposition de plus haut rang dans l'ordre d'appel 
            qui est sous la barre internat affichée et sous le rang de laquelle aucun candidat
            n'est en attente de proposition. */
            internat.barresAppelAffichees.put(gid, 0);

            for (Voeu voe : voeux.stream().sorted(Comparator.comparingInt((Voeu v) -> v.ordreAppel)).collect(Collectors.toList())) {
                if (!voe.groupeUID.equals(gid)) {
                    //voeu hors groupe: on ignore
                } else if (StatutVoeu.estProposition(voe.statut)) {
                    //proposition: on augmente la barre affichée
                    int aff = Math.max(internat.barresAppelAffichees.getOrDefault(gid, 0), voe.ordreAppelAffiche);
                    internat.barresAppelAffichees.put(gid, aff);
                } else if (voe.ignorerDansLeCalculRangsListesAttente) {
                    //cas exceptionnels: on continue
                } else if (voe.ignorerDansLeCalculBarresInternatAffichees) {
                    //cas exceptionnels: on continue
                } else if (StatutVoeu.estEnAttenteDeProposition(voe.statut)
                        && (voe.rangInternat <= internat.barresInternatAffichees.get(gid))) {
                    //en attente et sous la barre internat affichée: fin de l'augmentation
                    break;
                }
            }
        }
    }

    private AlgosAffichages() {
    }

}
