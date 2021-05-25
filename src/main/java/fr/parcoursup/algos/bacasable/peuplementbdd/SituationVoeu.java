package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Table("A_SIT_VOE")
@IdName("A_SV_COD")
public class SituationVoeu extends EntitePersistante {
        
    public static final String ID_SITUATION_VOEU = "SituationVoeu:ID_SITUATION_VOEU"; 
    public static final String CODE_VOEU_AFFECTE = "SituationVoeu:CODE_VOEU_AFFECTE"; 
    public static final String CODE_VOEU_EN_ATTENTE_DE_PROPOSITION = "SituationVoeu:CODE_VOEU_EN_ATTENTE_DE_PROPOSITION"; 
    public static final String CODE_VOEU_CLOTURE = "SituationVoeu:CODE_VOEU_CLOTURE"; 
    public static final String ETIQUETTE = "SituationVoeu:ETIQUETTE"; 
    
    protected static final Map<String,String> mappingNomsChamps = new HashMap<>();
    protected static final List<String> champsReserves = new ArrayList<>();
    
    static {
        
        // Mapping noms champs

        mappingNomsChamps.put(ID_SITUATION_VOEU, "A_SV_COD");
        // Code relatif à la situation du vœu
        // Type attendu : Integer
        
        mappingNomsChamps.put(CODE_VOEU_AFFECTE, "A_SV_FLG_AFF");
        // Code indiquant si le vœu est affecté (proposition soumise au candidat et acceptée par lui)
        // Valeurs possibles : 0 = non, 1 = oui
        // Type attendu : Integer
        
        mappingNomsChamps.put(CODE_VOEU_EN_ATTENTE_DE_PROPOSITION, "A_SV_FLG_ATT");
        // Code indiquant si le vœu est en attente de proposition (sur liste d'attente ou pas encore de résultat)
        // Valeurs possibles : 0 = non, 1 = oui
        // Type attendu : Integer
        
        mappingNomsChamps.put(CODE_VOEU_CLOTURE, "A_SV_FLG_CLO");
        // Code indiquant si le vœu est clôturé
        // Valeurs possibles : 0 = non, 1 = oui
        // Type attendu : Integer
        
        mappingNomsChamps.put(ETIQUETTE, "ETIQUETTE1");
        // Etiquette n°1
        // Type attendu : String
        
        
        // Champs réservés (ne peuvent être définis directement via le 
        // Map parametresSupplementaires passé en argument
        // du constructeur)
        
        champsReserves.add(ID_SITUATION_VOEU);
        champsReserves.add(CODE_VOEU_AFFECTE);
        champsReserves.add(CODE_VOEU_EN_ATTENTE_DE_PROPOSITION);
        champsReserves.add(CODE_VOEU_CLOTURE);
                
        
        // Vérification valeurs champs
        
        validatePresenceOf(
            mappingNomsChamps.get(ID_SITUATION_VOEU)
        );

    }
    
    
    public SituationVoeu() {
        // constructeur par défaut requis par activejdbc
    } 
    
    
    public SituationVoeu(
            int idSituationVoeu,
            int codeVoeuEnAttente,
            int codeVoeuAffecte,
            int codeVoeuCloture,
            Map<String,Object> parametresSupplementaires
            ) {
        // rappel : parametresSupplementaires peut embarquer la valeur
        // ETIQUETTE qui est optionnelle

        this.set(mappingNomsChamps.get(ID_SITUATION_VOEU), idSituationVoeu);
        this.set(mappingNomsChamps.get(CODE_VOEU_EN_ATTENTE_DE_PROPOSITION), codeVoeuEnAttente);
        this.set(mappingNomsChamps.get(CODE_VOEU_AFFECTE), codeVoeuAffecte);
        this.set(mappingNomsChamps.get(CODE_VOEU_CLOTURE), codeVoeuCloture);
        
        for (Map.Entry<String,Object> entry : parametresSupplementaires.entrySet()) {
            String nom = entry.getKey();
            checkSiChampReserve(nom, champsReserves);
            this.set(mappingNomsChamps.get(nom), parametresSupplementaires.get(nom));   
        }
        
    }
    
    
    public SituationVoeu(
            int idSituationVoeu,
            int codeVoeuEnAttente,
            int codeVoeuAffecte,
            int codeVoeuCloture
            ) {
               
        this(
            idSituationVoeu,
            codeVoeuEnAttente,
            codeVoeuAffecte,
            codeVoeuCloture,
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
        
        return this.get(SituationVoeu.mappingNomsChamps.get(nom));
        
    }
    
}
