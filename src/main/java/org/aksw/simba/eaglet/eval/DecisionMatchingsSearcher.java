package org.aksw.simba.eaglet.eval;

import java.util.List;

import org.aksw.gerbil.matching.MatchingsSearcher;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;

import com.carrotsearch.hppc.BitSet;

public class DecisionMatchingsSearcher implements MatchingsSearcher<NamedEntityCorrections> {

    @Override
    public BitSet findMatchings(NamedEntityCorrections expectedElement, List<NamedEntityCorrections> annotatorResults,
            BitSet alreadyUsedResults) {
        BitSet matching = new BitSet(alreadyUsedResults.size());
        for (int i = 0; i < annotatorResults.size(); ++i) {
            // If the user decision is matching and the result hasn't been used
            // before
            if ((!alreadyUsedResults.get(i))
                    && (expectedElement.getUserDecision() == annotatorResults.get(i).getUserDecision())) {
                matching.set(i);
            }
        }
        return matching;
    }

}
