package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.annotations.CompositePK;
import org.javalite.activejdbc.annotations.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Table("C_CAN_GRP_INT")
@CompositePK({"G_CN_COD", "C_GI_COD"})
public class PositionCandidatDansGroupeClassementInternat extends EntitePersistante {
        
    public static final String ID_INTERNAT = "PositionCandidatDansGroupeClassementInternat:ID_INTERNAT"; 
    public static final String ID_CANDIDAT = "PositionCandidatDansGroupeClassementInternat:ID_CANDIDAT"; 
    public static final String CODE_ETAT_AVANCEMENT_DOSSIER = "PositionCandidatDansGroupeClassementInternat:CODE_ETAT_AVANCEMENT_DOSSIER"; 
    public static final String RANG_CANDIDAT = "PositionCandidatDansGroupeClassementInternat:RANG_CANDIDAT"; 
    public static final String ETIQUETTE = "PositionCandidatDansGroupeClassementInternat:ETIQUETTE"; 
    
    protected static final Map<String,String> mappingNomsChamps = new HashMap<>();
    protected static final List<String> champsReserves = new ArrayList<>();
    
    static {
        
        // Mapping noms champs

        mappingNomsChamps.put(ID_INTERNAT, "C_GI_COD");
        // Identifiant de l’internat
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_CANDIDAT, "G_CN_COD");
        // Identifiant du candidat
        // Type attendu : Integer
        
        mappingNomsChamps.put(CODE_ETAT_AVANCEMENT_DOSSIER, "I_IP_COD");
        // Code relatif à l’état d'avancement du dossier
        // Parmi les valeurs possibles : 5 = dossier reçu et complet, candidat classé
        // Type attendu : Integer
        
        mappingNomsChamps.put(RANG_CANDIDAT, "C_CI_RAN");
        // Rang du candidat dans le groupe de classement
        // Type attendu : Integer
        
        mappingNomsChamps.put(ETIQUETTE, "ETIQUETTE1");
        // Etiquette n°1
        // Type attendu : String
        
        
        // Champs réservés (ne peuvent être définis directement via le 
        // Map parametresSupplementaires passé en argument
        // du constructeur)
        
        champsReserves.add(ID_INTERNAT);
        champsReserves.add(ID_CANDIDAT);
        
        
        // Vérification valeurs champs
        
        validatePresenceOf(          
            mappingNomsChamps.get(ID_INTERNAT),
            mappingNomsChamps.get(ID_CANDIDAT)
        );

    }
    
    
    protected GroupeClassementInternat groupeClassementInternat;
    
    protected Candidat candidat;
    
    
    public PositionCandidatDansGroupeClassementInternat() {
        // constructeur par défaut requis par activejdbc
    } 
    
    
    public PositionCandidatDansGroupeClassementInternat(
        GroupeClassementInternat groupeClassementInternat,
        Candidat candidat,
        Map<String,Object> parametresSupplementaires
        ) {
        // rappel : parametresSupplementaires peut embarquer les valeurs
        //  CODE_ETAT_AVANCEMENT_DOSSIER, RANG_CANDIDAT et ETIQUETTE qui sont optionnelles
        
        this.groupeClassementInternat = groupeClassementInternat;
        this.candidat = candidat;

        int idGroupeClassementInternat = (int) this.groupeClassementInternat.getValeurChamp(GroupeClassementInternat.ID_INTERNAT);
        this.set(mappingNomsChamps.get(ID_INTERNAT), idGroupeClassementInternat);

        int idCandidat = (int) this.candidat.getValeurChamp(Candidat.ID_CANDIDAT);
        this.set(mappingNomsChamps.get(ID_CANDIDAT), idCandidat);
        
        for (Map.Entry<String,Object> entry : parametresSupplementaires.entrySet()) {
            String nom = entry.getKey();
            checkSiChampReserve(nom, champsReserves);
            this.set(mappingNomsChamps.get(nom), parametresSupplementaires.get(nom));   
        }
        
    }
    
    
    public PositionCandidatDansGroupeClassementInternat(
            GroupeClassementInternat groupeClassementInternat,
            Candidat candidat
            ) {
               
        this(
            groupeClassementInternat,
            candidat,
                new HashMap<>()
            );      
    }
    
    
    public GroupeClassementInternat groupeClassementInternat() {

        return this.groupeClassementInternat;

    }


    public Candidat getCandidat() {

        return this.candidat;

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
        
        return this.get(PositionCandidatDansGroupeClassementInternat.mappingNomsChamps.get(nom));
        
    }
    
}
