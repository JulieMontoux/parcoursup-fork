package fr.parcoursup.algos.propositions.algo;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class StatutsVoeux {

    private final Map<Voeu, StatutVoeu> statuts;

    /* trace des iterations où les voeux ont changé de statut */
    private final Map<Voeu, Integer> iterationsChangementsStatut;

    private final Set<Voeu> propositions;
    private final Set<Voeu> enAttente;

    private int iterationCourante = 0;

    public StatutsVoeux(StatutsVoeux o) {
        this.statuts = new HashMap<>(o.statuts);
        this.iterationsChangementsStatut =  new HashMap<>(o.iterationsChangementsStatut);
        this.propositions = new HashSet<>(o.propositions);
        this.enAttente = new HashSet<>(o.enAttente);
        this.iterationCourante = o.iterationCourante;
    }

    public void setIterationCourante(int principale, int secondaire) {
        this.iterationCourante = 100 * principale + secondaire;
    }

    StatutsVoeux(Collection<Voeu> voeux) {
        this.statuts = new HashMap<>();
        this.propositions = new HashSet<>();
        this.enAttente = new HashSet<>();
        this.iterationsChangementsStatut = new HashMap<>();
        for(Voeu voe : voeux) {
            this.statuts.put(voe, voe.statut);
            if(StatutVoeu.estProposition(voe.statut)) {
                propositions.add(voe);
            } else if(StatutVoeu.estEnAttenteDeProposition(voe.statut)) {
                enAttente.add(voe);
            }
        }
    }

    public boolean estPropositionDuJour(Voeu v) {
        return StatutVoeu.estPropositionDuJour(statuts.get(v));
    }

    public boolean estProposition(Voeu v) {
        return propositions.contains(v);
    }

    @Unmodifiable
    Set<Voeu> getPropositions() {
        return Collections.unmodifiableSet(propositions);
    }

    @Unmodifiable
    Set<Voeu> getVoeuxenAttente() {
        return Collections.unmodifiableSet(enAttente);
    }

    public boolean estEnAttenteDeProposition(Voeu v) {
        return StatutVoeu.estEnAttenteDeProposition(statuts.get(v));
    }

    public void setProposition(Voeu v, boolean estRepondeurAutomatique) throws VerificationException {
        StatutVoeu statut = statuts.get(v);
        if (statut != StatutVoeu.EN_ATTENTE_DE_PROPOSITION) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_PROPOSITION_IMPOSSIBLE, v);
        }
        StatutVoeu nouveauStatut = estRepondeurAutomatique ? StatutVoeu.REP_AUTO_ACCEPTE : StatutVoeu.PROPOSITION_DU_JOUR;
        statuts.put(v, nouveauStatut);
        propositions.add(v);
        enAttente.remove(v);
    }


    public void refuserAutomatiquementVoeuEnAttenteParApplicationDemissionVoeuxOrdonnesDesCandidats(
            Predicate<Voeu> selecteur
    ) {
        Iterator<Voeu> it = enAttente.iterator();
        while(it.hasNext()) {
            Voeu v = it.next();
            if (selecteur.test(v)) {
                statuts.put(v, StatutVoeu.DEMISSION_AUTO_VOEU_ORDONNE_EN_ATTENTE);
                iterationsChangementsStatut.put(v, iterationCourante);
                it.remove();
            }
        }
    }

    public List<Voeu> refuserAutomatiquementPropositionsDuJourParApplicationDemissionVoeuxOrdonnes(
            Predicate<Voeu> selecteur
    ) {
        List<Voeu> demissions = new ArrayList<>();
        Iterator<Voeu> it = propositions.iterator();
        while(it.hasNext()) {
            Voeu v = it.next();
            StatutVoeu statutActuel = statuts.get(v);
            if (StatutVoeu.estPropositionDuJour(statutActuel) && selecteur.test(v)) {
                StatutVoeu nouveauStatut = StatutVoeu.DEMISSION_AUTO_VOEU_ORDONNE_EN_ATTENTE;
                statuts.put(v, nouveauStatut);
                iterationsChangementsStatut.put(v, iterationCourante);
                demissions.add(v);
                it.remove();
            }
        }
        return demissions;
    }

    public void refuserAutomatiquementParApplicationRepondeurAutomatique(Voeu v) throws VerificationException {
        if (v.estAffecteHorsPP()) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_HORS_PP_NON_REFUSABLE_AUTOMATIQUEMENT, v);
        }
        if (estEnAttenteDeProposition(v) && !v.estRepondeurActivable()) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_REFUS_AUTOMATIQUE_IMPOSSIBLE, v);
        }
        if (!estProposition(v) && !estEnAttenteDeProposition(v)) {
            throw new VerificationException(VerificationExceptionMessage.VOEU_REFUS_AUTOMATIQUE_IMPOSSIBLE, v);
        }
        StatutVoeu nouveauStatut = estProposition(v) ?
            StatutVoeu.REP_AUTO_REFUS_PROPOSITION : StatutVoeu.REP_AUTO_DEMISSION_ATTENTE;
        statuts.put(v, nouveauStatut);
        propositions.remove(v);
        enAttente.remove(v);
        iterationsChangementsStatut.put(v, iterationCourante);
    }

    public boolean estDemissionAutomatiqueParRepondeurAutomatique(Voeu v) {
        return StatutVoeu.estDemissionAutomatiqueParRepondeurAutomatique(statuts.get(v));
    }

    public StatutVoeu getStatut(Voeu v) {
        return statuts.get(v);
    }

    public void simulerEtape() {
        throw new RuntimeException("TODO");
    }


    public boolean estRefusOuDemission(Voeu v) {
        return statuts.get(v) == StatutVoeu.REFUS_OU_DEMISSION;
    }

    public void simulerRefusProposition() {
        throw new RuntimeException("TODO");
    }

    public void setPropositions(Collection<Voeu> nouvellesPropositions, Set<Integer> candidatsAvecRepondeurAutomatique) throws VerificationException {
        for (Voeu voeu : nouvellesPropositions) {
            boolean estRepAuto = candidatsAvecRepondeurAutomatique.contains(voeu.id.gCnCod);
            setProposition(voeu, estRepAuto);
            iterationsChangementsStatut.put(voeu, iterationCourante);
            propositions.add(voeu);
            enAttente.remove(voeu);
        }

    }

    Map<Voeu, Integer> getIterationschangementsStatuts() {
        return Collections.unmodifiableMap(iterationsChangementsStatut);
    }

    public boolean estPropositionOuEnAttente(Voeu v) {
        return propositions.contains(v) || enAttente.contains(v);
    }

    public boolean estPropositionAcceptee(Voeu v) {
        return statuts.get(v) == StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE;
    }

}
