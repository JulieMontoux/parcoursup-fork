/* Copyright 2019 © Ministère de l'Enseignement Supérieur, de la Recherche et de l'Innovation,
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
package fr.parcoursup.algos.prod;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

@XmlRootElement
public class ExecutionParams implements Serializable {

    public final String url;
    public final String tnsAlias;
    public final String user;
    public final String password;
    public final String outputDir;

    private static final String PARAMS_FILE_ENV = "PARAMS_FILE";
    private static final String PARAMS_FILE_DEFAULT = "params.xml";

    private static final Logger LOGGER = Logger.getLogger(ExecutionParams.class.getSimpleName());

    public static ExecutionParams fromFile(String filename) throws IOException, JAXBException {
        File file = new File(filename);
        if (file.exists()) {
            try (BufferedInputStream in = new BufferedInputStream(
                    Files.newInputStream(file.toPath()))) {
                Unmarshaller m = JAXBContext.newInstance(ExecutionParams.class).createUnmarshaller();
                return (ExecutionParams) m.unmarshal(in);
            }
        } else {
            LOGGER.log(Level.WARNING,"Le fichier de configuration {0} est introuvable.", filename);
            LOGGER.log(Level.WARNING,"Exemple de contenu : %%n%%n%%n{0}%%n%%n%%n", getExemple());
            throw new FileNotFoundException("Le fichier de configuration " + filename + " est introuvable");
        }
    }

    public static ExecutionParams fromEnv() throws IOException, JAXBException {
        String filename = PARAMS_FILE_DEFAULT;
        try {
            filename = System.getenv(PARAMS_FILE_ENV);
        } catch (NullPointerException | SecurityException ex) {
            LOGGER.log(Level.WARNING, "Echec de la lecture de la variable d'environnement " +PARAMS_FILE_ENV);
        }
        if(filename == null) {
            LOGGER.log(Level.WARNING, "La variable d'environnement "
                    + PARAMS_FILE_ENV + " n''est pas positionn\u00e9e ou accessible, utilisation du fichier de param\u00e8tres par d\u00e9faut: " + PARAMS_FILE_DEFAULT
                    );
            filename = PARAMS_FILE_DEFAULT;

        }
        return fromFile(filename);
    }

    public static String getExemple() {
        try {
            ExecutionParams e = new ExecutionParams();
            StringWriter out = new StringWriter();
            Marshaller marshaller = JAXBContext.newInstance(ExecutionParams.class).createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(e, out);
            return out.toString();
        } catch (JAXBException ex) {
            return ex.getMessage();
        }
    }

    public ExecutionParams() {
        url = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=127.0.0.1)(PORT=1234))(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=MON_SERVICE)))";
        tnsAlias = "my_TNS";
        user = "my_login";
        password = "my_pwd";
        outputDir = "";
    }
}
