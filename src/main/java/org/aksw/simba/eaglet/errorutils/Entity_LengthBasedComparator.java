package org.aksw.simba.eaglet.errorutils;

import java.util.Comparator;

import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;

import edu.stanford.nlp.ling.CoreLabel;

public class Entity_LengthBasedComparator implements Comparator<NamedEntityCorrections> {

	public int compare(NamedEntityCorrections o1, NamedEntityCorrections o2) {
		// TODO Auto-generated method stub
		int diff = o1.getNumber_of_lemma() - o2.getNumber_of_lemma();
		if (diff < 0) {
			return -1;
		} else if (diff > 0) {
			return 0;
		} else {
			return 1;
		}
	}
}
