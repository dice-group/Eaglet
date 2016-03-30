package org.aksw.simba.eaglet.completion;

import java.util.List;

import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;

public interface GoldStandardCompletion {
	  public void check(List<Document> documents) throws GerbilException;
}
