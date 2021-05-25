package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Table("G_TRI_AFF")
@IdName("G_TA_COD")
public class FormationAffectation extends EntitePersistante {
    
    public static final String ID_FORMATION_AFFECTATION = "FormationAffectation:ID_FORMATION_AFFECTATION"; 
    public static final String ETIQUETTE = "FormationAffectation:ETIQUETTE";
    
    protected static final Map<String,String> mappingNomsChamps = new HashMap<>();
    protected static final List<String> champsReserves = new ArrayList<>();
    
    static {
        
        // Mapping noms champs

        mappingNomsChamps.put(ID_FORMATION_AFFECTATION, "G_TA_COD");
        // Identifiant de la formation d'affectation
        // Type attendu : Integer
        
        mappingNomsChamps.put(ETIQUETTE, "ETIQUETTE1");
        // Etiquette n°1
        // Type attendu : String
        
        
        // Champs réservés (ne peuvent être définis directement via le 
        // Map parametresSupplementaires passé en argument
        // du constructeur)
        
        champsReserves.add("formation");

        
        // Vérification valeurs champs

        validatePresenceOf(
            mappingNomsChamps.get(ID_FORMATION_AFFECTATION)
        );

    }
    
    
    public FormationAffectation() {
        // constructeur par défaut requis par activejdbc
    } 
    
    
    public FormationAffectation(
        int idFormationAffectation,
        Map<String,Object> parametresSupplementaires
        ) {
        // rappel : parametresSupplementaires peut embarquer la valeur
        // ETIQUETTE qui est optionnelle
        
        this.set(mappingNomsChamps.get(ID_FORMATION_AFFECTATION), idFormationAffectation);
        
        for (Map.Entry<String,Object> entry : parametresSupplementaires.entrySet()) {
            String nom = entry.getKey();
            checkSiChampReserve(nom, champsReserves);
            this.set(mappingNomsChamps.get(nom), parametresSupplementaires.get(nom));   
        }
        
    }
      
    
    public FormationAffectation(
            int idFormationAffectation
            ) {
               
        this(
                idFormationAffectation,
                new HashMap<>()
            );
               
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
        
        return this.get(FormationAffectation.mappingNomsChamps.get(nom));
        
    }
        
}