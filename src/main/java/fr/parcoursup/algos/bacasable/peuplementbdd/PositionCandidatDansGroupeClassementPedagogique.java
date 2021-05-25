package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.annotations.BelongsTo;
import org.javalite.activejdbc.annotations.BelongsToParents;
import org.javalite.activejdbc.annotations.CompositePK;
import org.javalite.activejdbc.annotations.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Table("C_CAN_GRP")
@CompositePK({"G_CN_COD", "C_GP_COD"})
@BelongsToParents({ 
@BelongsTo(parent = Candidat.class, foreignKeyName = "G_CN_COD"),
@BelongsTo(parent = GroupeClassementPedagogique.class, foreignKeyName = "C_GP_COD")
}) 
public class PositionCandidatDansGroupeClassementPedagogique extends EntitePersistante {
        
    public static final String ID_CANDIDAT = "PositionCandidatDansGroupeClassementPedagogique:ID_CANDIDAT"; 
    public static final String ID_GROUPE_CLASSEMENT_PEDAGOGIQUE = "PositionCandidatDansGroupeClassementPedagogique:ID_GROUPE_CLASSEMENT_PEDAGOGIQUE"; 
    public static final String CODE_ETAT_AVANCEMENT_DOSSIER = "PositionCandidatDansGroupeClassementPedagogique:CODE_ETAT_AVANCEMENT_DOSSIER"; 
    public static final String RANG_INITIAL_CANDIDAT = "PositionCandidatDansGroupeClassementPedagogique:RANG_INITIAL_CANDIDAT"; 
    public static final String RANG_CANDIDAT_APRES_CALCUL_ORDRE_APPEL = "PositionCandidatDansGroupeClassementPedagogique:RANG_CANDIDAT_APRES_CALCUL_ORDRE_APPEL"; 
    public static final String RANG_CANDIDAT_AFFICHE = "PositionCandidatDansGroupeClassementPedagogique:RANG_CANDIDAT_AFFICHE"; 
    public static final String ETIQUETTE = "PositionCandidatDansGroupeClassementPedagogique:ETIQUETTE"; 
    
    protected static final Map<String,String> mappingNomsChamps = new HashMap<>();
    protected static final List<String> champsReserves = new ArrayList<>();
    
    static {
        
        // Mapping noms champs

        mappingNomsChamps.put(ID_CANDIDAT, "G_CN_COD");
        // Identifiant du candidat
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE, "C_GP_COD");
        // Identifiant du groupe de classement
        // Type attendu : Integer
        
        mappingNomsChamps.put(CODE_ETAT_AVANCEMENT_DOSSIER, "I_IP_COD");
        // Code relatif à l’état d'avancement du dossier
        // Parmi les valeurs possibles : 5 = dossier reçu et complet, candidat classé
        // Type attendu : Integer
        
        mappingNomsChamps.put(RANG_INITIAL_CANDIDAT, "C_CG_RAN");
        // Rang initial du candidat dans le groupe de classement pédagogique
        // Type attendu : Integer
        
        mappingNomsChamps.put(RANG_CANDIDAT_APRES_CALCUL_ORDRE_APPEL, "C_CG_ORD_APP");
        // Rang du candidat après calcul de l’ordre d’appel (prise en compte des taux minimum de boursiers et résidents)
        // Type attendu : Integer
        
        mappingNomsChamps.put(RANG_CANDIDAT_AFFICHE, "C_CG_ORD_APP_AFF");
        // Rang du candidat affiché sur le site Parcoursup
        // Type attendu : Integer
        
        mappingNomsChamps.put(ETIQUETTE, "ETIQUETTE1");
        // Etiquette n°1
        // Type attendu : String
        
        
        // Champs réservés (ne peuvent être définis directement via le 
        // Map parametresSupplementaires passé en argument
        // du constructeur)
        
        champsReserves.add(ID_CANDIDAT);
        champsReserves.add(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE);
        
        
        // Vérification valeurs champs
        
        validatePresenceOf(            
            mappingNomsChamps.get(ID_CANDIDAT),
            mappingNomsChamps.get(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE)
        );

    }
    
    

    protected GroupeClassementPedagogique groupeClassementPedagogique;
    
    protected Candidat candidat;

    
    public PositionCandidatDansGroupeClassementPedagogique() {
        // constructeur par défaut requis par activejdbc
    } 
    
    
    
    public PositionCandidatDansGroupeClassementPedagogique(
        GroupeClassementPedagogique groupeClassementPedagogique,
        Candidat candidat,
        Map<String,Object> parametresSupplementaires
        ) {
        // rappel : parametresSupplementaires peut embarquer les valeurs
        // CODE_ETAT_AVANCEMENT_DOSSIER, RANG_INITIAL_CANDIDAT, RANG_CANDIDAT_APRES_CALCUL_ORDRE_APPEL, ETIQUETTE... qui sont optionnelles
        
        this.groupeClassementPedagogique = groupeClassementPedagogique;
        this.candidat = candidat;

        int idGroupeClassementPedagogique = (int) this.groupeClassementPedagogique.getValeurChamp(GroupeClassementPedagogique.ID_GROUPE_CLASSEMENT_PEDAGOGIQUE);
        this.set(mappingNomsChamps.get(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE), idGroupeClassementPedagogique);
        
        int idCandidat = (int) this.candidat.getValeurChamp(Candidat.ID_CANDIDAT);
        this.set(mappingNomsChamps.get(ID_CANDIDAT), idCandidat);
        
        for (Map.Entry<String,Object> entry : parametresSupplementaires.entrySet()) {
            String nom = entry.getKey();
            checkSiChampReserve(nom, champsReserves);
            this.set(mappingNomsChamps.get(nom), parametresSupplementaires.get(nom));   
        } 
        
    }
    
    
    public PositionCandidatDansGroupeClassementPedagogique(
            GroupeClassementPedagogique groupeClassementPedagogique,
            Candidat candidat
            ) {
               
        this(
            groupeClassementPedagogique,
            candidat,
                new HashMap<>()
            );      
    }
    
    
    public GroupeClassementPedagogique getGroupeClassementPedagogique() {
        
        if(this.groupeClassementPedagogique == null) {
            this.groupeClassementPedagogique = this.parent(GroupeClassementPedagogique.class);  
        }
        
        return this.groupeClassementPedagogique; 

    }


    public Candidat getCandidat() {
        
        if(this.candidat == null) {
            this.candidat = this.parent(Candidat.class);  
        }
        
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
        
        return this.get(PositionCandidatDansGroupeClassementPedagogique.mappingNomsChamps.get(nom));
        
    } 
 
}