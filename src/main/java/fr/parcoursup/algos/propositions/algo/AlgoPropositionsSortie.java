
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

import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@XmlRootElement
public class AlgoPropositionsSortie implements Serializable {

    /* les parametres de l'algorithme */
    @NotNull
    public final Parametres parametres;

    public AlgoPropositionsSortie(@NotNull Parametres parametres) {
        this.parametres = parametres;
    }

    AlgoPropositionsSortie(
            @NotNull AlgoPropositionsEntree entree,
            @NotNull Map<GroupeInternatUID, Integer> barresAdmissionInternats,
            Map<GroupeInternatUID, Integer> barresMaximalesAdmissionInternats,
            StatutsVoeux statutsVoeux
    ) {
        this.parametres = entree.parametres;
        this.voeux.addAll(
                entree.voeux.stream()
                        .map(v -> new Voeu(v, statutsVoeux.getStatut(v)))
                        .collect(Collectors.toList())
        );
        this.internats.addAll(entree.internats.values());
        this.indexInternats.ajouter(entree.internatsIndex);
        this.groupes.addAll(entree.groupesAffectations.values());
        this.barresAdmissionInternats.putAll(barresAdmissionInternats);
        this.barresMaximalesAdmissionInternats.putAll(barresMaximalesAdmissionInternats);
        this.candidatsAvecRepondeurAutomatique.addAll(entree.candidatsAvecRepondeurAutomatique);
        statutsVoeux.getIterationschangementsStatuts().forEach((voeu, iteration) -> this.iterationsChangementsStatut.put(voeu.id, iteration));
    }

    /* liste des voeux, avec statut mis à jour */
    public final List<Voeu> voeux
            = new ArrayList<>();

    /* liste des internats, permettant de récupérer les positions max d'admission */
    public final List<GroupeInternat> internats
            = new ArrayList<>();

    /* index des internats */
    public final IndexInternats indexInternats = new IndexInternats();

    /* barres d'admission aux internats */
    public final Map<GroupeInternatUID, Integer> barresAdmissionInternats = new HashMap<>();

    /* barres maximales d'admission aux internats */
    public final Map<GroupeInternatUID, Integer> barresMaximalesAdmissionInternats = new HashMap<>();

    /* liste des groupes */
    public final List<GroupeAffectation> groupes
            = new ArrayList<>();

    /* iteration des propositions */
    public final Map<VoeuUID, Integer> iterationsChangementsStatut = new HashMap<>();

    /* iteration des propositions */
    public final Set<Integer> candidatsAvecRepondeurAutomatique = new HashSet<>();

    /* signale que la vérification a déclenché une alerte
    (groupes ignorés lors de l'export, intervention rapide nécessaire) */
    private boolean alerte = false;

    public boolean hasAlerte() {
        return alerte;
    }
    
    public String getAlerteMessage() {
        StringBuilder out = new StringBuilder();
        out.append("La vérification a déclenché une alerte. Les groupes suivants seront ignorés.");
        groupesNonExportes.forEach(grp -> 
            out.append(grp.toString())
        );
        return out.toString();
    }

    public void setAlerte() {
        alerte = true;
        avertissement = false;
    }
    
    /* supprime de la sortie les données liées à ces groupes 
    et renvoie la lise de spropositions annulées */
    public Collection<Voeu> invaliderPropositionsDuJourDesGroupes(
            Collection<GroupeAffectationUID> groupesNonValides
    ) {

        groupesNonExportes.clear();
        groupesNonExportes.addAll(groupesNonValides);

        Collection<Voeu> propositionsSupprimees =
                voeux.stream().filter(v -> StatutVoeu.estPropositionDuJour(v.statut) && groupesNonValides.contains(v.groupeUID))
                        .collect(Collectors.toList());

        /* suppression des propositions des groupes invalidés */
        voeux.removeAll(propositionsSupprimees);

        return propositionsSupprimees;
    }
    
    /* liste des groupes d'affectations ignorés par l'alerte */
    private final Set<GroupeAffectationUID> groupesNonExportes = new HashSet<>();
        

    /* signale que la vérification a déclenché un avertissement
    (pas de groupe ignoré donc pas d'intervention immédiate nécessaire)*/
    private boolean avertissement = false;
    
    public boolean getAvertissement() {
        return avertissement;
    }
    
    public void setAvertissement() {
        if(!alerte) {
            avertissement = true;
        }
    }

    public long nbPropositionsDuJour() {
        return voeux.stream().filter(v -> StatutVoeu.estPropositionDuJour(v.statut)).count();
    }

    public long nbDemissions() {
        return voeux.stream().filter(v -> StatutVoeu.estDemissionAutomatiqueParRepondeurAutomatique(v.statut)).count();
    }

    /**
     * Utilisé par les désérialisations Json et XML
     */
    public AlgoPropositionsSortie() {
        parametres = new Parametres(0,0,0);
    }

    public List<GroupeAffectationUID> groupesAffectationsConcernesParInternat(GroupeInternatUID id) {
        return this.voeux.stream()
                .filter(v -> id.equals(v.internatUID))
                .map(v -> v.groupeUID)
                .distinct()
                .collect(Collectors.toList());
    }

    public Map<GroupeAffectationUID, GroupeAffectation> getGroupesParId() {
        return groupes.stream().collect(Collectors.toMap(
                g -> g.id,
                g -> g
        ));
    }

    public Map<GroupeInternatUID, GroupeInternat> getInternatsParId() {
        return internats.stream().collect(Collectors.toMap(
                g -> g.id,
                g -> g
        ));
    }

    public int getIterationChangementStatut(@NotNull VoeuUID id) {
        return iterationsChangementsStatut.getOrDefault(id, 0);
    }
}
