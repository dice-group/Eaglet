package org.aksw.simba.eaglet.eval;

import java.util.List;

import org.aksw.gerbil.evaluate.DoubleEvaluationResult;
import org.aksw.gerbil.evaluate.EvaluationResultContainer;
import org.aksw.gerbil.evaluate.Evaluator;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.DecisionValue;

import com.carrotsearch.hppc.BitSet;

public class CorrectnessAccuracy implements Evaluator<NamedEntityCorrections> {

    @Override
    public void evaluate(List<List<NamedEntityCorrections>> annotatorResults,
            List<List<NamedEntityCorrections>> goldStandard, EvaluationResultContainer results) {
        int size = 0, correctCount = 0;
        DecisionMatchingsSearcher searcher = new DecisionMatchingsSearcher();
        BitSet matchingElements;
        for (int i = 0; i < goldStandard.size(); ++i) {
            List<NamedEntityCorrections> docGoldStandard = goldStandard.get(i);
            List<NamedEntityCorrections> docAnnotatorResults = goldStandard.get(i);
            BitSet alreadyUsedResults = new BitSet(docAnnotatorResults.size());
            for (NamedEntityCorrections expectedElement : docGoldStandard) {
                matchingElements = searcher.findMatchings(expectedElement, docAnnotatorResults, alreadyUsedResults);
                if (!matchingElements.isEmpty()) {
                    alreadyUsedResults.set(matchingElements.nextSetBit(0));
                    if (expectedElement.getUserDecision() == DecisionValue.CORRECT) {
                        ++correctCount;
                    }
                }
                ++size;
            }
            size += docAnnotatorResults.size() - alreadyUsedResults.cardinality();
        }
        results.addResult(new DoubleEvaluationResult("Micro Correctness (%)", (double) correctCount / (double) size));
    }

}
