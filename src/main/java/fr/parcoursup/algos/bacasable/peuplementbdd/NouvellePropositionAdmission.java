package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.annotations.CompositePK;
import org.javalite.activejdbc.annotations.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Table("A_ADM_PROP")
@CompositePK({"G_CN_COD", "G_TA_COD", "I_RH_COD", "NB_JRS"})
public class NouvellePropositionAdmission extends EntitePersistante {
        
    public static final String ID_CANDIDAT = "NouvellePropositionAdmission:ID_CANDIDAT"; 
    public static final String ID_FORMATION_AFFECTATION = "NouvellePropositionAdmission:ID_FORMATION_AFFECTATION"; 
    public static final String ID_FORMATION_INSCRIPTION = "NouvellePropositionAdmission:ID_FORMATION_INSCRIPTION"; 
    public static final String ID_GROUPE_CLASSEMENT_PEDAGOGIQUE = "NouvellePropositionAdmission:ID_GROUPE_CLASSEMENT_PEDAGOGIQUE"; 
    public static final String ID_REGIME_HEBERGEMENT = "NouvellePropositionAdmission:ID_REGIME_HEBERGEMENT"; 
    public static final String ID_INTERNAT = "NouvellePropositionAdmission:ID_INTERNAT"; 
    public static final String NOMBRE_JOURS_DEPUIS_DEBUT_CAMPAGNE = "NouvellePropositionAdmission:NOMBRE_JOURS_DEPUIS_DEBUT_CAMPAGNE"; 
    public static final String ETIQUETTE = "NouvellePropositionAdmission:ETIQUETTE"; 
    
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
        
        mappingNomsChamps.put(ID_FORMATION_INSCRIPTION, "G_TI_COD");
        // Identifiant de la formation d'inscription
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE, "C_GP_COD");
        // Identifiant du groupe de classement
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_REGIME_HEBERGEMENT, "I_RH_COD");
        // Code relatif au régime d'hébergement demandé
        // Valeurs possibles : 0 = pas d’hébergement en internat demandé, 1 = hébergement en internat demandé
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_INTERNAT, "C_GI_COD");
        // Identifiant de l’internat
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
        champsReserves.add(ID_FORMATION_INSCRIPTION);
        champsReserves.add(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE);
        champsReserves.add(ID_REGIME_HEBERGEMENT);
        champsReserves.add(ID_INTERNAT);
        champsReserves.add(NOMBRE_JOURS_DEPUIS_DEBUT_CAMPAGNE);
        
        
        // Vérification valeurs champs
        
        validatePresenceOf(           
            mappingNomsChamps.get(ID_CANDIDAT),
            mappingNomsChamps.get(ID_FORMATION_AFFECTATION),
            mappingNomsChamps.get(ID_REGIME_HEBERGEMENT),
            mappingNomsChamps.get(NOMBRE_JOURS_DEPUIS_DEBUT_CAMPAGNE),
            mappingNomsChamps.get(ID_FORMATION_INSCRIPTION),
            mappingNomsChamps.get(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE)
        );

    }
    
    
    protected Voeu voeu;
    
    
    public NouvellePropositionAdmission() {
        // constructeur par défaut requis par activejdbc
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
    
    
    private Object getValeurChamp(String nom) {
        
        return this.get(NouvellePropositionAdmission.mappingNomsChamps.get(nom));
        
    }
        
}
