package fr.parcoursup.algos.prod.test;

import fr.parcoursup.algos.prod.ExecutionParams;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;


@XmlRootElement
public class TestPropositionsConfig implements Serializable {

    public static final String DEFAULT_FILENAME = "params2.xml";

    public static final String ENV_VARIABLE = "PARAMS2";

    public final ExecutionParams input;

    public final ExecutionParams output;

    public TestPropositionsConfig() {
        input = new ExecutionParams();
        output = new ExecutionParams();
    }

    public static TestPropositionsConfig fromFile() throws IOException {
        String filename = DEFAULT_FILENAME;
        if (new File(filename).exists()) {
            try (FileInputStream fis = new FileInputStream(filename);
                 BufferedInputStream in = new BufferedInputStream(fis)
            ) {
                try {
                    Unmarshaller m = JAXBContext.newInstance(TestPropositionsConfig.class).createUnmarshaller();
                    return (TestPropositionsConfig) m.unmarshal(in);
                } catch (JAXBException ex) {
                    throw new RuntimeException("Veuillez corriger le contenu du fichier de config " + filename);
                }
            }
        } else {
            throw new FileNotFoundException(
                    "Le fichier de configuration " + filename + " est introuvable");
        }
    }

}
