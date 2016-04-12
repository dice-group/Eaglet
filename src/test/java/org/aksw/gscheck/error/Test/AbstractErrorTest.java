package org.aksw.gscheck.error.Test;

import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.simba.eaglet.documentprocessor.DocumentProcessor;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.aksw.simba.eaglet.error.CombinedTaggingError;
import org.aksw.simba.eaglet.error.ErrorChecker;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractErrorTest {

    protected static final String TEXTS[] = new String[] {
            "Florence May Harding studied at a school in Sydney, and with Douglas Robert Dundas , but in effect had no formal training in either botany or art.",
            "Such notables include James Carville, who was the senior political adviser to Bill Clinton, and Donna Brazile, the campaign manager of the 2000 presidential campaign of Vice-President Al Gore.",
            "The senator received a Bachelor of Laws from the Columbia University." };

    protected List<Document> doc = new ArrayList<Document>();
    protected List<Check[]> expectedResults = new ArrayList<Check[]>();
    protected List<NamedEntityCorrections[]> partner_list = new ArrayList<NamedEntityCorrections[]>();
    protected boolean preprocessingNeeded = false;
    protected ErrorChecker errorChecker;

    @Test
    public void test() throws GerbilException {
        if (preprocessingNeeded) {
            DocumentProcessor preprocessor = new DocumentProcessor();
            preprocessor.process(doc);
        }

        errorChecker.check(doc);

        List<NamedEntityCorrections> markings;
        Check[] expectedResult;

        for (int i = 0; i < doc.size(); i++) {
            markings = doc.get(i).getMarkings(NamedEntityCorrections.class);
            expectedResult = expectedResults.get(i);
            NamedEntityCorrections[] partner = partner_list.get(i);

            Assert.assertEquals("Got an unexpected number of results for document #" + i, expectedResult.length,
                    markings.size());

            for (int j = 0; j < markings.size(); j++) {
                Assert.assertEquals("Got an unexpected result for marking #" + j + " of document #" + i,
                        expectedResult[j], markings.get(j).getResult());
                Assert.assertEquals("Got an unexpected partner for marking #" + j + " of document #" + i, partner[j],
                        markings.get(j).getPartner());

            }
        }
    }
}
