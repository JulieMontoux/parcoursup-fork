
/* Copyright 2019 © Ministère de l'Enseignement Supérieur, de la Recherche et de
l'Innovation,
    David Auber (david.auber@u-bordeaux.fr)
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
package parcoursup.carte;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import parcoursup.donnees.ConnecteurOracle;

public class ExportDonneesCarte {

    private static final Logger LOGGER = Logger.getLogger(ExportDonneesCarte.class.getSimpleName());

    public ExportDonneesCarte(ConnecteurOracle connecteur) {
        this.connecteur = connecteur;
    }

    /* nom du xml regroupant les noms des fichiers de données */
    private static final String datasFilename = "files.xml";

    /* suffixe variable des noms de fichiers de données */
    private final String suffix = LocalDateTime.now().toString().replace(":", "-");

    /* taille du buffer pour le zip */
    private static final int BUFFER_SIZE = 8192;

    public void exporterDonnees(
            boolean exporterCapacites,
            boolean exporterPC,
            boolean exporterDonneesCampagne
    ) throws SQLException, IOException, JAXBException {

        LOGGER.info("Export des données de la carte");

        DonneesCarteFilenames filenames = new DonneesCarteFilenames();

        /* Récupération des anciens noms des fichiers de données */
        if ((new File(datasFilename)).canRead()) {
            JAXBContext jc = JAXBContext.newInstance(DonneesCarteFilenames.class);
            Unmarshaller um = jc.createUnmarshaller();
            filenames = (DonneesCarteFilenames) um.unmarshal(new File(datasFilename));
        }

        if (exporterCapacites || filenames.capacitesFilename.isEmpty()) {
            filenames.capacitesFilename = DonneesCarteFilenames.donneesCapacitePrefix + "-" + suffix + ".json";
            exporterDonneesCapacite(filenames.capacitesFilename);
            zipper(filenames.capacitesFilename);
        }
        if (exporterPC || filenames.disposFilename.isEmpty()) {
            filenames.disposFilename = DonneesCarteFilenames.donneesPlacesDisposPrefix + "-" + suffix + ".json";
            exporterDonneesPlacesDispos(filenames.disposFilename);
            zipper(filenames.disposFilename);
        }

        if (exporterDonneesCampagne || filenames.campagneFilename.isEmpty()) {
            filenames.campagneFilename = DonneesCarteFilenames.donneesCampagnePrefix + "-" + suffix + ".json";
            throw new RuntimeException("L'export des données de campagne n'est pas encore implémenté");
            //exporterDonneesCampagne(filenames.campagneFilename);
            //zipper(filenames.campagneFilename);
        }

        /* Mise à jour des noms des fichiers de données */
        JAXBContext jc = JAXBContext.newInstance(DonneesCarteFilenames.class);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(filenames, new File(datasFilename));

    }

    /* export des capacités */
    void exporterDonneesCapacite(String filename) throws SQLException, FileNotFoundException, IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename, true)))) {

            LOGGER.log(Level.INFO, "Export des capacités dans le fichier {0}", filename);

            writer.write("{\"capacites_formations\":[");

            try (Statement stmt = connection().createStatement()) {

                /* récupère la liste des groupes et les taux minimum de boursiers
            et de candidats du secteur depuis la base de données */
                LOGGER.info("Récupération des capacités des formations");
                stmt.setFetchSize(1_000_000);

                String sql
                        = " SELECT g_ta_cod,MAX(capacite) FROM"
                        + " ("
                        + "(SELECT g_ta_cod, a_rc_cap_inf capacite"
                        + " FROM A_REC where a_rc_cap_inf is not null)"
                        + " UNION "
                        + " (SELECT g_ta_cod, a_rc_cap capacite"
                        + " FROM A_REC where a_rc_cap is not null)"
                        + " UNION "
                        + " (SELECT g_ta_cod, SUM(A_RG_PLA) capacite FROM A_REC_GRP "
                        + " WHERE A_RG_PLA is not null GROUP BY g_ta_cod)"
                        + ") "
                        + " GROUP BY g_ta_cod";

                LOGGER.info(sql);

                try (ResultSet result = stmt.executeQuery(sql)) {

                    boolean first = true;
                    while (result.next()) {

                        int G_TA_COD = result.getInt(1);
                        int capacite = result.getInt(2);
                        writer.newLine();
                        writer.write((first ? "" : ",") + "[" + G_TA_COD + "," + capacite + "]");
                        first = false;
                    }
                }
            }

            writer.newLine();
            writer.write("]}");
        }
    }

    /* export des données de PC */
    void exporterDonneesPlacesDispos(String filename) throws IOException, SQLException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename, true)))) {

            LOGGER.log(Level.INFO, "Export des formations avec des places libres dans le fichier {0}", filename);

            writer.write("{\"formations_avec_dispos\":[");

            try (Statement stmt = connection().createStatement()) {

                LOGGER.info("Récupération des formations avec places disponibles");
                stmt.setFetchSize(1_000_000);

                String sql
                        = "with zero_voeu_attente as "
                        + "( "
                        + "SELECT arec.G_TA_COD,arec.A_RC_CAP capacite "
                        + "FROM A_REC arec "
                        + "WHERE NOT EXISTS( "
                        + "SELECT 1 FROM  "
                        + "A_VOE voe, A_SIT_VOE asv "
                        + "WHERE "
                        + "voe.g_ta_cod=arec.g_ta_cod "
                        + "and "
                        + "voe.a_sv_cod=asv.a_sv_cod "
                        + "AND "
                        + "asv.a_sv_flg_att=1) "
                        + ") "
                        + ", "
                        + " "
                        + "nb_affectes as "
                        + "( "
                        + "SELECT adm.g_ta_cod, COUNT(*) nb_aff "
                        + "FROM "
                        + "zero_voeu_attente, A_ADM adm, A_SIT_VOE asv "
                        + "where "
                        + "zero_voeu_attente.g_ta_cod=adm.g_ta_cod "
                        + "AND "
                        + "adm.a_sv_cod=asv.a_sv_cod "
                        + "and "
                        + "asv.a_sv_flg_aff=1 "
                        + "GROUP BY adm.g_ta_cod "
                        + ") "
                        + " "
                        + "SELECT zero_voeu_attente.g_ta_Cod FROM  "
                        + "zero_voeu_attente, nb_affectes  "
                        + "where  "
                        + "zero_voeu_attente.g_ta_cod=nb_affectes.g_ta_cod "
                        + "AND "
                        + "nb_aff < capacite";

                LOGGER.info(sql);

                try (ResultSet result = stmt.executeQuery(sql)) {

                    boolean first = true;
                    while (result.next()) {

                        int G_TA_COD = result.getInt(1);
                        writer.newLine();
                        writer.write((first ? "" : ",") + "[" + G_TA_COD + "]");
                        first = false;
                    }
                }
            }

            writer.newLine();
            writer.write("]}");
        }

    }

    /* export des données valables durant toute la campagne */
    public void exporterDonneesCampagne(String filename) {
        throw new RuntimeException("Unimplemented");
    }

    private final ConnecteurOracle connecteur;

    private Connection connection() {
        return connecteur.connection();
    }

    private void zipper(String filename) throws FileNotFoundException, IOException {
        String output = filename + ".zip";

        try (
                BufferedInputStream buffer = new BufferedInputStream(new FileInputStream(filename), BUFFER_SIZE);
                ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(output)))) {
            out.setMethod(ZipOutputStream.DEFLATED);
            out.setLevel(9);

            ZipEntry entry = new ZipEntry(filename);
            out.putNextEntry(entry);

            byte data[] = new byte[BUFFER_SIZE];

            while (true) {
                int count = buffer.read(data, 0, BUFFER_SIZE);
                if (count <= 0) {
                    break;
                }
                out.write(data, 0, count);
            }
        }

    }

}
