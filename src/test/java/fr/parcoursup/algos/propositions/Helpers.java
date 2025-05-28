package fr.parcoursup.algos.propositions;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.propositions.algo.GroupeAffectation;
import fr.parcoursup.algos.propositions.algo.GroupeInternat;
import fr.parcoursup.algos.propositions.algo.StatutVoeu;
import fr.parcoursup.algos.propositions.algo.Voeu;

public class Helpers {
    /** helpers **/
    public static Voeu creeVoeuAvecInternat(
            int gCnCod,
            GroupeAffectation groupeAffectation,
            GroupeInternat groupeInternat,
            StatutVoeu statutVoeu,
            int ordreAppel,
            int rangInternat
    ) throws VerificationException {

        return new Voeu(
                gCnCod,
                groupeAffectation.id,
                ordreAppel,
                ordreAppel,
                groupeInternat.id,
                rangInternat,
                0,
                statutVoeu,
                false
        );
    }

    public static Voeu creeVoeuSansInternatEtInjecteDependances(
            int gCnCod,
            GroupeAffectation groupeAffectation,
            StatutVoeu statutVoeu,
            int ordreAppel
    ) throws VerificationException {

        Voeu voeu = new Voeu(
                gCnCod,
                false, // parametre avecInternat
                groupeAffectation.id,
                ordreAppel,
                ordreAppel,
                0,
                statutVoeu,
                false
        );
        return voeu;
    }
}
