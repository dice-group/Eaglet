package org.aksw.simba.eaglet.eval;

import java.util.List;

import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.Check;

public class CountChanges {
	static int count;

	public static void countchanges(List<Document> documents) {
		for (Document doc : documents) {
			List<NamedEntityCorrections> markings = doc.getMarkings(NamedEntityCorrections.class);
			for (NamedEntityCorrections nec : markings) {
				if (!nec.getResult().equals(Check.GOOD)) {
					count++;
				}
			}
			System.out.println("THE NUMBER OF CORRECTIONS for doc " + doc.getDocumentURI() + " is " + count);
		}
	}
}
