package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Table("A_REC_GRP_INT")
@IdName("C_GI_COD")
public class GroupeAffectationInternat extends EntitePersistante {
    
    public static final String ID_INTERNAT = "GroupeAffectationInternat:ID_INTERNAT"; 
    public static final String ID_FORMATION_INSCRIPTION = "GroupeAffectationInternat:ID_FORMATION_INSCRIPTION"; 
    public static final String ID_FORMATION_AFFECTATION = "GroupeAffectationInternat:ID_FORMATION_AFFECTATION"; 
    public static final String ID_ETABLISSEMENT_INSCRIPTION = "GroupeAffectationInternat:ID_ETABLISSEMENT_INSCRIPTION"; 
    public static final String NOMBRE_RECRUTEMENTS_SOUHAITE = "GroupeAffectationInternat:NOMBRE_RECRUTEMENTS_SOUHAITE"; 
    public static final String ETIQUETTE = "GroupeAffectationInternat:ETIQUETTE";     
    
    public static final String CLE_FORMATION = "formation"; 
    public static final String CLE_ETABLISSEMENT = "etablissement"; 
    
    protected static final Map<String,String> mappingNomsChamps = new HashMap<>();
    protected static final List<String> champsReserves = new ArrayList<>();
    
    static {
        
        // Mapping noms champs

        mappingNomsChamps.put(ID_INTERNAT, "C_GI_COD");
        // Identifiant de l’internat
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_FORMATION_INSCRIPTION, "G_TI_COD");
        // Identifiant de la formation d'inscription
        //  (peut être null si l’internat n'est lié à aucune formation particulière)
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_FORMATION_AFFECTATION, "G_TA_COD");
        // Identifiant de la formation d'affectation
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_ETABLISSEMENT_INSCRIPTION, "G_EA_COD_INS");
        // Identifiant de l’établissement d’inscription
        // Type attendu : String
        
        mappingNomsChamps.put(NOMBRE_RECRUTEMENTS_SOUHAITE, "A_RI_NBR_SOU");
        // Nombre de recrutements souhaité
        // Type attendu : Integer
        
        mappingNomsChamps.put(ETIQUETTE, "ETIQUETTE1");
        // Etiquette n°1
        // Type attendu : String
        
        
        // Champs réservés (ne peuvent être définis directement via le 
        // Map parametresSupplementaires passé en argument
        // du constructeur)
        
        champsReserves.add(ID_INTERNAT);
        champsReserves.add(ID_FORMATION_INSCRIPTION);
        champsReserves.add(ID_FORMATION_AFFECTATION);
        champsReserves.add(ID_ETABLISSEMENT_INSCRIPTION);
        
        
        // Vérification valeurs champs
        
        validatePresenceOf(
            mappingNomsChamps.get(ID_INTERNAT)
        );

    }
    
    
    public GroupeAffectationInternat() {
        // constructeur par défaut requis par activejdbc
    } 
    
    
    
    public GroupeAffectationInternat(
        int idInternat,
        Map<String,Object> parametresSupplementaires
        ) {
        // rappel : parametresSupplementaires peut embarquer 
        // plusieurs valeurs parmi lesquelles les références aux
        // objets de type Formation ou Etablissement qui lui sont
        // éventuellement associés
        
        if(parametresSupplementaires.containsKey(CLE_FORMATION) && parametresSupplementaires.containsKey(CLE_ETABLISSEMENT)) {
            throw new IllegalArgumentException("Impossible de spécifier à la fois la formation et l'établissement, veuillez ne spécifier dans ce cas que la formation");
        }
        
        this.set(mappingNomsChamps.get(ID_INTERNAT), idInternat);
        
        if(parametresSupplementaires.containsKey(CLE_FORMATION)) {
            
            Formation formation = (Formation) parametresSupplementaires.get(CLE_FORMATION);
            
            FormationInscription formationInscription = formation.getFormationInscription();
            FormationAffectation formationAffectation  = formation.getFormationAffectation();
            
            int typeInternatAssocie = (int) formationInscription.getValeurChamp(FormationInscription.CODE_TYPE_INTERNAT_ASSOCIE);
            
            
            if(typeInternatAssocie == 0) {
                // Internat spécifique à la formation
                
                formation.setGroupeAffectationInternat(this);

                int idFormationInscription = (int) formationInscription.getValeurChamp(FormationInscription.ID_FORMATION_INSCRIPTION);
                this.set(mappingNomsChamps.get(ID_FORMATION_INSCRIPTION), idFormationInscription);

                int idFormationAffectation = (int) formationAffectation.getValeurChamp(FormationAffectation.ID_FORMATION_AFFECTATION);
                this.set(mappingNomsChamps.get(ID_FORMATION_AFFECTATION), idFormationAffectation);

            }

            else if (typeInternatAssocie == 1) {
                // Internat commun à toutes les formations proposées par l'établissement
                
                formation.setGroupeAffectationInternat(this);

                Etablissement etablissement = formationInscription.getEtablissement();

                String idEtablissementInscription = (String) etablissement.getValeurChamp(Etablissement.ID_ETABLISSEMENT);
                this.set(mappingNomsChamps.get(ID_ETABLISSEMENT_INSCRIPTION), idEtablissementInscription);
                
            }
                 
            parametresSupplementaires.remove(CLE_FORMATION);
            
        }
        
        
        else if(parametresSupplementaires.containsKey(CLE_ETABLISSEMENT)) {
            
            Etablissement etablissement = (Etablissement) parametresSupplementaires.get(CLE_ETABLISSEMENT);
            
            String idEtablissementInscription = (String) etablissement.getValeurChamp(Etablissement.ID_ETABLISSEMENT);
            this.set(mappingNomsChamps.get(ID_ETABLISSEMENT_INSCRIPTION), idEtablissementInscription);
            
            parametresSupplementaires.remove(CLE_ETABLISSEMENT);
            
        }
        
        for (Map.Entry<String,Object> entry : parametresSupplementaires.entrySet()) {
            String nom = entry.getKey();
            checkSiChampReserve(nom, champsReserves);
            this.set(mappingNomsChamps.get(nom), parametresSupplementaires.get(nom));   
        }

    }
         
    
    
    public GroupeAffectationInternat(
            int idInternat
            ) {
               
        this(
            idInternat,
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
        
        return this.get(GroupeAffectationInternat.mappingNomsChamps.get(nom));
        
    } 

}
