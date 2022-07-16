/* Copyright 2020 © Ministère de l'Enseignement Supérieur, de la Recherche et de
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
package fr.parcoursup.algos.donnees;

import fr.parcoursup.algos.exceptions.AccesDonneesException;
import fr.parcoursup.algos.exceptions.AccesDonneesExceptionMessage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Serialisation<T> {
    
    /* Sauvegarde des données au format xml.
    Si le paramètre filename est null, un nom par défaut est utilisé,
    paramétré par la date et l'heure.
     */
    public void serialiserEtCompresser(String filename, T o , Class c, int level) throws AccesDonneesException {
        serialiserEtCompresser(filename, o, new Class[] { c } , level);
    }
    
    public void serialiserEtCompresser(String filename, T o , Class[] c, int level) throws AccesDonneesException {
        if (filename == null) {
            filename = o.getClass().getSimpleName() + LocalDateTime.now().toString().replace(':','_') + ".xml";
        }
        String outfilename = filename + ".zip";

        try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                Files.newOutputStream(Paths.get(outfilename))))) {
            Marshaller m = JAXBContext.newInstance(c).createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            out.setMethod(ZipOutputStream.DEFLATED);
            out.setLevel(level);
            ZipEntry entry = new ZipEntry(filename);
            out.putNextEntry(entry);
            m.marshal(o, out);
        } catch (IOException ex) {
            throw new AccesDonneesException(AccesDonneesExceptionMessage.SERIALISATION_ENTREE_SORTIE, ex);
        } catch (JAXBException ex) {
            throw new AccesDonneesException(AccesDonneesExceptionMessage.SERIALISATION_SERIALISATION, ex);
        }

    }

    public void serialiserEtCompresser(T o , Class<T> c) throws AccesDonneesException {
        serialiserEtCompresser(null, o, c);
    }
    
    public void serialiserEtCompresser(String filename, T o , Class<T> c) throws AccesDonneesException {
        serialiserEtCompresser(filename, o , c, 9);
    }
    
    public T decompresserEtDeserialiser(String filename, Class<T> c) throws IOException, JAXBException {
        try (ZipInputStream in = new ZipInputStream(new BufferedInputStream(
                Files.newInputStream(Paths.get(filename))))) {
            Unmarshaller m = JAXBContext.newInstance(c).createUnmarshaller();
            in.getNextEntry();
            Object o = m.unmarshal(in);
            if(c.isInstance(o)) {
                return c.cast(o);
            } else {
                throw new ClassCastException("decompresserEtDeserialiser: type mismatch");
            }
        }
    }

    public T deserialiser(String filename,Class<T> c) throws IOException, JAXBException {
        try (BufferedInputStream in = new BufferedInputStream(
                Files.newInputStream(Paths.get(filename)))) {
            Unmarshaller m = JAXBContext.newInstance(c).createUnmarshaller();
            Object o = m.unmarshal(in);
            if(c.isInstance(o)) {
                return c.cast(o);
            } else {
                throw new ClassCastException("decompresserEtDeserialiser: type mismatch");
            }
        }
    }
}
