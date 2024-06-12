package fr.parcoursup.algos.propositions.affichages;

import fr.parcoursup.algos.exceptions.VerificationException;
import fr.parcoursup.algos.propositions.Helpers;
import fr.parcoursup.algos.propositions.algo.*;
import org.junit.Test;
import org.powermock.reflect.Whitebox;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class TestAlgoAffichages {
    
    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<AlgosAffichages> constructor = AlgosAffichages.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void mettreAJourAffichage_doit_reussir() throws Exception {
        Parametres p = new Parametres(1, 60, 90);
        AlgoPropositionsSortie sortie = new AlgoPropositionsSortie(p);
        GroupeAffectation g = new GroupeAffectation(2, new GroupeAffectationUID(1, 1, 1), 2, 2, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, g.id.gTaCod), 2);

        Voeu v1 = Helpers.creeVoeuSansInternatEtInjecteDependances(1,g, Voeu.StatutVoeu.PROPOSITION_DU_JOUR,1);
        Voeu v2 = Helpers.creeVoeuSansInternatEtInjecteDependances(2,g, Voeu.StatutVoeu.PROPOSITION_DU_JOUR,2);
        // Voeu avec internat du candidat du v1 qui a déjà été accepté sans internat
        Voeu v3 = Helpers.creeVoeuAvecInternatEtInjecteDependances(v1.id.gCnCod, g, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, v1.ordreAppel, 3);
        // Voeu avec internat du candidat du v2 qui a déjà été accepté sans internat
        Voeu v4 = Helpers.creeVoeuAvecInternatEtInjecteDependances(v2.id.gCnCod, g, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, v1.ordreAppel, 4);

        Set<VoeuUID> voeuxAvecPropositionDansMemeFormation = new HashSet<>(Arrays.asList(v3.id, v4.id));
        Set<VoeuUID> propositionsDuJour = new HashSet<>(Arrays.asList(v3.id, v4.id));

        sortie.groupes.add(g);
        sortie.internats.add(gi);
        sortie.voeux.addAll(Arrays.asList(v1, v2, v3, v4));

        AlgosAffichages.mettreAJourAffichages(sortie, voeuxAvecPropositionDansMemeFormation, propositionsDuJour);
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void mettreAJourAffichage_doit_reussirSiVoeuAffecteJoursPrecedents() throws Exception {
        Parametres p = new Parametres(1, 60, 90);
        AlgoPropositionsSortie sortie = new AlgoPropositionsSortie(p);
        GroupeAffectation g = new GroupeAffectation(2, new GroupeAffectationUID(1, 1, 1), 2, 2, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, g.id.gTaCod), 2);

        Voeu v1 = Helpers.creeVoeuSansInternatEtInjecteDependances(1, g, Voeu.StatutVoeu.PROPOSITION_JOURS_PRECEDENTS_ACCEPTEE, 1);
        Voeu v3 = Helpers.creeVoeuAvecInternatEtInjecteDependances(v1.id.gCnCod, g , gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, v1.ordreAppel, 3);

        Set<VoeuUID> voeuxAvecPropositionDansMemeFormation = new HashSet<>(Collections.singletonList(v3.id));
        Set<VoeuUID> propositionsDuJour = new HashSet<>(Collections.singletonList(v3.id));

        sortie.groupes.add(g);
        sortie.internats.add(gi);
        sortie.voeux.addAll(Arrays.asList(v1, v3));

        AlgosAffichages.mettreAJourAffichages(sortie, voeuxAvecPropositionDansMemeFormation, propositionsDuJour);
    }

    @Test(expected = Test.None.class /* no exception expected */)
    public void mettreAJourRangsListeAttente_doit_reussirSiVoeuPasDansVoeuxAvecPropositionDansMemeFormation() throws Exception {
        Parametres p = new Parametres(1, 60, 90);
        AlgoPropositionsSortie sortie = new AlgoPropositionsSortie(p);
        GroupeAffectation g = new GroupeAffectation(2, new GroupeAffectationUID(1, 1, 1), 2, 2, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, g.id.gTaCod), 2);

        Voeu v1 = Helpers.creeVoeuSansInternatEtInjecteDependances(1 , g, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, 1);
        Voeu v2 = Helpers.creeVoeuSansInternatEtInjecteDependances(2, g, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 2);

        Voeu v3 = Helpers.creeVoeuAvecInternatEtInjecteDependances(v1.id.gCnCod, g, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, v1.ordreAppel, 3);
        Voeu v4 = Helpers.creeVoeuAvecInternatEtInjecteDependances(v2.id.gCnCod, g, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, v2.ordreAppel, 4);

        Set<VoeuUID> voeuxAvecPropositionDansMemeFormation = new HashSet<>(Arrays.asList(v3.id, v4.id));
        Set<VoeuUID> propositionsDuJour = new HashSet<>(Collections.emptyList());

        sortie.groupes.add(g);
        sortie.internats.add(gi);
        sortie.voeux.addAll(Arrays.asList(v1, v2, v3, v4));

        Whitebox.invokeMethod(
            AlgosAffichages.class,
            "mettreAJourRangsListeAttente",
            Arrays.asList(v1, v2, v3, v4),  // Les voeux du GroupeAffectation g
            voeuxAvecPropositionDansMemeFormation,
            propositionsDuJour,0,g
        );
    }



    @Test(expected = Test.None.class /* no exception expected */)
    public void mettreAJourRangDernierAppeleAffiche_doit_reussirPourGroupeAffectationAvecVoeuEnAttenteSansDemandeInternat() throws Exception {
        Parametres p = new Parametres(1, 60, 90);
        GroupeAffectation g = new GroupeAffectation(2, new GroupeAffectationUID(1, 1, 1), 2, 2, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, g.id.gTaCod), 2);

        Voeu v1 = Helpers.creeVoeuSansInternatEtInjecteDependances(1, g, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, 1);
        Voeu v2 = Helpers.creeVoeuSansInternatEtInjecteDependances(2, g, Voeu.StatutVoeu.PROPOSITION_DU_JOUR, 2);
        Voeu v3 = Helpers.creeVoeuAvecInternatEtInjecteDependances(v1.id.gCnCod, g, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION,  v1.ordreAppel, 1 );
        Voeu v4 = Helpers.creeVoeuAvecInternatEtInjecteDependances(v2.id.gCnCod, g, gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION,  v2.ordreAppel, 4 );

        Whitebox.invokeMethod(
            AlgosAffichages.class,
            "mettreAJourRangDernierAppeleAffiche",
            g,
            Arrays.asList(v1, v2, v3, v4)  // Les voeux du GroupeAffectation g
        );
    }

    
    
    @Test
    public void testCalculrangListeAttente() throws VerificationException {
    	 Parametres p = new Parametres(1, 60, 90);
    	 GroupeAffectation g = new GroupeAffectation(2, new GroupeAffectationUID(1, 1, 1), 2, 2, p);
    	  

    	Voeu v1 = Helpers.creeVoeuSansInternatEtInjecteDependances(1, g, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 1);
    	Voeu v2 = Helpers.creeVoeuSansInternatEtInjecteDependances(2, g, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 2);
    	Voeu v3 = Helpers.creeVoeuSansInternatEtInjecteDependances(3, g, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 3);
    	Voeu v4 = Helpers.creeVoeuSansInternatEtInjecteDependances(4, g, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 4);
    	Voeu v5 = Helpers.creeVoeuSansInternatEtInjecteDependances(5, g, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 5);
    	Voeu v6 = Helpers.creeVoeuSansInternatEtInjecteDependances(6, g, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 6);
    	Voeu v7 = Helpers.creeVoeuSansInternatEtInjecteDependances(7, g, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 7);
    	Voeu v8 = Helpers.creeVoeuSansInternatEtInjecteDependances(8, g, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 8);
    	
    	List<Voeu> listeVoeux = new ArrayList<>();
    	listeVoeux.add(v1);
    	listeVoeux.add(v2);
    	listeVoeux.add(v3);
    	listeVoeux.add(v4);
    	listeVoeux.add(v5);
    	listeVoeux.add(v6);
    	listeVoeux.add(v7);
    	listeVoeux.add(v8);
    	
    	 Set<VoeuUID> voeuxAvecPropositionDansMemeFormation = new HashSet<>();
         Set<VoeuUID> propositionsDuJour  = new HashSet<>();

         int nbJour = 1;
         
         /* On commence au jour 1*/
    	AlgosAffichages.mettreAJourRangsListeAttente(listeVoeux, 
    			voeuxAvecPropositionDansMemeFormation, propositionsDuJour, nbJour,g);
    	
    	System.out.println("--------------DEPART JOUR 1-------------------------");
    	afficherResultat(listeVoeux);
    	

    	nbJour = 2;
    	System.out.println("------------Le 1 et 2 ont une proposition -----------------");
    	listeVoeux.remove(v1);
    	listeVoeux.remove(v2);
    	AlgosAffichages.mettreAJourRangsListeAttente(listeVoeux, voeuxAvecPropositionDansMemeFormation, propositionsDuJour, nbJour,g);
    	afficherResultat(listeVoeux);
    	
    	
    	nbJour = 3;
    	
    	System.out.println("------------Le 5 et 6 demissionne -----------------");
    	listeVoeux.remove(v5);
    	listeVoeux.remove(v6);

    	AlgosAffichages.mettreAJourRangsListeAttente(listeVoeux, voeuxAvecPropositionDansMemeFormation, propositionsDuJour, nbJour,g);
    	afficherResultat(listeVoeux);

    	
    	nbJour = 4;
    	System.out.println("------------Le 5 réintégré  -----------------");
    	listeVoeux.add(v5);
    	v5.setRangListeAttenteVeille(0);
    	
    	AlgosAffichages.mettreAJourRangsListeAttente(listeVoeux, voeuxAvecPropositionDansMemeFormation, propositionsDuJour, nbJour,g);
    	afficherResultat(listeVoeux);
    	
    	
    	nbJour = 5;
     	System.out.println("------------Le 6 réintégré  -----------------");
    	listeVoeux.add(v6);
    	v6.setRangListeAttenteVeille(0);
    	AlgosAffichages.mettreAJourRangsListeAttente(listeVoeux, voeuxAvecPropositionDansMemeFormation, propositionsDuJour, nbJour,g);
    	afficherResultat(listeVoeux);

    	
//       	nbJour = 6;
//     	System.out.println("------------FIN  -----------------");
//    	AlgosAffichages.mettreAJourRangsListeAttente(listeVoeux, voeuxAvecPropositionDansMemeFormation, propositionsDuJour, nbJour);
//    	afficherResultat(listeVoeux);

    }
    
    
    
    
    @Test
    public void test2CalculrangListeAttente() throws VerificationException {
    	 Parametres p = new Parametres(1, 60, 90);
    	 GroupeAffectation g = new GroupeAffectation(2, new GroupeAffectationUID(1, 1, 1), 2, 2, p);
    	  

    	Voeu v1 = Helpers.creeVoeuSansInternatEtInjecteDependances(1, g, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 1);
    	v1.setRangListeAttenteVeille(3);
    	
    	Voeu v2 = Helpers.creeVoeuSansInternatEtInjecteDependances(2, g, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 2);
    	v2.setRangListeAttenteVeille(4);
    	
    	Voeu v3 = Helpers.creeVoeuSansInternatEtInjecteDependances(3, g, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 3);
     	v3.setRangListeAttenteVeille(5);
     	
    	Voeu v4 = Helpers.creeVoeuSansInternatEtInjecteDependances(4, g, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 4);
    	v4.setRangListeAttenteVeille(6);
    	
    	Voeu v5 = Helpers.creeVoeuSansInternatEtInjecteDependances(5, g, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 5);
    	v5.setRangListeAttenteVeille(0);
    	
    	Voeu v6 = Helpers.creeVoeuSansInternatEtInjecteDependances(6, g, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 6);
    	v6.setRangListeAttenteVeille(8);
    	
    	Voeu v7 = Helpers.creeVoeuSansInternatEtInjecteDependances(7, g, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, 7);
    	v7.setRangListeAttenteVeille(9);
    	
    	List<Voeu> listeVoeux = new ArrayList<>();
    	listeVoeux.add(v1);
    	listeVoeux.add(v2);
    	listeVoeux.add(v3);
    	listeVoeux.add(v4);
    	listeVoeux.add(v5);
    	listeVoeux.add(v6);
     	listeVoeux.add(v7);
    	
    	 Set<VoeuUID> voeuxAvecPropositionDansMemeFormation = new HashSet<>();
         Set<VoeuUID> propositionsDuJour  = new HashSet<>();

         int nbJour = 3;
         
         /* On commence au jour 1*/
    	AlgosAffichages.mettreAJourRangsListeAttente(listeVoeux, voeuxAvecPropositionDansMemeFormation, propositionsDuJour, nbJour,g);
    	
    	System.out.println("-----------------DEPART-------------------------");
    	for (Voeu v : listeVoeux) {
    		System.out.println(v.id.gCnCod + " - " + v.getRangListeAttente());
    		v.setRangListeAttenteVeille(v.getRangListeAttente());
    	}
    
    }
    
    
    private void afficherResultat(List<Voeu> listeVoeux) {
    	for (Voeu v : listeVoeux) {
    		System.out.println(v.id.gCnCod + " - " + v.getRangListeAttente() + " - " + v.getRangListeAttenteVeille());
    		//Maj rang de la veille
    		v.setRangListeAttenteVeille(v.getRangListeAttente());
    	}
    }
    
}