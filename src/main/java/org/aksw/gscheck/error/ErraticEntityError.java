package org.aksw.gscheck.error;

import java.util.ArrayList;
import java.util.HashMap;
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

	HashMap<String, List<NamedEntityCorrections>> hm = new HashMap<String, List<NamedEntityCorrections>>();

	public void ErraticEntityProb(List<Document> documents) throws GerbilException {
		LOGGER.info(" ERRATIC ENTITY MODULE RUNNING");
		for (Document doc : documents) {
			List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);
			entities = nameEntity(doc, entities);
			for(NamedEntityCorrections entity :entities)
			{
				hm.put(entity.getEntity_name(), value)
			}
		}

	}

	public List<NamedEntityCorrections> nameEntity(Document doc, List<NamedEntityCorrections> entities) {
		List<StanfordParsedMarking> stanfordAnns = doc.getMarkings(StanfordParsedMarking.class);
		StanfordParsedMarking stanfordAnn = stanfordAnns.get(0);
		List<CoreLabel> tokens = stanfordAnn.getAnnotation().get(TokensAnnotation.class);
		if (stanfordAnns.size() != 1) {
			// TODO PANIC!!!!
			LOGGER.error(" Parser not working ");
		}
		for (NamedEntityCorrections entity : entities) {
			for (CoreLabel token : tokens) {
				if (entity.getStartPosition() == token.beginPosition()
						&& ((token.endPosition() - token.beginPosition()) == entity.getLength())) {

					if (token.get(LemmaAnnotation.class).contains(" ")) {
						String[] name = token.get(LemmaAnnotation.class).split(" ");

						entity.setEntity_name(name[0]);
						entity.setDoc(doc.getDocumentURI());
					}

					else {
						entity.setEntity_name(token.get(LemmaAnnotation.class));
						entity.setDoc(doc.getDocumentURI());
					}
				}
			}
		}
		return entities;
	}

	@Override
	public void check(List<Document> documents) throws GerbilException {
		// TODO Auto-generated method stub
		this.ErraticEntityProb(documents);
	}

}
