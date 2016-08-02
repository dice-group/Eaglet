package org.aksw.simba.eaglet.entitytypemodify;

import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.MeaningSpan;

/**
 * The class to convert the normal marking into the modified markings to store
 * error types.
 *
 * @author Kunal
 * @author Michael
 */
public class EntityTypeChange {
	/**
	 * The method to change entity type.
	 *
	 * @param d
	 *            :A document to be processed
	 * @return A list of Markings
	 */
	public static List<Marking> changeType(Document d) {
		List<MeaningSpan> original_list = d.getMarkings(MeaningSpan.class);
		List<Marking> new_list = new ArrayList<Marking>(original_list.size());
		changeListType(original_list, new_list, d);

		return new_list;
	}

	/**
	 * The method to change entity type.
	 *
	 * @param original_list
	 *            :A list of Marking from documents
	 * @return A list of Markings
	 */
	public static List<NamedEntityCorrections> changeListType(
			List<MeaningSpan> original_list) {
		List<NamedEntityCorrections> new_list = new ArrayList<NamedEntityCorrections>(
				original_list.size());
		changeListType(original_list, new_list, null);
		return new_list;
	}

	/**
	 * A utility method to change types.
	 *
	 * @param original_list
	 *            : A list of MeaningSpan
	 * @param newList
	 *            : The new list
	 * @param d
	 *            :A document to be processed
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Marking> void changeListType(
			List<MeaningSpan> original_list, List<T> newList, Document d) {
		for (MeaningSpan entity : original_list) {
			newList.add((T) new NamedEntityCorrections(entity
					.getStartPosition(), entity.getLength(), entity.getUris(),
					d));
		}
	}
}
