package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.annotations.BelongsTo;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Table("C_JUR_ADM")
@IdName("C_JA_COD")
@BelongsTo(parent = FormationInscription.class, foreignKeyName = "G_TI_COD")
public class JuryPedagogique extends EntitePersistante {
        
    public static final String ID_JURY_PEDAGOGIQUE = "JuryPedagogique:ID_JURY_PEDAGOGIQUE"; 
    public static final String ID_FORMATION_INSCRIPTION = "JuryPedagogique:ID_FORMATION_INSCRIPTION";
    public static final String ETIQUETTE = "JuryPedagogique:ETIQUETTE"; 
    
    protected static final Map<String,String> mappingNomsChamps = new HashMap<>();
    protected static final List<String> champsReserves = new ArrayList<>();
    
    static {
        
        // Mapping noms champs

        mappingNomsChamps.put(ID_JURY_PEDAGOGIQUE, "C_JA_COD");
        // Identifiant du jury d’admission pédagogique
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_FORMATION_INSCRIPTION, "G_TI_COD");
        // Identifiant de la formation d'inscription
        // Type attendu : Integer
        
        mappingNomsChamps.put(ETIQUETTE, "ETIQUETTE1");
        // Etiquette n°1
        // Type attendu : String
        
        
        // Champs réservés (ne peuvent être définis directement via le 
        // Map parametresSupplementaires passé en argument
        // du constructeur)
        
        champsReserves.add(ID_JURY_PEDAGOGIQUE);
        champsReserves.add(ID_FORMATION_INSCRIPTION);

        
        // Vérification valeurs champs
        
        validatePresenceOf(
            mappingNomsChamps.get(ID_JURY_PEDAGOGIQUE)
        );

    }
    
    
    protected FormationInscription formationInscription;
    
    
    public static List<JuryPedagogique> findFieldsEqual(Map<String,Object> criteresRecherche) {
        
        String requeteSql = prepareRequeteRechercheAvecCriteresEgalite(
            getTableName(),
            JuryPedagogique.mappingNomsChamps,
            criteresRecherche);
        
        return findBySQL(requeteSql);
        
    }
    
    
    public JuryPedagogique() {
        // constructeur par défaut requis par activejdbc
    } 
    
    
    public JuryPedagogique(
        int idJuryPedagogique,
        FormationInscription formationInscription,
        Map<String,Object> parametresSupplementaires
        ) {
        // rappel : parametresSupplementaires peut embarquer la valeur
        // ETIQUETTE qui est optionnelle
        
        this.set(mappingNomsChamps.get(ID_JURY_PEDAGOGIQUE), idJuryPedagogique);
        
        
        this.formationInscription = formationInscription;
        int idFormationInscription = (int) this.formationInscription.getValeurChamp(FormationInscription.ID_FORMATION_INSCRIPTION);
        this.set(mappingNomsChamps.get(ID_FORMATION_INSCRIPTION), idFormationInscription);
        
        formationInscription.setJuryPedagogique(this);
        
        for (Map.Entry<String,Object> entry : parametresSupplementaires.entrySet()) {
            String nom = entry.getKey();
            checkSiChampReserve(nom, champsReserves);
            this.set(mappingNomsChamps.get(nom), parametresSupplementaires.get(nom));   
        } 
        
    }
    
    
    public JuryPedagogique(
            int idJuryPedagogique,
            FormationInscription formationInscription
            ) {
               
        this(
                idJuryPedagogique,
            formationInscription,
                new HashMap<>()
            );
               
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
        
        return this.get(JuryPedagogique.mappingNomsChamps.get(nom));
        
    }
        
}
