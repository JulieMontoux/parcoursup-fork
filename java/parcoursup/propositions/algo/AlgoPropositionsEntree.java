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
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import parcoursup.exceptions.VerificationException;
import parcoursup.propositions.meilleursbacheliers.MeilleurBachelier;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AlgoPropositionsEntree {

    private static final Logger LOGGER = Logger.getLogger(AlgoPropositionsEntree.class.getSimpleName());

    /* les parametres de l'algorithme */
    Parametres parametres;

    /* la liste des voeux */
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

    /* pour chaque formation, identifiée par son g_ta_cod,
    le nombre de places réservées pour les meilleurs bacheliers. */
    public final Map<Integer, Integer> nbPlacesMeilleursBacheliers = new HashMap<>();

    public AlgoPropositionsEntree(Parametres parametres) {
        this.parametres = parametres;
    }
    
    public AlgoPropositionsEntree(AlgoPropositionsEntree o) throws VerificationException {
        this.parametres = new Parametres(o.parametres);
        o.voeux.forEach(v -> voeux.add(new Voeu(v)));
        for(Map.Entry<GroupeAffectationUID, GroupeAffectation> e : o.groupesAffectations.entrySet()) {
                groupesAffectations.put(
                        e.getKey(), 
                        new GroupeAffectation(e.getValue(),o.parametres)
                );
        }
        for(Map.Entry<GroupeInternatUID, GroupeInternat> e : internats.entrySet()) {
            internats.put(e.getKey(), new GroupeInternat(e.getValue()));
        }
        
        injecterGroupesEtInternatsDansVoeux();
        
        candidatsAvecRepondeurAutomatique.addAll(o.candidatsAvecRepondeurAutomatique);
        meilleursBacheliers.addAll(o.meilleursBacheliers);
        propositionsMeilleursBacheliers.addAll(o.propositionsMeilleursBacheliers);
        nbPlacesMeilleursBacheliers.putAll(o.nbPlacesMeilleursBacheliers);
    }

    public static AlgoPropositionsEntree deserialiser(String filename) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(AlgoPropositionsEntree.class);
        Unmarshaller um = jc.createUnmarshaller();
        return (AlgoPropositionsEntree) um.unmarshal(new File(filename));
    }

    /* Deux helpers */
    public void ajouter(GroupeAffectation g) throws VerificationException {
        if (groupesAffectations.containsKey(g.id)) {
            throw new VerificationException("GroupeAffectation dupliqué");
        }
        groupesAffectations.put(g.id, g);
    }

    public void ajouter(GroupeInternat g) throws VerificationException {
        if (internats.containsKey(g.id)) {
            throw new VerificationException("Internat dupliqué");
        }
        internats.put(g.id, g);
    }

    public void ajouter(Voeu v) throws VerificationException {
        if (voeux.contains(v)) {
            throw new VerificationException("Ajout de voeu dupliqué");
        }
        voeux.add(v);
    }

    public void ajouterSiNecessaire(Voeu v) {
        if (!voeux.contains(v)) {
            voeux.add(v);
        }
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
    AlgoPropositionsEntree() {
        parametres = new Parametres(0, 0);
    }

    public final void injecterGroupesEtInternatsDansVoeux() {
        voeux.forEach(v ->
        {
                v.groupe = groupesAffectations.get(v.groupeUID);
                if(v.internatUID != null) {
                    v.internat = internats.get(v.internatUID);
                }
        }
        );
    }

    /**
     * Callback method invoked after unmarshalling XML data into target..
     * @param unmarshaller   non-null instance of JAXB mapped class prior to unmarshalling into it
     * @param parent instance of JAXB mapped class that will reference target. null when target is root element.
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        injecterGroupesEtInternatsDansVoeux();
    }
    
    
    
}
