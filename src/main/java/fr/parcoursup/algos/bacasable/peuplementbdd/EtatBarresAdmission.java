package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.annotations.CompositePK;
import org.javalite.activejdbc.annotations.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Table("A_REC_GRP_INT_PROP")
@CompositePK({"C_GI_COD", "G_TA_COD", "G_TI_COD", "C_GP_COD", "NB_JRS"})
public class EtatBarresAdmission extends EntitePersistante {
         
    public static final String ID_INTERNAT = "EtatBarresAdmission:ID_INTERNAT"; 
    public static final String ID_FORMATION_AFFECTATION = "EtatBarresAdmission:ID_FORMATION_AFFECTATION"; 
    public static final String ID_FORMATION_INSCRIPTION = "EtatBarresAdmission:ID_FORMATION_INSCRIPTION"; 
    public static final String ID_GROUPE_CLASSEMENT_PEDAGOGIQUE = "EtatBarresAdmission:ID_GROUPE_CLASSEMENT_PEDAGOGIQUE"; 
    public static final String VALEUR_BARRE_ADMISSION_GROUPE_FORMATION = "EtatBarresAdmission:VALEUR_BARRE_ADMISSION_GROUPE_FORMATION"; 
    public static final String VALEUR_BARRE_ADMISSION_INTERNAT = "EtatBarresAdmission:VALEUR_BARRE_ADMISSION_INTERNAT"; 
    public static final String NOMBRE_JOURS_DEPUIS_DEBUT_CAMPAGNE = "EtatBarresAdmission:NOMBRE_JOURS_DEPUIS_DEBUT_CAMPAGNE"; 
    public static final String ETIQUETTE = "EtatBarresAdmission:ETIQUETTE"; 
    
    protected static final Map<String,String> mappingNomsChamps = new HashMap<>();
    protected static final List<String> champsReserves = new ArrayList<>();
    
    static {

        // Mapping noms champs
        
        mappingNomsChamps.put(ID_INTERNAT, "C_GI_COD");
        // Identifiant de l’internat
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_FORMATION_AFFECTATION, "G_TA_COD");
        // Identifiant de la formation d'affectation
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_FORMATION_INSCRIPTION, "G_TI_COD");
        // Identifiant de la formation d'inscription
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE, "C_GP_COD");
        // Identifiant du groupe de classement
        // Type attendu : Integer
        
        mappingNomsChamps.put(VALEUR_BARRE_ADMISSION_GROUPE_FORMATION, "A_RG_RAN_DER");
        // Valeur de la barre d’admission pour le groupe d’affectation / formation concerné
        // type attendu : Integer
        
        mappingNomsChamps.put(VALEUR_BARRE_ADMISSION_INTERNAT, "A_RG_RAN_DER_INT");
        // Valeur de la barre d’admission pour l’internat concerné
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
        
        champsReserves.add(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE);
        champsReserves.add(ID_FORMATION_AFFECTATION);
        champsReserves.add(ID_FORMATION_INSCRIPTION);
        champsReserves.add(ID_INTERNAT);
        champsReserves.add(VALEUR_BARRE_ADMISSION_GROUPE_FORMATION);
        champsReserves.add(NOMBRE_JOURS_DEPUIS_DEBUT_CAMPAGNE);
                
        
        // Vérification valeurs champs
        
        validatePresenceOf(
            mappingNomsChamps.get(ID_INTERNAT),
            mappingNomsChamps.get(ID_FORMATION_AFFECTATION),
            mappingNomsChamps.get(ID_FORMATION_INSCRIPTION),
            mappingNomsChamps.get(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE),
            mappingNomsChamps.get(NOMBRE_JOURS_DEPUIS_DEBUT_CAMPAGNE),
            mappingNomsChamps.get(VALEUR_BARRE_ADMISSION_INTERNAT)
        );

    }
    
    
    protected GroupeAffectationFormation groupeAffectationFormation;
    
    
    public EtatBarresAdmission() {
        // constructeur par défaut requis par activejdbc
    } 
    
    
    public EtatBarresAdmission(
        GroupeAffectationFormation groupeAffectationFormation,
        int valeurBarreAdmissionGroupeFormation,
        int nombreJoursDepuisDebutCampagne,
        Map<String,Object> parametresSupplementaires
        ) {
        // rappel : parametresSupplementaires peut embarquer la valeur VALEUR_BARRE_ADMISSION_INTERNAT qui est optionnelle
        // car toutes les formations ne proposent pas un internat
        
        this.groupeAffectationFormation = groupeAffectationFormation;

        Formation formation = groupeAffectationFormation.getFormation();
        FormationAffectation formationAffectation = formation.getFormationAffectation();
        FormationInscription formationInscription = formation.getFormationInscription();
        GroupeAffectationInternat groupeAffectationInternat = formation.getGroupeAffectationInternat();

        this.set(mappingNomsChamps.get(VALEUR_BARRE_ADMISSION_GROUPE_FORMATION), valeurBarreAdmissionGroupeFormation);
        
        this.set(mappingNomsChamps.get(NOMBRE_JOURS_DEPUIS_DEBUT_CAMPAGNE), nombreJoursDepuisDebutCampagne);

        int idGroupeClassementPedagogoque = (int) this.groupeAffectationFormation.getValeurChamp(GroupeAffectationFormation.ID_GROUPE_CLASSEMENT_PEDAGOGIQUE);
        this.set(mappingNomsChamps.get(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE), idGroupeClassementPedagogoque);
        
        int idFormationAffectation = (int) formationAffectation.getValeurChamp(FormationAffectation.ID_FORMATION_AFFECTATION);
        this.set(mappingNomsChamps.get(ID_FORMATION_AFFECTATION), idFormationAffectation);

        int idFormationInscription = (int) formationInscription.getValeurChamp(FormationInscription.ID_FORMATION_INSCRIPTION);
        this.set(mappingNomsChamps.get(ID_FORMATION_INSCRIPTION), idFormationInscription);
        
        if(groupeAffectationInternat != null) { 
            int idInternat = (int) groupeAffectationInternat.getValeurChamp(GroupeAffectationInternat.ID_INTERNAT);
            this.set(mappingNomsChamps.get(ID_INTERNAT), idInternat);
        }
        
        for (Map.Entry<String,Object> entry : parametresSupplementaires.entrySet()) {
            String nom = entry.getKey();
            checkSiChampReserve(nom, champsReserves);
            this.set(mappingNomsChamps.get(nom), parametresSupplementaires.get(nom));   
        }
        
    }
    
    
    public EtatBarresAdmission(
            GroupeAffectationFormation groupeAffectationFormation,
            int valeurBarreAdmissionGroupeFormation,
            int nombreJoursDepuisDebutCampagne
            ) {
               
        this(
            groupeAffectationFormation,
            valeurBarreAdmissionGroupeFormation,
            nombreJoursDepuisDebutCampagne,
                new HashMap<>());
                
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
        
        return this.get(EtatBarresAdmission.mappingNomsChamps.get(nom));
        
    }

}