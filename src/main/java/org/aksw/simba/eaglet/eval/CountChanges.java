package org.aksw.simba.eaglet.eval;

import java.util.List;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.Check;

public class CountChanges {


	public static void countchanges(List<Document> documents, String name) {
		int count;
		int sum = 0;
		System.out.println(" Dataset : " + name);
		for (Document doc : documents) {
			List<NamedEntityCorrections> markings = doc.getMarkings(NamedEntityCorrections.class);
			count = 0;
			for (NamedEntityCorrections nec : markings) {
				if (!nec.getResult().equals(Check.GOOD)) {
					count++;
					System.out.println(doc.getText().substring(nec.getStartPosition(),
							nec.getStartPosition() + nec.getLength()));
				}
			}
			sum += count;
		}
		System.out.println("Total number of corrections: " + sum);
	}
}
