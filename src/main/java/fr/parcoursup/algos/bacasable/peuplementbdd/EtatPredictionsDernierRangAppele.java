package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.annotations.CompositePK;
import org.javalite.activejdbc.annotations.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Table("A_ADM_PRED_DER_APP")
@CompositePK({"G_TA_COD", "C_GP_COD", "NB_JRS"})
public class EtatPredictionsDernierRangAppele extends EntitePersistante {
        
    public static final String ID_FORMATION_AFFECTATION = "EtatPredictionsDernierRangAppele:ID_FORMATION_AFFECTATION"; 
    public static final String ID_GROUPE_CLASSEMENT_PEDAGOGIQUE = "EtatPredictionsDernierRangAppele:ID_GROUPE_CLASSEMENT_PEDAGOGIQUE"; 
    public static final String ESTIMATION_RANG_DERNIER_APPELE_DATE_PIVOT = "EtatPredictionsDernierRangAppele:ESTIMATION_RANG_DERNIER_APPELE_DATE_PIVOT"; 
    public static final String NOMBRE_JOURS_DEPUIS_DEBUT_CAMPAGNE = "EtatPredictionsDernierRangAppele:NOMBRE_JOURS_DEPUIS_DEBUT_CAMPAGNE"; 
    public static final String ETIQUETTE = "ETIQUETTE"; 
    
   
    protected static final Map<String,String> mappingNomsChamps = new HashMap<>();
    protected static final List<String> champsReserves = new ArrayList<>();
    
    static {
        
        // Mapping noms champs

        mappingNomsChamps.put(ID_FORMATION_AFFECTATION, "G_TA_COD");
        // Identifiant de la formation d'affectation
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE, "C_GP_COD");
        // Identifiant du groupe de classement
        // Type attendu : Integer
        
        mappingNomsChamps.put(ESTIMATION_RANG_DERNIER_APPELE_DATE_PIVOT, "A_RG_RAN_DER");
        // Estimation du rang du dernier appelé à la date pivot
        // Type attendu : Integer
        
        mappingNomsChamps.put(NOMBRE_JOURS_DEPUIS_DEBUT_CAMPAGNE, "NB_JRS");
        // Nombre de jours écoulés depuis le début de la campagne
        // Type attendu : Integer
        
        mappingNomsChamps.put(ETIQUETTE, "ETIQUETTE1");
        // Etiquette n°1
        // Type attendu : String

        
        // Champs réservés (ne peuvent être définis directement via le 
        // Map parametresSupplementaires passé en argument
        // du constructeur)
        
        champsReserves.add(ID_FORMATION_AFFECTATION);
        champsReserves.add(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE);
        champsReserves.add(ESTIMATION_RANG_DERNIER_APPELE_DATE_PIVOT);
        champsReserves.add(NOMBRE_JOURS_DEPUIS_DEBUT_CAMPAGNE);           
        
        
        // Vérification valeurs champs
        
        validatePresenceOf(            
            mappingNomsChamps.get(ID_FORMATION_AFFECTATION),
            mappingNomsChamps.get(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE),  
            mappingNomsChamps.get(ESTIMATION_RANG_DERNIER_APPELE_DATE_PIVOT),  
            mappingNomsChamps.get(NOMBRE_JOURS_DEPUIS_DEBUT_CAMPAGNE)  
        );

    }
    
    
    protected GroupeAffectationFormation groupeAffectationFormation;
    
    
    public EtatPredictionsDernierRangAppele() {
        // constructeur par défaut requis par activejdbc
    } 
    
    
    public EtatPredictionsDernierRangAppele(
        GroupeAffectationFormation groupeAffectationFormation,
        int estimationRangDernierAppeleDatePivot,
        int nombreJoursDepuisDebutCampagne,
        Map<String,Object> parametresSupplementaires
        ) {
        // rappel : parametresSupplementaires peut embarquer la valeur
        // ETIQUETTE qui est optionnelle
                
        this.set(mappingNomsChamps.get(ESTIMATION_RANG_DERNIER_APPELE_DATE_PIVOT), estimationRangDernierAppeleDatePivot);
        this.set(mappingNomsChamps.get(NOMBRE_JOURS_DEPUIS_DEBUT_CAMPAGNE), nombreJoursDepuisDebutCampagne);
        
        this.groupeAffectationFormation = groupeAffectationFormation;
        
        Formation formation = groupeAffectationFormation.getFormation();
        FormationAffectation formationAffectation  = formation.getFormationAffectation();

        int idGroupeAffectationFormation = (int) this.groupeAffectationFormation.getValeurChamp(GroupeAffectationFormation.ID_GROUPE_CLASSEMENT_PEDAGOGIQUE);
        this.set(mappingNomsChamps.get(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE), idGroupeAffectationFormation);
        
        int idFormationAffectation = (int) formationAffectation.getValeurChamp(FormationAffectation.ID_FORMATION_AFFECTATION);
        this.set(mappingNomsChamps.get(ID_FORMATION_AFFECTATION), idFormationAffectation);
        
        for (Map.Entry<String,Object> entry : parametresSupplementaires.entrySet()) {
            String nom = entry.getKey();
            checkSiChampReserve(nom, champsReserves);
            this.set(mappingNomsChamps.get(nom), parametresSupplementaires.get(nom));   
        }
         
    }
    
    
    public EtatPredictionsDernierRangAppele(
            GroupeAffectationFormation groupeAffectationFormation,
            int estimationRangDernierAppeleDatePivot,
            int nombreJoursDepuisDebutCampagne
            ) {
               
        this(
            groupeAffectationFormation,
            estimationRangDernierAppeleDatePivot,
            nombreJoursDepuisDebutCampagne,
                new HashMap<>()
            );
               
    }
     
    
    public GroupeAffectationFormation getGroupeAffectationFormation() {

        return this.groupeAffectationFormation;

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
        
        return this.get(EtatPredictionsDernierRangAppele.mappingNomsChamps.get(nom));
        
    }
    
    
}