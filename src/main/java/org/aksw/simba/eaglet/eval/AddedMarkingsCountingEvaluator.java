package org.aksw.simba.eaglet.eval;

import java.util.List;

import org.aksw.gerbil.evaluate.DoubleEvaluationResult;
import org.aksw.gerbil.evaluate.EvaluationResultContainer;
import org.aksw.gerbil.evaluate.Evaluator;
import org.aksw.gerbil.matching.EvaluationCounts;
import org.aksw.gerbil.matching.MatchingsCounter;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddedMarkingsCountingEvaluator implements Evaluator<NamedEntityCorrections> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddedMarkingsCountingEvaluator.class);

    protected MatchingsCounter<NamedEntityCorrections> matchingsCounter;

    public AddedMarkingsCountingEvaluator(MatchingsCounter<NamedEntityCorrections> matchingsCounter) {
        this.matchingsCounter = matchingsCounter;
    }

    @Override
    public void evaluate(List<List<NamedEntityCorrections>> annotatorResults,
            List<List<NamedEntityCorrections>> goldStandard, EvaluationResultContainer results) {
        EvaluationCounts counts[] = generateMatchingCounts(annotatorResults, goldStandard);
        int sum = 0;
        for (int i = 0; i < counts.length; ++i) {
            sum += counts[i].truePositives;
        }
        results.addResults(new DoubleEvaluationResult("Annotations added", sum));
    }

    protected EvaluationCounts[] generateMatchingCounts(List<List<NamedEntityCorrections>> annotatorResults,
            List<List<NamedEntityCorrections>> goldStandard) {
        EvaluationCounts counts[] = new EvaluationCounts[annotatorResults.size()];
        for (int i = 0; i < counts.length; ++i) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("doc " + i + "|||||||||");
            }
            counts[i] = matchingsCounter.countMatchings(annotatorResults.get(i), goldStandard.get(i));
        }
        return counts;
    }

}
