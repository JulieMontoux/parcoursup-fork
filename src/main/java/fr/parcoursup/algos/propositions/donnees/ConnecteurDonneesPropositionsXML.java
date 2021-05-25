/* Copyright 2019 © Ministère de l'Enseignement Supérieur, de la Recherche et de
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
package fr.parcoursup.algos.propositions.donnees;

import fr.parcoursup.algos.donnees.Serialisation;
import fr.parcoursup.algos.exceptions.AccesDonneesException;
import fr.parcoursup.algos.exceptions.AccesDonneesExceptionMessage;
import fr.parcoursup.algos.propositions.algo.AlgoPropositionsEntree;
import fr.parcoursup.algos.propositions.algo.AlgoPropositionsSortie;

import javax.xml.bind.JAXBException;

public class ConnecteurDonneesPropositionsXML implements ConnecteurDonneesPropositions {

    /* la source de données */
    private final String filename;

    public ConnecteurDonneesPropositionsXML(String filename) {
        this.filename = filename;
    }

    @Override
    public AlgoPropositionsEntree recupererDonnees() throws AccesDonneesException {
        try {
            return AlgoPropositionsEntree.deserialiser(filename);
        } catch (JAXBException | IllegalArgumentException ex) {
            throw new AccesDonneesException(AccesDonneesExceptionMessage.CONNECTEUR_DONNEES_PROPOSITIONS_XML_DESERIALISATION, ex);
        }
    }

    @Override
    public void exporterDonnees(AlgoPropositionsSortie sortie) throws AccesDonneesException {
        new Serialisation<AlgoPropositionsSortie>().serialiserEtCompresser(
                filename + ".out.xml", 
                sortie,
                AlgoPropositionsSortie.class);
    }

}
