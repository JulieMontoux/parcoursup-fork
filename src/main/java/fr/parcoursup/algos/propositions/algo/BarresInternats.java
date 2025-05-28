package fr.parcoursup.algos.propositions.algo;

import java.util.*;
import java.util.stream.Collectors;

public class BarresInternats {

    static Map<GroupeInternatUID, Integer> calculerBarresMaximalesInternats(
            Set<GroupeInternatUID> finReservationInternat,
            Collection<GroupeAffectation> groupesAffectations,
            Map<GroupeInternatUID, GroupeInternat> internats,
            Map<GroupeInternatUID, List<Voeu>> voeuxParInternat,
            Parametres parametres
            ) {

        Map<GroupeAffectationUID, Integer> estimationsRangsDernierAppeles =
                groupesAffectations.stream()
                        .collect(Collectors.toMap(
                                g -> g.id,
                                GroupeAffectation::getEstimationRangDernierAppeleADateFinReservationInternats
                        ));

        Map<GroupeInternatUID, Integer> result = new HashMap<>();
        internats.forEach((id, internat) -> {
                    final int barre;
                    if (finReservationInternat.contains(id)) {
                        barre = Integer.MAX_VALUE;
                    } else {
                        barre = internat.calculerRangMaximalAdmissionInternatSelonEstimationRangDernierAppele(
                                parametres,
                                voeuxParInternat.getOrDefault(id, List.of()),
                                estimationsRangsDernierAppeles
                        );
                    }
                    result.put(id, barre);
                }
        );
        return result;
    }

    public static Map<GroupeInternatUID, Integer> calculerBarresInitialesInternats(
            Map<GroupeInternatUID, Integer> barresMaximalesAdmissionInternats,
            Map<GroupeInternatUID, List<Integer>> rangsEnAttenteParInternat
    ) {
        return rangsEnAttenteParInternat.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            GroupeInternatUID id = e.getKey();
                            int rangMaxCandidatEnAttente = e.getValue().stream().mapToInt(rang -> rang).max().orElse(0);
                            int barreMax = barresMaximalesAdmissionInternats.getOrDefault(id, 0);
                            return Math.min(rangMaxCandidatEnAttente, barreMax);
                        }
                ));
    }

}
