package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.annotations.CompositePK;
import org.javalite.activejdbc.annotations.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Table("A_VOE")
@CompositePK({"G_CN_COD", "G_TA_COD", "I_RH_COD"})
public class Voeu extends EntitePersistante {
    
    public static final String ID_CANDIDAT = "Voeu:ID_CANDIDAT"; 
    public static final String ID_FORMATION_AFFECTATION = "Voeu:ID_FORMATION_AFFECTATION";
    public static final String ID_REGIME_HEBERGEMENT = "Voeu:ID_REGIME_HEBERGEMENT";
    public static final String ID_SITUATION_VOEU = "Voeu:ID_SITUATION_VOEU";
    public static final String RANG_VOEU_REPONDEUR = "Voeu:RANG_VOEU_REPONDEUR";
    public static final String CODE_TYPE_MAJ = "Voeu:CODE_TYPE_MAJ";
    public static final String ETIQUETTE = "Voeu:ETIQUETTE";
    
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
        // Code relatif au régime d'hébergement demandé
        // Valeurs possibles : 0 = pas d’hébergement en internat demandé, 1 = hébergement en internat demandé
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_SITUATION_VOEU, "A_SV_COD");
        // Code relatif à la situation du vœu
        // Type attendu : Integer
        
        mappingNomsChamps.put(RANG_VOEU_REPONDEUR, "A_VE_ORD");
        // Rang du vœu (ordre de préférence spécifié par le candidat s’il a activé son répondeur auttomatique)
        // Type attendu : Integer
        
        mappingNomsChamps.put(CODE_TYPE_MAJ, "A_VE_TYP_MAJ");
        // Code identifiant les circonstances particulières ayant entraîné la modification de l’état du vœu
        // Valeurs possibles : 1 = annulation de démission d''un vœu, 10 ou 20 = modification du classement
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
        champsReserves.add(ID_SITUATION_VOEU);
        
        
        // Vérification valeurs champs
        
        validatePresenceOf(          
            mappingNomsChamps.get(ID_CANDIDAT),
            mappingNomsChamps.get(ID_FORMATION_AFFECTATION),
            mappingNomsChamps.get(ID_REGIME_HEBERGEMENT),
            mappingNomsChamps.get(ID_SITUATION_VOEU)
        );

    }
    
    
    protected Candidat candidat;
    
    protected GroupeAffectationFormation groupeAffectationFormation;
    
    protected RegimeHebergement regimeHebergement;
    
    protected SituationVoeu situationVoeu;
    

    
    public Voeu() {
        // constructeur par défaut requis par activejdbc
    } 
    
    
    public Voeu(
            Candidat candidat,
            GroupeAffectationFormation groupeAffectationFormation,
            RegimeHebergement regimeHebergement,
            SituationVoeu situationVoeu,
            Map<String,Object> parametresSupplementaires
            ) {
        // rappel : parametresSupplementaires peut embarquer les valeurs
        // RANG_VOEU_REPONDEUR, CODE_TYPE_MAJ, ETIQUETTE... qui sont optionnelles
        
        this.candidat = candidat;
        this.groupeAffectationFormation = groupeAffectationFormation;
        this.regimeHebergement = regimeHebergement;
        this.situationVoeu = situationVoeu;

        int idCandidat = (int) this.candidat.getValeurChamp(Candidat.ID_CANDIDAT);
        this.set(mappingNomsChamps.get(ID_CANDIDAT), idCandidat);

        int idRegimeHebergement = (int) this.regimeHebergement.getValeurChamp(RegimeHebergement.ID_REGIME_HEBERGEMENT);
        this.set(mappingNomsChamps.get(ID_REGIME_HEBERGEMENT), idRegimeHebergement);

        int idSituationVoeu = (int) this.situationVoeu.getValeurChamp(SituationVoeu.ID_SITUATION_VOEU);
        this.set(mappingNomsChamps.get(ID_SITUATION_VOEU), idSituationVoeu);
        
        Formation formation = this.groupeAffectationFormation.getFormation();
        
        FormationAffectation formationAffectation  = formation.getFormationAffectation();
        
        int idFormationAffectation = (int) formationAffectation.getValeurChamp(FormationAffectation.ID_FORMATION_AFFECTATION);
        this.set(mappingNomsChamps.get(ID_FORMATION_AFFECTATION), idFormationAffectation);

        for (Map.Entry<String,Object> entry : parametresSupplementaires.entrySet()) {
            String nom = entry.getKey();
            checkSiChampReserve(nom, champsReserves);
            this.set(mappingNomsChamps.get(nom), parametresSupplementaires.get(nom));   
        }
                 
    }
    
    
    public Voeu(
            Candidat candidat,
            GroupeAffectationFormation groupeAffectationFormation,
            RegimeHebergement regimeHebergement,
            SituationVoeu situationVoeu
            ) {
               
        this(
            candidat,
            groupeAffectationFormation,
            regimeHebergement,
            situationVoeu,
                new HashMap<>()
            );      
    }
    
    
    public Candidat getCandidat() {
                
        if(this.candidat == null) {
            this.candidat = Candidat.findById(this.getValeurChamp(ID_CANDIDAT));
        }
        
        return this.candidat;  
        
    }
    
    
    public Formation getFormation() {
     
        HashMap<String,Object> criteresRecherche = new HashMap<>();
                
        criteresRecherche.put(Formation.ID_FORMATION_AFFECTATION, this.getValeurChamp(ID_FORMATION_AFFECTATION));
        
        String requeteSql = prepareRequeteRechercheAvecCriteresEgalite(
            Formation.getTableName(),
            Formation.mappingNomsChamps,
            criteresRecherche
        );
            
        List<Formation> formations = Formation.findBySQL(requeteSql);

        return formations.get(0);
                    
    }
        
        

    public FormationInscription getFormationInscription() {
        
        Formation formation = this.getFormation();
        
        return formation.getFormationInscription();
                    
    }
    
    
    
    public FormationAffectation getFormationAffectation() {
        
        Formation formation = this.getFormation();
        
        return formation.getFormationAffectation();
                    
    }
    
    
    
    public SituationVoeu getSituationVoeu() {
        
        if(this.situationVoeu == null) {
            this.situationVoeu = SituationVoeu.findById(this.getValeurChamp(ID_SITUATION_VOEU));
        }
        
        return this.situationVoeu;  

    }
    
    
        
    public GroupeAffectationFormation getGroupeAffectationFormation() {
        
        return this.groupeAffectationFormation; 
 
    }
        


    public RegimeHebergement getRegimeHebergement() {
        
        if(this.regimeHebergement == null) {
            this.regimeHebergement = new RegimeHebergement(((BigDecimal) this.getValeurChamp(ID_REGIME_HEBERGEMENT)).intValue());
        }

        return this.regimeHebergement;

    }
    
    
    
    public Object getRangSurRepondeur() {
                
        return this.getValeurChamp(RANG_VOEU_REPONDEUR);
        
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
        
        return this.get(Voeu.mappingNomsChamps.get(nom));
        
    }
    
}