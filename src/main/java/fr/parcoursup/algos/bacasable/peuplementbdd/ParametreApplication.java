package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Table("G_PAR")
@IdName("G_PR_COD")
public class ParametreApplication extends EntitePersistante {
    
    public static final String ID_PARAMETRE = "ParametreApplication:ID_PARAMETRE"; 
    public static final String VALEUR_PARAMETRE = "ParametreApplication:VALEUR_PARAMETRE"; 
    public static final String ETIQUETTE = "ParametreApplication:ETIQUETTE"; 
    
    protected static final Map<String,String> mappingNomsChamps = new HashMap<>();
    protected static final List<String> champsReserves = new ArrayList<>();
    
    static {
        
        // Mapping noms champs

        mappingNomsChamps.put(ID_PARAMETRE, "G_PR_COD");
        // Code identifiant un paramètre de l’application
        // Type attendu : Integer
        
        mappingNomsChamps.put(VALEUR_PARAMETRE, "G_PR_VAL");
        // Valeur du paramètre
        // Type attendu : String
        
        mappingNomsChamps.put(ETIQUETTE, "ETIQUETTE1");
        // Etiquette n°1
        // Type attendu : String
        
        
        // Champs réservés (ne peuvent être définis directement via le 
        // Map parametresSupplementaires passé en argument
        // du constructeur)
        
        champsReserves.add(ID_PARAMETRE);
        champsReserves.add(VALEUR_PARAMETRE);

        
        // Vérification valeurs champs
        
        validatePresenceOf(            
            mappingNomsChamps.get(ID_PARAMETRE)
        );

    }
    
    
    public ParametreApplication() {
        // constructeur par défaut requis par activejdbc
    } 
    
    
    public ParametreApplication(
        int idParametre,
        String valeurParametre,
        Map<String,Object> parametresSupplementaires
        ) {
        // rappel : parametresSupplementaires peut embarquer la valeur
        // ETIQUETTE qui est optionnelle
        
        this.set(mappingNomsChamps.get(ID_PARAMETRE), idParametre);
        this.set(mappingNomsChamps.get(VALEUR_PARAMETRE), valeurParametre);
        
        for (Map.Entry<String,Object> entry : parametresSupplementaires.entrySet()) {
            String nom = entry.getKey();
            checkSiChampReserve(nom, champsReserves);
            this.set(mappingNomsChamps.get(nom), parametresSupplementaires.get(nom));   
        }
        
    }
          
    
    public ParametreApplication(
            int idParametre,
            String valeurParametre
            ) {
               
        this(
            idParametre,
            valeurParametre,
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
        
        return this.get(ParametreApplication.mappingNomsChamps.get(nom));
        
    }

}