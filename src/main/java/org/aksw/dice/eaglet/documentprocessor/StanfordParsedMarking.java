package org.aksw.dice.eaglet.documentprocessor;

import org.aksw.gerbil.transfer.nif.Marking;

import edu.stanford.nlp.pipeline.Annotation;

/**
 * This class is a modified marking containing the Stanford NLP information.
 *
 * @author Kunal
 * @author Michael
 *
 */
public class StanfordParsedMarking implements Marking {

	private Annotation annotation;

	public StanfordParsedMarking(Annotation annotation) {
		this.annotation = annotation;
	}

	@Override
	public Object clone() {
		return null;
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}
}