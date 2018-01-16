package org.aksw.dice.eaglet.errorutils;

import java.util.Comparator;

import edu.stanford.nlp.ling.CoreLabel;

public class Token_StartposbasedComparator implements Comparator<CoreLabel> {

	public int compare(CoreLabel a1, CoreLabel a2) {
		// TODO Auto-generated method stub
		int diff = a1.beginPosition() - a2.beginPosition();
		if (diff < 0) {
			return -1;
		} else if (diff > 0) {
			return 1;
		} else {
			return 0;
		}

	}
}
