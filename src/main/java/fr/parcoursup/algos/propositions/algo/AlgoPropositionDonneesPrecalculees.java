package fr.parcoursup.algos.propositions.algo;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

class AlgoPropositionDonneesPrecalculees {

    //les groupes sans internat à mettre à jour
    final @NotNull List<GroupeAffectation> groupesSansInternat;

    //les groupes avec internat sans classement à mettre à jour
    final @NotNull List<GroupeAffectation> groupesAvecInternatSansClassement;

    //les groupes avec internat avec classement à mettre à jour
    final @NotNull List<GroupeAffectation> groupesAvecInternatAClassement;

    //les voeux par groupe, triés par ordre d'appel
    final Map<GroupeAffectationUID, List<Voeu>> voeuxInitialementEnAttenteTriesParOrdreAppel;

    //les voeux par groupe internat, triés par ordre d'appel
    final Map<GroupeInternatUID, List<Voeu>> voeuxInternatsInitialementEnAttenteTriesParClassementInternatDecroissant;

    //les voeux pouvant faire l'objet d'affectations dans les groupes
    final Map<GroupeAffectationUID, List<Voeu>> voeuxInitialementEnAttenteOuProposition;

    //les voeux pouvant faire l'objet d'affectations dans les internats
    final Set<Voeu> voeuxInternatsInitialementEnAttenteOuProposition;

    //les barres maximales d'admission dans les internas
    final Map<GroupeInternatUID, Integer> barresMaximalesAdmissionInternats;

    //les affectations possibles aux internats, par voeu
    final Map<Voeu, CandidatAffecteInternat> affectationsPossiblesAuxInternats;

    //les voeux potentiellement modifiables par le repondeur auto
    final List<Voeu> voeuxDesCandidatsAvecRepAutoInitialementEnAttenteOuProposition;

    final Set<Integer> candidatsAvecRepondeurAutomatique;

    public AlgoPropositionDonneesPrecalculees(
            Set<Voeu> voeux,
            Map<GroupeInternatUID, Integer> barresMaximalesAdmissionInternats,
            Set<GroupeAffectation> groupesAMettreAJour,
            Set<Integer> candidatsAvecRepondeurAutomatique
    ) {

        Set<GroupeAffectationUID> groupesAvecInternatAClassementIds = voeux.stream()
                .filter(Voeu::avecInternatAClassementPropre)
                .map(v -> v.groupeUID).collect(Collectors.toSet());
        Set<GroupeAffectationUID> groupesAvecInternatSansClassementIds = voeux.stream()
                .filter(v -> v.id.iRhCod && !v.avecInternatAClassementPropre())
                .map(v -> v.groupeUID).collect(Collectors.toSet());
        this.groupesAvecInternatAClassement = groupesAMettreAJour.stream().filter(g -> groupesAvecInternatAClassementIds.contains(g.id)).collect(Collectors.toList());
        this.groupesAvecInternatSansClassement = groupesAMettreAJour.stream().filter(g -> groupesAvecInternatSansClassementIds.contains(g.id)).collect(Collectors.toList());
        this.groupesSansInternat = groupesAMettreAJour.stream().filter(g -> !groupesAvecInternatAClassementIds.contains(g.id) && !groupesAvecInternatSansClassementIds.contains(g.id)).collect(Collectors.toList());

        this.voeuxInitialementEnAttenteTriesParOrdreAppel = voeux.stream()
                .filter(v -> StatutVoeu.estEnAttenteDeProposition(v.statut))
                .sorted(Comparator.comparing(v -> v.ordreAppel))
                .collect(Collectors.groupingBy(v -> v.groupeUID));

        this.barresMaximalesAdmissionInternats = barresMaximalesAdmissionInternats;
        this.voeuxInitialementEnAttenteOuProposition = voeux.stream()
                .filter(v -> (StatutVoeu.estEnAttenteDeProposition(v.statut) || StatutVoeu.estProposition(v.statut)))
                .collect(Collectors.groupingBy(v -> v.groupeUID));
        this.voeuxInternatsInitialementEnAttenteOuProposition = voeux.stream()
                .filter(v -> v.avecInternatAClassementPropre() &&
                        (StatutVoeu.estEnAttenteDeProposition(v.statut) || StatutVoeu.estProposition(v.statut)))
                .collect(toSet());
        //noinspection DataFlowIssue
        this.voeuxInternatsInitialementEnAttenteTriesParClassementInternatDecroissant = voeux.stream()
                .filter(Voeu::avecInternatAClassementPropre)
                .filter(v -> StatutVoeu.estEnAttenteDeProposition(v.statut))
                .sorted(Comparator.comparing(v -> -v.rangInternat))
                .collect(Collectors.groupingBy(
                                v -> v.internatUID
                        )
                );

        //noinspection DataFlowIssue
        this.affectationsPossiblesAuxInternats = this.voeuxInternatsInitialementEnAttenteOuProposition.stream()
                .collect(Collectors.toMap(
                        v -> v,
                        Voeu::getAffectationInternat
                ));

        this.voeuxDesCandidatsAvecRepAutoInitialementEnAttenteOuProposition =
                voeux.stream()
                        .filter(v ->  !v.estAffecteHorsPP()
                                && (StatutVoeu.estEnAttenteDeProposition(v.statut) || StatutVoeu.estProposition(v.statut))
                                && candidatsAvecRepondeurAutomatique.contains(v.id.gCnCod)
                        ).collect(Collectors.toList());

        this.candidatsAvecRepondeurAutomatique = new HashSet<>(candidatsAvecRepondeurAutomatique);
    }

}
