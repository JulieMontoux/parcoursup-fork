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
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@SuppressWarnings("unused")
@XmlRootElement
public final class AlgoPropositionsEntree implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(AlgoPropositionsEntree.class.getSimpleName());

    /**
     * les parametres de l'algorithme
     */
    Parametres parametres;

    /**
     * la liste des voeux
     */
    public final Set<Voeu> voeux
            = new HashSet<>();

    /**
     * La liste des groupes d'affectation
     */
    public final Map<GroupeAffectationUID, GroupeAffectation> groupesAffectations
            = new HashMap<>();

    /**
     * La liste des internats
     */
    public final Map<GroupeInternatUID, GroupeInternat> internats
            = new HashMap<>();

    /**
     * indexation des internats, utilisé pour l'export
     */
    public final IndexInternats internatsIndex
            = new IndexInternats();

    /**
     * liste des candidats (identifiés par leur G_CN_COD) dont le répondeur automatique est activé
     */
    public final Set<Integer> candidatsAvecRepondeurAutomatique
            = new HashSet<>();

    public AlgoPropositionsEntree(Parametres parametres) {
        this.parametres = parametres;
    }

    public static AlgoPropositionsEntree deserialiser(String filename) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(AlgoPropositionsEntree.class);
        Unmarshaller um = jc.createUnmarshaller();
        return (AlgoPropositionsEntree) um.unmarshal(new File(filename));
    }

    public void serialiser(String filename) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(AlgoPropositionsEntree.class);
        Marshaller um = jc.createMarshaller();
        um.marshal(this, new File(filename));
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
        EnumMap<StatutVoeu, Integer> statutsVoeux = new EnumMap<>(StatutVoeu.class);
        for (StatutVoeu s : StatutVoeu.values()) {
            statutsVoeux.put(s, 0);
        }
        for (Voeu v : voeux) {
            StatutVoeu s = v.statut;
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
        parametres = new Parametres(0, 0, 0);
    }

    /**
     * Renvoie la liste des internats pour lesquels il n'y a pas de réservation de place
     *
     * @return la liste des internats pour lesquels il n'y a pas de réservation de place
     */
    public Set<GroupeInternatUID> getInternatsSansReservationDePlace() {
        Set<GroupeAffectationUID> groupesSansResa = groupesAffectations.values().stream()
                .filter(GroupeAffectation::getFinDeReservationPlacesInternats)
                .map(g -> g.id)
                .collect(Collectors.toSet());
        return voeux.stream()
                .filter(v -> v.internatUID != null)
                .filter(v -> groupesSansResa.contains(v.groupeUID))
                .map(v -> v.internatUID)
                .collect(Collectors.toSet());

    }

    /**
     * Renvoie le nombre de candidats affectés par internat
     *
     * @param statuts les statuts des voeux
     * @return le nombre de candidats affectés par internat
     */
    public Map<GroupeInternatUID, Long> getNbCandidatsAffectesParInternat(StatutsVoeux statuts) {

        Map<GroupeInternatUID, List<Voeu>> propositionsParInternat
                = voeux.stream().filter(v ->
                v.internatUID != null
                        && statuts.estProposition(v)
        ).collect(Collectors.groupingBy(v -> v.internatUID)
        );

        return propositionsParInternat.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().stream()
                                        .map(v -> v.id.gCnCod)
                                        .distinct()
                                        .count()
                        )
                );
    }

    /**
     * Renvoie la liste des voeux par internat
     *
     * @return la liste des voeux par internat
     */
    public Map<GroupeInternatUID, List<Voeu>> getVoeuxParInternat() {
        //noinspection DataFlowIssue
        return voeux.stream().filter(Voeu::avecInternatAClassementPropre)
                .collect(Collectors.groupingBy(
                                v -> v.internatUID
                        )
                );
    }

    /**
     * Renvoie la liste des voeux par internat
     *
     * @return la liste des voeux par internat
     */
    public Map<GroupeInternatUID, List<Integer>> rangsEnAttenteParInternat() {
        return voeux.stream()
                .filter(v -> StatutVoeu.estEnAttenteDeProposition(v.statut) && v.internatUID != null)
                .collect(Collectors.groupingBy(
                        v -> v.internatUID
                )).entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().map(v -> v.rangInternat).collect(Collectors.toList())
                ));
    }

    /**
     * Renvoie la liste des voeux par internat
     *
     * @param statuts les statuts des voeux
     * @return la liste des voeux par internat
     */
    public Set<GroupeInternatUID> getInternatsAvecAuMoinsUneNouvelleProposition(StatutsVoeux statuts) {
        return voeux.stream()
                .filter(Voeu::avecInternatAClassementPropre)
                .filter(statuts::estPropositionDuJour)
                .map(v -> v.internatUID)
                .collect(Collectors.toSet());
    }

    /**
     * Renvoie la liste des voeux par internat
     *
     * @return la liste des voeux par internat
     */
    public boolean estRepondeurAutomatiqueActivable() {
        return !candidatsAvecRepondeurAutomatique.isEmpty();
    }

    /**
     * Renvoie la liste des voeux par internat
     *
     * @param id l'internat
     * @return la liste des voeux par internat
     */
    public List<Voeu> voeuxDeLInternat(GroupeInternatUID id) {
        return voeux.stream().filter(v -> id.equals(v.internatUID)).collect(Collectors.toList());
    }

    /**
     * Renvoie la liste des voeux par internat
     *
     * @return la liste des voeux par internat
     */
    public StatutsVoeux getStatutsInitiaux() {
        return new StatutsVoeux(this.voeux);
    }

    /**
     * Renvoie des données préclaculées utilisées par l'algorithme d'admission
     *
     * @return les données préclaculées
     */
    AlgoPropositionDonneesPrecalculees getDonneesPrecalculees() {
        Map<GroupeInternatUID, Integer> barresMaximalesAdmissionInternats =
                BarresInternats.calculerBarresMaximalesInternats(
                        getInternatsSansReservationDePlace(),
                        groupesAffectations.values(),
                        internats,
                        getVoeuxParInternat(),
                        parametres
                );
        return new AlgoPropositionDonneesPrecalculees(
                voeux,
                barresMaximalesAdmissionInternats,
                /*EVOL 2024 : On ne traite pas le groupes si il a le flag adm_stop. */
                groupesAffectations.values().stream()
                        .filter(GroupeAffectation::estOuvertAuxAdmission)
                        .collect(toSet()),
                candidatsAvecRepondeurAutomatique
        );
    }
}
