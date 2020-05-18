
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

import java.util.logging.Level;
import java.util.logging.Logger;
import parcoursup.exceptions.AccesDonneesException;
import parcoursup.exceptions.VerificationException;
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
            int cGnCod /* code candidat à insérer*/,
            int nouveauRang /* nouveau rang */,
            boolean estBoursier /* est-il boursier */,
            boolean estDuSecteur /* est-il résident du secteur */,
            int cGpCod /* groupe concerné */
    ) throws VerificationException, AccesDonneesException {

        GroupeClassement groupe = acces.recupererDonneesOrdreAppelGroupe(cGpCod);

        LOGGER.log(Level.INFO, "Calcul du nouveau rang dans l''ordre d''appel du candidat{0}dans le groupe {1}", new Object[]{cGnCod, cGpCod});
        
        LOGGER.log(Level.INFO, "Le candidat est {0}", estBoursier ? "boursier" : "non-boursier");
        LOGGER.log(Level.INFO, "Le candidat est {0}", estDuSecteur ? "du secteur" : "hors-secteur");
      

        GroupeClassement nouveauGroupe
                = new GroupeClassement(
                        groupe.cGpCod,
                        groupe.tauxMinBoursiersPourcents,
                        groupe.tauxMinDuSecteurPourcents);

        for (VoeuClasse v : groupe.voeuxClasses) {
            if (v.gCnCod == cGnCod) {
                continue;
            }

            int rang = (v.rang >= nouveauRang) ? (v.rang + 1) : v.rang;

            nouveauGroupe.ajouterVoeu(
                    new VoeuClasse(v.gCnCod, rang, v.estBoursier(), v.estDuSecteur())
            );
        }

        nouveauGroupe.ajouterVoeu(
                new VoeuClasse(cGnCod, nouveauRang, estBoursier, estDuSecteur)
        );

        OrdreAppel ordreAppel = nouveauGroupe.calculerOrdreAppel();

        for (CandidatClasse v : ordreAppel.candidats) {
            if (v.gCnCod == cGnCod) {
                return v.rangAppel;
            }
        }

        throw new VerificationException("Incohérence logique");

    }

    public static int calculeNouveauRangOrdreAppel(
            ConnecteurDonneesAppel acces /* acces données */,
            int gCnCod /* code candidat à insérer*/,
            int cGpCod /* groupe concerné */,
            int nouveauRang /* nouveau rang */
    ) throws AccesDonneesException, VerificationException {

        boolean estBoursier = acces.estBoursier(gCnCod);
        boolean estDuSecteur = acces.estDuSecteur(gCnCod, cGpCod);
      
        return calculeNouveauRangOrdreAppel(
                acces,
                gCnCod,
                nouveauRang,
                estBoursier,
                estDuSecteur,
                cGpCod
        );
    }
 
    private static final Logger LOGGER = Logger.getLogger(ModifClassement.class.getSimpleName());

    private ModifClassement() {
    }
}
