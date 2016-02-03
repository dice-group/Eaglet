package org.aksw.gscheck.error;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;

public class CombinedTaggingError {
	private static final DatasetConfiguration DATASET = new NIFFileDatasetConfig("DBpedia",
			"C:/Users/Kunal/workspace/gerbil/gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl", false,
			ExperimentType.A2KB);

	static String text, text1;

	public static void main(String[] args) throws GerbilException {
		List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();
		int j = 0;

		List<String> result = new ArrayList<String>();
		for (Document doc : documents) {
			text = doc.getText();
			List<NamedEntity> entities = doc.getMarkings(NamedEntity.class);
			NamedEntity a, b;
			boolean s = false;
			StringBuffer sb = new StringBuffer();
			for (NamedEntity entity : entities) {
				int i = entities.indexOf(entity);
				if (i < entities.size()) {
					text1 = text.substring(entity.getStartPosition() + entity.getLength(),
							entities.get(i + 1).getStartPosition());
					if (text1.equals("//s")) {
						sb.append(text.substring(entity.getStartPosition(),
								entities.get(i + 1).getStartPosition() + entities.get(i + 1).getLength()));

						s = true;

					} else {
						s = false;
					}

				}

				text1 = "";
				
				if(s==true)
				{
					result.add(sb.toString());
					sb.delete(0,sb.length());
				}
			}

		}
		System.out.println(result);
	}

}
