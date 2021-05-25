package fr.parcoursup.algos.bacasable.peuplementbdd;

import java.io.Serializable;

public class GroupeClassementInternat extends EntiteLiaison implements Serializable {

    public static final String ID_INTERNAT = "cGiCod"; 
    
    private static final long serialVersionUID = 2220731475116675730L;
    
    protected final GroupeAffectationInternat groupeAffectationInternat;
    
    
    public GroupeClassementInternat(
            GroupeAffectationInternat groupeAffectationInternat
            ) {
        
        this.groupeAffectationInternat = groupeAffectationInternat;

        int idInternat = (int) this.groupeAffectationInternat.getValeurChamp(GroupeAffectationInternat.ID_INTERNAT);
        this.setValeurChamp(ID_INTERNAT, idInternat);
        
    }
    
    
    public GroupeAffectationInternat getGroupeAffectationInternat() {

        return this.groupeAffectationInternat;

    }
    
}
