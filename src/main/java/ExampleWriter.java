import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.io.nif.NIFWriter;
import org.aksw.gerbil.io.nif.impl.TurtleNIFWriter;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.apache.commons.io.IOUtils;

public class ExampleWriter {

    public static void main(String[] args) {
        List<Document> documents = new ArrayList<>();

        Document document = new DocumentImpl(
                "In December 2012, Seattle qualified for the MLS Cup Playoffs in each of its first five seasons.",
                "http://example.org/Example1FromThePaper");
        document.addMarking(new NamedEntity(3, 8, "http://dbpedia.org/resource/December"));
        document.addMarking(new NamedEntity(12, 4, "http://dbpedia.org/resource/2012"));
        document.addMarking(new NamedEntity(18, 7, "http://dbpedia.org/resource/Seattle"));
        document.addMarking(new NamedEntity(44, 7, "http://dbpedia.org/resource/MLS_Cup"));
        document.addMarking(new NamedEntity(48, 12, "http://dbpedia.org/resource/Playoffs"));
        documents.add(document);

        document = new DocumentImpl(
                "John played football for Seattle after he spent a semester in China. Such a team that won Supporters' Shield in 2014 boosts players like him in their career.",
                "http://example.org/Example2FromThePaper");
        document.addMarking(new NamedEntity(12, 4, "http://dbpedia.org/resource/Foot"));
        document.addMarking(new NamedEntity(62, 5, "http://dbpedia.org/resource/People's_Republic_of_China"));
        document.addMarking(new NamedEntity(76, 40, "http://dbpedia.org/resource/Seattle_Sounders_FC"));
        documents.add(document);

        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream("example.ttl");
            NIFWriter writer = new TurtleNIFWriter();
            writer.writeNIF(documents, fout);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(fout);
        }

    }
}
