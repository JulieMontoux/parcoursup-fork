/* Copyright 2022 © Ministère de l'Enseignement Supérieur, de la Recherche et de
l'Innovation, Hugo Gimbert (hugo.gimbert@enseignementsup.gouv.fr)

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

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static fr.parcoursup.algos.propositions.algo.IndexInternats.TypeInternat.*;

public class IndexInternats implements Serializable {

    @XmlRootElement
    public enum TypeInternat {
        /* internat dont la capacité d'accueil est partagée entre plusieurs formations
        identifié par son cGiCod */
        INTERNAT_D_ETABLISSEMENT,
        /* internat dont la capacité d'accueil est partagée entre plusieurs formations
        identifié par une paire cGiCod,gTiCod */
        INTERNAT_FORMATIONS_MULTIPLES,
        /* internat pour une unique formation,
        identifié par la paire cGiCod,gTaCod */
        INTERNAT_FORMATION_UNIQUE
    }

    /* index via la clé cGiCod */
    private final Map<Integer, TypeInternat> index = new HashMap<>();

    public void indexer(GroupeInternatUID gid) throws VerificationException {
        if(gid.cGiCod == 0) {
            throw new VerificationException(VerificationExceptionMessage.INTERNAT_CGI_NUL);
        }
        final TypeInternat typ=
        (gid.gTiCod == 0 && gid.gTaCod ==0) ? INTERNAT_D_ETABLISSEMENT
                : (gid.gTaCod != 0) ? INTERNAT_FORMATION_UNIQUE
                : INTERNAT_FORMATIONS_MULTIPLES;

        TypeInternat typActuel = index.get(gid.cGiCod);
        if(typActuel == null) {
            index.put(gid.cGiCod, typ);
        } else if(typActuel != typ) {
            throw new VerificationException((VerificationExceptionMessage.INTERNAT_INCONSISTENCE_INDEX), gid.cGiCod);
        }
    }

    public GroupeInternatUID getInternat(int cGiCod, GroupeAffectationUID gid) throws VerificationException {
        TypeInternat typ = index.get(cGiCod);
        if(typ == null) {
            return null;
        } else {
            switch(typ) {
                case INTERNAT_D_ETABLISSEMENT: return new GroupeInternatUID(cGiCod,0,0);
                case INTERNAT_FORMATIONS_MULTIPLES: return new GroupeInternatUID(cGiCod,gid.gTiCod,0);
                case INTERNAT_FORMATION_UNIQUE: return new GroupeInternatUID(cGiCod,gid.gTiCod,gid.gTaCod);
                default: throw new VerificationException(VerificationExceptionMessage.INTERNAT_TYPE_INCONNU, cGiCod);
            }
        }
    }

    public void ajouter(IndexInternats internatsIds) {
        index.putAll(internatsIds.index);
    }
}
