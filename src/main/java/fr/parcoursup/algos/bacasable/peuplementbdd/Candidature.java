package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.annotations.CompositePK;
import org.javalite.activejdbc.annotations.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Table("I_INS")
@CompositePK({"G_CN_COD", "G_TI_COD"})
public class Candidature extends EntitePersistante {

    public static final String ID_CANDIDAT = "Candidature:ID_CANDIDAT";
    public static final String ID_FORMATION_INSCRIPTION = "Candidature:ID_FORMATION_INSCRIPTION";
    public static final String CODE_CANDIDAT_EST_DU_SECTEUR = "Candidature:CODE_CANDIDAT_EST_DU_SECTEUR";
    public static final String CODE_CANDIDATURE_VALIDEE = "Candidature:CODE_CANDIDATURE_VALIDEE";
    public static final String ETIQUETTE = "Candidature:ETIQUETTE";

    protected static final Map<String, String> mappingNomsChamps = new HashMap<>();
    protected static final List<String> champsReserves = new ArrayList<>();

    static {

        // Mapping noms champs

        mappingNomsChamps.put(ID_CANDIDAT, "G_CN_COD");
        // Identifiant du candidat
        // Type attendu : Integer

        mappingNomsChamps.put(ID_FORMATION_INSCRIPTION, "G_TI_COD");
        // Identifiant de la formation d'inscription
        // Type attendu : Integer

        mappingNomsChamps.put(CODE_CANDIDAT_EST_DU_SECTEUR, "I_IS_FLC_SEC");
        // Code indiquant si le candidat est considéré comme étant du secteur pour cette formation
        // Valeurs possibles : 0 = non, 1 = oui
        // Type attendu : Integer

        mappingNomsChamps.put(CODE_CANDIDATURE_VALIDEE, "I_IS_VAL");
        // Code indiquant si la candidature a été validée
        // Valeurs possibles : 0 = non, 1 = oui
        // Type attendu : Integer

        mappingNomsChamps.put(ETIQUETTE, "ETIQUETTE1");
        // Etiquette n°1
        // Type attendu : String


        // Champs réservés (ne peuvent être définis directement via le 
        // Map parametresSupplementaires passé en argument
        // du constructeur)

        champsReserves.add(ID_CANDIDAT);
        champsReserves.add(ID_FORMATION_INSCRIPTION);


        // Vérification valeurs champs

        validatePresenceOf(
                mappingNomsChamps.get(ID_CANDIDAT),
                mappingNomsChamps.get(ID_FORMATION_INSCRIPTION)
        );

    }


    protected Candidat candidat;

    protected FormationInscription formationInscription;


    public Candidature() {
        // constructeur par défaut requis par activejdbc
    }


    public Candidature(
            Candidat candidat,
            FormationInscription formationInscription,
            Map<String, Object> parametresSupplementaires
    ) {
        // rappel : parametresSupplementaires peut embarquer les valeurs
        // CODE_CANDIDAT_EST_DU_SECTEUR, CODE_CANDIDATURE_VALIDEE et ETIQUETTE qui sont optionnelles

        this.candidat = candidat;
        this.formationInscription = formationInscription;

        int idCandidat = (int) this.candidat.getValeurChamp(Candidat.ID_CANDIDAT);
        this.set(mappingNomsChamps.get(ID_CANDIDAT), idCandidat);

        int idFormationInscription = (int) this.formationInscription.getValeurChamp(FormationInscription.ID_FORMATION_INSCRIPTION);
        this.set(mappingNomsChamps.get(ID_FORMATION_INSCRIPTION), idFormationInscription);

        for (Map.Entry<String, Object> entry : parametresSupplementaires.entrySet()) {
            String nom = entry.getKey();
            checkSiChampReserve(nom, champsReserves);
            this.set(mappingNomsChamps.get(nom), parametresSupplementaires.get(nom));
        }

    }


    public Candidature(
            Candidat candidat,
            FormationInscription formationInscription
    ) {

        this(
                candidat,
                formationInscription,
                new HashMap<>()
        );

    }


    public Candidat getCandidat() {

        return this.candidat;

    }


    public FormationInscription getFormationInscription() {

        return this.formationInscription;

    }


    @Override
    public String getNomTable() {

        // Méthode récupérant le nom de la table associée à l'entité persistante courante
        // via l'appel de la méthode statique getTableName()
        // ("getTableName() should be accessed in a static way")
        return getTableName();

    }


    @Override
    public Object getValeurChamp(String nom) {

        return this.get(Candidature.mappingNomsChamps.get(nom));

    }

}
