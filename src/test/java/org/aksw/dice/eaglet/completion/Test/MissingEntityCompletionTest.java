package org.aksw.dice.eaglet.completion.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.dice.eaglet.annotator.TestA2KBAnnotator;
import org.aksw.dice.eaglet.completion.MissingEntityCompletion;
import org.aksw.dice.eaglet.documentprocessor.DocumentProcessor;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.aksw.dice.eaglet.error.ErrorChecker;
import org.aksw.gerbil.annotator.A2KBAnnotator;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class MissingEntityCompletionTest {
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
				Arrays.asList(
						new DocumentImpl(TEXTS[0], "http://example.org/sentence-1",
								new ArrayList<Marking>(Arrays.asList(
										(Marking) new NamedEntityCorrections(0, 20,
												"http://example.org/Florence_May_Harding"),
										(Marking) new NamedEntityCorrections(34, 6,
												"http://example.org/National_Art_School"),
										(Marking) new NamedEntityCorrections(44, 6, "http://example.org/Sydney"),
										(Marking) new NamedEntityCorrections(82, 21,
												"http://example.org/Douglas_Robert_Dundas")))),
						new DocumentImpl(TEXTS[0], "http://example.org/sentence-2",
								new ArrayList<Marking>(Arrays.asList(
										(Marking) new NamedEntityCorrections(0, 20,
												"http://example.org/Florence_May_Harding"),
										(Marking) new NamedEntityCorrections(44, 6, "http://example.org/Sydney"))))),
				Arrays.asList(
						new Document[] { new DocumentImpl(TEXTS[0], "http://example.org/sentence-1",
								new ArrayList<Marking>(
										Arrays.asList((Marking) new NamedEntity(0, 20,
												"http://example.org/Florence_May_Harding")))) },
						new Document[] { new DocumentImpl(TEXTS[0], "http://example.org/sentence-2",
								new ArrayList<Marking>(Arrays.asList(
										(Marking) new NamedEntity(0, 20, "http://example.org/Florence_May_Harding"),
										(Marking) new NamedEntity(34, 6, "http://example.org/National_Art_School"),
										(Marking) new NamedEntity(44, 6, "http://example.org/Sydney"),
										(Marking) new NamedEntity(82, 21,
												"http://example.org/Douglas_Robert_Dundas")))) }),
				Arrays.asList(new DocumentImpl(TEXTS[0], "http://example.org/sentence-1",
						new ArrayList<Marking>(Arrays.asList(
								(Marking) new NamedEntityCorrections(0, 20, "http://example.org/Florence_May_Harding",
										Check.GOOD),
								(Marking) new NamedEntityCorrections(34, 6, "http://example.org/National_Art_School",
										Check.GOOD),
								(Marking) new NamedEntityCorrections(44, 6, "http://example.org/Sydney", Check.GOOD),
								(Marking) new NamedEntityCorrections(82, 21, "http://example.org/Douglas_Robert_Dundas",
										Check.GOOD)))),
						new DocumentImpl(TEXTS[0], "http://example.org/sentence-2",
								new ArrayList<Marking>(Arrays.asList(
										(Marking) new NamedEntityCorrections(0, 20,
												"http://example.org/Florence_May_Harding", Check.GOOD),
										(Marking) new NamedEntityCorrections(34, 6,
												"http://example.org/National_Art_School", Check.COMPLETED),
										(Marking) new NamedEntityCorrections(44, 6, "http://example.org/Sydney",
												Check.GOOD),
										(Marking) new NamedEntityCorrections(82, 21,
												"http://example.org/Douglas_Robert_Dundas", Check.COMPLETED))))),
				Arrays.asList(new NamedEntityCorrections[] { null, null, null, null },
						new NamedEntityCorrections[] { null, null }) });

		return testCases;
	}

	private List<Document> documents;
	private List<Document[]> annotationResults;
	private List<Document> expectedDocuments;

	public MissingEntityCompletionTest(List<Document> documents, List<Document[]> annotationResults,
			List<Document> expectedDocuments, List<NamedEntityCorrections[]> expectedPartners) {
		this.documents = documents;
		this.expectedDocuments = expectedDocuments;
		this.annotationResults = annotationResults;
	}

	@Test
	public void test() throws GerbilException {
		PREPROCESSOR.process(documents);
		// AnnotatorResult ar = new
		// AnnotatorResult("C:/Users/Kunal/workspace/gerbil/Results_anontator_dbpedia");
		// List<A2KBAnnotator> annotators = ar.getAnnotators();

		List<A2KBAnnotator> annotators = generateAnnotators();
		ErrorChecker test_var = new MissingEntityCompletion(annotators);
		test_var.check(documents);

		List<NamedEntityCorrections> markings, expectedMarkings;

		for (int i = 0; i < documents.size(); i++) {
			markings = documents.get(i).getMarkings(NamedEntityCorrections.class);
			expectedMarkings = expectedDocuments.get(i).getMarkings(NamedEntityCorrections.class);

			Assert.assertEquals(
					"The number of markings in document " + i + " (" + markings.toString()
							+ ") does not match the number of expected markings (" + expectedMarkings.toString() + ")",
					expectedMarkings.size(), markings.size());
			Set<NamedEntityCorrections> result = new HashSet<NamedEntityCorrections>(markings);
			for (int j = 0; j < expectedMarkings.size(); j++) {
				Assert.assertTrue("Couldn't find the expected " + expectedMarkings.get(j) + " inside the set "
						+ result.toString() + ".", result.contains(expectedMarkings.get(j)));
			}
		}
	}

	private List<A2KBAnnotator> generateAnnotators() {
		List<List<Document>> annotatorDocuments = new ArrayList<List<Document>>();
		List<Document> docsOfAnnotator;
		for (Document[] annotatorResults : annotationResults) {
			for (int i = 0; i < annotatorResults.length; ++i) {
				if (annotatorDocuments.size() <= i) {
					docsOfAnnotator = new ArrayList<Document>();
					annotatorDocuments.add(docsOfAnnotator);
				} else {
					docsOfAnnotator = annotatorDocuments.get(i);
				}
				docsOfAnnotator.add(annotatorResults[i]);
			}
		}
		List<A2KBAnnotator> annotators = new ArrayList<A2KBAnnotator>();
		for (List<Document> annotatorDocs : annotatorDocuments) {
			annotators.add(new TestA2KBAnnotator(annotatorDocs));
		}
		return annotators;
	}

}
