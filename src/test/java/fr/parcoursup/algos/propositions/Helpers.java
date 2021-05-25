package fr.parcoursup.algos.propositions;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.propositions.algo.GroupeAffectation;
import fr.parcoursup.algos.propositions.algo.GroupeInternat;
import fr.parcoursup.algos.propositions.algo.Voeu;

public class Helpers {
    /** helpers **/
    public static Voeu creeVoeuAvecInternatEtInjecteDependances(
            int gCnCod,
            GroupeAffectation groupeAffectation,
            GroupeInternat groupeInternat,
            Voeu.StatutVoeu statutVoeu,
            int ordreAppel,
            int rangInternat
    ) throws VerificationException {

        Voeu voeu = new Voeu(
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
        voeu.setGroupeAffectation(groupeAffectation);
        voeu.setInternat(groupeInternat);
        voeu.ajouterAuxGroupes();
        return voeu;
    }

    public static Voeu creeVoeuSansInternatEtInjecteDependances(
            int gCnCod,
            GroupeAffectation groupeAffectation,
            Voeu.StatutVoeu statutVoeu,
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
        voeu.setGroupeAffectation(groupeAffectation);
        voeu.ajouterAuxGroupes();
        return voeu;
    }
}
