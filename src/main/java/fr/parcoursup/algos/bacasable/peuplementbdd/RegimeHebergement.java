package fr.parcoursup.algos.bacasable.peuplementbdd;

import java.io.Serializable;

public class RegimeHebergement extends EntiteLiaison implements Serializable {
    
    public static final String ID_REGIME_HEBERGEMENT = "iRhCod"; 

    private static final long serialVersionUID = -6694220831431387781L;

    public RegimeHebergement(
        int iRhCod
        ) {
                
        this.setValeurChamp(ID_REGIME_HEBERGEMENT, iRhCod);
        
    }
    
    public String toString() {
        if ((int) this.getValeurChamp(ID_REGIME_HEBERGEMENT) == 0) {
            return "Herbergement sans internat";
        }
        else {
            return "Herbergement avec internat";
        }
    }
}