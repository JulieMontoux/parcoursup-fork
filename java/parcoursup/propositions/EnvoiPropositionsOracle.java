/* Copyright 2018, 2018 Hugo Gimbert (hugo.gimbert@enseignementsup.gouv.fr)

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
package parcoursup.propositions;

import java.time.LocalDateTime;

import parcoursup.propositions.donnees.ConnecteurDonneesPropositions;
import parcoursup.propositions.donnees.ConnecteurDonneesPropositionsOracle;

public class EnvoiPropositionsOracle {

    public static void main(String[] args) throws Exception {

        if (args.length < 3) {
            log("Usage: envoiPropositions serveur login password");
            return;
        }

        ConnecteurDonneesPropositions acces
                = new ConnecteurDonneesPropositionsOracle(args[0], args[1], args[2]);

        EnvoiPropositions envoiPropositions = new EnvoiPropositions(acces);
        envoiPropositions.execute();
    }

    static void log(String msg) {
        System.out.println(LocalDateTime.now().toLocalTime() + ": " + msg);
    }
}
