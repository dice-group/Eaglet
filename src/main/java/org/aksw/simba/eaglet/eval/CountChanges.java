package org.aksw.simba.eaglet.eval;

import java.util.List;

import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;

public class CountChanges {

	private static final Logger LOGGER = LoggerFactory.getLogger(CountChanges.class);

	public static void countchanges(List<Document> documents, String name) {
		ObjectIntOpenHashMap<Check> checkCounts = new ObjectIntOpenHashMap<Check>();
		for (Document doc : documents) {
			List<NamedEntityCorrections> markings = doc.getMarkings(NamedEntityCorrections.class);
			for (NamedEntityCorrections nec : markings) {
				checkCounts.putOrAdd(nec.getResult(), 1, 1);
				if (!nec.getResult().equals(Check.GOOD)&&(nec.getStartPosition()>0))
					System.out.println(" ERROR ENTITY::" + doc.getText().substring(nec.getStartPosition()-1,
							nec.getStartPosition() + nec.getLength()));
				else if (!nec.getResult().equals(Check.GOOD))
						{
					System.out.println(" ERROR ENTITY::" + doc.getText().substring(nec.getStartPosition(),
							nec.getStartPosition() + nec.getLength()));
						}
			}
		}
		LOGGER.error("Dataset : " + name);
		for (int i = 0; i < checkCounts.allocated.length; ++i) {
			if (checkCounts.allocated[i]) {
				LOGGER.error("Total number of " + ((Object[]) checkCounts.keys)[i] + ": " + checkCounts.values[i]);
			}
		}
	}
}
