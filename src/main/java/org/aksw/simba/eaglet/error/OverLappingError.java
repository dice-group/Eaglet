package org.aksw.simba.eaglet.error;

import java.util.Collections;
import java.util.List;

import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverLappingError implements ErrorChecker {
	private static final Logger LOGGER = LoggerFactory.getLogger(ErraticEntityError.class);

	/*
	 * private static final DatasetConfiguration DATASET = new
	 * NIFFileDatasetConfig("DBpedia",
	 * "gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl", false,
	 * ExperimentType.A2KB);
	 */

	public void overlapcheck(List<Document> documents) throws GerbilException {
		// List<Document> documents =
		// DATASET.getDataset(ExperimentType.A2KB).getInstances();

		LOGGER.info(" OVERLAPPING ENTITY MODULE RUNNING");
		for (Document doc : documents) {

			List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);
			Collections.sort(entities, new StartPosBasedComparator());
			for (int i = 0; i < entities.size() - 1; i++) {
				if (entities.get(i).getResult().equals(Check.GOOD)) {

					if ((entities.get(i).getStartPosition() + entities.get(i).getLength()) >= entities.get(i + 1)
							.getStartPosition()) {

						entities.get(i).setResult(Check.OVERLAPS);

						entities.get(i).setPartner(entities.get(i + 1));

					}
				}

			}
		}
	}

	@Override
	public void check(List<Document> documents) throws GerbilException {
		// TODO Auto-generated method stub
		this.overlapcheck(documents);

	}

}
