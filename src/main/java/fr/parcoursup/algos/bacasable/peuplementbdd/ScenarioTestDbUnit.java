package fr.parcoursup.algos.bacasable.peuplementbdd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

public class ScenarioTestDbUnit {

    private static final String[] tablesATraiter = {
            "A_ADM",
            "A_ADM_DEM",
            "A_ADM_PRED_DER_APP",
            "A_ADM_PROP",
            "A_REC",
            "A_REC_GRP",
            "A_REC_GRP_INT",
            "A_REC_GRP_INT_PROP",
            "A_SIT_VOE",
            "A_VOE",
            "A_VOE_PROP",
            "C_CAN_GRP",
            "C_CAN_GRP_INT",
            "C_GRP",
            "C_JUR_ADM",
            "G_CAN",
            "G_FIL",
            "G_FOR",
            "G_PAR",
            "G_TRI_AFF",
            "G_TRI_INS",
            "I_INS"};

    private final Map<String, List<EntitePersistante>> entitesParTable = Stream.of(tablesATraiter)
            .collect(toMap(identity(), table -> new ArrayList<>()));

    public void ajouteEntite(EntitePersistante entite) {
        String nomTable = entite.getNomTable();
        this.entitesParTable.get(nomTable).add(entite);
    }

    public String getFlatXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<dataset>" +
                this.entitesParTable.entrySet().stream()
                        .map(ScenarioTestDbUnit::getRepresentationTable)
                        .collect(joining()) +
                "</dataset>";

    }

    private static String getRepresentationTable(Map.Entry<String, List<EntitePersistante>> entry) {
        if (entry.getValue().isEmpty()) {
            return "<" + entry.getKey() + " />";
        }
        return entry.getValue().stream()
                .map(ScenarioTestDbUnit::getRepresentationEntite)
                .collect(joining());
    }

    private static String getRepresentationEntite(EntitePersistante entite) {
        String nomTable = entite.getNomTable();
        return entite.getAttributes().entrySet().stream()
                .map(ScenarioTestDbUnit::getRepresentationAttribut)
                .collect(joining(" ", "<" + nomTable + "  ", "/>"));

    }

    private static String getRepresentationAttribut(Map.Entry<String, Object> attribut) {
        return attribut.getKey() + "=\"" + attribut.getValue() + "\"";
    }

}
