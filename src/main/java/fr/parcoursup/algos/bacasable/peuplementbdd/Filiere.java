package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Table("G_FIL")
@IdName("G_FL_COD")
public class Filiere extends EntitePersistante {
       
    public static final String ID_FILIERE_FORMATION = "Filiere:ID_FILIERE_FORMATION"; 
    public static final String CODE_FORMATION_INITIALE_APPRENTISSAGE = "Filiere:CODE_FORMATION_INITIALE_APPRENTISSAGE"; 
    public static final String CODE_FORMATION_EN_APPRENTISSAGE = "Filiere:CODE_FORMATION_EN_APPRENTISSAGE"; 
    public static final String LIBELLE_FILIERE_FORMATION = "Filiere:LIBELLE_FILIERE_FORMATION"; 
    public static final String LIBELLE_FILIERE_FORMATION_RACCOURCI = "Filiere:LIBELLE_FILIERE_FORMATION_RACCOURCI"; 
    public static final String ETIQUETTE = "Filiere:ETIQUETTE"; 
    
    
    protected static final Map<String,String> mappingNomsChamps = new HashMap<>();
    protected static final List<String> champsReserves = new ArrayList<>();
    
    static {
        
        // Mapping noms champs

        mappingNomsChamps.put(ID_FILIERE_FORMATION, "G_FL_COD");
        // Code identifiant la filière de formation
        // Type attendu : Integer
        
        mappingNomsChamps.put(CODE_FORMATION_INITIALE_APPRENTISSAGE, "G_FL_COD_FI");
        // Code de la formation initiale correspondant à la formation avec apprentissage
        // Type attendu : Integer
        
        mappingNomsChamps.put(CODE_FORMATION_EN_APPRENTISSAGE, "G_FL_FLG_APP");
        // Code indiquant si la formation est effectuée en apprentissage
        // Type attendu : Integer
        
        mappingNomsChamps.put(LIBELLE_FILIERE_FORMATION, "G_FL_LIB");
        // Libellé décrivant la filière de formation
        // Type attendu : String
        
        mappingNomsChamps.put(LIBELLE_FILIERE_FORMATION_RACCOURCI, "G_FL_SIG");
        // Libellé sous forme raccourcie
        // Type attendu : String
        
        mappingNomsChamps.put(ETIQUETTE, "ETIQUETTE1");
        // Etiquette n°1
        // Type attendu : String
        
        
        // Champs réservés (ne peuvent être définis directement via le 
        // Map parametresSupplementaires passé en argument
        // du constructeur)
        
        champsReserves.add(ID_FILIERE_FORMATION);
        
        
        // Vérification valeurs champs
        
        validatePresenceOf(
            mappingNomsChamps.get(ID_FILIERE_FORMATION)
        );    

    }
    
    
    public Filiere() {
        // constructeur par défaut requis par activejdbc
    } 
    
    
    public Filiere(
        int idFiliere,
        Map<String,Object> parametresSupplementaires
        ) {
        // rappel : parametresSupplementaires peut embarquer les valeurs
        // CODE_FORMATION_INITIALE_APPRENTISSAGE, CODE_FORMATION_EN_APPRENTISSAGE, ETIQUETTE... qui sont optionnelles
        
        this.set(mappingNomsChamps.get(ID_FILIERE_FORMATION), idFiliere);
        
        for (Map.Entry<String,Object> entry : parametresSupplementaires.entrySet()) {
            String nom = entry.getKey();
            checkSiChampReserve(nom, champsReserves);
            this.set(mappingNomsChamps.get(nom), parametresSupplementaires.get(nom));   
        }
        
    }
    
    
    public Filiere(
            int idFiliere
            ) {
               
        this(
            idFiliere,
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
        
        return this.get(Filiere.mappingNomsChamps.get(nom));
        
    }
         
}
