package fr.parcoursup.algos.propositions.algo;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.utils.UtilService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class CalculPropositionsEtDemissions {

    private static final Logger LOGGER = Logger.getLogger(AlgoPropositions.class.getSimpleName());

    /**
     * Calculer les nouvelles propositions et démissions, à barre d'admission internats fixées
     *
     * @param barresAdmissionInternats              les barres d'admission internats
     * @param statutsInitiaux                       les statuts initiaux des voeux
     * @param compteurBoucleDecrementBarresInternat le compteur de la boucle de décrement des barres internats
     * @param appliquerDemissionsAutomatiques       détermine si les démissions automatiques sont mises en oeuvre (GDDà
     * @param appliquerRepondeurAutomatique         détermine si le répondeur automatique est mis en oeuvre
     * @param donneesPrecalculees                   les données précalculées, permettant d'accélérer le calcul
     * @return les statuts des voeux mis à jour
     * @throws VerificationException en cas de problème d'intégrité des données ou de comportement innatendu du calcul
     */
    static StatutsVoeux calculerPropositionsEtDemissions(
            Map<GroupeInternatUID, Integer> barresAdmissionInternats,
            StatutsVoeux statutsInitiaux,
            int compteurBoucleDecrementBarresInternat,
            boolean appliquerDemissionsAutomatiques,
            boolean appliquerRepondeurAutomatique,
            AlgoPropositionDonneesPrecalculees donneesPrecalculees) throws VerificationException {
        //boucle de démission auto des voeux ordonnés et d'application du répondeur auto, qui met à jour les statuts
        int nbIterationsBoucleLiberationDePlaces = 1;
        boolean premiereIteration = true;

        //les status qui vont être mis à jour pendant le calcul
        StatutsVoeux statuts = new StatutsVoeux(statutsInitiaux);

        List<GroupeAffectation> groupesSansInternat = new ArrayList<>(donneesPrecalculees.groupesSansInternat);
        List<GroupeAffectation> groupesAvecInternatSansClassement = new ArrayList<>(donneesPrecalculees.groupesAvecInternatSansClassement);
        List<GroupeAffectation> groupesAvecInternatAClassement = new ArrayList<>(donneesPrecalculees.groupesAvecInternatAClassement);

        //noinspection WhileCanBeDoWhile
        while (true) {
            statuts.setIterationCourante(compteurBoucleDecrementBarresInternat, nbIterationsBoucleLiberationDePlaces);

            @Unmodifiable
            Set<Voeu> propositions = statuts.getPropositions();
            @Unmodifiable
            Set<Voeu> voeuxEnAttente = statuts.getVoeuxenAttente();

            LOGGER.info(UtilService.petitEncadrementLog("Itération: barres internat #" + compteurBoucleDecrementBarresInternat + " / réponses auto #" + nbIterationsBoucleLiberationDePlaces));

            List<Voeu> propositionsDansGroupesSansInternat = calculerPropositionsDansGroupesSansInternat(
                    donneesPrecalculees,
                    propositions,
                    voeuxEnAttente,
                    groupesSansInternat
            );
            LOGGER.info(String.format("%d nouvelles propositions dans %d groupes sans internat: ",
                    propositionsDansGroupesSansInternat.size(),
                    groupesSansInternat.size()
            ));
            statuts.setPropositions(
                    propositionsDansGroupesSansInternat,
                    donneesPrecalculees.candidatsAvecRepondeurAutomatique
            );

            List<Voeu> propositionsDansGroupesAvecInternatSansClassement = calculerPropositionsDansGroupesAvecInternatSansClassement(
                    donneesPrecalculees,
                    propositions,
                    voeuxEnAttente,
                    groupesAvecInternatSansClassement
            );
            LOGGER.info(String.format("%d nouvelles propositions dans %d groupes avec internat sans classement: ",
                    propositionsDansGroupesAvecInternatSansClassement.size(),
                    groupesAvecInternatSansClassement.size()
            ));
            statuts.setPropositions(
                    propositionsDansGroupesAvecInternatSansClassement,
                    donneesPrecalculees.candidatsAvecRepondeurAutomatique
            );

            List<Voeu> propositionsGroupesAvecBarreInternat = calculerPropositionsDansGroupesAvecInternatAClassement(
                    barresAdmissionInternats,
                    donneesPrecalculees,
                    propositions,
                    voeuxEnAttente,
                    groupesAvecInternatAClassement
            );
            LOGGER.info(String.format("%d nouvelles propositions dans %d groupes avec internat à classement: ",
                    propositionsGroupesAvecBarreInternat.size(),
                    groupesAvecInternatAClassement.size()
            ));
            statuts.setPropositions(
                    propositionsGroupesAvecBarreInternat,
                    donneesPrecalculees.candidatsAvecRepondeurAutomatique
            );

            //on ne break pas à la première itération, car il peut y avoir des démissions automatiques en suspens,
            //même en l'absence de propositions du jour
            if (!premiereIteration
                    && propositionsDansGroupesSansInternat.isEmpty()
                    && propositionsDansGroupesAvecInternatSansClassement.isEmpty()
                    && propositionsGroupesAvecBarreInternat.isEmpty()
            ) {
                LOGGER.info("Aucune nouvelle proposition: sortie de boucle démission");
                break;
            }

            if (!appliquerDemissionsAutomatiques && !appliquerRepondeurAutomatique) {
                LOGGER.info("Aucune démission automatique applicable: sortie de boucle démission");
                break;
            }


            Stream<Voeu> nouvellesPropositions = Stream.concat(
                    propositionsDansGroupesSansInternat.stream(),
                    Stream.concat(
                            propositionsDansGroupesAvecInternatSansClassement.stream(),
                            propositionsGroupesAvecBarreInternat.stream()
                    )
            );
            Map<Integer, Integer> meilleursRangsNouvellesPropositions = getMeilleursRangsNouvellesPropositions(
                    nouvellesPropositions.filter(statuts::estPropositionDuJour)
            );

            List<Voeu> placesLiberees = new ArrayList<>();

            if (appliquerDemissionsAutomatiques) {
                List<Voeu> placesLibereesParDemissionAutomatiques
                        = DemissionAutoVoeuxOrdonnes.appliquerDemissionAutomatiqueVoeuOrdonnes(
                        statuts,
                        meilleursRangsNouvellesPropositions,
                        donneesPrecalculees.candidatsAvecRepondeurAutomatique
                );
                placesLiberees.addAll(placesLibereesParDemissionAutomatiques
                );
            }

            if (appliquerRepondeurAutomatique) {
                List<Voeu> placesLibereesParRepondeurAutomatique
                        = RepondeurAutomatique.appliquerRepondeurAutomatique(
                        donneesPrecalculees.voeuxDesCandidatsAvecRepAutoInitialementEnAttenteOuProposition,
                        donneesPrecalculees.candidatsAvecRepondeurAutomatique,
                        statuts,
                        meilleursRangsNouvellesPropositions
                );
                placesLiberees.addAll(placesLibereesParRepondeurAutomatique);
            }

            //on itere jusqu'à ce qu'aucune place ne soit libérée
            if (placesLiberees.isEmpty()) {
                LOGGER.info("Aucune place libérée par les démissions auto et le répondeur automatique: sortie de boucle démission");
                break;
            }

            Set<GroupeAffectationUID> groupesAvecNouvellesPlaces = placesLiberees.stream().map(v -> v.groupeUID).collect(toSet());
            groupesAvecInternatSansClassement = donneesPrecalculees.groupesAvecInternatSansClassement.stream().filter(g -> groupesAvecNouvellesPlaces.contains(g.id)).collect(Collectors.toList());
            groupesAvecInternatAClassement = donneesPrecalculees.groupesAvecInternatAClassement.stream().filter(g -> groupesAvecNouvellesPlaces.contains(g.id)).collect(Collectors.toList());
            groupesSansInternat = donneesPrecalculees.groupesSansInternat.stream().filter(g -> groupesAvecNouvellesPlaces.contains(g.id)).collect(Collectors.toList());

            premiereIteration = false;
            nbIterationsBoucleLiberationDePlaces++;
        }

        return statuts;

    }

    /**
     * Calcul des meilleurs rangs des nouvelles propositions
     *
     * @param propositionsDuJour les propositions du jour
     * @return le meilleurs rangs des nouvelles propositions reçues par chaque candidat, indexé par numéro de candidat (gCnCod)
     */
    private static @NotNull Map<Integer, Integer> getMeilleursRangsNouvellesPropositions(
            Stream<Voeu> propositionsDuJour
    ) {
        return propositionsDuJour.collect(
                Collectors.toMap(v -> v.id.gCnCod, Voeu::getRangPreferencesCandidat, BinaryOperator.minBy(Comparator.naturalOrder()))
        );
    }

    /**
     * @param donneesPrecalculees             données constantes, précalculées pour améliorer les performances
     * @param propositions                    les voeux ayant le statut propositions
     * @param voeuxEnAttente                  les voeux ayant le statut enAttente
     * @param groupesSansInternatAMettreAJour les groupes à mettre à jour
     */
    static List<Voeu> calculerPropositionsDansGroupesSansInternat(
            AlgoPropositionDonneesPrecalculees donneesPrecalculees,
            Set<Voeu> propositions,
            Set<Voeu> voeuxEnAttente,
            List<GroupeAffectation> groupesSansInternatAMettreAJour
    ) {
        return groupesSansInternatAMettreAJour.parallelStream().flatMap(
                        gc -> {
                            @Nullable List<Voeu> voeuxInitialementEnAttenteTriesParOrdreAppel
                                    = donneesPrecalculees.voeuxInitialementEnAttenteTriesParOrdreAppel.get(gc.id);
                            if (voeuxInitialementEnAttenteTriesParOrdreAppel != null) {
                                @Nullable List<Voeu> voeuxInitialementEnAttenteOuPropositionDansCeGroupe
                                        = donneesPrecalculees.voeuxInitialementEnAttenteOuProposition.get(gc.id);
                                long candidatsAffectes = voeuxInitialementEnAttenteOuPropositionDansCeGroupe == null ? 0L :
                                        voeuxInitialementEnAttenteOuPropositionDansCeGroupe.stream()
                                                .filter(propositions::contains)
                                                .count();
                                return calculerNouvellesPropositionsDansUnGroupeSansBarreInternat(
                                        gc,
                                        voeuxInitialementEnAttenteTriesParOrdreAppel,
                                        candidatsAffectes,
                                        voeuxEnAttente
                                ).stream();
                            } else {
                                return Stream.of();
                            }
                        })
                .collect(Collectors.toList());
    }

    /**
     * Calcul des nouvelles propositions dans un groupe sans internat
     *
     * @param groupe                                       le groupe concerné
     * @param voeuxInitialementEnAttenteTriesParOrdreAppel les voeux dans ce groupe initialement en attente triés par ordre d'appel
     * @param candidatsAffectes                            le nombre de candidats ayant un voeu avec statut "proposition" dans ce groupe'
     * @param voeuxEnAttente                               les voeux ayant le statut en attente
     * @return les nouvelles propositions
     */
    private static List<Voeu> calculerNouvellesPropositionsDansUnGroupeSansBarreInternat(
            @NotNull GroupeAffectation groupe,
            @NotNull List<Voeu> voeuxInitialementEnAttenteTriesParOrdreAppel,
            long candidatsAffectes,
            @NotNull Set<Voeu> voeuxEnAttente
    ) {

        List<Voeu> nouvellesPropositions = new ArrayList<>();
        long candidatsAffectables = groupe.getNbRecrutementsSouhaite() - candidatsAffectes;

        for (Voeu v : voeuxInitialementEnAttenteTriesParOrdreAppel) {
            if(voeuxEnAttente.contains(v)) {
                boolean appelParBloc = v.ordreAppel <= groupe.getRangLimite();
                if (candidatsAffectables > 0 || appelParBloc) {
                    nouvellesPropositions.add(v);
                    candidatsAffectables--;
                } else {
                    break;
                }
            }
        }
        return nouvellesPropositions;
    }

    /**
     * @param donneesPrecalculees             données constantes, précalculées pour améliorer les performances
     * @param propositions                    les voeux ayant le statut propositions
     * @param voeuxEnAttente                  les voeux ayant le statut enAttente
     * @param groupesSansInternatAMettreAJour les groupes à mettre à jour
     */
    static List<Voeu> calculerPropositionsDansGroupesAvecInternatSansClassement(
            AlgoPropositionDonneesPrecalculees donneesPrecalculees,
            Set<Voeu> propositions,
            Set<Voeu> voeuxEnAttente,
            List<GroupeAffectation> groupesSansInternatAMettreAJour
    ) {
        return groupesSansInternatAMettreAJour.stream().flatMap(
                        gc -> {
                            @Nullable List<Voeu> voeuxInitialementEnAttenteTriesParOrdreAppel
                                    = donneesPrecalculees.voeuxInitialementEnAttenteTriesParOrdreAppel.get(gc.id);
                            if (voeuxInitialementEnAttenteTriesParOrdreAppel != null) {
                                @Nullable List<Voeu> voeuxInitialementEnAttenteOuPropositionDansCeGroupe
                                        = donneesPrecalculees.voeuxInitialementEnAttenteOuProposition.get(gc.id);
                                Set<Integer> candidatsAffectes = voeuxInitialementEnAttenteOuPropositionDansCeGroupe == null ? new HashSet<>() :
                                        voeuxInitialementEnAttenteOuPropositionDansCeGroupe.stream()
                                                .filter(propositions::contains)
                                                .map(v -> v.id.gCnCod)
                                                .collect(toSet());
                                return calculerNouvellesPropositionsDansUnGroupeAvecInternatSansClassement(
                                        gc,
                                        voeuxInitialementEnAttenteTriesParOrdreAppel,
                                        candidatsAffectes,
                                        voeuxEnAttente
                                ).stream();
                            } else {
                                return Stream.of();
                            }
                        })
                .collect(Collectors.toList());
    }

    /**
     * Calcul des nouvelles propositions dans un groupe sans internat
     *
     * @param groupe                                       le groupe concerné
     * @param voeuxInitialementEnAttenteTriesParOrdreAppel les voeux dans ce groupe initialement en attente triés par ordre d'appel
     * @param candidatsAffectes                            le nombre de candidats ayant un voeu avec statut "proposition" dans ce groupe'
     * @param voeuxEnAttente                               les voeux ayant le statut en attente
     * @return les nouvelles propositions
     */
    private static List<Voeu> calculerNouvellesPropositionsDansUnGroupeAvecInternatSansClassement(
            @NotNull GroupeAffectation groupe,
            @NotNull List<Voeu> voeuxInitialementEnAttenteTriesParOrdreAppel,
            Set<Integer> candidatsAffectes,
            @NotNull Set<Voeu> voeuxEnAttente
    ) {

        List<Voeu> nouvellesPropositions = new ArrayList<>();

        for (Voeu v : voeuxInitialementEnAttenteTriesParOrdreAppel) {
            if(voeuxEnAttente.contains(v)) {
                boolean appelParBloc = v.ordreAppel <= groupe.getRangLimite();
                boolean placesDisponibles = groupe.getNbRecrutementsSouhaite() > candidatsAffectes.size();
                if (placesDisponibles || appelParBloc || candidatsAffectes.contains(v.id.gCnCod)) {
                    nouvellesPropositions.add(v);
                    candidatsAffectes.add(v.id.gCnCod);
                } else {
                    //on ne peut plus faire de proposition
                    break;
                }
            }
        }
        return nouvellesPropositions;
    }



    /**
     * Calcul des nouvelles propositions dans un groupe avec internat
     * @param barresAdmissionInternats       les barres d'admission dans les internats
     * @param donneesPrecalculees          données constantes, précalculées pour améliorer les performances
     * @param propositions                   les voeux ayant le statut propositions
     * @param voeuxEnAttente                 les voeux ayant le statut enAttente
     * @param groupesAvecInternatAMettreAJour les groupes à mettre à jour
     * @return les nouvelles propositions
     */
    private static List<Voeu> calculerPropositionsDansGroupesAvecInternatAClassement(
            Map<GroupeInternatUID, Integer> barresAdmissionInternats,
            AlgoPropositionDonneesPrecalculees donneesPrecalculees,
            Set<Voeu> propositions,
            Set<Voeu> voeuxEnAttente,
            List<GroupeAffectation> groupesAvecInternatAMettreAJour) {

        Set<CandidatAffecteInternat> candidatsAffectesAuxInternats
                = donneesPrecalculees.affectationsPossiblesAuxInternats.entrySet().stream()
                .filter(e -> propositions.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(toSet());

        List<Voeu> nouvellesPropositions = new ArrayList<>();
        for (GroupeAffectation gc : groupesAvecInternatAMettreAJour) {
            List<Voeu> voeuxEnAttenteTriesParOrdreAppel
                    = donneesPrecalculees.voeuxInitialementEnAttenteTriesParOrdreAppel
                    .getOrDefault(gc.id, new ArrayList<>())
                    .stream()
                    .filter(voeuxEnAttente::contains)
                    .collect(Collectors.toList());
            List<Voeu> voeuxInitialementEnAttenteOuProposition
                    = donneesPrecalculees.voeuxInitialementEnAttenteOuProposition.get(gc.id);
            @NotNull Set<Integer> candidatsAffectes = voeuxInitialementEnAttenteOuProposition.stream()
                    .filter(propositions::contains)
                    .map(v -> v.id.gCnCod)
                    .collect(toSet());
            nouvellesPropositions.addAll(
                    calculerNouvellesPropositionsDansUnGroupeAvecBarreInternat(
                            gc,
                            voeuxEnAttenteTriesParOrdreAppel,
                            candidatsAffectesAuxInternats,
                            barresAdmissionInternats,
                            candidatsAffectes
                    )
            );
        }
        return nouvellesPropositions;
    }
    /*
        return groupesAvecInternatAMettreAJour.stream().collect(Collectors.toMap(
                gc -> gc.id,
                gc -> {
                })
        );
    }*/


    /**
     * Calcul des nouvelles propositions dans un groupe avec internat
     * @param groupe le groupe concerné
     * @param voeuxEnAttenteTriesParOrdreAppel les voeux dans ce groupe, avec statut "en attente",  triés par ordre d'appel
     * @param barresAdmissionInternats barres d'admission dans les internats
     * @return les nouvelles propositions
     */
    private static List<Voeu> calculerNouvellesPropositionsDansUnGroupeAvecBarreInternat(
            GroupeAffectation groupe,
            List<Voeu> voeuxEnAttenteTriesParOrdreAppel,
            Set<CandidatAffecteInternat> candidatsAffectesParInternat,
            Map<GroupeInternatUID, Integer> barresAdmissionInternats,
            @NotNull Set<Integer> candidatsAffectes
    ) {

        List<Voeu> nouvellesPropositions = new ArrayList<>();

        for (Voeu v : voeuxEnAttenteTriesParOrdreAppel) {
            final boolean estEligibleAProposition;
            if(v.avecInternatAClassementPropre()) {
                /* Si c'est le voeu d'un candidat qui a fait une demande d'internat et si
                le classement à l'internat de ce candidat est strictement supérieur à la barre d'admission
                dans l'internat alors pas de proposition possible.
                */
                assert v.internatUID != null;
                boolean estDejaAffecteDanscetInternat
                        = candidatsAffectesParInternat.contains(
                                new CandidatAffecteInternat(v.id.gCnCod, v.internatUID)
                );
                @NotNull Integer barreAdmissionDansCetInternat = barresAdmissionInternats.get(v.internatUID);
                estEligibleAProposition =
                        (v.rangInternat <= barreAdmissionDansCetInternat) || estDejaAffecteDanscetInternat;
            } else {
                estEligibleAProposition = true;
            }

            if (estEligibleAProposition &&
                    (v.ordreAppel <= groupe.getRangLimite()
                        || candidatsAffectes.contains(v.id.gCnCod)
                        || candidatsAffectes.size() < groupe.getNbRecrutementsSouhaite())
                ) {
                    nouvellesPropositions.add(v);
                    candidatsAffectes.add(v.id.gCnCod);
                }

        }
        return nouvellesPropositions;
    }

    private CalculPropositionsEtDemissions() {
        // Constructeur privé pour empêcher l'instanciation
    }
}
