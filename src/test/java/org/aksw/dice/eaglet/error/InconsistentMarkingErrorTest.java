package org.aksw.dice.eaglet.error;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.dice.eaglet.documentprocessor.DocumentProcessor;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.Correction;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.ErrorType;
import org.aksw.dice.eaglet.error.InconsistentMarkingError;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class InconsistentMarkingErrorTest {
	private static final String TEXTS[] = new String[] {
			"Florence May Harding studied at a school in Sydney that is in Australia, and with Douglas Robert Dundas, but in effect had no formal training in either botany or art.",
			"Such notables include James Carville, who was the senior political adviser to Bill Clinton, and Donna Brazile, the campaign manager of the 2000 presidential campaign of Vice-President Al Gore.",
			"The senator received a Bachelor of Laws from the Columbia University." };

	private static final DocumentProcessor PREPROCESSOR = new DocumentProcessor();

	@Parameters
	public static Collection<Object[]> data() {
		List<Object[]> testCases = new ArrayList<Object[]>();
		// two documents. In the second document, two entities are missing that
		// have been marked in the first document.
		testCases.add(new Object[] {
				Arrays.asList(new DocumentImpl(TEXTS[0], "http://example.org/sentence-1",
						new ArrayList<Marking>(Arrays.asList(
								(Marking) new NamedEntityCorrections(0, 20, "http://example.org/Florence_May_Harding"),
								(Marking) new NamedEntityCorrections(34, 6, "http://example.org/National_Art_School"),
								(Marking) new NamedEntityCorrections(44, 6, "http://example.org/Sydney"),
								(Marking) new NamedEntityCorrections(82, 21,
										"http://example.org/Douglas_Robert_Dundas")))),
						new DocumentImpl(TEXTS[0], "http://example.org/sentence-2",
								new ArrayList<Marking>(Arrays.asList(
										(Marking) new NamedEntityCorrections(0, 20,
												"http://example.org/Florence_May_Harding"),
										(Marking) new NamedEntityCorrections(44, 6, "http://example.org/Sydney"))))),
				Arrays.asList(
						new DocumentImpl(
								TEXTS[0], "http://example.org/sentence-1",
								new ArrayList<Marking>(Arrays.asList(
										(Marking) new NamedEntityCorrections(0, 20,
												"http://example.org/Florence_May_Harding", ErrorType.NOERROR),
										(Marking) new NamedEntityCorrections(34, 6,
												"http://example.org/National_Art_School", ErrorType.NOERROR),
										(Marking) new NamedEntityCorrections(44, 6, "http://example.org/Sydney",
												ErrorType.NOERROR),
										(Marking) new NamedEntityCorrections(82, 21,
												"http://example.org/Douglas_Robert_Dundas", ErrorType.NOERROR)))),
						new DocumentImpl(TEXTS[0], "http://example.org/sentence-2",
								new ArrayList<Marking>(Arrays.asList(
										(Marking) new NamedEntityCorrections(0, 20,
												"http://example.org/Florence_May_Harding", ErrorType.NOERROR),
										(Marking) new NamedEntityCorrections(34, 6,
												"http://example.org/National_Art_School",
												ErrorType.INCONSITENTMARKINGERR),
										(Marking) new NamedEntityCorrections(44, 6, "http://example.org/Sydney",
												ErrorType.NOERROR),
										(Marking) new NamedEntityCorrections(82, 21,
												"http://example.org/Douglas_Robert_Dundas", ErrorType.NOERROR))))),
				Arrays.asList(new NamedEntityCorrections[] { null, null, null, null },
						new NamedEntityCorrections[] { null, null }) });

		return testCases;
	}

	private List<Document> documents;
	private List<Document> expectedDocuments;
	private List<NamedEntityCorrections[]> expectedPartners = new ArrayList<NamedEntityCorrections[]>();

	public InconsistentMarkingErrorTest(List<Document> documents, List<Document> expectedDocuments,
			List<NamedEntityCorrections[]> expectedPartners) {
		this.documents = documents;
		this.expectedDocuments = expectedDocuments;
		this.expectedPartners = expectedPartners;
	}

	@Test
	public void test() throws GerbilException {
		PREPROCESSOR.process(documents);
		InconsistentMarkingError test_var = new InconsistentMarkingError();
		test_var.check(documents);

		List<NamedEntityCorrections> markings, expectedMarkings;

		for (int i = 0; i < documents.size(); i++) {
			markings = documents.get(i).getMarkings(NamedEntityCorrections.class);
			expectedMarkings = expectedDocuments.get(i).getMarkings(NamedEntityCorrections.class);

			Assert.assertEquals(
					"The number of markings (" + markings.toString()
							+ ") does not match the number of expected markings (" + expectedMarkings.toString() + ")",
					expectedMarkings.size(), markings.size());
			Set<NamedEntityCorrections> result = new HashSet<NamedEntityCorrections>(markings);
			for (int j = 0; j < expectedMarkings.size(); j++) {
				Assert.assertTrue("Couldn't find the expected " + expectedMarkings.get(j) + " inside the set "
						+ result.toString() + ".", result.contains(expectedMarkings.get(j)));
			}
		}
	}

}
