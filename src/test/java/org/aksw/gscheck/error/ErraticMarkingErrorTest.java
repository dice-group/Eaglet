package org.aksw.gscheck.error;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.simba.eaglet.documentprocessor.DocumentProcessor;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.aksw.simba.eaglet.error.ErraticEntityError;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ErraticMarkingErrorTest {
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
                                new ArrayList<Marking>(Arrays.asList((Marking) new NamedEntityCorrections(0, 20,
                                        "http://example.org/Florence_May_Harding"),
                                (Marking) new NamedEntityCorrections(34, 6, "http://example.org/National_Art_School"),
                                (Marking) new NamedEntityCorrections(44, 6, "http://example.org/Sydney"),
                                (Marking) new NamedEntityCorrections(82, 21,
                                        "http://example.org/Douglas_Robert_Dundas")))),
                new DocumentImpl(TEXTS[0], "http://example.org/sentence-2",
                        new ArrayList<Marking>(Arrays.asList(
                                (Marking) new NamedEntityCorrections(0, 20, "http://example.org/Florence_May_Harding"),
                                (Marking) new NamedEntityCorrections(44, 6, "http://example.org/Sydney"))))),
                Arrays.asList(
                        new DocumentImpl(TEXTS[0], "http://example.org/sentence-1",
                                new ArrayList<Marking>(Arrays.asList(
                                        (Marking) new NamedEntityCorrections(0, 20,
                                                "http://example.org/Florence_May_Harding", Check.GOOD),
                                        (Marking) new NamedEntityCorrections(34, 6,
                                                "http://example.org/National_Art_School", Check.GOOD),
                                (Marking) new NamedEntityCorrections(44, 6, "http://example.org/Sydney", Check.GOOD),
                                (Marking) new NamedEntityCorrections(82, 21, "http://example.org/Douglas_Robert_Dundas",
                                        Check.GOOD)))),
                        new DocumentImpl(TEXTS[0], "http://example.org/sentence-2",
                                new ArrayList<Marking>(Arrays.asList(
                                        (Marking) new NamedEntityCorrections(0, 20,
                                                "http://example.org/Florence_May_Harding", Check.GOOD),
                                        (Marking) new NamedEntityCorrections(34, 6,
                                                "http://example.org/National_Art_School", Check.INSERTED),
                                        (Marking) new NamedEntityCorrections(44, 6, "http://example.org/Sydney",
                                                Check.GOOD),
                                        (Marking) new NamedEntityCorrections(82, 21,
                                                "http://example.org/Douglas_Robert_Dundas", Check.INSERTED))))),
                Arrays.asList(new NamedEntityCorrections[] { null, null, null, null },
                        new NamedEntityCorrections[] { null, null }) });

        // Multiple entity combine
        // testCases.add(new Object[] {
        // Arrays.asList(new DocumentImpl(TEXTS[1],
        // "http://example.org/sentence-2",
        // new ArrayList<Marking>(Arrays.asList( (Marking) new
        // NamedEntityCorrections(22, 14,
        // "http://example.org/James_Carville"
        // ), (Marking) new NamedEntityCorrections(50, 6,
        // "http://example.org/senior"
        // ), (Marking) new NamedEntityCorrections(57, 9,
        // "http://example.org/Political"
        // ), (Marking) new NamedEntityCorrections(67, 7,
        // "http://example.org/adviser"
        // ), (Marking) new NamedEntityCorrections(78, 12,
        // "http://example.org/Bill_Clinton"
        // ), (Marking) new NamedEntityCorrections(78, 13,
        // "http://example.org/Donna_Brazile"
        // ), (Marking) new NamedEntityCorrections(115, 16,
        // "http://example.org/Campaign_manager"
        // ), (Marking) new NamedEntityCorrections(184, 7,
        // "http://example.org/Al_Gore"
        // ))))); expectedResults.add(new Check[] { Check.GOOD, Check.GOOD,
        // Check.NEED_TO_PAIR, Check.NEED_TO_PAIR, Check.GOOD, Check.GOOD,
        // Check.GOOD, Check.GOOD }); partner_list.add(new
        // NamedEntityCorrections[] { null, null, new NamedEntityCorrections(50,
        // 6,
        // "http://example.org/senior"
        // ), new NamedEntityCorrections(57, 9,
        // "http://example.org/Political"
        // ),
        // null, null, null, null });

        // // No error
        // testCases.add(new Object[] {
        // Arrays.asList(new DocumentImpl(TEXTS[0],
        // "http://example.org/sentence-1",
        // new ArrayList<Marking>(Arrays.asList(
        // (Marking) new NamedEntityCorrections(0, 20,
        // "http://dbpedia.org/resource/Florence_May_Harding"),
        // (Marking) new NamedEntityCorrections(44, 6,
        // "http://dbpedia.org/resource/Sydney"))))),
        // Arrays.asList(new DocumentImpl(TEXTS[0],
        // "http://example.org/sentence-1",
        // new ArrayList<Marking>(Arrays.asList(
        // (Marking) new NamedEntityCorrections(0, 12,
        // "http://example.org/Florence_May_Harding",
        // Check.GOOD),
        // (Marking) new NamedEntityCorrections(34, 6,
        // "http://example.org/National_Art_School",
        // Check.INSERTED),
        // (Marking) new NamedEntityCorrections(44, 6,
        // "http://example.org/Sydney",
        // Check.GOOD),
        // (Marking) new NamedEntityCorrections(61, 21,
        // "http://example.org/Douglas_Robert_Dundas",
        // Check.INSERTED))))),
        // Arrays.asList(new NamedEntityCorrections[] { null, null }) });

        // Multiple overlap
        // doc.add(new DocumentImpl(TEXTS[1],
        // "http://example.org/sentence-2",
        // Arrays.asList( (Marking) new NamedEntityCorrections(22, 14,
        // "http://dbpedia.org/resource/James_Carville"), (Marking) new
        // NamedEntityCorrections(26, 17,
        // "http://dbpedia.org/resource/Political_consulting"), (Marking) new
        // NamedEntityCorrections(29, 12,
        // "http://dbpedia.org/resource/Bill_Clinton"), (Marking) new
        // NamedEntityCorrections(96, 13,
        // "http://dbpedia.org/resource/Donna_Brazile"), (Marking) new
        // NamedEntityCorrections(115, 16,
        // "http://dbpedia.org/resource/Campaign_manager"), (Marking) new
        // NamedEntityCorrections(184, 7,
        // "http://dbpedia.org/resource/Al_Gore"))));
        //
        // expectedResults.add(new Check[] { Check.GOOD, Check.GOOD, Check.GOOD,
        // Check.GOOD, Check.GOOD, Check.GOOD });
        //
        // partner_list.add(new NamedEntityCorrections[] { null, null, null,
        // null, null, null });

        /*
         * doc.add(new DocumentImpl(TEXTS[2], "http://example.org/sentence-3",
         * Arrays.asList((Marking) new NamedEntity(4, 7,
         * "http://aksws.org/notInWiki/Senator_1"), (Marking) new
         * NamedEntity(49, 19,
         * "http://dbpedia.org/resource/Columbia_University"))));
         */

        return testCases;
    }

    private List<Document> documents;
    private List<Document> expectedDocuments;
    private List<NamedEntityCorrections[]> expectedPartners = new ArrayList<NamedEntityCorrections[]>();

    public ErraticMarkingErrorTest(List<Document> documents, List<Document> expectedDocuments,
            List<NamedEntityCorrections[]> expectedPartners) {
        this.documents = documents;
        this.expectedDocuments = expectedDocuments;
        this.expectedPartners = expectedPartners;
    }

    @Test
    public void test() throws GerbilException {
        PREPROCESSOR.process(documents);
        ErraticEntityError test_var = new ErraticEntityError();
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
