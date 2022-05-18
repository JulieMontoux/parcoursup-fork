package fr.parcoursup.algos.bacasable.peuplementbdd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ScenarioTestDbUnit {
        
        
    protected static final ArrayList<String> tablesATraiter = new ArrayList<>();
    
    static {
        
        tablesATraiter.add("A_ADM");
        tablesATraiter.add("A_ADM_DEM");
        tablesATraiter.add("A_ADM_PRED_DER_APP");
        tablesATraiter.add("A_ADM_PROP");
        tablesATraiter.add("A_REC");
        tablesATraiter.add("A_REC_GRP");
        tablesATraiter.add("A_REC_GRP_INT");
        tablesATraiter.add("A_REC_GRP_INT_PROP");
        tablesATraiter.add("A_SIT_VOE");
        tablesATraiter.add("A_VOE");
        tablesATraiter.add("A_VOE_PROP");
        tablesATraiter.add("C_CAN_GRP");
        tablesATraiter.add("C_CAN_GRP_INT");
        tablesATraiter.add("C_GRP");
        tablesATraiter.add("C_JUR_ADM");
        tablesATraiter.add("G_CAN");
        tablesATraiter.add("G_FIL");
        tablesATraiter.add("G_FOR");
        tablesATraiter.add("G_PAR");
        tablesATraiter.add("G_TRI_AFF");
        tablesATraiter.add("G_TRI_INS");
        tablesATraiter.add("I_INS");
        
    }
    
        
    protected StringBuilder flatXmlBuilder;
    
  
    protected final Map<String,Integer> decompteTablesEntites = new HashMap<>();
    
    
    public ScenarioTestDbUnit() {
        
        for(String nomTable : ScenarioTestDbUnit.tablesATraiter) {
            this.decompteTablesEntites.put(nomTable, 0);
        }
                        
        this.flatXmlBuilder = new StringBuilder();

        this.flatXmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        this.flatXmlBuilder.append("<dataset>");
        
        
    }
    
    
    public void ajouteEntite(EntitePersistante entite) {
        
        String nomTable = entite.getNomTable();
        
        this.decompteTablesEntites.put(nomTable, this.decompteTablesEntites.get(nomTable) + 1);
                
        this.flatXmlBuilder.append(this.getRepresentationEntiteFlatXml(entite));
        
    }
    
    
    public String getRepresentationEntiteFlatXml(EntitePersistante entite) {
        
        String nomTable = entite.getNomTable();
        
        Map<String,Object> attributsEntite = entite.getAttributes();
        
        StringBuilder bld = new StringBuilder();
        
        bld.append("<").append(nomTable).append("  ");
        
        for (Map.Entry<String,Object> entry : attributsEntite.entrySet()) {
      
            String nomChampTable = entry.getKey();
      
            Object valeur = attributsEntite.get(nomChampTable);
            bld.append(nomChampTable).append("=\"").append(valeur).append("\" ");
        }
        bld.append("/>");
        
        return bld.toString();
                  
      }
    
    
    public String getFlatXml() {
                    
        for (Map.Entry<String,Integer> entry : this.decompteTablesEntites.entrySet()) {
            
            String nomTable = entry.getKey();
            
            if(this.decompteTablesEntites.get(nomTable) == 0) {
                this.flatXmlBuilder.append("<").append(nomTable).append(" />");
            }
            
        }
        
        this.flatXmlBuilder.append("</dataset>");
        
        return this.flatXmlBuilder.toString();
                
    }
    
}
