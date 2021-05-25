package fr.parcoursup.algos.bacasable.peuplementbdd;

import org.javalite.activejdbc.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static fr.parcoursup.algos.donnees.SQLStringsConstants.*;


public abstract class EntitePersistante extends Model {
              
    protected static String prepareRequeteRechercheAvecCriteresEgalite(
            String nomTable,
            Map<String,String> mappingNomsChamps,
            Map<String,Object> criteresRecherche) {
        
        List<String> conditions = new ArrayList<>();       
        
        for (Map.Entry<String,Object> entry : criteresRecherche.entrySet()) {

            String nom = entry.getKey();

            Object valeur = criteresRecherche.get(nom);
          
            String condition;
           
             if(valeur instanceof String) {
                 condition = mappingNomsChamps.get(nom) + "='" + criteresRecherche.get(nom) + "'";
             }
           
             else {
                 condition = mappingNomsChamps.get(nom) + "=" + criteresRecherche.get(nom);
             }
           
             conditions.add(condition);
            
        }

       return SELECT + "*" + FROM + nomTable + WHERE + "" + String.join(AND, conditions);
        
    }
    
    
    abstract protected String getNomTable();
    

    abstract protected Object getValeurChamp(String nom);  
    
    
    protected static void checkSiChampReserve(
        String nom,
        List<String> champsReserves) {
        
        if(champsReserves.contains(nom)) {
            throw new IllegalArgumentException(
                "Champ réservé, à ne pas spécifier dans les caractéristiques supplémentaires de l'entité : " + nom
            );
        }

    }
        

    @Override
    public Map<String,Object> getAttributes() {
        return super.getAttributes();
    }
    
}
