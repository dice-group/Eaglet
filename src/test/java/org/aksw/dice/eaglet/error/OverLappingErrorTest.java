package org.aksw.dice.eaglet.error;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.Correction;
import org.aksw.dice.eaglet.error.OverLappingError;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class OverLappingErrorTest {

	private static final String TEXTS[] = new String[] {
			"Florence May Harding studied at a school in Sydney, and with Douglas Robert Dundas , but in effect had no formal training in either botany or art.",
			"Such notables include James Carville, who was the senior political adviser to Bill Clinton, and Donna Brazile, the campaign manager of the 2000 presidential campaign of Vice-President Al Gore.",
			"The senator received a Bachelor of Laws from the Columbia University." };
	List<Document> doc = new ArrayList<Document>();
	List<Correction[]> expectedResults = new ArrayList<Correction[]>();
	List<NamedEntityCorrections[]> partner_list = new ArrayList<NamedEntityCorrections[]>();

	@Before
	public void setUp() throws Exception {
		// NO error
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
		expectedResults.add(new Correction[] { Correction.GOOD, Correction.GOOD, Correction.GOOD, Correction.GOOD });
		partner_list.add(new NamedEntityCorrections[] { null, null, null, null });

		// Complete subset overlap
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
		expectedResults
				.add(new Correction[] { Correction.GOOD, Correction.GOOD, Correction.OVERLAPS, Correction.OVERLAPS, Correction.GOOD, Correction.GOOD });

		partner_list.add(new NamedEntityCorrections[] { null, null, new NamedEntityCorrections(78, 13,
				"http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/Donna_Brazile", Correction.OVERLAPS), null,
				null, null });

		// Partial Overlap
				doc.add(new DocumentImpl(TEXTS[0], "http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/sentence-1",
						Arrays.asList(
								(Marking) new NamedEntityCorrections(0, 20, "http://dbpedia.org/resource/Florence_May_Harding"),
								(Marking) new NamedEntityCorrections(10, 24, "http://dbpedia.org/resource/Sydney"))));
				partner_list.add(new NamedEntityCorrections[] {
						new NamedEntityCorrections(10, 24, "http://dbpedia.org/resource/Sydney", Correction.OVERLAPS), null });

				expectedResults.add(new Correction[] { Correction.OVERLAPS, Correction.OVERLAPS });

				// Multiple overlap
				doc.add(new DocumentImpl(TEXTS[1], "http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/sentence-2",
						Arrays.asList(
								(Marking) new NamedEntityCorrections(22, 14, "http://dbpedia.org/resource/James_Carville"),
								(Marking) new NamedEntityCorrections(26, 17,
										"http://dbpedia.org/resource/Political_consulting"),
								(Marking) new NamedEntityCorrections(29, 12, "http://dbpedia.org/resource/Bill_Clinton"),
								(Marking) new NamedEntityCorrections(96, 13, "http://dbpedia.org/resource/Donna_Brazile"),
								(Marking) new NamedEntityCorrections(115, 16, "http://dbpedia.org/resource/Campaign_manager"),
								(Marking) new NamedEntityCorrections(184, 7, "http://dbpedia.org/resource/Al_Gore"))));

				expectedResults.add(
						new Correction[] { Correction.OVERLAPS, Correction.OVERLAPS, Correction.OVERLAPS, Correction.OVERLAPS, Correction.GOOD, Correction.GOOD });

				partner_list
						.add(new NamedEntityCorrections[] {
								new NamedEntityCorrections(26, 17, "http://dbpedia.org/resource/Political_consulting",
										Correction.OVERLAPS,
										new NamedEntityCorrections(29, 12, "http://dbpedia.org/resource/Bill_Clinton",
												Correction.OVERLAPS)),
								new NamedEntityCorrections(29, 12, "http://dbpedia.org/resource/Donna_Brazile", Correction.OVERLAPS),
								null, null, null, null });

				/*
				 * doc.add(new DocumentImpl(TEXTS[2],
				 * "http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/sentence-3",
				 * Arrays.asList((Marking) new NamedEntity(4, 7,
				 * "http://aksws.org/notInWiki/Senator_1"), (Marking) new NamedEntity(49, 19,
				 * "http://dbpedia.org/resource/Columbia_University"))));
				 */

	}

	@Test
	public void test() throws GerbilException {
		// fail("Not yet implemented");
		OverLappingError test_var = new OverLappingError();
		test_var.check(doc);

		List<NamedEntityCorrections> markings;
		Correction[] expectedResult;

		for (int i = 0; i < doc.size(); i++) {
			markings = doc.get(i).getMarkings(NamedEntityCorrections.class);
			expectedResult = expectedResults.get(i);
			NamedEntityCorrections[] partner = partner_list.get(i);
			Assert.assertEquals(expectedResult.length, markings.size());
			Assert.assertEquals(partner_list.get(i).length, markings.size());

			for (int j = 0; j < markings.size(); j++) {
				Assert.assertEquals(expectedResult[j], markings.get(j).getResult());
				Assert.assertEquals(partner[j], markings.get(j).getPartner());

			}
		}
	}

}
