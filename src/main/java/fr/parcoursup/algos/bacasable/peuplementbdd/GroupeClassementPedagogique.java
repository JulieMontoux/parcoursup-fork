package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Table("C_GRP")
@IdName("C_GP_COD")
public class GroupeClassementPedagogique extends EntitePersistante {
        
    public static final String ID_GROUPE_CLASSEMENT_PEDAGOGIQUE = "GroupeClassementPedagogique:ID_GROUPE_CLASSEMENT_PEDAGOGIQUE"; 
    public static final String CODE_FORMATION_SANS_CLASSEMENT = "GroupeClassementPedagogique:CODE_FORMATION_SANS_CLASSEMENT"; 
    public static final String ID_JURY_PEDAGOGIQUE = "GroupeClassementPedagogique:ID_JURY_PEDAGOGIQUE"; 
    public static final String CODE_ETAT_CLASSEMENT_DOSSIERS = "GroupeClassementPedagogique:CODE_ETAT_CLASSEMENT_DOSSIERS"; 
    public static final String ETIQUETTE = "GroupeClassementPedagogique:ETIQUETTE"; 
    
    protected static final Map<String,String> mappingNomsChamps = new HashMap<>();
    protected static final List<String> champsReserves = new ArrayList<>();
    
    static {
        
        // Mapping noms champs

        mappingNomsChamps.put(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE, "C_GP_COD");
        // Identifiant du groupe de classement
        // Type attendu : Integer
        
        mappingNomsChamps.put(CODE_FORMATION_SANS_CLASSEMENT, "C_GP_FLG_PAS_CLA");
        // Parmi les valeurs possibles, 1 = pas de classement 
        // Type attendu : Integer
        
        mappingNomsChamps.put(ID_JURY_PEDAGOGIQUE, "C_JA_COD");
        // Identifiant du jury d’admission pédagogique
        // Type attendu : Integer
        
        mappingNomsChamps.put(CODE_ETAT_CLASSEMENT_DOSSIERS, "C_GP_ETA_CLA");
        // Etat du classement des dossiers
        // Parmi les valeurs possibles : 2 = terminé
        // Type attendu : Integer
        
        mappingNomsChamps.put(ETIQUETTE, "ETIQUETTE1");
        // Etiquette n°1
        // Type attendu : String
        
        
        // Champs réservés (ne peuvent être définis directement via le 
        // Map parametresSupplementaires passé en argument
        // du constructeur)
        
        champsReserves.add(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE);
        champsReserves.add(ID_JURY_PEDAGOGIQUE);
        
        
        // Vérification valeurs champs
        
        validatePresenceOf(
            mappingNomsChamps.get(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE)
        );

    }
        
    
    protected GroupeAffectationFormation groupeAffectationFormation;
    
    
    public GroupeClassementPedagogique() {
        // constructeur par défaut requis par activejdbc
    } 
    
    
    public GroupeClassementPedagogique(
        GroupeAffectationFormation groupeAffectationFormation,
        Map<String,Object> parametresSupplementaires
        ) {
        // rappel : parametresSupplementaires peut embarquer les valeurs
        //  CODE_ETAT_CLASSEMENT_DOSSIERS et ETIQUETTE qui sont optionnelles
        
        this.groupeAffectationFormation = groupeAffectationFormation;

        int idGroupeAffectationFormation = (int) this.groupeAffectationFormation.getValeurChamp(GroupeAffectationFormation.ID_GROUPE_CLASSEMENT_PEDAGOGIQUE);
        this.set(mappingNomsChamps.get(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE), idGroupeAffectationFormation);
        
        Formation formation = this.groupeAffectationFormation.getFormation();
        FormationInscription formationInscription = formation.getFormationInscription();
        JuryPedagogique juryPedagogique = formationInscription.getJuryPedagogique();

        int idJuryPedagogique = (int) juryPedagogique.getValeurChamp(JuryPedagogique.ID_JURY_PEDAGOGIQUE);
        this.set(mappingNomsChamps.get(ID_JURY_PEDAGOGIQUE), idJuryPedagogique);
        
        for (Map.Entry<String,Object> entry : parametresSupplementaires.entrySet()) {
            String nom = entry.getKey();
            checkSiChampReserve(nom, champsReserves);
            this.set(mappingNomsChamps.get(nom), parametresSupplementaires.get(nom));   
        }

    }
    
    
    public GroupeClassementPedagogique(
            GroupeAffectationFormation groupeAffectationFormation
            ) {
               
        this(
            groupeAffectationFormation,
                new HashMap<>()
            );
               
    }
    
    
    public GroupeAffectationFormation getGroupeAffectationFormation() {

        return this.groupeAffectationFormation;

    }
    
    
    public List<Candidat> getlisteOrdonneeClassementPedagogique() {
        
        List<Candidat> candidats = new ArrayList<>();
        
        List<PositionCandidatDansGroupeClassementPedagogique> positionsCandidats =
          PositionCandidatDansGroupeClassementPedagogique.where("C_GP_COD = ?",  getValeurChamp(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE)).orderBy("C_CG_RAN asc");
        for(PositionCandidatDansGroupeClassementPedagogique positionsCandidat:positionsCandidats) {
            candidats.add(positionsCandidat.getCandidat());
        }
 
        return candidats;
                
    }
    
    
    
    public List<Candidat> getlisteOrdonneeOrdreAppel() {
        
        List<Candidat> candidats = new ArrayList<>();
        
        List<PositionCandidatDansGroupeClassementPedagogique> positionsCandidats =
          PositionCandidatDansGroupeClassementPedagogique.where("C_GP_COD = ?",  getValeurChamp(ID_GROUPE_CLASSEMENT_PEDAGOGIQUE)).orderBy("C_CG_ORD_APP asc");
        for(PositionCandidatDansGroupeClassementPedagogique positionsCandidat:positionsCandidats) {
            candidats.add(positionsCandidat.getCandidat());
        }
 
        return candidats;
                
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
        
        return this.get(GroupeClassementPedagogique.mappingNomsChamps.get(nom));
        
    }
    
}