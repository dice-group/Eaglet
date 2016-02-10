import java.util.List;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.check.EntityCheckerManagerImpl;
import org.aksw.gerbil.dataset.check.HttpBasedEntityChecker;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Meaning;
import org.aksw.gerbil.transfer.nif.data.Annotation;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;

public class uricheck {

	private static final DatasetConfiguration DATASET = new NIFFileDatasetConfig("DBpedia",
			"C:/Users/Kunal/workspace/gerbil/gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl", false,
			ExperimentType.A2KB);

	public static void main(String[] args) throws GerbilException {
		EntityCheckerManagerImpl manager = new EntityCheckerManagerImpl();
		HttpBasedEntityChecker checker = new HttpBasedEntityChecker();
		manager.registerEntityChecker("http://dbpedia.org/resource", checker);
		

		List<Document> documents = DATASET.getDataset(ExperimentType.A2KB).getInstances();
		for (Document doc : documents) {
			List<NamedEntity> entities = doc.getMarkings(NamedEntity.class);
			for (NamedEntity entity : entities) 
			{
				Meaning m = new Annotation(entity.getUri());
				manager.checkMeaning(m);
				
				
			}

		}

	}
}
