package fr.parcoursup.algos.ordreappel.donnees.testConnecteurDonneesAppelSQL;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.parcoursup.algos.donnees.ConnecteurSQL;
import fr.parcoursup.algos.ordreappel.algo.AlgoOrdreAppelSortie;
import fr.parcoursup.algos.ordreappel.algo.GroupeClassement;
import fr.parcoursup.algos.ordreappel.algo.OrdreAppel;
import fr.parcoursup.algos.ordreappel.algo.VoeuClasse;
import fr.parcoursup.algos.ordreappel.donnees.ConnecteurDonneesAppelSQL;
import org.junit.Test;
import org.powermock.reflect.Whitebox;


public class TestConnecteurDonneesAppelSQLExportation extends TestConnecteurDonneesAppelSQL {

    public TestConnecteurDonneesAppelSQLExportation(String name) throws Exception {

        super(name);

    }



    ///////////////////////////////////////////////////////////////////////////
    //
    // Tests
    //
    ///////////////////////////////////////////////////////////////////////////


    @Test
    public void test_exportation_DonneesOrdresAppel_doit_reussir() throws Exception {

        try (ConnecteurSQL connecteurSQL = this.getConnecteurSQL()) {
            ConnecteurDonneesAppelSQL connecteurDonneesAppel =  new ConnecteurDonneesAppelSQL(connecteurSQL.connection());
            Connection connexionSQL = getConnectionSQL(connecteurDonneesAppel);
            
            // on récupère le premier enregistrement de la table C_CAN_GRP
            // (ce même enregistrement sera mis à jour dans la suite du test)
            
            Statement stmt = connexionSQL.createStatement();
            String sqlReq =
                    "SELECT C_GP_COD, G_CN_COD, C_CG_RAN "
                    + "FROM C_CAN_GRP "
                    + "ORDER BY C_GP_COD asc, G_CN_COD asc";
            ResultSet rs = stmt.executeQuery(sqlReq);
            rs.next();
            
            int cGpCod = rs.getInt(1);
            int gCnCod = rs.getInt(2);
            int rang = rs.getInt(3);
            int rangAppel = 1;
            
            AlgoOrdreAppelSortie donneesSortie = new AlgoOrdreAppelSortie();
            
            GroupeClassement groupeClassement = new GroupeClassement(
                    cGpCod,
                    20,   // parametre tauxMinBoursiersPourcents
                    20    // parametre tauxMinResidentsPourcents
            );
            
            VoeuClasse voeu = new VoeuClasse(
                    gCnCod,
                    rang,
                    true,   // paramètre estBoursier
                    true    // paramètre estDuSecteur
            );
            
            Whitebox.setInternalState(voeu, "rangAppel", rangAppel);
            
            List<VoeuClasse> listeVoeuxClasses = new ArrayList<>();
            listeVoeuxClasses.add(voeu);
            
            OrdreAppel ordreAppel = new OrdreAppel(listeVoeuxClasses);
            
            Map<Integer, OrdreAppel> ordresAppel = new HashMap<>();
            ordresAppel.put(groupeClassement.cGpCod, ordreAppel);
            
            Whitebox.setInternalState(donneesSortie, "ordresAppel", ordresAppel);
            
            connecteurDonneesAppel.exporterDonneesOrdresAppel(donneesSortie);
            
            stmt = connexionSQL.createStatement();
            sqlReq = "SELECT count(*) "
                    + "FROM C_CAN_GRP "
                    + "WHERE "
                    + "C_GP_COD = " + cGpCod + " "
                    + "AND G_CN_COD = " + gCnCod + " "
                    + "AND C_CG_RAN = " + rang + " "
                    + "AND C_CG_ORD_APP = " + rangAppel;
            
            rs = stmt.executeQuery(sqlReq);
            rs.next();
            int count = rs.getInt(1);
            
            assertEquals(count,  1);
        }

    }


}
