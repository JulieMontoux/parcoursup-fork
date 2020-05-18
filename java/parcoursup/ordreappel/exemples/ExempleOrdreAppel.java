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
package parcoursup.ordreappel.exemples;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import parcoursup.exceptions.VerificationException;
import parcoursup.ordreappel.algo.AlgoOrdreAppel;
import parcoursup.ordreappel.algo.AlgoOrdreAppelEntree;
import parcoursup.ordreappel.algo.AlgoOrdreAppelSortie;
import parcoursup.ordreappel.algo.GroupeClassement;

public abstract class ExempleOrdreAppel {

    /* nom de l'exemple */
    abstract String nom();

    /* crée un groupe de classement avec les données de l'exemple */
    abstract GroupeClassement initialise() throws VerificationException ;

    public void execute(boolean logFiles) throws JAXBException, VerificationException {

        GroupeClassement groupe = initialise();

        AlgoOrdreAppelEntree entree = new AlgoOrdreAppelEntree();
        entree.groupesClassements.add(groupe);

        AlgoOrdreAppelSortie sortie = AlgoOrdreAppel.calculerOrdresAppels(entree);

        if (logFiles) {
            JAXBContext jc = JAXBContext.newInstance(AlgoOrdreAppelEntree.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(entree, new File(nom() + "_entree.xml"));

            jc = JAXBContext.newInstance(AlgoOrdreAppelSortie.class);
            m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(sortie, new File(nom() + "_sortie.xml"));
        }
        
    }

}
