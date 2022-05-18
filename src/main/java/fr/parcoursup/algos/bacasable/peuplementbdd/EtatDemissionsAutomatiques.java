package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.annotations.CompositePK;
import org.javalite.activejdbc.annotations.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Table("A_ADM_DEM")
@CompositePK({"G_CN_COD", "G_TA_COD", "I_RH_COD", "NB_JRS"})
public class EtatDemissionsAutomatiques extends EntitePersistante {
        
    public static final String ID_CANDIDAT = "EtatDemissionsAutomatiques:ID_CANDIDAT"; 
    public static final String ID_FORMATION_AFFECTATION = "EtatDemissionsAutomatiques:ID_FORMATION_AFFECTATION"; 
    public static final String ID_REGIME_HEBERGEMENT = "EtatDemissionsAutomatiques:ID_REGIME_HEBERGEMENT"; 
    public static final String ID_GROUPE_CLASSEMENT_PEDAGOGIQUE = "EtatDemissionsAutomatiques:ID_GROUPE_CLASSEMENT_PEDAGOGIQUE"; 
    public static final String ID_FORMATION_INSCRIPTION = "EtatDemissionsAutomatiques:ID_FORMATION_INSCRIPTION"; 
    public static final String ID_INTERNAT = "EtatDemissionsAutomatiques:ID_INTERNAT"; 
    public static final String CODE_PROPOSITION_REJETEE_PAR_REPONDEUR_AUTOMATIQUE = "EtatDemissionsAutomatiques:CODE_PROPOSITION_REJETEE_PAR_REPONDEUR_AUTOMATIQUE"; 
    public static final String NOMBRE_JOURS_DEPUIS_DEBUT_CAMPAGNE = "EtatDemissionsAutomatiques:NOMBRE_JOURS_DEPUIS_DEBUT_CAMPAGNE"; 
    public static final String ETIQUETTE = "EtatDemissionsAutomatiques:ETIQUETTE"; 
    
    protected static final Map<String,String> mappingNomsChamps = new HashMap<>();
    protected static final List<String> champsReserves = new ArrayList<>();
    
    static {

        // Mapping noms champs
        
        mappingNomsChamps.put(ID_CANDIDAT, "G_CN_COD");
        // Identifiant du candidat
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_FORMATION_AFFECTATION, "G_TA_COD");
        // Identifiant de la formation d'affectation
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_REGIME_HEBERGEMENT, "I_RH_COD");
        // code identifiant le régime d'hébergement demandé
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE, "C_GP_COD");
        // Identifiant du groupe de classement
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_FORMATION_INSCRIPTION, "G_TI_COD");
        // Identifiant de la formation d'inscription
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_INTERNAT, "C_GI_COD");
        // Identifiant de l’internat
        // Type attendu : Integer
        
        mappingNomsChamps.put(CODE_PROPOSITION_REJETEE_PAR_REPONDEUR_AUTOMATIQUE, "EST_DEM_PROP");
        // Code indiquant si la proposition a été rejetée
        // par suite de la mise en oeuvre du répondeur automatique
        // 0 = non, 1 = oui
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
        
        champsReserves.add(ID_CANDIDAT);
        champsReserves.add(ID_FORMATION_AFFECTATION);
        champsReserves.add(ID_REGIME_HEBERGEMENT);
        champsReserves.add(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE);
        champsReserves.add(ID_FORMATION_INSCRIPTION);
        champsReserves.add(ID_INTERNAT);
        champsReserves.add(CODE_PROPOSITION_REJETEE_PAR_REPONDEUR_AUTOMATIQUE);
        champsReserves.add(NOMBRE_JOURS_DEPUIS_DEBUT_CAMPAGNE);
        
        
        // Vérification valeurs champs
        
        validatePresenceOf(            
            mappingNomsChamps.get(ID_CANDIDAT),
            mappingNomsChamps.get(ID_FORMATION_AFFECTATION),
            mappingNomsChamps.get(ID_REGIME_HEBERGEMENT),
            mappingNomsChamps.get(NOMBRE_JOURS_DEPUIS_DEBUT_CAMPAGNE),
            mappingNomsChamps.get(CODE_PROPOSITION_REJETEE_PAR_REPONDEUR_AUTOMATIQUE)  
        );

    }
    
    
    protected Voeu voeu;
    
    
    public EtatDemissionsAutomatiques() {
        // constructeur par défaut requis par activejdbc
    } 
    
    
    public EtatDemissionsAutomatiques(
        Voeu voeu,
        int propositionRejeteeParRepondeurAutomatique,
        int nombreJoursDepuisDebutCampagne,
        Map<String,Object> parametresSupplementaires
        ) {
        // rappel : parametresSupplementaires peut embarquer la valeur
        // ETIQUETTE qui est optionnelle
        
        
        this.voeu = voeu;

        Candidat candidat = this.voeu.getCandidat();
        GroupeAffectationFormation groupeAffectationFormation = this.voeu.getGroupeAffectationFormation();
        Formation formation = groupeAffectationFormation.getFormation();
        FormationInscription formationInscription = formation.getFormationInscription();
        FormationAffectation formationAffectation  = formation.getFormationAffectation();
        GroupeAffectationInternat groupeAffectationInternat = formation.getGroupeAffectationInternat();
        RegimeHebergement regimeHebergement = this.voeu.getRegimeHebergement();
        
        this.set(mappingNomsChamps.get(CODE_PROPOSITION_REJETEE_PAR_REPONDEUR_AUTOMATIQUE), propositionRejeteeParRepondeurAutomatique);
        this.set(mappingNomsChamps.get(NOMBRE_JOURS_DEPUIS_DEBUT_CAMPAGNE), nombreJoursDepuisDebutCampagne);

        int idCandidat = (int) candidat.getValeurChamp(Candidat.ID_CANDIDAT);
        this.set(mappingNomsChamps.get(ID_CANDIDAT), idCandidat);

        int idFormationAffectation = (int) formationAffectation.getValeurChamp(FormationAffectation.ID_FORMATION_AFFECTATION);
        this.set(mappingNomsChamps.get(ID_FORMATION_AFFECTATION), idFormationAffectation);

        int iRegimeHebergement = (int) regimeHebergement.getValeurChamp(ID_REGIME_HEBERGEMENT);
        this.set(mappingNomsChamps.get(ID_REGIME_HEBERGEMENT), iRegimeHebergement);

        int idGroupeAffectationFormation = (int) groupeAffectationFormation.getValeurChamp(GroupeAffectationFormation.ID_GROUPE_CLASSEMENT_PEDAGOGIQUE);
        this.set(mappingNomsChamps.get(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE), idGroupeAffectationFormation);

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
    
    
    public EtatDemissionsAutomatiques(
            Voeu voeu,
            int propositionRejeteeParRepondeurAutomatique,
            int nombreJoursDepuisDebutCampagne
            ) {
               
        this(
            voeu,
            propositionRejeteeParRepondeurAutomatique,
            nombreJoursDepuisDebutCampagne,
                new HashMap<>()
            );
               
    }
    
    
    public Voeu getVoeu() {
        
        if(this.voeu == null) {
            this.voeu = Voeu.findByCompositeKeys(
                this.getValeurChamp(ID_CANDIDAT), 
                this.getValeurChamp(ID_FORMATION_AFFECTATION), 
                this.getValeurChamp(ID_REGIME_HEBERGEMENT)
            );
        }
        
        return this.voeu;      

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
        
        return this.get(EtatDemissionsAutomatiques.mappingNomsChamps.get(nom));
        
    }
        
}
