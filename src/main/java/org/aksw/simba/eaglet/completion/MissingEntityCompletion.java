package org.aksw.simba.eaglet.completion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.gerbil.annotator.A2KBAnnotator;
import org.aksw.gerbil.exceptions.GerbilException;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.MeaningSpan;
import org.aksw.simba.eaglet.entitytypemodify.EntityTypeChange;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections;
import org.aksw.simba.eaglet.entitytypemodify.NamedEntityCorrections.Check;
import org.aksw.simba.eaglet.error.ErrorChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MissingEntityCompletion implements ErrorChecker {

	private List<A2KBAnnotator> annotators;
	private static final Logger LOGGER = LoggerFactory.getLogger(MissingEntityCompletion.class);

	public MissingEntityCompletion(List<A2KBAnnotator> annotators) {
		this.annotators = annotators;
	}

	public void addMissingEntity(List<List<MeaningSpan>> annotator_result, Document doc) throws GerbilException {
		List<NamedEntityCorrections> existingEntities = doc.getMarkings(NamedEntityCorrections.class);
		Set<MeaningSpan> newEntities = new HashSet<MeaningSpan>();
		NamedEntityCorrections existingEntity;
		boolean found = false;
		// iterate over all annotatorResult
		for (List<MeaningSpan> list : annotator_result) {
			for (MeaningSpan annotatorentity : list) {
				found = false;
				for (int i = 0; !found && (i < existingEntities.size()); ++i) {
					existingEntity = existingEntities.get(i);
					if ((existingEntity.getStartPosition() == annotatorentity.getStartPosition())
							&& (existingEntity.getLength() == annotatorentity.getLength())) {
						found = true;
					}
				}
				if (!found) {
					newEntities.add(annotatorentity);
				}
			}
		}
		// add new entities
		for (MeaningSpan newEntity : newEntities) {
			doc.addMarking(new NamedEntityCorrections(newEntity.getStartPosition(), newEntity.getLength(),
					newEntity.getUris(), Check.COMPLETED));
		}
	}

	public List<List<NamedEntityCorrections>> adjustAnnotatorresult(Document doc,
			List<List<MeaningSpan>> annotator_resutl) {
		List<List<NamedEntityCorrections>> formatedList = new ArrayList<List<NamedEntityCorrections>>();
		for (List<MeaningSpan> lis : annotator_resutl) {
			List<NamedEntityCorrections> new_list = EntityTypeChange.changeListType(lis);
			formatedList.add(new_list);
		}
		return formatedList;
	}

	@Override
	public void check(List<Document> documents) throws GerbilException {
		LOGGER.info("COMPLETION MODULE RUNNING!!");
		List<List<MeaningSpan>> annotatorResults = new ArrayList<List<MeaningSpan>>();
		for (Document document : documents) {
			annotatorResults.clear();
			for (A2KBAnnotator annotator : annotators) {
				annotatorResults.add(annotator.performA2KBTask(document));
			}
			addMissingEntity(annotatorResults, document);
		}
	}
}
