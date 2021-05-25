package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Table("G_CAN")
@IdName("G_CN_COD")
public class Candidat extends EntitePersistante {
     
    public static final String ID_CANDIDAT = "Candidat:ID_CANDIDAT"; 
    public static final String CODE_AVANCEMENT_SAISIE_DOSSIER = "Candidat:CODE_AVANCEMENT_SAISIE_DOSSIER"; 
    public static final String CODE_BOURSIER = "Candidat:CODE_BOURSIER"; 
    public static final String CODE_VALIDATION_STATUT_BOURSIER = "Candidat:CODE_VALIDATION_STATUT_BOURSIER"; 
    public static final String CODE_SERIE_ENSEIGNEMENT = "Candidat:CODE_SERIE_ENSEIGNEMENT"; 
    public static final String CODE_ACTIVATION_REPONDEUR_AUTOMATIQUE = "Candidat:CODE_ACTIVATION_REPONDEUR_AUTOMATIQUE"; 
    public static final String ETIQUETTE = "Candidat:ETIQUETTE";
    
    protected static final Map<String,String> mappingNomsChamps = new HashMap<>();
    protected static final List<String> champsReserves = new ArrayList<>();
    
    
    static {
        
        // Mapping noms champs

        mappingNomsChamps.put(ID_CANDIDAT, "G_CN_COD");
        // Identifiant du candidat
        // Type attendu : Integer
        
        mappingNomsChamps.put(CODE_AVANCEMENT_SAISIE_DOSSIER, "G_IC_COD");
        // Code relatif à l’état d'avancement de la saisie du dossier d'inscription du candidat sur Parcoursup
        // Parmi les valeurs possibles : -10 = dossier annulé, 100 = candidat inscrit
        // Type attendu : Integer

        mappingNomsChamps.put(CODE_BOURSIER, "G_CN_BRS");
        // Code indiquant si le candidat est boursier
        // Parmi les valeurs possibles : 0 = non boursier, 1 = boursier du secondaire, 2 = boursier de l'enseignement supérieur
        // Type attendu : Integer
        
        mappingNomsChamps.put(CODE_VALIDATION_STATUT_BOURSIER, "G_CN_FLG_BRS_CER");
        // Code identifiant la source de saisie / validation du statut de boursier
        // Parmi les valeurs possibles : 0 = non certifié, 1 = certifié SIECLE, 2 = certifié par le Chef d'Etablissement
        // Type attendu : Integer

        mappingNomsChamps.put(CODE_SERIE_ENSEIGNEMENT, "I_CL_COD_BAC");
        // Identifiant de la série de l’enseignement secondaire ou supérieur à laquelle est rattaché le candidat  
        // Type attendu : String
        
        mappingNomsChamps.put(CODE_ACTIVATION_REPONDEUR_AUTOMATIQUE, "G_CN_FLG_RA");
        // Code indiquant si le candidat a activé son répondeur automatique
        // Parmi les valeurs possibles : 1 = oui
        // Type attendu : Integer
        
        mappingNomsChamps.put(ETIQUETTE, "ETIQUETTE1");
        // Etiquette n°1
        // Type attendu : String
        
        
        // Champs réservés (ne peuvent être définis directement via le 
        // Map parametresSupplementaires passé en argument
        // du constructeur)
        
        champsReserves.add(ID_CANDIDAT);
        champsReserves.add(CODE_AVANCEMENT_SAISIE_DOSSIER);
        
        
        // Vérification valeurs champs

        validatePresenceOf(
            mappingNomsChamps.get(ID_CANDIDAT),
            mappingNomsChamps.get(CODE_AVANCEMENT_SAISIE_DOSSIER)
        );

    }
    
    
    public static List<Candidat> findFieldsEqual(Map<String,Object> criteresRecherche) {
        
        String requeteSql = prepareRequeteRechercheAvecCriteresEgalite(
            getTableName(),
            Candidat.mappingNomsChamps,
            criteresRecherche);
        
        return findBySQL(requeteSql);
                
    }
   
    
    public Candidat() {
        // constructeur par défaut requis par activejdbc
    } 


    public Candidat(
            int idCandidat,
            int codeAvancementSaisieDossier,
            Map<String,Object> parametresSupplementaires
            ) {
        // rappel : parametresSupplementaires peut embarquer les valeurs
        // CODE_BOURSIER, CODE_VALIDATION_STATUT_BOURSIER, CODE_SERIE_ENSEIGNEMENT, ETIQUETTE... qui sont optionnelles
        
        this.set(mappingNomsChamps.get(ID_CANDIDAT), idCandidat);
        this.set(mappingNomsChamps.get(CODE_AVANCEMENT_SAISIE_DOSSIER), codeAvancementSaisieDossier);
        
        for (Map.Entry<String,Object> entry : parametresSupplementaires.entrySet()) {
            String nom = entry.getKey();
            checkSiChampReserve(nom, champsReserves);
            this.set(mappingNomsChamps.get(nom), parametresSupplementaires.get(nom));   
        }
                
    }
    
    
    public Candidat(
            int idCandidat,
            int codeAvancementSaisieDossier
            ) {
               
        this(
            idCandidat,
            codeAvancementSaisieDossier,
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
        
        return this.get(Candidat.mappingNomsChamps.get(nom));
        
    }
    
    
}

