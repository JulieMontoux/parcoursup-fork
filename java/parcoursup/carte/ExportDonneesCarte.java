
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
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

    public ExportDonneesCarte(ConnecteurOracle connecteur, String repOut) {
        this.connecteur = connecteur;

        this.repOut = repOut;

        if (!this.repOut.endsWith(File.separator)) {
            this.repOut += File.separator;
        }
    }

    /* nom du xml regroupant les noms des fichiers de données */
    private static final String DATAS_FILENAME = "files.xml";

    /* suffixe variable des noms de fichiers de données */
    private final String suffix = LocalDateTime.now().toString().replace(":", "-") + ".json";

    /* taille du buffer pour le zip */
    private static final int BUFFER_SIZE = 8192;

    /* taille réperoire de génération des fichiers */
    private String repOut = ".";

    public void exporterDonnees(
            boolean exporterCapacites,
            boolean exporterPC
    ) throws SQLException, IOException, JAXBException {

        LOGGER.info("Export des données de la carte");

        DonneesCarteFilenames filenames = new DonneesCarteFilenames();

        /* Récupération des anciens noms des fichiers de données */
        if ((new File(this.repOut + DATAS_FILENAME)).canRead()) {
            JAXBContext jc = JAXBContext.newInstance(DonneesCarteFilenames.class);
            Unmarshaller um = jc.createUnmarshaller();
            filenames = (DonneesCarteFilenames) um.unmarshal(new File(this.repOut + DATAS_FILENAME));
        }

        if (exporterCapacites || filenames.capacitesFilename.isEmpty()) {
            filenames.capacitesFilename = DonneesCarteFilenames.DONNEES_CAPACITES_PREFIX + "-" + suffix;
            exporterDonneesCapacite(this.repOut + filenames.capacitesFilename);
            zipper(this.repOut + filenames.capacitesFilename);
        }
        if (exporterPC || filenames.disposFilename.isEmpty()) {
            filenames.disposFilename = DonneesCarteFilenames.DONNEES_PLACES_DISPOS_PREFIX + "-" + suffix;
            exporterDonneesPlacesDispos(this.repOut + filenames.disposFilename);
            zipper(this.repOut + filenames.disposFilename);
        }

        /* Mise à jour des noms des fichiers de données */
        JAXBContext jc = JAXBContext.newInstance(DonneesCarteFilenames.class);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(filenames, new File(this.repOut + DATAS_FILENAME));

    }

    /* export des capacités */
    void exporterDonneesCapacite(String filename) throws SQLException, IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename, true), StandardCharsets.UTF_8))) {

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

                        int gtacod = result.getInt(1);
                        int capacite = result.getInt(2);
                        writer.newLine();
                        writer.write((first ? "" : ",") + "[" + gtacod + "," + capacite + "]");
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
                new FileOutputStream(filename, true), StandardCharsets.UTF_8))) {

            LOGGER.log(Level.INFO, "Export des formations avec des places libres dans le fichier {0}", filename);

            writer.write("{\"formations_avec_dispos\":[");

            try (Statement stmt = connection().createStatement()) {

                LOGGER.info("Récupération des formations avec places disponibles");
                stmt.setFetchSize(1_000_000);

                String sql = "select g_ta_cod, sc_flc_grp_bac_gen,sc_flc_grp_bac_tec, sc_flc_grp_bac_pro,sc_flc_grp_aut from v_carte_export_dispo";

                LOGGER.info(sql);

                try (ResultSet result = stmt.executeQuery(sql)) {

                    boolean first = true;
                    while (result.next()) {

                        int gtacod = result.getInt(1);
                        int scFlcGrpBacGen = result.getInt(2);
                        int scFlcGrpBacTec = result.getInt(3);
                        int scFlcGrpBacPro = result.getInt(4);
                        int scFlcGrpAut = result.getInt(5);

                        writer.newLine();
                        writer.write((first ? "" : ",") + "["
                                + gtacod + ","
                                + scFlcGrpBacGen + ","
                                + scFlcGrpBacTec + ","
                                + scFlcGrpBacPro + ","
                                + scFlcGrpAut + ""
                                + "]"
                        );
                        first = false;
                    }
                }
            }

            writer.newLine();
            writer.write("]}");
        }

    }

    private final ConnecteurOracle connecteur;

    private Connection connection() {
        return connecteur.connection();
    }

    private void zipper(String filename) throws IOException {
        String output = filename + ".zip";

        try (
                BufferedInputStream buffer = new BufferedInputStream(new FileInputStream(filename), BUFFER_SIZE);
                ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(output)))) {
            out.setMethod(ZipOutputStream.DEFLATED);
            out.setLevel(9);

            File file = new File(filename);

            ZipEntry entry = new ZipEntry(file.getName());
            out.putNextEntry(entry);

            byte[] data = new byte[BUFFER_SIZE];

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
