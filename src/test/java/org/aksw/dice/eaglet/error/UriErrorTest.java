package org.aksw.dice.eaglet.error;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.dice.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.aksw.dice.eaglet.error.UriError;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;

public class UriErrorTest extends AbstractErrorTest {

    @Before
    public void setUp() throws Exception {
        preprocessingNeeded = false;
        errorChecker = new UriError();
        // NO error
        doc.add(new DocumentImpl(TEXTS[0], "http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/sentence-1",
                Arrays.asList(
                        (Marking) new NamedEntityCorrections(0, 20,
                                "http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/Florence_May_Harding"),
                        (Marking) new NamedEntityCorrections(34, 6,
                                "http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/National_Art_School"),
                        (Marking) new NamedEntityCorrections(44, 6, "http://dbpedia.org/resource/Sydney"),
                        (Marking) new NamedEntityCorrections(61, 21,
                                "http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/Douglas_Robert_Dundas"))));

        expectedResults.add(new Check[] { Check.GOOD, Check.GOOD, Check.GOOD, Check.GOOD });
        partner_list.add(new NamedEntityCorrections[] { null, null, null, null });

        // some wrong entities
        doc.add(new DocumentImpl(TEXTS[1], "http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/sentence-2",
                Arrays.asList((Marking) new NamedEntityCorrections(22, 14, ""),
                        (Marking) new NamedEntityCorrections(57, 17, "Political_adviser"),
                        (Marking) new NamedEntityCorrections(78, 12, "*null*"),
                        (Marking) new NamedEntityCorrections(80, 4,
                                "http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/Donna_Brazile"),
                        (Marking) new NamedEntityCorrections(115, 16,
                                "http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/Campaign_manager"),
                        (Marking) new NamedEntityCorrections(184, 7,
                                "http://dbpedia.org/resource/Al_the_one_and_only_Gore"))));
        expectedResults.add(new Check[] { Check.INVALID_URI, Check.INVALID_URI, Check.INVALID_URI, Check.GOOD,
                Check.GOOD, Check.INVALID_URI });
        partner_list.add(new NamedEntityCorrections[] { null, null, null, null, null, null });

        // a URI that points to a disambiguation page
        doc.add(new DocumentImpl("The delay was short.",
                "http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/sentence-3",
                Arrays.asList((Marking) new NamedEntityCorrections(4, 5, "http://dbpedia.org/resource/Delay"))));
        expectedResults.add(new Check[] { Check.DISAMBIG_URI });
        partner_list.add(new NamedEntityCorrections[] { null });

        // an outdated URI
        doc.add(new DocumentImpl("China is a large country.",
                "http://www.ontologydesignpatterns.org/data/oke-challenge/task-1/sentence-4",
                Arrays.asList((Marking) new NamedEntityCorrections(0, 5, "http://dbpedia.org/resource/People's_Republic_of_China"))));
        expectedResults.add(new Check[] { Check.OUTDATED_URI });
        partner_list.add(new NamedEntityCorrections[] { null });
    }

    @After
    public void close() throws IOException {
        IOUtils.closeQuietly((Closeable) errorChecker);
    }
}
