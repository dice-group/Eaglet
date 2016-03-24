package org.aksw.simba.eaglet.error;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aksw.gerbil.dataset.DatasetConfiguration;
import org.aksw.gerbil.dataset.impl.nif.NIFFileDatasetConfig;
import org.aksw.gerbil.datatypes.ExperimentType;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gerbil.transfer.nif.data.StartPosBasedComparator;
import org.aksw.simba.eaglet.corrections.NamedEntityCorrections;
import org.aksw.simba.eaglet.corrections.NamedEntityCorrections.Check;
import org.aksw.simba.eaglet.documentprocessor.DocumentProcessor;
import org.aksw.simba.eaglet.documentprocessor.StanfordParsedMarking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;

public class LongDescriptionError implements ErrorChecker {
	private static final Logger LOGGER = LoggerFactory.getLogger(ErraticEntityError.class);

	/*
	 * private static final DatasetConfiguration DATASET = new
	 * NIFFileDatasetConfig("DBpedia",
	 * "gerbil_data/datasets/spotlight/dbpedia-spotlight-nif.ttl", false,
	 * ExperimentType.A2KB);
	 */
	static DocumentProcessor dp = new DocumentProcessor();

	public void LongDescription(List<Document> documents) throws GerbilException {
		// List<Document> documents =
		// DATASET.getDataset(ExperimentType.A2KB).getInstances();
		LOGGER.info("LONG DESCRIPTION MODULE RUNNING");

		for (Document doc : documents) {
			String text = doc.getText();
			List<NamedEntityCorrections> entities = doc.getMarkings(NamedEntityCorrections.class);
			List<CoreLabel> POSBlackList = LongEntity_Extracter_util(doc);
			Collections.sort(entities, new StartPosBasedComparator());

			for (NamedEntityCorrections entity : entities) {
				String entity_text = text.substring(entity.getStartPosition(),
						entity.getLength() + entity.getStartPosition());
				String[] arr = entity_text.split(" ");
				for (String dummy : arr) {
					for (CoreLabel bentity : POSBlackList) {
						if (bentity.get(TextAnnotation.class).equals(dummy)) {
							/*
							 * System.out.println(dummy + "-----> " +
							 * text.substring(entity.getStartPosition(),
							 * entity.getLength() + entity.getStartPosition()) +
							 * " " + bentity.get(TextAnnotation.class) + " " +
							 * entity.getStartPosition() + " " +
							 * entity.getLength());
							 */
							entity.setResult(Check.DELETED);

						}
					}
				}

			}

		}

	}

	public List<CoreLabel> LongEntity_Extracter_util(Document doc) {
		List<CoreLabel> POS_Blacklist = new ArrayList<CoreLabel>();

		List<StanfordParsedMarking> stanfordAnns = doc.getMarkings(StanfordParsedMarking.class);
		StanfordParsedMarking stanfordAnn = stanfordAnns.get(0);
		List<CoreLabel> tokens = stanfordAnn.getAnnotation().get(TokensAnnotation.class);
		if (stanfordAnns.size() != 1) {
			// TODO PANIC!!!!
			LOGGER.error(" Parser not working ");
		}

		for (CoreLabel token : tokens) {
			// we can get the token that has been marked inside the text

			// System.out.println(token.get(PartOfSpeechAnnotation.class));
			// System.out.print(token.get(TextAnnotation.class));
			// System.out.print("??????");
			if ((token.get(PartOfSpeechAnnotation.class).equals("VBP"))
					|| (token.get(PartOfSpeechAnnotation.class).equals("VBZ")
							|| (token.get(PartOfSpeechAnnotation.class).equals("WDT"))
							|| (token.get(PartOfSpeechAnnotation.class).equals("PRP"))
							|| (token.get(PartOfSpeechAnnotation.class).equals("MD")))) {
				POS_Blacklist.add(token);

			}
		}

		return POS_Blacklist;
	}

	@Override
	public void check(List<Document> documents) throws GerbilException {
		// TODO Auto-generated method stub
		this.LongDescription(documents);
	}

}
