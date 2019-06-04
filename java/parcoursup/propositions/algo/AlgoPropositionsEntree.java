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
package parcoursup.propositions.algo;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;
import parcoursup.propositions.meilleursbacheliers.MeilleurBachelier;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AlgoPropositionsEntree {

    private static final Logger LOGGER = Logger.getLogger(AlgoPropositionsEntree.class.getSimpleName());

    /* la liste des voeuxEnAttente */
    public final Collection<Voeu> voeux
            = new HashSet<>();

    /* La liste des groupes d'affectation */
    public final Map<GroupeAffectationUID, GroupeAffectation> groupesAffectations
            = new HashMap<>();

    /* La liste des internats */
    public final Map<GroupeInternatUID, GroupeInternat> internats
            = new HashMap<>();

    /* liste des candidats (identifiés par leur G_CN_COD) dont le répondeur automatique est activé */
    public final Set<Integer> candidatsAvecRepondeurAutomatique
            = new HashSet<>();

    /* liste des meilleurs bacheliers */
    public final List<MeilleurBachelier> meilleursBacheliers = new ArrayList<>();

    /* liste des propositions faites dans le cadre du dispositif meilleurs bacheliers */
    public final Set<VoeuUID> propositionsMeilleursBacheliers = new HashSet<>();

    /* pour chaque formation, identifiée par son G_TA_COD,
    le nombre de places réservées pour les meilleurs bacheliers. */
    public final Map<Integer, Integer> nbPlacesMeilleursBacheliers = new HashMap<>();

    /* Sauvegarde des données au format xml.
    Si le paramètre filename est null, un nom par défaut est utilisé,
    paramétré par la date et l'heure.
     */
    public void serialiser(String filename) throws JAXBException {
        if (filename == null) {
            filename = "entree_" + LocalDateTime.now() + ".xml";
        }
        Marshaller m = JAXBContext.newInstance(AlgoPropositionsEntree.class).createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(this, new File(filename));
    }

    /* Deux helpers */
    public void ajouter(GroupeAffectation g) {
        groupesAffectations.put(g.id, g);
    }

    public void ajouter(GroupeInternat g) {
        internats.put(g.id, g);
    }

    public void loggerEtatAdmission() {
        
        /* Bilan statuts voeux */
        Map<Voeu.StatutVoeu, Integer> statutsVoeux = new HashMap<>();
        for (Voeu.StatutVoeu s : Voeu.StatutVoeu.values()) {
            statutsVoeux.put(s, 0);
        }
        for (Voeu v : voeux) {
            Voeu.StatutVoeu s = v.getStatut();
            statutsVoeux.put(s, statutsVoeux.get(s) + 1);
        }

        LOGGER.info(
                "Jour " + GroupeInternat.nbJoursCampagne + System.lineSeparator()
                + "Voeux " + voeux.size() + System.lineSeparator()
                + "Statuts " + statutsVoeux);

    }

}
