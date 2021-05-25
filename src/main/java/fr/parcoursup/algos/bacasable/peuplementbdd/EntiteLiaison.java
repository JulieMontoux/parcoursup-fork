package fr.parcoursup.algos.bacasable.peuplementbdd;

import java.util.HashMap;
import java.util.Map;

public abstract class EntiteLiaison {
    
    protected final Map<String,Object> champsValeurs = new HashMap<>();
    

    public void setValeurChamp(String nomChamp, Object valeur) {
        
        champsValeurs.put(nomChamp, valeur);

    }
     

    public Object getValeurChamp(String nomChamp) {
        
        return champsValeurs.get(nomChamp);

    }
    
}
