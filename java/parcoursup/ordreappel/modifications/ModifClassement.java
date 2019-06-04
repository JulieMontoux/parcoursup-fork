
/* Copyright 2018 © Ministère de l'Enseignement Supérieur, de la Recherche et de
l'Innovation,
    Hugo Gimbert (hugo.gimbert@enseignementsup.gouv.fr) 

    This file is part of Algorithmes-de-parcoursup.

    Algorithmes-de-parcoursup is free software: you can redistribute it and/or modify
    it under the terms of the Affero GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Algorithmes-de-parcoursup is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    Affero GNU General Public License for more details.

    You should have received a copy of the Affero GNU General Public License
    along with Algorithmes-de-parcoursup.  If not, see <http://www.gnu.org/licenses/>.

 */
package parcoursup.ordreappel.modifications;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import parcoursup.ordreappel.algo.CandidatClasse;
import parcoursup.ordreappel.algo.GroupeClassement;
import parcoursup.ordreappel.algo.OrdreAppel;
import parcoursup.ordreappel.algo.VoeuClasse;
import parcoursup.ordreappel.donnees.ConnecteurDonneesAppel;

/* Permet, après le début de la procédure,
de mettre à jour l'ordre d'appel suite à un ajout ou une modification de classement
d'un candidat suite à un traitement tardif du dossier
(typiquement erreurs de classement / dossier égaré / etc...)
*/
public class ModifClassement {

    /* renvoie le nouveau rang dans l'ordre d'appel */
    public static int calculeNouveauRangOrdreAppel(
            ConnecteurDonneesAppel acces, /* acces données */
            int G_CN_COD /* code candidat à insérer*/,
            int nouveauRang /* nouveau rang */,
            boolean estBoursier /* est-il boursier */,
            boolean estDuSecteur /* est-il résident du secteur */,
            int C_GP_COD /* groupe concerné */
    ) throws SQLException, Exception {

        GroupeClassement groupe = acces.recupererDonneesOrdreAppelGroupe(C_GP_COD);

        LOGGER.log(Level.INFO, "Calcul du nouveau rang dans l''ordre d''appel du candidat{0}dans le groupe {1}", new Object[]{G_CN_COD, C_GP_COD});
        
        LOGGER.log(Level.INFO, "Le candidat est {0}", estBoursier ? "boursier" : "non-boursier");
        LOGGER.log(Level.INFO, "Le candidat est {0}", estDuSecteur ? "du secteur" : "hors-secteur");
      

        GroupeClassement nouveauGroupe
                = new GroupeClassement(
                        groupe.C_GP_COD,
                        groupe.tauxMinBoursiersPourcents,
                        groupe.tauxMinDuSecteurPourcents);

        for (VoeuClasse v : groupe.voeuxClasses) {
            if (v.G_CN_COD == G_CN_COD) {
                continue;
            }

            int rang = (v.rang >= nouveauRang) ? (v.rang + 1) : v.rang;

            nouveauGroupe.ajouterVoeu(
                    new VoeuClasse(v.G_CN_COD, rang, v.estBoursier(), v.estDuSecteur())
            );
        }

        nouveauGroupe.ajouterVoeu(
                new VoeuClasse(G_CN_COD, nouveauRang, estBoursier, estDuSecteur)
        );

        OrdreAppel ordreAppel = nouveauGroupe.calculerOrdreAppel();

        for (CandidatClasse v : ordreAppel.candidats) {
            if (v.G_CN_COD == G_CN_COD) {
                return v.rangAppel;
            }
        }

        throw new RuntimeException("Inconsistence logique");

    }

    public static int calculeNouveauRangOrdreAppel(
            ConnecteurDonneesAppel acces /* acces données */,
            int G_CN_COD /* code candidat à insérer*/,
            int C_GP_COD /* groupe concerné */,
            int nouveauRang /* nouveau rang */
    ) throws SQLException, Exception {

        boolean estBoursier = acces.estBoursier(G_CN_COD);
        boolean estDuSecteur = acces.estDuSecteur(G_CN_COD, C_GP_COD);
      
        return calculeNouveauRangOrdreAppel(
                acces,
                G_CN_COD,
                nouveauRang,
                estBoursier,
                estDuSecteur,
                C_GP_COD
        );
    }
 
    private static final Logger LOGGER = Logger.getLogger(ModifClassement.class.getSimpleName());

    private ModifClassement() {
    }
}
