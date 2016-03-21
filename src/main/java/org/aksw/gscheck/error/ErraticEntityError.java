package org.aksw.gscheck.error;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Span;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;
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

	Map<String, List<NamedEntityCorrections>> map = new HashMap<String, List<NamedEntityCorrections>>();

	public void ErraticEntityProb(List<Document> documents) throws GerbilException {
		LOGGER.info(" ERRATIC ENTITY MODULE RUNNING");

		generate_map(documents);
		for (Document doc : documents) {
			List<StanfordParsedMarking> stanfordAnns = doc.getMarkings(StanfordParsedMarking.class);
			StanfordParsedMarking stanfordAnn = stanfordAnns.get(0);
			List<CoreLabel> tokens = stanfordAnn.getAnnotation().get(TokensAnnotation.class);
			if (stanfordAnns.size() != 1) {
				// TODO PANIC!!!!
				LOGGER.error(" Parser not working ");
			}
			for (CoreLabel token : tokens) {
				if (map.containsKey(token)) {
					for (NamedEntityCorrections ne : map.get(token)) {
						// missing in the same document
						if (doc.getDocumentURI().equals(ne.getDoc())) {
							if (token.beginPosition() == ne.getStartPosition()) {
								break;
							} else {
								NamedEntityCorrections newentity = new NamedEntityCorrections(token.beginPosition(),
										token.endPosition() - token.beginPosition() + 1, " ", Check.INSERTED);
								doc.addMarking(newentity);
							}
						} else {
							NamedEntityCorrections newentity = new NamedEntityCorrections(token.beginPosition(),
									token.endPosition() - token.beginPosition() + 1, " ", Check.INSERTED);
							doc.addMarking(newentity);
						}

					}
				}
			}

		}

	}

	public void generate_map(List<Document> documents) {
		for (Document doc : documents) {
			List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);

			entities = nameEntity(doc, entities);

			for (NamedEntityCorrections entity : entities) {
				entity.setDoc(doc.getDocumentURI());
				if (map.containsKey(entity.getEntity_name())) {
					map.get(entity.getEntity_name()).add(entity);
				}

				else {
					List<NamedEntityCorrections> sub = new ArrayList<NamedEntityCorrections>();
					sub.add(entity);
					map.put(entity.getEntity_name(), sub);
				}
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
		String text = doc.getText();
		// 1. create Bitset containing the ne positions and gather ne positions
		BitSet nePositions = new BitSet(text.length());
		int start[] = new int[entities.size()];
		int end[] = new int[entities.size()];

		Collections.sort(entities, new StartPosBasedComparator());
		for (int i = 0; i < entities.size(); ++i) {
			start[i] = entities.get(i).getStartPosition();
			end[i] = entities.get(i).getStartPosition() + entities.get(i).getLength();
			nePositions.set(start[i], end[i]);
		}
		// 2. Iterate over the tokens and search for tokens that are inside of
		// ne boarders
		List<List<CoreLabel>> entityTokens = new ArrayList<List<CoreLabel>>(entities.size());
		for (CoreLabel token : tokens) {
			if (nePositions.get(token.beginPosition()) || nePositions.get(token.endPosition() - 1)) {
				// search for the matching named entity
				int pos = 0;
				while ((pos < start.length) && (start[pos] <= token.beginPosition())
						&& (end[pos] >= token.endPosition())) {
					++pos;
				}
				if (pos < start.length) {
					// nes.get(pos) is the matching ne
					// add the token to the list of tokens of ne
					if (entityTokens.get(pos) == null) {
						List<CoreLabel> dummy = new ArrayList<CoreLabel>();
						dummy.add(token);
						entityTokens.add(pos, dummy);
					}
					entityTokens.get(pos).add(token);
				} else

				{
					// Error (maybe a wrong positioned ne?)
					LOGGER.error("CANNOT FIND THE TOKEN WITHIN THE MARKING");
				}
			}

		}
		for (int i = 0; i < entityTokens.size(); i++) {

			Collections.sort(entityTokens.get(i), new Comparator<CoreLabel>() {
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
			});
		}
		for (int i = 0; i < entities.size(); i++) {
			entities.get(i).setEntity_name(entityTokens.get(i).get(0).get(LemmaAnnotation.class));
		}
		return entities;
	}

	@Override
	public void check(List<Document> documents) throws GerbilException {
		// TODO Auto-generated method stub
		this.ErraticEntityProb(documents);
	}

}
