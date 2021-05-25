package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.annotations.CompositePK;
import org.javalite.activejdbc.annotations.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Table("A_REC")
@CompositePK({"G_TI_COD", "G_TA_COD"})
public class Formation extends EntitePersistante {
        
    public static final String ID_FORMATION_INSCRIPTION = "Formation:ID_FORMATION_INSCRIPTION"; 
    public static final String ID_FORMATION_AFFECTATION = "Formation:ID_FORMATION_AFFECTATION"; 
    public static final String CAPACITE_TOTALE_FORMATION = "Formation:CAPACITE_TOTALE_FORMATION"; 
    public static final String CAPACITE_INTERNAT_FILLES = "Formation:CAPACITE_INTERNAT_FILLES"; 
    public static final String CAPACITE_INTERNAT_GARCONS = "Formation:CAPACITE_INTERNAT_GARCONS"; 
    public static final String CAPACITE_INTERNAT_MIXTE = "Formation:CAPACITE_INTERNAT_MIXTE"; 
    public static final String NOMBRE_PLACES_DISPONIBLES = "Formation:NOMBRE_PLACES_DISPONIBLES"; 
    public static final String ID_JURY_PEDAGOGIQUE = "Formation:ID_JURY_PEDAGOGIQUE"; 
    public static final String CODE_TAUX_MINIMUM_BOURSIERS_APPLIQUE = "Formation:CODE_TAUX_MINIMUM_BOURSIERS_APPLIQUE"; 
    public static final String TAUX_MINIMUM_BOURSIERS = "Formation:TAUX_MINIMUM_BOURSIERS"; 
    public static final String CODE_PREFERENCE_CANDIDATS_RESIDENTS_APPLIQUEE = "Formation:CODE_PREFERENCE_CANDIDATS_RESIDENTS_APPLIQUEE"; 
    public static final String TAUX_MAXIMUM_NON_RESIDENTS = "Formation:TAUX_MAXIMUM_NON_RESIDENTS"; 
    public static final String ETIQUETTE = "Formation:ETIQUETTE"; 
    
    protected static final Map<String,String> mappingNomsChamps = new HashMap<>();
    protected static final List<String> champsReserves = new ArrayList<>();
    
    static {
        
        // Mapping noms champs

        mappingNomsChamps.put(ID_FORMATION_INSCRIPTION, "G_TI_COD");
        // Identifiant de la formation d'inscription
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_FORMATION_AFFECTATION, "G_TA_COD");
        // Identifiant de la formation d'affectation
        // Type attendu : Integer
        
        mappingNomsChamps.put(CAPACITE_TOTALE_FORMATION, "A_RC_CAP");
        // Capacité totale de la formation
        // Type attendu : Integer
        
        mappingNomsChamps.put(CAPACITE_INTERNAT_FILLES, "A_RC_CAP_INT_FIL");
        // Capacité de l'internat filles éventuellement associé à la formation
        // Type attendu : Integer
        
        mappingNomsChamps.put(CAPACITE_INTERNAT_GARCONS, "A_RC_CAP_INT_GAR");
        // Capacité de l'internat garçons éventuellement associé à la formation
        // Type attendu : Integer
        
        mappingNomsChamps.put(CAPACITE_INTERNAT_MIXTE, "A_RC_CAP_INT_MIX");
        // Capacité de l'internat mixte éventuellement associé à la formation
        // Type attendu : Integer
        
        mappingNomsChamps.put(NOMBRE_PLACES_DISPONIBLES, "A_RC_CAP_INF");
        // Nombre de places disponibles au moment du paramétrage
        // Type attendu : Integer

        mappingNomsChamps.put(ID_JURY_PEDAGOGIQUE, "C_JA_COD");
        // Identifiant du jury d’admission pédagogique
        // Type attendu : Integer
        
        mappingNomsChamps.put(CODE_TAUX_MINIMUM_BOURSIERS_APPLIQUE, "A_RC_FLG_TAU_BRS");
        // Code indiquant si un taux minimum de boursiers doit être appliqué
        // Valeurs possibles : 0 = non, 1 = oui
        
        mappingNomsChamps.put(TAUX_MINIMUM_BOURSIERS, "A_RC_TAU_BRS_REC");
        // Taux minimum de boursiers
        // Valeur entière entre 0 et 100
        // Type attendu : Integer
        
        mappingNomsChamps.put(CODE_PREFERENCE_CANDIDATS_RESIDENTS_APPLIQUEE, "A_RC_FLG_TAU_NON_RES");
        // Code indiquant si un taux maximum de non résidents doit être appliqué
        // Valeurs possibles : 0 = non, 1 = oui
        // Type attendu : Integer
        
        mappingNomsChamps.put(TAUX_MAXIMUM_NON_RESIDENTS, "A_RC_TAU_NON_RES_REC");
        // Taux maximum de non résidents
        // Valeur entière entre 0 et 100
        // Type attendu : Integer
        
        mappingNomsChamps.put(ETIQUETTE, "ETIQUETTE1");
        // Etiquette n°1
        // Type attendu : String
        
        
        // Champs réservés (ne peuvent être définis directement via le 
        // Map parametresSupplementaires passé en argument
        // du constructeur)
        
        champsReserves.add(ID_FORMATION_INSCRIPTION);
        champsReserves.add(ID_FORMATION_AFFECTATION);
        champsReserves.add(ID_JURY_PEDAGOGIQUE);
        
        
        // Vérification valeurs champs
        
        validatePresenceOf(            
            mappingNomsChamps.get(ID_FORMATION_INSCRIPTION),
            mappingNomsChamps.get(ID_FORMATION_AFFECTATION)
        );

    }
    
    
    protected FormationInscription formationInscription;
    
    protected FormationAffectation formationAffectation;
    
    protected GroupeAffectationInternat groupeAffectationInternat;
     
    
    public Formation() {
        // constructeur par défaut requis par activejdbc
    } 
    
    
    public Formation(
        FormationInscription formationInscription,
        FormationAffectation formationAffectation,
        Map<String,Object> parametresSupplementaires
        ) {
        // rappel : parametresSupplementaires peut embarquer les valeurs
        // CAPACITE_TOTALE_FORMATION, ETIQUETTE... qui sont optionnelles
        
        this.formationInscription = formationInscription;
        this.formationAffectation = formationAffectation;

        JuryPedagogique juryPedagogique = this.formationInscription.getJuryPedagogique();
        
        int idFormationInscription = (int) this.formationInscription.getValeurChamp(FormationInscription.ID_FORMATION_INSCRIPTION);
        this.set(mappingNomsChamps.get(ID_FORMATION_INSCRIPTION), idFormationInscription);

        int idFormationAffectation = (int) this.formationAffectation.getValeurChamp(FormationAffectation.ID_FORMATION_AFFECTATION);
        this.set(mappingNomsChamps.get(ID_FORMATION_AFFECTATION), idFormationAffectation);
        
        int idJuryPedagogique = (int) juryPedagogique.getValeurChamp(JuryPedagogique.ID_JURY_PEDAGOGIQUE);
        this.set(mappingNomsChamps.get(ID_JURY_PEDAGOGIQUE), idJuryPedagogique);  
        
        for (Map.Entry<String,Object> entry : parametresSupplementaires.entrySet()) {
            String nom = entry.getKey();
            checkSiChampReserve(nom, champsReserves);
            this.set(mappingNomsChamps.get(nom), parametresSupplementaires.get(nom));   
        } 
        
    }
    
    
    public Formation(
        FormationInscription formationInscription,
        FormationAffectation formationAffectation
        ) {
               
        this(
            formationInscription,
            formationAffectation,
                new HashMap<>()
            );
               
    }
    
    
    public FormationInscription getFormationInscription() {

        
        if(this.formationInscription == null) {
            this.formationInscription = FormationInscription.findById(this.getValeurChamp(ID_FORMATION_INSCRIPTION));
        }
        
        return this.formationInscription;  
    
    }


    public FormationAffectation getFormationAffectation() {

        if(this.formationAffectation == null) {
            this.formationAffectation = FormationInscription.findById(this.getValeurChamp(ID_FORMATION_AFFECTATION));
        }
        
        return this.formationAffectation;  

    }


    public GroupeAffectationInternat getGroupeAffectationInternat() {

        return this.groupeAffectationInternat;

    }


    public void setGroupeAffectationInternat(GroupeAffectationInternat groupeAffectationInternat) {

        this.groupeAffectationInternat = groupeAffectationInternat;

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
        
        return this.get(Formation.mappingNomsChamps.get(nom));
        
    }
    
}