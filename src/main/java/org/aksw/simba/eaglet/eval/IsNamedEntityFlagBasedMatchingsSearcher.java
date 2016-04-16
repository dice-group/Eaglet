package org.aksw.simba.eaglet.eval;

import java.util.List;

import org.aksw.gerbil.matching.MatchingsSearcher;
import org.aksw.simba.eaglet.entitytypemodify.EntityCheck;

import com.carrotsearch.hppc.BitSet;

public class IsNamedEntityFlagBasedMatchingsSearcher<T extends EntityCheck> implements MatchingsSearcher<T> {

    @Override
    public BitSet findMatchings(T expectedElement, List<T> annotatorResults, BitSet alreadyUsedResults) {
        BitSet matching = new BitSet(alreadyUsedResults.size());

        T annotatorResult;
        for (int i = 0; i < annotatorResults.size(); ++i) {
            if (!alreadyUsedResults.get(i)) {
                annotatorResult = annotatorResults.get(i);
                if (expectedElement.isNamedEntity() == annotatorResult.isNamedEntity()) {
                    matching.set(i);
                }
            }
        }
        return matching;
    }

}
