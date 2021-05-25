package fr.parcoursup.algos.bacasable.peuplementbdd;

import java.io.Serializable;


public class Etablissement extends EntiteLiaison implements Serializable {
    
    public static final String ID_ETABLISSEMENT = "gEaCod"; 
        
    private static final long serialVersionUID = -926332261007098464L;
    
    public Etablissement(
        String idEtablissement
        ) {
        
        this.setValeurChamp(ID_ETABLISSEMENT, idEtablissement);
        
    }
    
}