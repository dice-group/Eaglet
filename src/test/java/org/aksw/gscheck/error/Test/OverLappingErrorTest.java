package org.aksw.gscheck.error.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.matching.Matching;
import org.aksw.gerbil.semantic.kb.SimpleWhiteListBasedUriKBClassifier;
import org.aksw.gerbil.semantic.kb.UriKBClassifier;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gscheck.corrections.NamedEntityCorrections;
import org.aksw.gscheck.error.OverLappingError;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

public class OverLappingErrorTest {

	private static final String TEXTS[] = new String[] {
			"Florence May Harding studied at a school in Sydney, and with Douglas Robert Dundas , but in effect had no formal training in either botany or art.",
			"Such notables include James Carville, who was the senior political adviser to Bill Clinton, and Donna Brazile, the campaign manager of the 2000 presidential campaign of Vice-President Al Gore.",
			"The senator received a Bachelor of Laws from the Columbia University." };
	private static final DatasetConfiguration GOLD_STD = new NIFFileDatasetConfig("DBpedia",
			"gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl", false, ExperimentType.A2KB);
	private static final UriKBClassifier URI_KB_CLASSIFIER = new SimpleWhiteListBasedUriKBClassifier(
			"http://dbpedia.org/resource/");
	List<Document> doc = new ArrayList<Document>();

	@Before
	public void setUp() throws Exception {
		//NO error 
		doc.add(new DocumentImpl(TEXTS[0], "http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/sentence-1",
				Arrays.asList(
						(Marking) new NamedEntityCorrections(0, 20,
								"http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/Florence_May_Harding"),
						(Marking) new NamedEntityCorrections(34, 6,
								"http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/National_Art_School"),
						(Marking) new NamedEntityCorrections(44, 6,
								"http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/Sydney"),
						(Marking) new NamedEntityCorrections(61, 21,
								"http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/Douglas_Robert_Dundas"))));
		
		//Complete subset overlap
		
		doc.add(new DocumentImpl(TEXTS[1], "http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/sentence-2",
				Arrays.asList(
						(Marking) new NamedEntityCorrections(22, 14,
								"http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/James_Carville"),
						(Marking) new NamedEntityCorrections(57, 17,
								"http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/Political_adviser"),
						(Marking) new NamedEntityCorrections(78, 12,
								"http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/Bill_Clinton"),
						(Marking) new NamedEntityCorrections(78, 13,
								"http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/Donna_Brazile"),
						(Marking) new NamedEntityCorrections(115, 16,
								"http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/Campaign_manager"),
						(Marking) new NamedEntityCorrections(184, 7,
								"http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/Al_Gore"))));
		
		
		//Partial Overlap
		doc.add(new DocumentImpl(TEXTS[0], "http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/sentence-1",
				Arrays.asList((Marking) new NamedEntity(0, 20, "http://dbpedia.org/resource/Florence_May_Harding"),
						(Marking) new NamedEntityCorrections(10, 24, "http://dbpedia.org/resource/Sydney"))));
		
		
		doc.add(new DocumentImpl(TEXTS[1], "http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/sentence-2",
				Arrays.asList((Marking) new NamedEntity(22, 14, "http://dbpedia.org/resource/James_Carville"),
						(Marking) new NamedEntity(57, 17, "http://dbpedia.org/resource/Political_consulting"),
						(Marking) new NamedEntity(78, 12, "http://dbpedia.org/resource/Bill_Clinton"),
						(Marking) new NamedEntity(96, 13, "http://dbpedia.org/resource/Donna_Brazile"),
						(Marking) new NamedEntity(115, 16, "http://dbpedia.org/resource/Campaign_manager"),
						(Marking) new NamedEntity(184, 7, "http://dbpedia.org/resource/Al_Gore"))));
		//
		
		doc.add(new DocumentImpl(TEXTS[2], "http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/sentence-3",
				Arrays.asList((Marking) new NamedEntity(4, 7, "http://aksws.org/notInWiki/Senator_1"),
						(Marking) new NamedEntity(49, 19, "http://dbpedia.org/resource/Columbia_University"))));

	}

	@Test
	public void test() throws GerbilException {
		fail("Not yet implemented");
		OverLappingError test_var = new OverLappingError();
		test_var.check(doc);
		

	}

}
