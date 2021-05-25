package fr.parcoursup.algos.propositions.affichages;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.assertTrue;

import fr.parcoursup.algos.propositions.Helpers;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import fr.parcoursup.algos.propositions.algo.AlgoPropositionsSortie;
import fr.parcoursup.algos.propositions.algo.GroupeAffectation;
import fr.parcoursup.algos.propositions.algo.GroupeAffectationUID;
import fr.parcoursup.algos.propositions.algo.GroupeInternat;
import fr.parcoursup.algos.propositions.algo.GroupeInternatUID;
import fr.parcoursup.algos.propositions.algo.Parametres;
import fr.parcoursup.algos.propositions.algo.Voeu;
import fr.parcoursup.algos.propositions.algo.VoeuUID;

public class TestAlgoAffichages {
    
    @Test
    public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<AlgosAffichages> constructor = AlgosAffichages.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void mettreAJourAffichage_doit_reussir() throws Exception {
        Parametres p = new Parametres(1, 60);
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
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

    @Test
    public void mettreAJourAffichage_doit_reussirSiVoeuAffecteJoursPrecedents() throws Exception {
        Parametres p = new Parametres(1, 60);
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
        GroupeAffectation g = new GroupeAffectation(2, new GroupeAffectationUID(1, 1, 1), 2, 2, p);
        GroupeInternat gi = new GroupeInternat(new GroupeInternatUID(1, g.id.gTaCod), 2);

        Voeu v1 = Helpers.creeVoeuSansInternatEtInjecteDependances(1, g, Voeu.StatutVoeu.AFFECTE_JOURS_PRECEDENTS, 1);
        Voeu v3 = Helpers.creeVoeuAvecInternatEtInjecteDependances(v1.id.gCnCod, g , gi, Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION, v1.ordreAppel, 3);

        Set<VoeuUID> voeuxAvecPropositionDansMemeFormation = new HashSet<>(Collections.singletonList(v3.id));
        Set<VoeuUID> propositionsDuJour = new HashSet<>(Collections.singletonList(v3.id));

        sortie.groupes.add(g);
        sortie.internats.add(gi);
        sortie.voeux.addAll(Arrays.asList(v1, v3));

        AlgosAffichages.mettreAJourAffichages(sortie, voeuxAvecPropositionDansMemeFormation, propositionsDuJour);
    }

    @Test
    public void mettreAJourRangsListeAttente_doit_reussirSiVoeuPasDansVoeuxAvecPropositionDansMemeFormation() throws Exception {
        Parametres p = new Parametres(1, 60);
        AlgoPropositionsSortie sortie = Whitebox.invokeConstructor(AlgoPropositionsSortie.class, p);
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
            propositionsDuJour
        );
    }

    @Test
    public void mettreAJourRangDernierAppeleAffiche_doit_reussirPourGroupeAffectationAvecVoeuEnAttenteSansDemandeInternat() throws Exception {
        Parametres p = new Parametres(1, 60);
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

}