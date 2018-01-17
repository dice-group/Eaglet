package org.aksw.dice.eaglet.entitytypemodify;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aksw.gerbil.datatypes.marking.ClassifiedSpanMeaning;
import org.aksw.gerbil.datatypes.marking.MarkingClasses;
import org.aksw.gerbil.transfer.nif.Marking;

import com.carrotsearch.hppc.BitSet;

public class ClassifiedEntityCheck extends NamedEntityCorrections implements ClassifiedSpanMeaning {

	public ClassifiedEntityCheck(int startPosition, int length, Set<String> uris, DecisionValue des) {
		super(startPosition, length, uris, des);
	}

	public ClassifiedEntityCheck(NamedEntityCorrections marking) {

		this(marking.getStartPosition(), marking.getLength(), marking.getUris(), marking.getUserDecision());
	}

	protected BitSet classBits = new BitSet(MarkingClasses.NUMBER_OF_CLASSES);

	@Override
	public List<MarkingClasses> getClasses() {
		List<MarkingClasses> classes = new ArrayList<MarkingClasses>();
		for (int i = 0; i < MarkingClasses.NUMBER_OF_CLASSES; ++i) {
			if (classBits.get(i)) {
				classes.add(MarkingClasses.values()[i]);
			}
		}
		return classes;
	}

	@Override
	public boolean hasClass(MarkingClasses clazz) {
		return classBits.get(clazz.ordinal());
	}

	@Override
	public void setClass(MarkingClasses clazz) {
		classBits.set(clazz.ordinal());
	}

	@Override
	public void unsetClass(MarkingClasses clazz) {
		classBits.clear(clazz.ordinal());
	}

	@Override
	public Marking getWrappedMarking() {
		return this;
	}

}
