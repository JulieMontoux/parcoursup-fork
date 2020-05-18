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
package parcoursup.carte.algo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import parcoursup.exceptions.VerificationException;

/* 
    Une filière de Parcoursup.
    Exemple: "DUT - Production/Mesures physiques"
 */
public class Filiere {

    /* libellé (champ G_FL_LIB en base). */
    final String libelle;

    /* sigle */
    final String sigle;

    /* code unique identifiant uniquement la filière dans la base */
    public final int cle;

    /* deux versions pour certaines filières: avec ou sans apprentissage */
    final boolean apprentissage;

    /* une liste de domaines Onisep pour cette filière */
    private final Set<DomaineOnisep> domaines = new HashSet<>();

    public void ajouterDomaine(DomaineOnisep domaine) {
        domaines.add(domaine);
    }
    
    /* pour chaque autre filière, le nombre de candidats ayant fait un voeu pour
    ces deux filières à la fois */
    final Map<Filiere, Integer> candidatsCommuns = new HashMap<>();

    public void ajouterFiliereAvecvoeuxCommuns(Filiere filiere2, int nbVoeuxCommuns) throws VerificationException {
        if(candidatsCommuns.containsKey(filiere2) ) {
            throw new VerificationException("ajouterFiliereAvecvoeuxCommuns: redondance");
        }
        candidatsCommuns.put(filiere2, nbVoeuxCommuns);
    }
    
    /* nombre de candidats à l'année n-1 */
    public int nbCandidatsAnneePrecedente() {
        return candidatsCommuns.getOrDefault(this,0);
    }

    /* la liste de toutes les filières, indexées par leur code */
    static final Map<Integer, Filiere> cleToFiliere = new HashMap<>();
    
    public static Filiere getFiliere(int cle) { return cleToFiliere.get(cle); }
    
    public static Filiere creerFiliere(String libelle,
            String sigle,
            int cle,
            boolean apprentissage) throws VerificationException {
        if (cleToFiliere.containsKey(cle)) {
            throw new VerificationException("Duplication de filiere.");
        }
        Filiere filiere = new Filiere(libelle, sigle, cle, apprentissage);
        cleToFiliere.put(cle, filiere);
        return filiere;
    }

    private Filiere(String libelle,
            String sigle,
            int cle,
            boolean apprentissage) {
        this.libelle = libelle;
        this.sigle = sigle;
        this.cle = cle;
        this.apprentissage = apprentissage;
    }

    Set<String> motsClesOnisep = null;

    Set<String> getMotsClesOnisep() {
        if(motsClesOnisep == null) {
            motsClesOnisep = new HashSet<>();
            for (DomaineOnisep dom : domaines) {
                DomaineOnisep domSup = dom;
                while(domSup != null) {
                    motsClesOnisep.add(domSup.libelle);
                    domSup = domSup.getDomaineSuperieur();
                }
            }
        }
        return motsClesOnisep;
    }

    public String getMotsClesRechercheCarte() {

        /* We use a custom comparator in order to avoid duplicates */
        Set<String> motsCles = new TreeSet<>((String s1, String s2) -> s1.compareTo(s2));

        motsCles.add(libelle);
        motsCles.add(sigle);
        motsCles.addAll(Arrays.asList(sigle.replace('/', ' ').replace('-', ' ').split(" ")));
        if (apprentissage) {
            motsCles.add("apprentissage");
        }

        for (DomaineOnisep dom : domaines) {
            motsCles.add(dom.libelle);
        }
        
        final StringBuilder result = new StringBuilder();
        for(String mot : motsCles) {
            result.append(mot).append(" ");
        }
        return result.toString();
    }

}
