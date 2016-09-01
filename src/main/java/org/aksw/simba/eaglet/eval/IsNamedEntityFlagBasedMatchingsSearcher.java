package org.aksw.simba.eaglet.eval;

import java.util.List;

import org.aksw.gerbil.matching.MatchingsSearcher;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;

import com.carrotsearch.hppc.BitSet;

@Deprecated
public class IsNamedEntityFlagBasedMatchingsSearcher<T extends NamedEntityCorrections> implements MatchingsSearcher<T> {

    @Override
    public BitSet findMatchings(T expectedElement, List<T> annotatorResults, BitSet alreadyUsedResults) {
        BitSet matching = new BitSet(alreadyUsedResults.size());

//        T annotatorResult;
//        for (int i = 0; i < annotatorResults.size(); ++i) {
//            if (!alreadyUsedResults.get(i)) {
//                annotatorResult = annotatorResults.get(i);
//                if (expectedElement.isNamedEntity() == annotatorResult.isNamedEntity()) {
//                    matching.set(i);
//                }
//            }
//        }
        return matching;
    }

}
