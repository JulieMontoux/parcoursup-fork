package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.annotations.CompositePK;
import org.javalite.activejdbc.annotations.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Table("A_REC_GRP")
@CompositePK({"C_GP_COD", "G_TI_COD", "G_TA_COD"})
public class GroupeAffectationFormation extends EntitePersistante {
    
    public static final String ID_GROUPE_CLASSEMENT_PEDAGOGIQUE = "GroupeAffectationFormation:ID_GROUPE_CLASSEMENT_PEDAGOGIQUE"; 
    public static final String ID_FORMATION_INSCRIPTION = "GroupeAffectationFormation:ID_FORMATION_INSCRIPTION"; 
    public static final String ID_FORMATION_AFFECTATION = "GroupeAffectationFormation:ID_FORMATION_AFFECTATION"; 
    public static final String ID_JURY_PEDAGOGIQUE = "GroupeAffectationFormation:ID_JURY_PEDAGOGIQUE"; 
    public static final String NOMBRE_PLACES_DANS_GROUPE = "GroupeAffectationFormation:NOMBRE_PLACES_DANS_GROUPE"; 
    public static final String NOMBRE_RECRUTEMENTS_SOUHAITE = "GroupeAffectationFormation:NOMBRE_RECRUTEMENTS_SOUHAITE"; 
    public static final String RANG_LIMITE_APPEL = "GroupeAffectationFormation:RANG_LIMITE_APPEL"; 
    public static final String CODE_ADMISSIONS_BLOQUEES = "GroupeAffectationFormation:CODE_ADMISSIONS_BLOQUEES"; 
    public static final String ETIQUETTE = "GroupeAffectationFormation:ETIQUETTE"; 
    
    protected static final Map<String,String> mappingNomsChamps = new HashMap<>();
    protected static final List<String> champsReserves = new ArrayList<>();
    
    static {
        
        // Mapping noms champs

        mappingNomsChamps.put(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE, "C_GP_COD");
        // Identifiant du groupe de classement
        // Type attendu : Integer    
                
        mappingNomsChamps.put(ID_FORMATION_INSCRIPTION, "G_TI_COD");
        // Identifiant de la formation d'inscription
        // Type attendu : Integer
                
        mappingNomsChamps.put(ID_FORMATION_AFFECTATION, "G_TA_COD");
        // Identifiant de la formation d'affectation
        // Type attendu : Integer
                
        mappingNomsChamps.put(ID_JURY_PEDAGOGIQUE, "C_JA_COD");
        // Identifiant du jury d’admission pédagogique
        // Type attendu : Integer
                
        mappingNomsChamps.put(NOMBRE_PLACES_DANS_GROUPE, "A_RG_PLA");
        // Nombre de places dans le groupe
        // Type attendu : Integer
                
        mappingNomsChamps.put(NOMBRE_RECRUTEMENTS_SOUHAITE, "A_RG_NBR_SOU");
        // Nombre de candidats souhaité indiqué lors de la dernière phase de proposition d'admission
        // Type attendu : Integer
                
        mappingNomsChamps.put(RANG_LIMITE_APPEL, "A_RG_RAN_LIM");
        // Rang limite d'appel pour les candidats du groupe
        // Type attendu : Integer
              
        mappingNomsChamps.put(CODE_ADMISSIONS_BLOQUEES, "A_RG_ADM_STOP");
        // Code indiquant si les admissions sont bloquées pour ce groupe
        // Parmi les valeurs possibles : 0 = non
        // Type attendu : Integer
        
        mappingNomsChamps.put(ETIQUETTE, "ETIQUETTE1");
        // Etiquette n°1
        // Type attendu : String
        
        
        // Champs réservés (ne peuvent être définis directement via le 
        // Map parametresSupplementaires passé en argument
        // du constructeur)
        
        champsReserves.add(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE);
        champsReserves.add(ID_FORMATION_INSCRIPTION);
        champsReserves.add(ID_FORMATION_AFFECTATION);
        champsReserves.add(ID_JURY_PEDAGOGIQUE);
        
        
        // Vérification valeurs champs
        
        validatePresenceOf(
            mappingNomsChamps.get(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE),
            mappingNomsChamps.get(ID_FORMATION_INSCRIPTION),
            mappingNomsChamps.get(ID_FORMATION_AFFECTATION),
            mappingNomsChamps.get(ID_JURY_PEDAGOGIQUE)
        );

    }
    
    
    
    public GroupeAffectationFormation() {
        // constructeur par défaut requis par activejdbc
    } 
    
    
    
    public GroupeAffectationFormation(
        int idGroupeClassementPedagogique,
        Formation formation,
        Map<String,Object> parametresSupplementaires
        ) {
        // rappel : parametresSupplementaires peut embarquer les valeurs
        //  NOMBRE_PLACES_DANS_GROUPE, NOMBRE_RECRUTEMENTS_SOUHAITE, ETIQUETTE... qui sont optionnelles
        
        this.set(mappingNomsChamps.get(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE), idGroupeClassementPedagogique);
        
        this.formation = formation;

        FormationInscription formationInscription = this.formation.getFormationInscription();
        JuryPedagogique juryPedagogique = formationInscription.getJuryPedagogique();
        
        int idFormationAffectation = (int) this.formation.getValeurChamp(Formation.ID_FORMATION_AFFECTATION);
        this.set(mappingNomsChamps.get(ID_FORMATION_AFFECTATION), idFormationAffectation);

        int idFormationInscription = (int) this.formation.getValeurChamp(Formation.ID_FORMATION_INSCRIPTION);
        this.set(mappingNomsChamps.get(ID_FORMATION_INSCRIPTION), idFormationInscription); 

        int idJuryPedagogique = (int) juryPedagogique.getValeurChamp(JuryPedagogique.ID_JURY_PEDAGOGIQUE);
        this.set(mappingNomsChamps.get(ID_JURY_PEDAGOGIQUE), idJuryPedagogique);
        
        for (Map.Entry<String,Object> entry : parametresSupplementaires.entrySet()) {
            String nom = entry.getKey();
            checkSiChampReserve(nom, champsReserves);
            this.set(mappingNomsChamps.get(nom), parametresSupplementaires.get(nom));   
        }
        
    }
    
    
    public GroupeAffectationFormation(
            int idGroupeClassementPedagogique,
            Formation formation
            ) {
               
        this(
            idGroupeClassementPedagogique,
            formation,
                new HashMap<>()
            );
               
    }
    
    
    public Formation getFormation() {

        return this.formation;

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
        
        return this.get(GroupeAffectationFormation.mappingNomsChamps.get(nom));
        
    }        
    
    
    protected Formation formation;
    
    
}