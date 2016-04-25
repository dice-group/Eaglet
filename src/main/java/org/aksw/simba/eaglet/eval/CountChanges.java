package org.aksw.simba.eaglet.eval;

import java.util.List;

import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.Check;

public class CountChanges {

    public static void countchanges(List<Document> documents) {
        int count;
        int sum = 0;
        for (Document doc : documents) {
            List<NamedEntityCorrections> markings = doc.getMarkings(NamedEntityCorrections.class);
            count = 0;
            for (NamedEntityCorrections nec : markings) {
                if (!nec.getResult().equals(Check.GOOD)) {
                    count++;
                }
            }
            System.out.println("THE NUMBER OF CORRECTIONS for doc " + doc.getDocumentURI() + " is " + count);
            sum += count;
        }
        System.out.println("Total number of corrections: " + sum);
    }
}
