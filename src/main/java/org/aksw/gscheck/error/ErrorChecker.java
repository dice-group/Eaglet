package org.aksw.gscheck.error;

import java.util.List;

import org.aksw.gerbil.transfer.nif.Document;

public interface ErrorChecker {

    // TODO add the preprocessed documents as an argument
    public void check(List<Document> documents);
}
