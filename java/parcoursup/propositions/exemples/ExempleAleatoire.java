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
import parcoursup.propositions.algo.AlgoPropositionsEntree;
import parcoursup.propositions.algo.GroupeAffectation;
import parcoursup.propositions.algo.GroupeAffectationUID;
import parcoursup.propositions.algo.GroupeInternat;
import parcoursup.propositions.algo.GroupeInternatUID;
import parcoursup.propositions.algo.Voeu;
import static parcoursup.verification.VerificationEntreeAlgoPropositions.verifierIntegrite;

public class ExempleAleatoire extends ExemplePropositions {

    private static final Logger LOGGER = Logger.getLogger(ExempleAleatoire.class.getSimpleName());

    @Override
    String nom() {
        return "ExempleAleatoire";// + dateCreation;
    }

    final int nbCandidats;

    final double proportionConcoursCommuns = 0.1;
    final double proportionInternatsCommuns = 0.5;
    final double proportionInternats = 0.5;

    final int nbFormationsParConcours = 100;
    final int maxNbVoeuxParConcoursCommun = 80;

    final int nbFormationsParEtablissement = 5;

    final int maxNbVoeuxParCandidat = 10;

    final int capaciteMaxFormationNormale = 500;
    final int capaciteMaxFormationCC = 200;

    final int capaciteMaxInternat = 30;

    final int maxNbGroupesParFormation = 3;

    private final List<Etablissement> etablissements = new ArrayList<>();

    final Set<Voeu> voeux = new HashSet<>();

    int last_G_TI_COD = 1;
    int last_G_TA_COD = 1;
    int last_C_GP_COD = 1;
    int last_G_CN_COD = 1;

    final Random random = new Random();

    class Etablissement {

        final int G_TI_COD;

        final boolean isConcoursCommun;

        final boolean isInternatCommun;

        final boolean isInternatParFormation;

        final ArrayList<FormationAffectation> formations = new ArrayList<>();

        final ArrayList<GroupeClassement> jurys = new ArrayList<>();

        final Map<GroupeClassement, GroupeInternat> internatsCommuns = new HashMap<>();

        Etablissement() {
            this.G_TI_COD = last_G_TI_COD++;
            isConcoursCommun = (Math.random() < proportionConcoursCommuns);

            if (isConcoursCommun) {

                GroupeClassement g1 = new GroupeClassement();
                GroupeClassement g2 = new GroupeClassement();
                jurys.add(g1);
                jurys.add(g2);

                int nbformations = 1 + random.nextInt(nbFormationsParConcours);
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
                        = (Math.random() < proportionInternatsCommuns);

                isInternatParFormation
                        = !isInternatCommun && (Math.random() < proportionInternats);

                if (isInternatCommun) {
                    GroupeClassement ifilles = new GroupeClassement();
                    GroupeClassement igarcons = new GroupeClassement();
                    GroupeInternatUID ifillesid = new GroupeInternatUID(
                            ifilles.C_G_COD,
                            0);
                    GroupeInternatUID igarconsid = new GroupeInternatUID(
                            igarcons.C_G_COD,
                            0);
                    internatsCommuns.put(ifilles, new GroupeInternat(
                            ifillesid,
                            1 + random.nextInt(capaciteMaxInternat)
                    )
                    );
                    internatsCommuns.put(igarcons, new GroupeInternat(
                            igarconsid,
                            1 + random.nextInt(capaciteMaxInternat)
                    )
                    );
                }

                int nbFormations = 1 + random.nextInt(nbFormationsParEtablissement);

                for (int i = 0; i < nbFormations; i++) {
                    FormationAffectation f = new FormationAffectation();
                    formations.add(f);

                    if (isInternatParFormation) {
                        GroupeClassement juryInternat = new GroupeClassement();
                        GroupeInternatUID iid = new GroupeInternatUID(
                                juryInternat.C_G_COD,
                                f.G_TA_COD);
                        f.internat = new GroupeInternat(
                                iid,
                                1 + random.nextInt(capaciteMaxInternat)
                        );
                        f.juryInternat = juryInternat;
                    }

                    int nbGroupes = 1 + random.nextInt(maxNbGroupesParFormation);

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

        int ajouterVoeux(Candidat candidat) {
            int nbVoeux = isConcoursCommun
                    ? 1 + random.nextInt(maxNbVoeuxParConcoursCommun)
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

            final int G_TA_COD;

            GroupeInternat internat = null;
            GroupeClassement juryInternat = null;
            
            FormationAffectation() {
                this.G_TA_COD = last_G_TA_COD++;
            }

            void ajouterGroupe(GroupeClassement c) {

                GroupeAffectationUID gui
                        = new GroupeAffectationUID(
                                c.C_G_COD,
                                G_TI_COD,
                                G_TA_COD);

                int capaciteMax = isConcoursCommun ? capaciteMaxFormationCC : capaciteMaxFormationNormale;
                int capacite = random.nextInt(capaciteMax + 1);
                int rangLimite = (int) ((1 + Math.random()) * capacite);
                int nbPropositions = (int) ((1 + Math.random()) * capacite);
                GroupeAffectation ga
                        = new GroupeAffectation(
                                capacite,
                                gui,
                                rangLimite,
                                nbPropositions
                        );

                this.classements.put(ga, c);
                groupes.add(ga);
            }

            void ajouterVoeu(Candidat candidat, boolean avecInternat, int rangRepondeurAutomatique) {

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
                                ? Voeu.StatutVoeu.affecteJoursPrecedents
                                : Voeu.StatutVoeu.enAttenteDeProposition;

                if (!avecInternat || (internat == null && internatsCommuns.isEmpty())) {
                    if (rang <= cl.plusHautRangAffecte) {
                        statut = Voeu.StatutVoeu.affecteJoursPrecedents;
                    }
                    voeux.add(
                            new Voeu(
                                    candidat.G_CN_COD,
                                    avecInternat,
                                    ga,
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
                        statut = Voeu.StatutVoeu.affecteJoursPrecedents;
                    }
                    
                    voeux.add(
                            new Voeu(
                                    candidat.G_CN_COD,
                                    ga,
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
                                        candidat.G_CN_COD,
                                        false,
                                        ga,
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

            final int C_G_COD;

            /* le rang le plus haut dans l'ordre d'appel d'un candidat recruté */
            int plusHautRangAffecte = random.nextInt(nbCandidats / 4);

            GroupeClassement() {
                this.C_G_COD = last_C_GP_COD++;
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

    class Candidat {

        final int G_CN_COD;

        final boolean repondeurAutomatiqueActive;

        Candidat(boolean repondeurAutomatiqueActive) {
            this.G_CN_COD = last_G_CN_COD++;
            /* deux candidats sur 3 avec répondeur automatique */
            this.repondeurAutomatiqueActive = repondeurAutomatiqueActive;
        }

    }

    public ExempleAleatoire(int nbCandidats) {

        this.nbCandidats = Math.max(100, nbCandidats);
        GroupeInternat.nbJoursCampagneDatePivotInternats = 60;
        GroupeInternat.nbJoursCampagne = 1;
    }

    @Override
    AlgoPropositionsEntree donneesEntree() throws Exception {

        int capacite_totale = 0;

        LOGGER.info("Génération aléatoire des établissements et formations");
        while (capacite_totale < nbCandidats) {
            Etablissement e = new Etablissement();
            etablissements.add(e);
            capacite_totale += e.capacite();
        }

        LOGGER.info("Génération aléatoire des voeux et classements");
        for (int i = 0; i < nbCandidats; i++) {
            Candidat c = new Candidat(random.nextBoolean());
            int nbVoeux = (int) (Math.random() * maxNbVoeuxParCandidat);
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

        AlgoPropositionsEntree entree = new AlgoPropositionsEntree();

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
            int G_CN_COD = v.id.G_CN_COD;
            if(!entree.candidatsAvecRepondeurAutomatique.contains(G_CN_COD) 
                    || v.estAffecteHorsPP()) {
                continue;
            }
            if (v.estAcceptationAutomatique() || v.estAffecteJoursPrecedents()) {
                if (propositionsAuxCandidatsAvecRepAuto.containsKey(G_CN_COD)) {
                    v.refuserAutomatiquement();
                }
                propositionsAuxCandidatsAvecRepAuto.put(G_CN_COD, v);
            }
        }
        
        verifierIntegrite(entree);

        return entree;

    }
}
