package org.aksw.gscheck.error.Test;

import java.util.Arrays;

import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.aksw.simba.eaglet.error.UriError;
import org.junit.Before;

public class InvalidUriErrorTest extends AbstractErrorTest {

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

    }

}
