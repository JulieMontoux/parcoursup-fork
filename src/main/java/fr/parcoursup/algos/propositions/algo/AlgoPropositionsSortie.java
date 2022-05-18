
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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

@XmlRootElement
public class AlgoPropositionsSortie implements Serializable {

    /* les parametres de l'algorithme */
    @NotNull
    public final Parametres parametres;
    
    AlgoPropositionsSortie(@NotNull Parametres parametres) {
        this.parametres = parametres;
    }

    AlgoPropositionsSortie(@NotNull AlgoPropositionsEntree entree) {
        this.parametres = entree.parametres;
        voeux.addAll(entree.voeux);
        internats.addAll(entree.internats.values());
        indexInternats.ajouter(entree.internatsIndex);
        groupes.addAll(entree.groupesAffectations.values());
    }

    /* liste des voeux, avec statut mis à jour */
    public final List<Voeu> voeux
            = new ArrayList<>();

    /* liste des internats, permettant de récupérer les positions max d'admission */
    public transient List<GroupeInternat> internats
            = new ArrayList<>();

    /* index des internats */
    public transient IndexInternats indexInternats = new IndexInternats();

    /* liste des groupes */
    public transient List<GroupeAffectation> groupes
            = new ArrayList<>();


    /* signale que la vérification a déclenché une alerte
    (groupes ignorés lors de l'export, intervention rapide nécessaire) */
    private boolean alerte = false;

    public boolean getAlerte() {
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
    public Collection<Voeu> invaliderGroupes(Set<GroupeAffectation> groupesNonValides) {
        
            Collection<Voeu> resultat = new ArrayList<>();
            
            groupesNonExportes.clear();
            for (Voeu v : voeux) {
                if (!v.estPropositionDuJour()) {
                    continue;
                }
                if (groupesNonValides.contains(v.getGroupeAffectation())) {
                    groupesNonExportes.add(v.groupeUID);
                    resultat.add(v);
                }
            }

            /* suppression des propositions des groupes invalidés,
                y compris par influence */
            voeux.removeIf(v -> groupesNonExportes.contains(v.groupeUID));
            groupes.removeIf(g -> groupesNonExportes.contains(g.id));
            internats.removeIf(
                    internat -> internat.aUnVoeuDansGroupe(groupesNonExportes)
            );

            return resultat;
    }
    
    /* liste des groupes d'affectations ignorés par l'alerte */
    final Set<GroupeAffectationUID> groupesNonExportes = new HashSet<>();
        

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
        return voeux.stream().filter(Voeu::estPropositionDuJour).count();
    }

    public long nbDemissions() {
        return voeux.stream().filter(Voeu::estDemissionAutomatiqueParRepondeurAutomatique).count();
    }

    /**
     * Utilisé par les désérialisations Json et XML
     */
    private AlgoPropositionsSortie() {
        parametres = new Parametres(0,0);
    }

    /**
     * Utilisé par la désérialisation Java
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        internats = new ArrayList<>();
        indexInternats = new IndexInternats();
        groupes = new ArrayList<>();
    }

}
