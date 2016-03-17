package org.aksw.gscheck.error;

import java.util.ArrayList;
import java.util.List;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;

import org.aksw.gscheck.corrections.NamedEntityCorrections;
import org.aksw.gscheck.corrections.NamedEntityCorrections.Check;

import org.aksw.simba.gscheck.documentprocessor.StanfordParsedMarking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;



public class ErraticEntityError implements ErrorChecker {
	private static final Logger LOGGER = LoggerFactory.getLogger(ErraticEntityError.class);
	
	List<NamedEntityCorrections> entity_set = new ArrayList<NamedEntityCorrections>();
	String entity_name;
	List<NamedEntityCorrections> lemma_set = new ArrayList<NamedEntityCorrections>();

	
	
	public void ErraticEntityProb(List<Document> documents) throws GerbilException 
	{
		LOGGER.info(" ERRATIC ENTITY MODULE RUNNING");
		for (Document doc : documents) {
			List<NamedEntityCorrections> dummy_list = lemmatize(doc);
			lemma_set.addAll(dummy_list);

		}

		for (Document doc : documents) {

			List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);

			for (NamedEntityCorrections le : lemma_set) {
				// same entity
				for (NamedEntityCorrections es : entities) {
					if ((le.getEntity_name().equals(es.getEntity_name())) && (le.getDoc().equals(doc.getDocumentURI()))
							&& ((le.getStartPosition() >= es.getStartPosition() && ((le.getStartPosition()
									+ le.getLength()) <= (es.getStartPosition() + es.getLength()))))) {
						break;
					}
					// missing in the same document
					if (le.getEntity_name().equals(es.getEntity_name()) && (le.getDoc().equals(doc.getDocumentURI()))) {
						if ((le.getStartPosition() >= es.getStartPosition() && ((le.getStartPosition()
								+ le.getLength()) <= (es.getStartPosition() + es.getLength())))) {
							break;
						}
						// same doc but positions dont match
						else {
							le.setResult(Check.INSERTED);
							doc.addMarking(le);
						}
					}
					// missing from different doc
					if ((le.getEntity_name().equals(es.getEntity_name())) && (!(le.getDoc().equals(es.getDoc())))) {
						le.setResult(Check.INSERTED);
						doc.addMarking(le);
					}

				}
			}

		}

	}
	

	public List<NamedEntityCorrections> lemmatize(Document doc) {
		List<NamedEntityCorrections> lemma_list = new ArrayList<NamedEntityCorrections>();
		List<StanfordParsedMarking> stanfordAnns = doc.getMarkings(StanfordParsedMarking.class);
		StanfordParsedMarking stanfordAnn = stanfordAnns.get(0);
		List<CoreLabel> tokens = stanfordAnn.getAnnotation().get(TokensAnnotation.class);
		if (stanfordAnns.size() != 1) {
			// TODO PANIC!!!!
			LOGGER.error(" Parser not working ");
		}

		for (CoreLabel token : tokens) {
			NamedEntityCorrections dummy = new NamedEntityCorrections(token.beginPosition(),
					token.endPosition() - token.beginPosition(), null);
			dummy.setEntity_name((token.get(LemmaAnnotation.class)));
			dummy.setDoc(doc.getDocumentURI());

			if (dummy.getLength() != 0)
				lemma_list.add(dummy);

		}
		return lemma_list;

	}

	@Override
	public void check(List<Document> documents) throws GerbilException {
		// TODO Auto-generated method stub
		this.ErraticEntityProb(documents);
	}

}
