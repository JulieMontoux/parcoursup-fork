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
package fr.parcoursup.algos.propositions.algo;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.exceptions.VerificationExceptionMessage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
@XmlRootElement
public final class AlgoPropositionsEntree implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(AlgoPropositionsEntree.class.getSimpleName());

    /** les parametres de l'algorithme */
    Parametres parametres;

    /** la liste des voeux */
    public final Set<Voeu> voeux
            = new HashSet<>();

    /** La liste des groupes d'affectation */
    public final Map<GroupeAffectationUID, GroupeAffectation> groupesAffectations
            = new HashMap<>();

    /** La liste des internats */
    public final Map<GroupeInternatUID, GroupeInternat> internats
            = new HashMap<>();

    /** indexation des internats, utilisé pour l'export */
    public final IndexInternats internatsIndex
            = new IndexInternats();

    /** liste des candidats (identifiés par leur G_CN_COD) dont le répondeur automatique est activé */
    public final Set<Integer> candidatsAvecRepondeurAutomatique
            = new HashSet<>();

    public AlgoPropositionsEntree(Parametres parametres) {
        this.parametres = parametres;
    }

    public AlgoPropositionsEntree(AlgoPropositionsEntree o) throws VerificationException {
        this.parametres = o.parametres;//immutable
        o.voeux.forEach(v -> voeux.add(new Voeu(v)));
        for (Map.Entry<GroupeAffectationUID, GroupeAffectation> e : o.groupesAffectations.entrySet()) {
            groupesAffectations.put(
                    e.getKey(),
                    new GroupeAffectation(e.getValue())
            );
        }
        for (Map.Entry<GroupeInternatUID, GroupeInternat> e : o.internats.entrySet()) {
            internats.put(e.getKey(), new GroupeInternat(e.getValue()));
        }
        this.internatsIndex.ajouter(o.internatsIndex);

        injecterGroupesEtInternatsDansVoeux();

        candidatsAvecRepondeurAutomatique.addAll(o.candidatsAvecRepondeurAutomatique);
    }

    public static AlgoPropositionsEntree deserialiser(String filename) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(AlgoPropositionsEntree.class);
        Unmarshaller um = jc.createUnmarshaller();
        return (AlgoPropositionsEntree) um.unmarshal(new File(filename));
    }

    /* Deux helpers */
    public void ajouter(GroupeAffectation g) throws VerificationException {
        if (groupesAffectations.containsKey(g.id)) {
            throw new VerificationException(VerificationExceptionMessage.ALGO_PROPOSITIONS_ENTREE_GROUPE_AFFECTATION_DUPLIQUE);
        }
        groupesAffectations.put(g.id, g);
    }

    public void ajouter(GroupeInternat g) throws VerificationException {
        if (internats.containsKey(g.id)) {
            throw new VerificationException(VerificationExceptionMessage.ALGO_PROPOSITIONS_ENTREE_GROUPE_INTERNAT_DUPLIQUE);
        }
        internatsIndex.indexer(g.id);
        internats.put(g.id, g);
    }

    public void ajouter(Voeu v) throws VerificationException {
        if (voeux.contains(v)) {
            throw new VerificationException(VerificationExceptionMessage.ALGO_PROPOSITIONS_ENTREE_VOEU_DUPLIQUE);
        }
        voeux.add(v);
    }

    public void ajouterOuRemplacer(Voeu v) {
        voeux.remove(v);//removes any voeu with the same id
        voeux.add(v);
    }

    public void loggerEtatAdmission() {

        /* Bilan statuts voeux */
        EnumMap<Voeu.StatutVoeu, Integer> statutsVoeux = new EnumMap<>(Voeu.StatutVoeu.class);
        for (Voeu.StatutVoeu s : Voeu.StatutVoeu.values()) {
            statutsVoeux.put(s, 0);
        }
        for (Voeu v : voeux) {
            Voeu.StatutVoeu s = v.getStatut();
            statutsVoeux.put(s, statutsVoeux.get(s) + 1);
        }

        LOGGER.log(Level.INFO, "Jour {0}{1}Voeux {2}{3}Statuts {4}", new Object[]{parametres.nbJoursCampagne, System.lineSeparator(), voeux.size(), System.lineSeparator(), statutsVoeux});

    }

    public Parametres getParametres() {
        return parametres;
    }

    /* pour tests */
    public void setParametres(Parametres p) {
        this.parametres = p;
    }

    /* for deserialization */
    public AlgoPropositionsEntree() {
        parametres = new Parametres(0, 0, 0, 0);
    }

    public void injecterGroupesEtInternatsDansVoeux() throws VerificationException {
        for (Voeu v : voeux) {
            GroupeAffectation g = groupesAffectations.get(v.groupeUID);
            if(g == null) {
                throw new VerificationException(VerificationExceptionMessage.ALGO_PROPOSITIONS_ENTREE_GROUPE_INCONNU, v.groupeUID);
            }
            v.setGroupeAffectation(g);

            if (v.internatUID != null) {
                GroupeInternat internat =internats.get(v.internatUID);
                if(internat == null) {
                    throw new VerificationException(VerificationExceptionMessage.ALGO_PROPOSITIONS_ENTREE_GROUPE_INTERNAT_INCONNU, v.internatUID);
                }
                v.setInternat(internat);
            }
        }
    }

    /**
     * Callback method invoked after unmarshalling XML data into target..
     *
     * @param unmarshaller non-null instance of JAXB mapped class prior to
     * unmarshalling into it
     * @param parent instance of JAXB mapped class that will reference target.
     * null when target is root element.
     * @throws VerificationException erreur de vérification
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) throws VerificationException {
        injecterGroupesEtInternatsDansVoeux();
    }

}
