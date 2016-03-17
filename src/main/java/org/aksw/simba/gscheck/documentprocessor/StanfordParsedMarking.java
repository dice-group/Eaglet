package org.aksw.simba.gscheck.documentprocessor;

import org.aksw.gerbil.transfer.nif.Marking;

import edu.stanford.nlp.pipeline.Annotation;

public  class StanfordParsedMarking implements Marking {

    private Annotation annotation;

    public StanfordParsedMarking(Annotation annotation) {
        this.annotation = annotation;
    }

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