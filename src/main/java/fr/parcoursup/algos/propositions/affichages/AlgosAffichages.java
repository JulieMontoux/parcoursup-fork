
package fr.parcoursup.algos.propositions.affichages;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.propositions.algo.*;
import fr.parcoursup.algos.utils.UtilService;
import fr.parcoursup.algos.verification.VerificationAffichages;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.isNull;

public class AlgosAffichages {

    private static final Logger LOGGER = Logger.getLogger(AlgosAffichages.class.getSimpleName());

    public static void mettreAJourAffichages(
            AlgoPropositionsSortie sortie,
            Set<VoeuUID> voeuxAvecPropositionDansMemeFormation,
            Set<VoeuUID> propositionsDuJour) throws VerificationException {

        Map<GroupeAffectation, List<Voeu>> voeuxParGroupes = new HashMap<>();
        Map<GroupeInternat, List<Voeu>> voeuxParInternat = new HashMap<>();


        for (Voeu v : sortie.voeux) {
            if (v.aEteProposeJoursPrecedents() || v.estAffecteHorsPP()) {
                continue;
            }
            if (!voeuxParGroupes.containsKey(v.getGroupeAffectation())) {
                voeuxParGroupes.put(v.getGroupeAffectation(), new ArrayList<>());
            }
            voeuxParGroupes.get(v.getGroupeAffectation()).add(v);
            if (v.getInternat() != null) {
                if (!voeuxParInternat.containsKey(v.getInternat())) {
                    voeuxParInternat.put(v.getInternat(), new ArrayList<>());
                }
                voeuxParInternat.get(v.getInternat()).add(v);
            }
        }

        LOGGER.info(UtilService.petitEncadrementLog("Mise à jour des rangs sur liste d'attente et derniers appelés affichés"));
        for (Entry<GroupeAffectation, List<Voeu>> entry : voeuxParGroupes.entrySet()) {
            GroupeAffectation groupe = entry.getKey();
            List<Voeu> voeux = entry.getValue();
            mettreAJourRangsListeAttente(
                    voeux,
                    voeuxAvecPropositionDansMemeFormation,
                    propositionsDuJour,
                    sortie.parametres.nbJoursCampagne,
                    groupe);
            mettreAJourRangDernierAppeleAffiche(groupe, voeux);
        }

        LOGGER.info(UtilService.petitEncadrementLog("Mise à jour des rangs des derniers appeles affichés dans les internats"));
        for (Entry<GroupeInternat, List<Voeu>> entry : voeuxParInternat.entrySet()) {
            GroupeInternat internat = entry.getKey();
            List<Voeu> voeux = entry.getValue();
            mettreAJourRangDernierAppeleAffiche(internat, voeux);
        }

        LOGGER.info(UtilService.petitEncadrementLog("Vérification des rangs sur liste attente"));
        for(GroupeAffectation groupe : sortie.groupes) {
           VerificationAffichages.verifierRangsSurListeAttente(groupe);
        }

    }
    
    /**
     * Si un candidat n'a pas de rang dans la liste d'attente de la veille, alors c'est qu'il est réintégré.
     * Du coup son rang dans la liste d'attente doit etre égal au rang du candidat suivant dans cette liste.
     * @param voeux
     * @param ordreAppel
     * @return
     */
    private static int getRangListeAttenteReintegration(List<Voeu> voeux, Voeu voeu, int rangListeAttente) {
    	/* On parcoure les voeux */
    	for (Voeu v : voeux) {
    		/* on recherche le voeux suivant dans la liste qui ne soit pas en réintégration et sur un autre candidat   */
    		if (v.ordreAppel > voeu.ordreAppel && v.getRangListeAttenteVeille() > 0 && v.id.gCnCod != voeu.id.gCnCod) {
    			/* Et on prend le min */
    			return Math.min(rangListeAttente, v.getRangListeAttenteVeille());
    			//return v.getRangListeAttenteVeille();
    		}
    	}
    	
    	return rangListeAttente;
    }

    /* met à jour les rangs sur liste d'attente */
    public static void mettreAJourRangsListeAttente(
            List<Voeu> voeux,
            Set<VoeuUID> voeuxAvecPropositionDansMemeFormation,
            Set<VoeuUID> propositionsDuJour,
            int nbJoursCampagne,
            GroupeAffectation groupe) {

        int dernierCandidatEnAttente = -1;
        int nbCandidatsEnAttente = 0;
        int nbRangVeille = 0;

        //initialisation
        groupe.setA_rg_nbr_att(0);
        voeux.forEach(voeu -> 
            voeu.setRangListeAttente(0)
        );
        
        voeux.sort(Comparator.comparingInt((Voeu v) -> v.ordreAppel));
        
        
        for (Voeu voeu : voeux) {
        	/* Voeu en attente et sans internat */
            if (voeu.estEnAttenteDeProposition() && voeu.internatUID == null) {
                /* on ne tient pas compte des candidats ayant eu 
            une proposition dans la même formation */
            /* afin que les rangs sur liste d'attente affichés aux candidats soient monotones,
            on ne tient pas compte des annulations de démissions 
            et des modifications de classement.
            Il peut y avoir deux voeux consecutifs pour le même candidat: un avec 
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

        voeux.sort(Comparator.comparingInt((Voeu v) -> v.ordreAppel));

        for (Voeu voe : voeux) {
            if (voe.estProposition()) {
                int aff = Math.max(groupe.getRangDernierAppeleAffiche(), voe.ordreAppelAffiche);
                groupe.setRangDernierAppeleAffiche(aff);
            } else if (
                    voe.estEnAttenteDeProposition()
                    && !voe.ignorerDansLeCalculRangsListesAttente
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

    /* Met à jour le rang du dernier appelé affiché, pour chaque groupe d'affectation.
    Tous les voeux sont des voeux pour cet internat.
    */
    public static void mettreAJourRangDernierAppeleAffiche(
            GroupeInternat internat,
            List<Voeu> voeux) throws VerificationException {

        /* Il y a deux barres par formation utilisant cet internat */
        internat.barresAppelAffichees.clear();
        internat.barresInternatAffichees.clear();
        for (GroupeAffectation g : internat.groupesConcernes()) {

            /* parmi les propositions, on cherche celle qui a le plus haut
            rang dans le classement internat. On trie les voeux internats du moins bien classé 
            au mieux classé, c'est à dire les plus hauts rangs en tête de liste. */
            OptionalInt rangDernierAppeleInternat
                    = voeux.stream()
                    .filter(Voeu::estProposition)
                    .filter(v -> v.groupeUID.equals(g.id))
                    .mapToInt(v -> v.rangInternat)
                    .max();
            if (rangDernierAppeleInternat.isPresent()) {
                internat.barresInternatAffichees.put(g.id, rangDernierAppeleInternat.getAsInt());
            }

            if (isNull(internat.barresInternatAffichees.get(g.id))) {
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
            voeux.sort(Comparator.comparingInt((Voeu v) -> v.ordreAppel));

            for (Voeu voe : voeux) {
                if (!voe.groupeUID.equals(g.id)) {
                    //voeu hors groupe: on ignore
                } else if (voe.estProposition()) {
                    //proposition: on augmente la barre affichée
                    int aff = Math.max(internat.barresAppelAffichees.getOrDefault(g.id, 0), voe.ordreAppelAffiche);
                    internat.barresAppelAffichees.put(g.id, aff);
                } else if (voe.ignorerDansLeCalculRangsListesAttente) {
                    //cas exceptionnels: on continue
                } else if (voe.ignorerDansLeCalculBarresInternatAffichees) {
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
