package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Table("G_FOR")
@IdName("G_FR_COD")
public class TypeFormation extends EntitePersistante {
    
    public static final String ID_TYPE_FORMATION = "TypeFormation:ID_TYPE_FORMATION"; 
    public static final String ETIQUETTE = "TypeFormation:ETIQUETTE"; 
    
    protected static final Map<String,String> mappingNomsChamps = new HashMap<>();
    protected static final List<String> champsReserves = new ArrayList<>();
    
    static {
        
        // Mapping noms champs

        mappingNomsChamps.put(ID_TYPE_FORMATION, "G_FR_COD");
        // Code identifiant le type de filière de formation
        // Type attendu : Integer
        
        mappingNomsChamps.put(ETIQUETTE, "ETIQUETTE1");
        // Etiquette n°1
        // Type attendu : String
        
        
        // Champs réservés (ne peuvent être définis directement via le 
        // Map parametresSupplementaires passé en argument
        // du constructeur)
        
        champsReserves.add(ID_TYPE_FORMATION);
        
        
        // Vérification valeurs champs
        
        validatePresenceOf(
            mappingNomsChamps.get(ID_TYPE_FORMATION)
        );
   
        
    }
    
    
    public TypeFormation() {
        // constructeur par défaut requis par activejdbc
    } 
    
    
    public TypeFormation(
        int idTypeFormation,
        Map<String,Object> parametresSupplementaires
        ) {
        // rappel : parametresSupplementaires peut embarquer la valeur
        // ETIQUETTE qui est optionnelle

        this.set(mappingNomsChamps.get(ID_TYPE_FORMATION), idTypeFormation);
        
        for (Map.Entry<String,Object> entry : parametresSupplementaires.entrySet()) {
            String nom = entry.getKey();
            checkSiChampReserve(nom, champsReserves);
            this.set(mappingNomsChamps.get(nom), parametresSupplementaires.get(nom));   
        }

    }
        
    
    
    public TypeFormation(
            int idTypeFormation
            ) {
               
        this(
                idTypeFormation,
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
        
        return this.get(TypeFormation.mappingNomsChamps.get(nom));
        
    }

}
