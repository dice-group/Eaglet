package org.aksw.dice.eaglet.error;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.Correction;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.ErrorType;
import org.aksw.dice.eaglet.error.OverLappingError;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SubsetMarkingErrorTest {
	private static final String TEXTS[] = new String[] {
			"Florence May Harding studied at a school in Sydney, and with Douglas Robert Dundas , but in effect had no formal training in either botany or art.",
			"Such notables include James Carville, who was the senior political adviser to Bill Clinton, and Donna Brazile, the campaign manager of the 2000 presidential campaign of Vice-President Al Gore.",
			"The senator received a Bachelor of Laws from the Columbia University." };

	List<Document> doc = new ArrayList<Document>();
	List<ErrorType[]> expectedResults = new ArrayList<ErrorType[]>();

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

		expectedResults
				.add(new ErrorType[] { ErrorType.NOERROR, ErrorType.NOERROR, ErrorType.NOERROR, ErrorType.NOERROR });

		// Complete subset overlap

		doc.add(new DocumentImpl(TEXTS[1], "http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/sentence-2",
				Arrays.asList(
						(Marking) new NamedEntityCorrections(22, 14,
								"http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/James_Carville"),
						(Marking) new NamedEntityCorrections(57, 17,
								"http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/Political_adviser"),
						(Marking) new NamedEntityCorrections(78, 12,
								"http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/Bill_Clinton"),
						(Marking) new NamedEntityCorrections(80, 4,
								"http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/Donna_Brazile"),
						(Marking) new NamedEntityCorrections(115, 16,
								"http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/Campaign_manager"),
						(Marking) new NamedEntityCorrections(184, 7,
								"http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/Al_Gore"))));
		expectedResults.add(new ErrorType[] { ErrorType.NOERROR, ErrorType.NOERROR, ErrorType.OVERLAPPINGERR,
				ErrorType.OVERLAPPINGERR, ErrorType.NOERROR, ErrorType.NOERROR });

		// Partial Overlap
		doc.add(new DocumentImpl(TEXTS[0], "http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/sentence-1",
				Arrays.asList(
						(Marking) new NamedEntityCorrections(0, 20, "http://dbpedia.org/resource/Florence_May_Harding"),
						(Marking) new NamedEntityCorrections(0, 10, "http://dbpedia.org/resource/Sydney"))));

		expectedResults.add(new ErrorType[] { ErrorType.OVERLAPPINGERR, ErrorType.OVERLAPPINGERR });

	}

	@Test
	public void test() throws GerbilException {
		// fail("Not yet implemented");
		OverLappingError test_var = new OverLappingError();
		test_var.check(doc);

		List<NamedEntityCorrections> markings;
		ErrorType[] expectedResult;

		for (int i = 0; i < doc.size(); i++) {
			markings = doc.get(i).getMarkings(NamedEntityCorrections.class);
			expectedResult = expectedResults.get(i);
			// NamedEntityCorrections[] partner = partner_list.get(i);
			Assert.assertEquals(expectedResult.length, markings.size());
			// Assert.assertEquals(partner_list.get(i).length, markings.size());

			for (int j = 0; j < markings.size(); j++) {
				Assert.assertEquals("Error at marking #" + j + " in doc" + i, expectedResult[j],
						markings.get(j).getError());

				// Assert.assertEquals(partner[j],
				// markings.get(j).getPartner());

			}
		}
	}
}
