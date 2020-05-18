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
package parcoursup.propositions.exemples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import parcoursup.exceptions.VerificationException;
import parcoursup.propositions.algo.AlgoPropositionsEntree;
import parcoursup.propositions.algo.GroupeAffectation;
import parcoursup.propositions.algo.GroupeAffectationUID;
import parcoursup.propositions.algo.GroupeInternat;
import parcoursup.propositions.algo.GroupeInternatUID;
import parcoursup.propositions.algo.Parametres;
import parcoursup.propositions.algo.Voeu;
import static parcoursup.verification.VerificationEntreeAlgoPropositions.verifierIntegrite;

public class ExempleAleatoire extends ExemplePropositions {

    private static final Logger LOGGER = Logger.getLogger(ExempleAleatoire.class.getSimpleName());

    @Override
    String nom() {
        return "ExempleAleatoire";
    }

    final int nbCandidats;

    static final double PROPORTION_CONCOURS_COMMUN = 0.1;
    static final double PROPORTION_INTERNATS_COMMUNS = 0.5;
    static final double PROPORTION_INTERNATS = 0.5;

    static final int NB_FORMATIONS_PAR_CONCOURS = 100;
    static final int MAX_NB_VOEUX_PAR_CONCOURS_COMMUN = 80;

    static final int NB_FORMATIONS_PAR_ETABLISSEMENT = 5;

    static final int MAX_NB_VOEUX_PAR_CANDIDAT = 10;

    static final int CAPACITE_MAX_FORMATION_NORMALE = 500;
    static final int CAPACITE_MAX_FORMATION_CC = 200;

    static final int CAPACITE_MAX_INTERNAT = 30;

    static final int MAX_NB_GROUPES_PAR_FORMATION = 3;

    private final List<Etablissement> etablissements = new ArrayList<>();

    final Set<Voeu> voeux = new HashSet<>();

    int lastGTiCod = 1;
    int lastGTaCod = 1;
    int lastCGpCod = 1;
    int lastGCnCod = 1;

    final Random random = new Random();

    class Etablissement {

        final int gTiCod;

        final boolean isConcoursCommun;

        final boolean isInternatCommun;

        final boolean isInternatParFormation;

        final ArrayList<FormationAffectation> formations = new ArrayList<>();

        final ArrayList<GroupeClassement> jurys = new ArrayList<>();

        final Map<GroupeClassement, GroupeInternat> internatsCommuns = new HashMap<>();

        Etablissement() throws VerificationException {
            this.gTiCod = lastGTiCod++;
            isConcoursCommun = (Math.random() < PROPORTION_CONCOURS_COMMUN);

            if (isConcoursCommun) {

                GroupeClassement g1 = new GroupeClassement();
                GroupeClassement g2 = new GroupeClassement();
                jurys.add(g1);
                jurys.add(g2);

                int nbformations = 1 + random.nextInt(NB_FORMATIONS_PAR_CONCOURS);
                for (int i = 0; i < nbformations; i++) {
                    FormationAffectation f = new FormationAffectation();
                    formations.add(f);
                    f.ajouterGroupe(g1);
                    f.ajouterGroupe(g2);
                }

                isInternatCommun = false;
                isInternatParFormation = false;

            } else {

                isInternatCommun
                        = (Math.random() < PROPORTION_INTERNATS_COMMUNS);

                isInternatParFormation
                        = !isInternatCommun && (Math.random() < PROPORTION_INTERNATS);

                if (isInternatCommun) {
                    GroupeClassement ifilles = new GroupeClassement();
                    GroupeClassement igarcons = new GroupeClassement();
                    GroupeInternatUID ifillesid = new GroupeInternatUID(
                            ifilles.cGCod,
                            0);
                    GroupeInternatUID igarconsid = new GroupeInternatUID(
                            igarcons.cGCod,
                            0);
                    internatsCommuns.put(ifilles, new GroupeInternat(
                            ifillesid,
                            1 + random.nextInt(CAPACITE_MAX_INTERNAT)
                    )
                    );
                    internatsCommuns.put(igarcons, new GroupeInternat(
                            igarconsid,
                            1 + random.nextInt(CAPACITE_MAX_INTERNAT)
                    )
                    );
                }

                int nbFormations = 1 + random.nextInt(NB_FORMATIONS_PAR_ETABLISSEMENT);

                for (int i = 0; i < nbFormations; i++) {
                    FormationAffectation f = new FormationAffectation();
                    formations.add(f);

                    if (isInternatParFormation) {
                        GroupeClassement juryInternat = new GroupeClassement();
                        GroupeInternatUID iid = new GroupeInternatUID(
                                juryInternat.cGCod,
                                f.gTaCod);
                        f.internat = new GroupeInternat(
                                iid,
                                1 + random.nextInt(CAPACITE_MAX_INTERNAT)
                        );
                        f.juryInternat = juryInternat;
                    }

                    int nbGroupes = 1 + random.nextInt(MAX_NB_GROUPES_PAR_FORMATION);

                    for (int j = 0; j < nbGroupes; j++) {
                        GroupeClassement g = new GroupeClassement();
                        f.ajouterGroupe(g);
                        jurys.add(g);
                    }
                }
            }
        }

        int capacite() {
            int result = 0;
            for (FormationAffectation f : formations) {
                result += f.capacite();
            }
            return result;
        }

        int ajouterVoeux(Candidat candidat) throws VerificationException {
            int nbVoeux = isConcoursCommun
                    ? 1 + random.nextInt(MAX_NB_VOEUX_PAR_CONCOURS_COMMUN)
                    : 1;

            /* ordre dans le répondeur automatique */
            List<Integer> list = new ArrayList<>();
            for (int i = 1; i <= nbVoeux; i++) {
                list.add(i);
            }
            java.util.Collections.shuffle(list);

            for (int i = 0; i < nbVoeux; i++) {
                FormationAffectation fa = formations.get(random.nextInt(formations.size()));
                int rangRepondeurAutomatique = 0;
                if (candidat.repondeurAutomatiqueActive) {
                    rangRepondeurAutomatique = list.get(i);
                }
                fa.ajouterVoeu(candidat, random.nextBoolean(), rangRepondeurAutomatique);
            }
            return nbVoeux;
        }

        class FormationAffectation {

            final int gTaCod;

            GroupeInternat internat = null;
            GroupeClassement juryInternat = null;
            
            FormationAffectation() {
                this.gTaCod = lastGTaCod++;
            }
            
            void ajouterGroupe(GroupeClassement c) throws VerificationException {

                GroupeAffectationUID gui
                        = new GroupeAffectationUID(
                                c.cGCod,
                                gTiCod,
                                gTaCod);

                int capaciteMax = isConcoursCommun ? CAPACITE_MAX_FORMATION_CC : CAPACITE_MAX_FORMATION_NORMALE;
                int capacite = 1 + random.nextInt(capaciteMax);
                int rangLimite = 1 + random.nextInt(capacite);
                int nbPropositions = 1 + random.nextInt(capacite);
                GroupeAffectation ga
                        = new GroupeAffectation(
                                capacite,
                                gui,
                                rangLimite,
                                nbPropositions,
                                parametres
                        );

                this.classements.put(ga, c);
                groupes.add(ga);
            }

            void ajouterVoeu(Candidat candidat, boolean avecInternat, int rangRepondeurAutomatique) throws VerificationException {

                /* pas deux fois le même voeu */
                if(vus.contains(candidat)) {
                    return;
                }
                
                vus.add(candidat);

                
                GroupeAffectation ga
                        = groupes.get(random.nextInt(groupes.size()));
                GroupeClassement cl
                        = classements.get(ga);
                int rang = cl.ajouterCandidat(candidat);

                boolean horsPP = (random.nextInt(100) == 0);
                Voeu.StatutVoeu statut
                        = horsPP
                                ? Voeu.StatutVoeu.AFFECTE_JOURS_PRECEDENTS
                                : Voeu.StatutVoeu.EN_ATTENTE_DE_PROPOSITION;

                if (!avecInternat || (internat == null && internatsCommuns.isEmpty())) {
                    if (rang <= cl.plusHautRangAffecte) {
                        statut = Voeu.StatutVoeu.AFFECTE_JOURS_PRECEDENTS;
                    }
                    voeux.add(
                            new Voeu(
                                    candidat.gCnCod,
                                    avecInternat,
                                    ga,
                                    rang,
                                    rang,
                                    rangRepondeurAutomatique,
                                    statut,
                                    horsPP
                            )
                    );
                } else {
                    
                    GroupeInternat g = internat;
                    GroupeClassement j = juryInternat;
                    
                    /* on choisit un internat commun au hasard */
                    if(g == null) {
                        int indice = random.nextInt(internatsCommuns.size());
                        for(Entry<GroupeClassement,GroupeInternat> entry : internatsCommuns.entrySet()) {
                            if(indice <= 0) {
                                j = entry.getKey();
                                g = entry.getValue();
                                break;
                            }
                            indice--;
                        }
                    }
                    
                    assert(j != null && g != null);
                    
                    int rangInternat = j.ajouterCandidat(candidat);

                    if ((rang <= cl.plusHautRangAffecte 
                            && rangInternat <= j.plusHautRangAffecte)) {
                        statut = Voeu.StatutVoeu.AFFECTE_JOURS_PRECEDENTS;
                    }
                    
                    voeux.add(
                            new Voeu(
                                    candidat.gCnCod,
                                    ga,
                                    rang,
                                    rang,
                                    g,
                                    rangInternat,
                                    rangRepondeurAutomatique,
                                    statut,
                                    horsPP
                            )
                    );

                    if (random.nextBoolean()) {
                        voeux.add(
                                new Voeu(
                                        candidat.gCnCod,
                                        false,
                                        ga,
                                        rang,
                                        rang,
                                        rangRepondeurAutomatique,
                                        statut,
                                        horsPP
                                )
                        );
                    }
                }
            }

            final Map<GroupeAffectation, GroupeClassement> classements
                    = new HashMap<>();

            final ArrayList<GroupeAffectation> groupes
                    = new ArrayList<>();

            final Set<Candidat> vus = new HashSet<>();

            int capacite() {
                int result = 0;
                for (GroupeAffectation g : groupes) {
                    result += g.nbRecrutementsSouhaite;
                }
                return result;
            }
        }

        class GroupeClassement {

            final int cGCod;

            /* le rang le plus haut dans l'ordre d'appel d'un candidat recruté */
            int plusHautRangAffecte = 1 + random.nextInt(1 + nbCandidats / 4);

            GroupeClassement() {
                this.cGCod = lastCGpCod++;
            }

            final Map<Candidat, Integer> rangs = new HashMap<>();

            /* ajoute un candidat et renvoie son rang.*/
            int ajouterCandidat(Candidat c) {

                if (rangs.containsKey(c)) {
                    return rangs.get(c);
                }

                while (true) {
                    int rang = 1 + random.nextInt(nbCandidats);
                    if (!rangs.containsValue(rang)) {
                        rangs.put(c, rang);
                        return rang;
                    }
                }
            }
        }

    }

    static class Candidat {

        final int gCnCod;

        final boolean repondeurAutomatiqueActive;

        Candidat(boolean repondeurAutomatiqueActive, int gCnCod) {
            this.gCnCod = gCnCod;
            /* deux candidats sur 3 avec répondeur automatique */
            this.repondeurAutomatiqueActive = repondeurAutomatiqueActive;
        }

    }

    Parametres parametres;
    
    public ExempleAleatoire(int nbCandidats) {

        this.nbCandidats = Math.max(100, nbCandidats);
        parametres = new Parametres(1,30);
    }

    private static final Random r = new Random();
        
    @Override
    AlgoPropositionsEntree donneesEntree() throws VerificationException {

        int capaciteTotale = 0;

        LOGGER.info("Génération aléatoire des établissements et formations");
        while (capaciteTotale < nbCandidats) {
            Etablissement e = new Etablissement();
            etablissements.add(e);
            capaciteTotale += e.capacite();
        }

        LOGGER.info("Génération aléatoire des voeux et classements");
        for (int i = 0; i < nbCandidats; i++) {
            Candidat c = new Candidat(random.nextBoolean(),lastGCnCod++);
            int nbVoeux = r.nextInt(MAX_NB_VOEUX_PAR_CANDIDAT);
            while (nbVoeux > 0) {
                Etablissement e
                        = etablissements.get(random.nextInt(etablissements.size()));
                nbVoeux -= e.ajouterVoeux(c);
            }
            if ((i + 1) % 100_000 == 0) {
                LOGGER.log(Level.INFO, "{0} candidats générés ...", i);
            }
        }

        LOGGER.info("Génération données entrée algorithme");

        entree = new AlgoPropositionsEntree(parametres);

        entree.voeux.addAll(voeux);

        for (Etablissement e : etablissements) {
            for (Etablissement.FormationAffectation fa : e.formations) {
                for (GroupeAffectation g : fa.groupes) {
                    entree.groupesAffectations.put(g.id, g);
                }
                if (fa.internat != null) {
                    entree.internats.put(fa.internat.id, fa.internat);
                }
            }
            for (GroupeInternat internat : e.internatsCommuns.values()) {
                entree.internats.put(internat.id, internat);
            }
        }

        /* On force la vérification de P7.2 */
        Map<Integer, Voeu> propositionsAuxCandidatsAvecRepAuto = new HashMap<>();
        for (Voeu v : entree.voeux) {
            int gCnCod = v.id.gCnCod;
            if(!entree.candidatsAvecRepondeurAutomatique.contains(gCnCod) 
                    || v.estAffecteHorsPP()) {
                continue;
            }
            if (v.estAcceptationAutomatique() || v.estAffecteJoursPrecedents()) {
                if (propositionsAuxCandidatsAvecRepAuto.containsKey(gCnCod)) {
                    v.refuserAutomatiquement();
                }
                propositionsAuxCandidatsAvecRepAuto.put(gCnCod, v);
            }
        }
        
        verifierIntegrite(entree);

        return entree;

    }
}
