package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.annotations.BelongsTo;
import org.javalite.activejdbc.annotations.BelongsToParents;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Table("G_TRI_INS")
@IdName("G_TI_COD")

@BelongsToParents({ 
@BelongsTo(parent = TypeFormation.class, foreignKeyName = "G_FR_COD_INS"),
@BelongsTo(parent = Filiere.class, foreignKeyName = "G_FL_COD_INS")
}) 
public class FormationInscription extends EntitePersistante {
      
    public static final String ID_FORMATION_INSCRIPTION = "FormationInscription:ID_FORMATION_INSCRIPTION"; 
    public static final String ID_ETABLISSEMENT_INSCRIPTION = "FormationInscription:ID_ETABLISSEMENT_INSCRIPTION"; 
    public static final String ID_TYPE_FORMATION = "FormationInscription:ID_TYPE_FORMATION"; 
    public static final String ID_FILIERE = "FormationInscription:ID_FILIERE"; 
    public static final String CODE_PARAMETRAGE_EFFECTUE = "FormationInscription:CODE_PARAMETRAGE_EFFECTUE"; 
    public static final String CODE_TYPE_INTERNAT_ASSOCIE = "FormationInscription:CODE_TYPE_INTERNAT_ASSOCIE"; 
    public static final String CODE_ETAT_CLASSEMENT_DOSSIERS = "FormationInscription:CODE_ETAT_CLASSEMENT_DOSSIERS"; 
    public static final String ETIQUETTE = "FormationInscription:ETIQUETTE"; 

    protected static final Map<String,String> mappingNomsChamps = new HashMap<>();
    protected static final List<String> champsReserves = new ArrayList<>();
    
    static {
        
        // Mapping noms champs

        mappingNomsChamps.put(ID_FORMATION_INSCRIPTION, "G_TI_COD");
        // Identifiant de la formation d'inscription
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_ETABLISSEMENT_INSCRIPTION, "G_EA_COD_INS");
        // Identifiant de l’établissement d’inscription
        // Type attendu : String
        
        mappingNomsChamps.put(ID_TYPE_FORMATION, "G_FR_COD_INS");
        // Code identifiant le type de formation
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_FILIERE, "G_FL_COD_INS");
        // Code identifiant la filière de formation
        // Type attendu : Integer
        
        mappingNomsChamps.put(CODE_PARAMETRAGE_EFFECTUE, "G_TI_FLG_PAR_EFF");
        // Code indiquant si le paramétrage de la formation a été effectué
        // Parmi les valeurs possibles : 0 = non effectué
        // Type attendu : Integer
        
        mappingNomsChamps.put(CODE_TYPE_INTERNAT_ASSOCIE, "G_TI_CLA_INT_UNI");
        // Code relatif au type d’internat associé (si internat possible)
        // Parmi les valeurs possibles :
        // -1 = pas d'internat,
        // 0 = internat propre à la formation,
        // 1 = internat commun à plusieurs formations proposées par l'établissement,
        // 2 = internat sans sélection, 3 = internat obligatoire
        // Type attendu : Integer
        
        mappingNomsChamps.put(CODE_ETAT_CLASSEMENT_DOSSIERS, "G_TI_ETA_CLA");
        // Etat du classement des dossiers
        // Parmi les valeurs possibles : 1 = commencé, 2 = terminé
        // Type attendu : Integer
        
        mappingNomsChamps.put(ETIQUETTE, "ETIQUETTE1");
        // Etiquette n°1
        // Type attendu : String
        
        
        // Champs réservés (ne peuvent être définis directement via le 
        // Map parametresSupplementaires passé en argument
        // du constructeur)
        
        champsReserves.add(ID_FORMATION_INSCRIPTION);
        champsReserves.add(ID_ETABLISSEMENT_INSCRIPTION);
        champsReserves.add(ID_TYPE_FORMATION);
        champsReserves.add(ID_FILIERE);
        
        
        // Vérification valeurs champs
        
        validatePresenceOf(            
            mappingNomsChamps.get(ID_FORMATION_INSCRIPTION),
            mappingNomsChamps.get(ID_FILIERE),
            mappingNomsChamps.get(ID_TYPE_FORMATION),
            mappingNomsChamps.get(ID_ETABLISSEMENT_INSCRIPTION)
        );

    }
    
    protected Etablissement etablissement;
    
    protected TypeFormation typeFormation;
    
    protected Filiere filiere;
    
    protected JuryPedagogique juryPedagogique;
    
    
    public static List<FormationInscription> findFieldsEqual(
            Map<String,Object> criteresRecherche) {
        
        String requeteSql = prepareRequeteRechercheAvecCriteresEgalite(
            getTableName(),
            FormationInscription.mappingNomsChamps,
            criteresRecherche
            );
        
        return findBySQL(requeteSql);
                
    }
    

    public FormationInscription() {
        // constructeur par défaut requis par activejdbc
    } 
    
    
    public FormationInscription(
        int idFormationInscription,
        Etablissement etablissement,
        TypeFormation typeFormation,
        Filiere filiere,
        Map<String,Object> parametresSupplementaires
        ) {
        // rappel : parametresSupplementaires peut embarquer les valeurs
        //  CODE_TYPE_INTERNAT_ASSOCIE, CODE_ETAT_CLASSEMENT_DOSSIERS, ETIQUETTE... qui sont optionnelles
        
        this.set(mappingNomsChamps.get(ID_FORMATION_INSCRIPTION), idFormationInscription);
        
        this.etablissement = etablissement;
        this.typeFormation = typeFormation;
        this.filiere = filiere;
        
        String idEtablissement = (String) this.etablissement.getValeurChamp(Etablissement.ID_ETABLISSEMENT);        
        this.set(mappingNomsChamps.get(ID_ETABLISSEMENT_INSCRIPTION), idEtablissement);
        
        int idTypeFormation = (int) this.typeFormation.getValeurChamp(TypeFormation.ID_TYPE_FORMATION);
        this.set(mappingNomsChamps.get(ID_TYPE_FORMATION), idTypeFormation);

        int ifFiliere = (int) this.filiere.getValeurChamp(Filiere.ID_FILIERE_FORMATION);
        this.set(mappingNomsChamps.get(ID_FILIERE), ifFiliere);
        
        for (Map.Entry<String,Object> entry : parametresSupplementaires.entrySet()) {
            String nom = entry.getKey();
            checkSiChampReserve(nom, champsReserves);
            this.set(mappingNomsChamps.get(nom), parametresSupplementaires.get(nom));   
        }
        
    }
    
    
    public FormationInscription(
            int idFormationInscription,
            Etablissement etablissement,
            TypeFormation typeFormation,
            Filiere filiere
            ) {
               
        this(
            idFormationInscription,
            etablissement,
            typeFormation,
            filiere,
                new HashMap<>()
            );
               
    }
    
    
    public Etablissement getEtablissement() {

        if(this.etablissement == null) {
            
            this.etablissement = new Etablissement((String) this.getValeurChamp(ID_ETABLISSEMENT_INSCRIPTION));

        }
        
        return this.etablissement;

    }


    public TypeFormation getTypeFormation() {

        if(this.typeFormation == null) {
            this.typeFormation = this.parent(TypeFormation.class);  
        }
        
        return this.typeFormation;

    }


    public Filiere getFiliere() {
        
        if(this.filiere == null) {
            this.filiere = this.parent(Filiere.class);  
        }
        
        return this.filiere;

    }


    public JuryPedagogique getJuryPedagogique() {        
                
        if(this.juryPedagogique == null) {
            JuryPedagogique jury;
            List<JuryPedagogique> jurysPedagogiques = this.getAll(JuryPedagogique.class);
            jury = jurysPedagogiques.get(0);
            this.juryPedagogique = jury;   
        }
        
        return this.juryPedagogique; 
        
    }


    public void setJuryPedagogique(JuryPedagogique juryPedagogique) {

        this.juryPedagogique = juryPedagogique;

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
        
        return this.get(FormationInscription.mappingNomsChamps.get(nom));
        
    }
    
}
